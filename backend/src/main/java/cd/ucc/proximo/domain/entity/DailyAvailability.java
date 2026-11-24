package cd.ucc.proximo.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "daily_availabilities", uniqueConstraints = {
    @UniqueConstraint(name = "uk_daily_avail_product_date", columnNames = {"product_id", "avail_date"})
})
public class DailyAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "avail_date", nullable = false)
    private LocalDate date;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity = 0;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    public DailyAvailability() {
    }

    public DailyAvailability(Product product, LocalDate date, int stockQuantity) {
        this.product = product;
        this.date = date;
        this.stockQuantity = stockQuantity;
        this.reservedQuantity = 0;
    }

    public int getAvailableQuantity() {
        return Math.max(0, stockQuantity - reservedQuantity);
    }

    public boolean canReserve(int requestedQuantity) {
        return requestedQuantity > 0 && getAvailableQuantity() >= requestedQuantity;
    }

    public void reserve(int quantity) {
        if (!canReserve(quantity)) {
            throw new IllegalStateException("Stock insuffisant pour réserver " + quantity + " article(s).");
        }
        this.reservedQuantity += quantity;
    }

    public void releaseReservation(int quantity) {
        this.reservedQuantity = Math.max(0, this.reservedQuantity - quantity);
    }

    public void confirmPayment(int quantity) {
        if (quantity > this.reservedQuantity || quantity > this.stockQuantity) {
            throw new IllegalStateException("Impossible de confirmer un paiement supérieur aux quantités enregistrées.");
        }
        this.stockQuantity -= quantity;
        this.reservedQuantity -= quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(int reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyAvailability that = (DailyAvailability) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
