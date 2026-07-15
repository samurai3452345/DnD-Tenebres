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

    @Column(name = "type", nullable = false)
    private ItemType type;

    @Column(name = "slot", nullable = false)
    private String slot; // Или Enum SlotType

    @Column(name = "rarity", nullable = false)
    private ItemRarity rarity;

    @Column(name = "armor_type", nullable = false)
    private String armorType; // Или Enum ArmorType

    @Column(name = "armor_class", nullable = false)
    private int armorClass;

    @Column(name = "required_strength", nullable = false)
    private int requiredStrength;

    @Column(name = "damage_dice")
    private String damageDice;

    @Column(name = "stat_budget", nullable = false)
    private int statBudget;
}