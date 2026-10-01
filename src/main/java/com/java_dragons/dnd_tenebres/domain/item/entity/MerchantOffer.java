package com.java_dragons.dnd_tenebres.domain.item.entity;
import jakarta.persistence.*;
import lombok.Getter;
@Entity @Table(name="merchant_offers") @Getter
public class MerchantOffer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="merchant_code", nullable=false) private String merchantCode;
    @Column(name="location_id", nullable=false) private String locationId;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="template_id") private ItemTemplate template;
    @Column(name="unit_price", nullable=false) private int unitPrice;
    @Column(name="available_quantity", nullable=false) private int availableQuantity;
    @Column(name="min_level", nullable=false) private int minLevel;
    @Column(nullable=false) private boolean enabled;
    public void take(int amount) {
        if (availableQuantity >= 0) {
            if (availableQuantity < amount) throw new IllegalStateException("Недостаточно товара");
            availableQuantity -= amount;
        }
    }
}
