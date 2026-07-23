package com.java_dragons.dnd_tenebres.domain.item.entity;

import com.java_dragons.dnd_tenebres.domain.item.model.*;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item_templates")
@Getter
@NoArgsConstructor
@Data
public class ItemTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ItemType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot", nullable = false)
    private EquipmentSlot slot;

    @Enumerated(EnumType.STRING)
    @Column(name = "rarity", nullable = false)
    private ItemRarity rarity;

    @Enumerated(EnumType.STRING)
    @Column(name = "armor_type", nullable = false)
    private ArmorType armorType;

    @Column(name = "armor_class", nullable = false)
    private int armorClass;

    @Column(name = "required_strength", nullable = false)
    private int requiredStrength;

    @Enumerated(EnumType.STRING)
    @Column(name = "damage_dice")
    private DiceType damageDice;

    @Column(name = "dice_count", nullable = false)
    private int diceCount = 0;

    @Column(name = "stat_budget", nullable = false)
    private int statBudget;

    @Enumerated(EnumType.STRING)
    @Column(name = "passive_effect", nullable = false)
    private ItemPassive passiveEffect = ItemPassive.NONE;
}