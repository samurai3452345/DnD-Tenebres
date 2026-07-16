package com.java_dragons.dnd_tenebres.domain.item.entity;

import com.java_dragons.dnd_tenebres.domain.item.model.ItemRarity;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item_templates")
@Getter
@NoArgsConstructor
public class ItemTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ItemType type;

//    @Enumerated(EnumType.STRING)
//    @Column(name = "slot", nullable = false)
//    private EquipmentSlot slot;

    @Enumerated(EnumType.STRING)
    @Column(name = "rarity", nullable = false)
    private ItemRarity rarity;

//    @Enumerated(EnumType.STRING)
//    @Column(name = "armor_type", nullable = false)
//    private ArmorType armorType;

    @Column(name = "armor_class", nullable = false)
    private int armorClass;

    @Column(name = "required_strength", nullable = false)
    private int requiredStrength;

    @Column(name = "damage_dice")
    private String damageDice;

    @Column(name = "stat_budget", nullable = false)
    private int statBudget;
}