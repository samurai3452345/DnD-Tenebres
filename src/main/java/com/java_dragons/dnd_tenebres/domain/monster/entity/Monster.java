package com.java_dragons.dnd_tenebres.domain.monster.entity;

import com.java_dragons.dnd_tenebres.core.math.DiceRoller;
import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.effect.model.ActiveEffect;
import com.java_dragons.dnd_tenebres.domain.item.model.DiceType;
import com.java_dragons.dnd_tenebres.domain.monster.model.MonsterSkill;
import com.java_dragons.dnd_tenebres.domain.monster.strategy.MonsterSkillStrategy;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "monsters")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Monster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "level")
    private int level;

    @Column(name = "max_hp", nullable = false)
    private int maxHp;

    @Column(name = "current_hp", nullable = false)
    @Setter(AccessLevel.NONE)
    private int currentHp;

    @Column(name = "armor_class", nullable = false)
    private int armorClass;

    @Column(name = "xp_reward", nullable = false)
    private int xpReward;

    @Column(name = "gold_reward", nullable = false)
    private int goldReward;

    @Enumerated(EnumType.STRING)
    @Column(name = "damage_dice", nullable = false)
    private DiceType damageDice;

    @Column(name = "dice_count", nullable = false)
    @Builder.Default
    private int diceCount = 1;

    @Column(name = "damage_bonus", nullable = false)
    private int damageBonus;

    @Column(name = "attack_name", nullable = false)
    private String attackName;

    @Enumerated(EnumType.STRING)
    @Column(name = "special_skill", nullable = false)
    @Builder.Default
    private MonsterSkill specialSkill = MonsterSkill.NONE;

    @Column(name = "skill_frequency", nullable = false)
    @Builder.Default
    private int skillFrequency = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "monster_elements", joinColumns = @JoinColumn(name = "monster_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "element")
    @Builder.Default
    private Set<DamageType> elements = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "monster_resistances", joinColumns = @JoinColumn(name = "monster_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "element")
    @Builder.Default
    private Set<DamageType> resistances = new HashSet<>();

    public void takeDamage(int damage, DamageType type) {
        int finalDamage = damage;

        if (this.resistances.contains(type)) {
            finalDamage = (int) (damage * 0.8);
        }

        this.currentHp = Math.max(0, this.currentHp - finalDamage);
    }

    public boolean isDead(){
        return currentHp <= 0;
    }

    @PrePersist
    public void initHp() {
        if (this.currentHp == 0) {
            this.currentHp = this.maxHp;
        }
    }

    public record MonsterAttackResult(String attackName, int totalDamage) {}

    public MonsterAttackResult performAttack(int round, MonsterSkillStrategy skillStrategy) {
        if (this.specialSkill != MonsterSkill.NONE && this.skillFrequency > 0 && round % this.skillFrequency == 0) {
            if (skillStrategy != null) {
                return skillStrategy.executeSkill(this);
            }
        }

        int diceDamage = DiceRoller.roll(this.diceCount, this.damageDice.getSides());
        int totalDamage = diceDamage + this.damageBonus;
        return new MonsterAttackResult(this.attackName, totalDamage);
    }
    @Transient
    private List<ActiveEffect> combatEffects = new ArrayList<>();
    public void addCombatEffect(ActiveEffect effect) {
        this.combatEffects.add(effect);
    }
    public List<ActiveEffect> getCombatEffects() {
        return this.combatEffects;
    }
}