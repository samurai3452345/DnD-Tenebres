package com.java_dragons.dnd_tenebres.domain.monster.entity;

import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.model.DiceType;
import com.java_dragons.dnd_tenebres.domain.monster.model.MonsterSkill;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "monster_templates")
public class MonsterTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "biome")
    private String biome;

    @Column(name = "level")
    private int level;

    @Column(name = "base_hp")
    private int baseHp;

    @Column(name = "armor_class")
    private int armorClass;

    @Column(name = "xp_reward")
    private int xpReward;

    @Column(name = "gold_reward")
    private int goldReward;

    @Enumerated(EnumType.STRING)
    @Column(name = "damage_dice", nullable = false)
    private DiceType damageDice;

    @Column(name = "damage_bonus", nullable = false)
    private int damageBonus;

    @Column(name = "attack_name", nullable = false)
    private String attackName;

    @Enumerated(EnumType.STRING)
    @Column(name = "special_skill", nullable = false)
    private MonsterSkill specialSkill = MonsterSkill.NONE;

    @Column(name = "skill_frequency", nullable = false)
    private int skillFrequency = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "template_elements", joinColumns = @JoinColumn(name = "template_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "element")
    private Set<DamageType> elements = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "template_resistances", joinColumns = @JoinColumn(name = "template_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "element")
    private Set<DamageType> resistances = new HashSet<>();
}