package com.java_dragons.dnd_tenebres.domain.combat.entity;

import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.model.DiceType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "spells")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Spell {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "tier", nullable = false)
    private int tier;

    @Column(name = "mana_cost", nullable = false)
    private int manaCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "element", nullable = false)
    private DamageType element;

    @Enumerated(EnumType.STRING)
    @Column(name = "damage_dice", nullable = false)
    private DiceType damageDice;

    @Column(name = "dice_count", nullable = false)
    private int diceCount;

    @Column(name = "flat_bonus", nullable = false)
    private int flatBonus;
}