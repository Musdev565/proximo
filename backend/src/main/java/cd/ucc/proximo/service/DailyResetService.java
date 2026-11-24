package cd.ucc.proximo.service;

import cd.ucc.proximo.domain.entity.DailyAvailability;
import cd.ucc.proximo.domain.entity.Order;
import cd.ucc.proximo.domain.entity.OrderItem;
import cd.ucc.proximo.domain.entity.OrderStatusHistory;
import cd.ucc.proximo.domain.enums.OrderStatus;
import cd.ucc.proximo.repository.DailyAvailabilityRepository;
import cd.ucc.proximo.repository.OrderRepository;
import cd.ucc.proximo.repository.OrderStatusHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
@EnableScheduling
public class DailyResetService {

    private static final Logger log = LoggerFactory.getLogger(DailyResetService.class);

    private final OrderRepository orderRepository;
    private final DailyAvailabilityRepository availabilityRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;

    @Value("${proximo.fuseau:Africa/Kinshasa}")
    private String timezone;

    public DailyResetService(OrderRepository orderRepository,
                             DailyAvailabilityRepository availabilityRepository,
                             OrderStatusHistoryRepository statusHistoryRepository) {
        this.orderRepository = orderRepository;
        this.availabilityRepository = availabilityRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    private ZoneId getZoneId() {
        return ZoneId.of(timezone);
    }

    /**
     * Tâche planifiée quotidienne à 16h30 (Kinshasa) :
     * Expiration des commandes EN_ATTENTE non livrées et libération des réservations.
     */
    @Scheduled(cron = "0 30 16 * * *", zone = "Africa/Kinshasa")
    @Transactional
    public void executeDailyReset() {
        log.info("Exécution du reset quotidien de 16h30 (fuseau {})", timezone);
        expirePendingOrders(LocalDateTime.now(getZoneId()));
    }

    /**
     * Rattrapage au réveil de l'application (ex: démarrage sur Render après mise en veille).
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void checkAndCatchUpOnStartup() {
        LocalDateTime now = LocalDateTime.now(getZoneId());
        log.info("Vérification au démarrage : heure actuelle = {}", now);

        // Si l'application redémarre après 16h30, on nettoie les commandes de la veille ou d'avant 16h30
        LocalTime resetTime = LocalTime.of(16, 30);
        if (now.toLocalTime().isAfter(resetTime)) {
            LocalDateTime todayResetPoint = LocalDateTime.of(now.toLocalDate(), resetTime);
            expirePendingOrders(todayResetPoint);
        } else {
            // Si on est le matin, on expire toute commande de la veille ou antérieure
            LocalDateTime yesterdayEnd = LocalDateTime.of(now.toLocalDate().minusDays(1), LocalTime.MAX);
            expirePendingOrders(yesterdayEnd);
        }
    }

    private void expirePendingOrders(LocalDateTime cutoffTime) {
        List<Order> pendingOrders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.EN_ATTENTE, cutoffTime);
        if (pendingOrders.isEmpty()) {
            log.info("Aucune commande en attente à expirer.");
            return;
        }

        log.info("Expiration de {} commande(s) en attente...", pendingOrders.size());
        for (Order order : pendingOrders) {
            LocalDate orderDate = order.getCreatedAt().toLocalDate();

            for (OrderItem item : order.getItems()) {
                availabilityRepository.findByProductIdAndDate(item.getProduct().getId(), orderDate)
                        .ifPresent(availability -> {
                            availability.releaseReservation(item.getQuantity());
                            availabilityRepository.save(availability);
                        });
            }

            OrderStatus prev = order.getStatus();
            order.expire();
            orderRepository.save(order);

            statusHistoryRepository.save(new OrderStatusHistory(
                    order,
                    prev,
                    OrderStatus.EXPIREE,
                    "SYSTEM_RESET_16H30"
            ));
        }
        log.info("Reset quotidien terminé avec succès.");
    }
}
