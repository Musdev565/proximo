package cd.ucc.proximo.domain.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "auditoires")
public class Auditoire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 120)
    private String promotion;

    @Column(nullable = false)
    private Integer level; // 1, 2, 3

    @Column(name = "position_order")
    private Integer positionOrder; // 1 = closer to entrance

    @Column(length = 20)
    private String side; // gauche, droite, etc.

    @Column(name = "pos_x")
    private Double posX;

    @Column(name = "pos_y")
    private Double posY;

    public Auditoire() {
    }

    public Auditoire(String code, String name, String promotion, Integer level, Integer positionOrder) {
        this.code = code;
        this.name = name;
        this.promotion = promotion;
        this.level = level;
        this.positionOrder = positionOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPromotion() {
        return promotion;
    }

    public void setPromotion(String promotion) {
        this.promotion = promotion;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getPositionOrder() {
        return positionOrder;
    }

    public void setPositionOrder(Integer positionOrder) {
        this.positionOrder = positionOrder;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public Double getPosX() {
        return posX;
    }

    public void setPosX(Double posX) {
        this.posX = posX;
    }

    public Double getPosY() {
        return posY;
    }

    public void setPosY(Double posY) {
        this.posY = posY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Auditoire auditoire = (Auditoire) o;
        return Objects.equals(code, auditoire.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
