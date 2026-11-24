package cd.ucc.proximo.service;

import cd.ucc.proximo.domain.entity.*;
import cd.ucc.proximo.domain.enums.CancellationReason;
import cd.ucc.proximo.domain.enums.OrderStatus;
import cd.ucc.proximo.domain.enums.UserRole;
import cd.ucc.proximo.dto.OrderItemRequest;
import cd.ucc.proximo.exception.InsufficientStockException;
import cd.ucc.proximo.exception.UnauthorizedOperationException;
import cd.ucc.proximo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderStatusHistoryRepository statusHistoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ShopRepository shopRepository;
    @Mock
    private AuditoireRepository auditoireRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private DailyAvailabilityRepository availabilityRepository;
    @Mock
    private DailyPresenceRepository presenceRepository;

    @InjectMocks
    private OrderService orderService;

    private User buyer;
    private User vendor;
    private Shop shop;
    private Product product;
    private Auditoire auditoire;
    private DailyAvailability availability;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "timezone", "Africa/Kinshasa");
        ReflectionTestUtils.setField(orderService, "closingTimeStr", "23:59"); // allow for testing

        buyer = new User("Acheteur", "buyer@ucc.cd", "+2439999999", "pass", UserRole.STUDENT);
        buyer.setId(1L);

        vendor = new User("Vendeur", "vendor@ucc.cd", "+2438888888", "pass", UserRole.VENDOR);
        vendor.setId(2L);

        shop = new Shop(vendor, "Snack UCC", "Délicieux", null);
        shop.setId(10L);

        product = new Product(shop, "Sandwich Poulet", BigDecimal.valueOf(3500), null);
        product.setId(100L);

        auditoire = new Auditoire("N2-A04", "Auditoire L3 GL", "Génie Logiciel", 2, 4);
        auditoire.setId(50L);

        availability = new DailyAvailability(product, LocalDate.now(), 5);
        availability.setId(1000L);
    }

    @Test
    @DisplayName("Création de commande réussie avec réservation de stock")
    void shouldCreateOrderAndReserveStock() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(shopRepository.findById(10L)).thenReturn(Optional.of(shop));
        when(presenceRepository.findByShopIdAndDate(eq(10L), any(LocalDate.class)))
                .thenReturn(Optional.of(new DailyPresence(shop, LocalDate.now(), true)));
        when(auditoireRepository.findById(50L)).thenReturn(Optional.of(auditoire));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(availabilityRepository.findByProductIdAndDateForUpdate(eq(100L), any(LocalDate.class)))
                .thenReturn(Optional.of(availability));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderItemRequest request = new OrderItemRequest(100L, 2);
        Order created = orderService.createOrder(1L, 10L, 50L, List.of(request));

        assertNotNull(created);
        assertEquals(OrderStatus.EN_ATTENTE, created.getStatus());
        assertEquals(1, created.getItems().size());
        assertEquals(BigDecimal.valueOf(7000), created.getTotalAmount());
        assertEquals(2, availability.getReservedQuantity());
        assertEquals(3, availability.getAvailableQuantity());

        verify(statusHistoryRepository, times(1)).save(any(OrderStatusHistory.class));
    }

    @Test
    @DisplayName("Refus si l'acheteur essaie de commander dans sa propre boutique")
    void shouldRefuseOrderFromOwnShop() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(vendor));
        when(shopRepository.findById(10L)).thenReturn(Optional.of(shop));

        OrderItemRequest request = new OrderItemRequest(100L, 1);
        assertThrows(UnauthorizedOperationException.class, () ->
                orderService.createOrder(2L, 10L, 50L, List.of(request))
        );
    }

    @Test
    @DisplayName("Refus si le stock disponible est insuffisant")
    void shouldRefuseOrderWhenStockInsufficient() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(shopRepository.findById(10L)).thenReturn(Optional.of(shop));
        when(presenceRepository.findByShopIdAndDate(eq(10L), any(LocalDate.class)))
                .thenReturn(Optional.of(new DailyPresence(shop, LocalDate.now(), true)));
        when(auditoireRepository.findById(50L)).thenReturn(Optional.of(auditoire));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(availabilityRepository.findByProductIdAndDateForUpdate(eq(100L), any(LocalDate.class)))
                .thenReturn(Optional.of(availability));

        // Demande de 10 alors qu'il n'y a que 5
        OrderItemRequest request = new OrderItemRequest(100L, 10);
        assertThrows(InsufficientStockException.class, () ->
                orderService.createOrder(1L, 10L, 50L, List.of(request))
        );
    }

    @Test
    @DisplayName("Confirmation de paiement : passe à LIVREE_PAYEE et décrémente stock réel et réserve")
    void shouldConfirmPaymentAndDecreaseRealStock() {
        availability.reserve(2); // 2 réservés sur 5
        Order order = new Order(buyer, shop, auditoire);
        order.setId(200L);
        order.addItem(new OrderItem(order, product, 2, product.getPrice()));

        when(orderRepository.findById(200L)).thenReturn(Optional.of(order));
        when(availabilityRepository.findByProductIdAndDateForUpdate(eq(100L), any(LocalDate.class)))
                .thenReturn(Optional.of(availability));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.confirmPaymentAndDelivery(200L, vendor.getId());

        assertEquals(OrderStatus.LIVREE_PAYEE, result.getStatus());
        assertEquals(3, availability.getStockQuantity());
        assertEquals(0, availability.getReservedQuantity());
    }

    @Test
    @DisplayName("Annulation de commande : libère la réservation sans toucher au stock physique")
    void shouldCancelOrderAndReleaseReservation() {
        availability.reserve(2);
        Order order = new Order(buyer, shop, auditoire);
        order.setId(200L);
        order.addItem(new OrderItem(order, product, 2, product.getPrice()));

        when(orderRepository.findById(200L)).thenReturn(Optional.of(order));
        when(availabilityRepository.findByProductIdAndDateForUpdate(eq(100L), any(LocalDate.class)))
                .thenReturn(Optional.of(availability));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.cancelOrder(200L, buyer.getId(), CancellationReason.REFUS_CLIENT);

        assertEquals(OrderStatus.ANNULEE, result.getStatus());
        assertEquals(CancellationReason.REFUS_CLIENT, result.getCancellationReason());
        assertEquals(5, availability.getStockQuantity());
        assertEquals(0, availability.getReservedQuantity());
    }
}
