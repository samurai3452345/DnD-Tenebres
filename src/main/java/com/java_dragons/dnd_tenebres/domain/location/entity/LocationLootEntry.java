package com.java_dragons.dnd_tenebres.domain.location.entity;

import com.java_dragons.dnd_tenebres.domain.item.entity.ItemTemplate;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "location_loot_tables")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationLootEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_id", nullable = false)
    private String locationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_template_id", nullable = false)
    private ItemTemplate itemTemplate;

    @Column(name = "min_amount", nullable = false)
    private int minAmount;

    @Column(name = "max_amount", nullable = false)
    private int maxAmount;

    @Column(name = "find_chance", nullable = false)
    private int findChance;
}