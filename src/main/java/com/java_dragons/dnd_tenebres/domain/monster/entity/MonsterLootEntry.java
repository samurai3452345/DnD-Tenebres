package com.java_dragons.dnd_tenebres.domain.monster.entity;


import com.java_dragons.dnd_tenebres.domain.item.entity.ItemTemplate;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "monster_loot_tables")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class MonsterLootEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monster_template_id")
    private MonsterTemplate monsterTemplate;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_template_id")
    private ItemTemplate itemTemplate;

    @Column(name = "min_amount", nullable = false)
    private int minAmount;

    @Column(name = "max_amount",  nullable = false)
    private int maxAmount;

    @Column(name = "drop_chance", nullable = false)
    private int dropChance;
}
