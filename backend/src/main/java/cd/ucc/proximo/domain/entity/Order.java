package cd.ucc.proximo.domain.entity;

import cd.ucc.proximo.domain.enums.CancellationReason;
import cd.ucc.proximo.domain.enums.OrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auditoire_id", nullable = false)
    private Auditoire deliveryAuditoire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.EN_ATTENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_reason", length = 30)
    private CancellationReason cancellationReason;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Order() {
    }

    public Order(User buyer, Shop shop, Auditoire deliveryAuditoire) {
        this.buyer = buyer;
        this.shop = shop;
        this.deliveryAuditoire = deliveryAuditoire;
        this.status = OrderStatus.EN_ATTENTE;
        this.totalAmount = BigDecimal.ZERO;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        recalculateTotal();
    }

    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void confirmPaid() {
        if (this.status != OrderStatus.EN_ATTENTE) {
            throw new IllegalStateException("Seule une commande EN_ATTENTE peut être marquée comme LIVREE_PAYEE.");
        }
        this.status = OrderStatus.LIVREE_PAYEE;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(CancellationReason reason) {
        if (this.status != OrderStatus.EN_ATTENTE) {
            throw new IllegalStateException("Seule une commande EN_ATTENTE peut être annulée.");
        }
        this.status = OrderStatus.ANNULEE;
        this.cancellationReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void expire() {
        if (this.status != OrderStatus.EN_ATTENTE) {
            throw new IllegalStateException("Seule une commande EN_ATTENTE peut expirer.");
        }
        this.status = OrderStatus.EXPIREE;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public Auditoire getDeliveryAuditoire() {
        return deliveryAuditoire;
    }

    public void setDeliveryAuditoire(Auditoire deliveryAuditoire) {
        this.deliveryAuditoire = deliveryAuditoire;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public CancellationReason getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(CancellationReason cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
