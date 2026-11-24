package cd.ucc.proximo.service;

import cd.ucc.proximo.domain.entity.*;
import cd.ucc.proximo.domain.enums.CancellationReason;
import cd.ucc.proximo.domain.enums.OrderStatus;
import cd.ucc.proximo.dto.OrderItemRequest;
import cd.ucc.proximo.exception.*;
import cd.ucc.proximo.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final AuditoireRepository auditoireRepository;
    private final ProductRepository productRepository;
    private final DailyAvailabilityRepository availabilityRepository;
    private final DailyPresenceRepository presenceRepository;

    @Value("${proximo.fuseau:Africa/Kinshasa}")
    private String timezone;

    @Value("${proximo.horaires.fermeture-commandes:15:30}")
    private String closingTimeStr;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        OrderStatusHistoryRepository statusHistoryRepository,
                        UserRepository userRepository,
                        ShopRepository shopRepository,
                        AuditoireRepository auditoireRepository,
                        ProductRepository productRepository,
                        DailyAvailabilityRepository availabilityRepository,
                        DailyPresenceRepository presenceRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.auditoireRepository = auditoireRepository;
        this.productRepository = productRepository;
        this.availabilityRepository = availabilityRepository;
        this.presenceRepository = presenceRepository;
    }

    private ZoneId getZoneId() {
        return ZoneId.of(timezone);
    }

    private LocalDate getCurrentDate() {
        return LocalDate.now(getZoneId());
    }

    private LocalTime getCurrentTime() {
        return LocalTime.now(getZoneId());
    }

    public void validateOrderHours() {
        LocalTime closingTime = LocalTime.parse(closingTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
        if (getCurrentTime().isAfter(closingTime)) {
            throw new OrderClosedException("Les commandes sont fermées pour aujourd'hui depuis " + closingTimeStr + ".");
        }
    }

    @Transactional
    public Order createOrder(Long buyerId, Long shopId, Long auditoireId, List<OrderItemRequest> itemRequests) {
        validateOrderHours();

        if (itemRequests == null || itemRequests.isEmpty()) {
            throw new IllegalArgumentException("Une commande doit contenir au moins un produit.");
        }

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur acheteur non trouvé."));

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Boutique non trouvée."));

        if (!shop.isActive()) {
            throw new IllegalStateException("Cette boutique n'est pas active actuellement.");
        }

        // Règle métier : un utilisateur ne peut pas commander dans sa propre boutique
        if (buyer.getId().equals(shop.getOwner().getId())) {
            throw new UnauthorizedOperationException("Vous ne pouvez pas commander dans votre propre boutique.");
        }

        LocalDate today = getCurrentDate();
        boolean isPresent = presenceRepository.findByShopIdAndDate(shopId, today)
                .map(DailyPresence::isPresent)
                .orElse(false);

        if (!isPresent) {
            throw new IllegalStateException("Le vendeur n'a pas déclaré sa présence pour aujourd'hui.");
        }

        Auditoire deliveryAuditoire = auditoireRepository.findById(auditoireId)
                .orElseThrow(() -> new ResourceNotFoundException("Auditoire de livraison non trouvé."));

        Order order = new Order(buyer, shop, deliveryAuditoire);

        for (OrderItemRequest req : itemRequests) {
            Product product = productRepository.findById(req.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit " + req.productId() + " non trouvé."));

            if (!product.getShop().getId().equals(shopId)) {
                throw new IllegalArgumentException("Le produit " + product.getName() + " n'appartient pas à cette boutique.");
            }

            // Verrou pessimiste pour garantir qu'on ne survend jamais en cas de requêtes simultanées
            DailyAvailability availability = availabilityRepository
                    .findByProductIdAndDateForUpdate(product.getId(), today)
                    .orElseThrow(() -> new InsufficientStockException("Aucun stock déclaré aujourd'hui pour " + product.getName() + "."));

            if (!availability.canReserve(req.quantity())) {
                throw new InsufficientStockException("Stock insuffisant pour " + product.getName() +
                        ". Disponible : " + availability.getAvailableQuantity() + ", Demandé : " + req.quantity());
            }

            // Réservation temporaire du stock
            availability.reserve(req.quantity());
            availabilityRepository.save(availability);

            // Création de la ligne avec prix figé
            OrderItem item = new OrderItem(order, product, req.quantity(), product.getPrice());
            order.addItem(item);
        }

        Order savedOrder = orderRepository.save(order);

        // Historique de statut initial
        OrderStatusHistory history = new OrderStatusHistory(
                savedOrder,
                null,
                OrderStatus.EN_ATTENTE,
                buyer.getFullName()
        );
        statusHistoryRepository.save(history);

        return savedOrder;
    }

    @Transactional
    public Order confirmPaymentAndDelivery(Long orderId, Long vendorUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande " + orderId + " non trouvée."));

        // Seul le vendeur propriétaire peut valider le paiement cash
        if (!order.getShop().getOwner().getId().equals(vendorUserId)) {
            throw new UnauthorizedOperationException("Seul le propriétaire de la boutique peut confirmer le paiement.");
        }

        if (order.getStatus() != OrderStatus.EN_ATTENTE) {
            throw new InvalidOrderTransitionException("Seule une commande EN_ATTENTE peut être marquée payée.");
        }

        LocalDate today = getCurrentDate();
        for (OrderItem item : order.getItems()) {
            DailyAvailability availability = availabilityRepository
                    .findByProductIdAndDateForUpdate(item.getProduct().getId(), today)
                    .orElseThrow(() -> new IllegalStateException("Disponibilité introuvable pour " + item.getProduct().getName()));

            // Le stock réel diminue ET la quantité réservée diminue
            availability.confirmPayment(item.getQuantity());
            availabilityRepository.save(availability);
        }

        OrderStatus previousStatus = order.getStatus();
        order.confirmPaid();
        Order updated = orderRepository.save(order);

        User vendor = userRepository.findById(vendorUserId).orElse(null);
        String vendorName = vendor != null ? vendor.getFullName() : "Vendeur #" + vendorUserId;

        statusHistoryRepository.save(new OrderStatusHistory(
                updated,
                previousStatus,
                OrderStatus.LIVREE_PAYEE,
                vendorName
        ));

        return updated;
    }

    @Transactional
    public Order cancelOrder(Long orderId, Long requestingUserId, CancellationReason reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande " + orderId + " non trouvée."));

        boolean isBuyer = order.getBuyer().getId().equals(requestingUserId);
        boolean isVendor = order.getShop().getOwner().getId().equals(requestingUserId);

        if (!isBuyer && !isVendor) {
            throw new UnauthorizedOperationException("Vous n'êtes pas autorisé à annuler cette commande.");
        }

        if (order.getStatus() != OrderStatus.EN_ATTENTE) {
            throw new InvalidOrderTransitionException("Impossible d'annuler une commande qui n'est pas EN_ATTENTE.");
        }

        LocalDate today = getCurrentDate();
        for (OrderItem item : order.getItems()) {
            DailyAvailability availability = availabilityRepository
                    .findByProductIdAndDateForUpdate(item.getProduct().getId(), today)
                    .orElseThrow(() -> new IllegalStateException("Disponibilité introuvable pour " + item.getProduct().getName()));

            // Libération de la réservation sans toucher au stock physique
            availability.releaseReservation(item.getQuantity());
            availabilityRepository.save(availability);
        }

        OrderStatus previousStatus = order.getStatus();
        order.cancel(reason != null ? reason : CancellationReason.AUTRE);
        Order updated = orderRepository.save(order);

        User requestingUser = userRepository.findById(requestingUserId).orElse(null);
        String authorName = requestingUser != null ? requestingUser.getFullName() : "Utilisateur #" + requestingUserId;

        statusHistoryRepository.save(new OrderStatusHistory(
                updated,
                previousStatus,
                OrderStatus.ANNULEE,
                authorName + " (" + (reason != null ? reason.name() : "NON_PRECISE") + ")"
        ));

        return updated;
    }
}
