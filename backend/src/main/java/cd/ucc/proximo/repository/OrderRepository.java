package cd.ucc.proximo.repository;

import cd.ucc.proximo.domain.entity.Order;
import cd.ucc.proximo.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    List<Order> findByShopIdAndStatus(Long shopId, OrderStatus status);

    @Query("SELECT o FROM Order o JOIN FETCH o.deliveryAuditoire a WHERE o.shop.id = :shopId AND o.status = :status ORDER BY a.level ASC, a.positionOrder ASC, o.createdAt ASC")
    List<Order> findActiveOrdersForVendorGroupedByAuditoire(@Param("shopId") Long shopId, @Param("status") OrderStatus status);

    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime cutoff);
}
