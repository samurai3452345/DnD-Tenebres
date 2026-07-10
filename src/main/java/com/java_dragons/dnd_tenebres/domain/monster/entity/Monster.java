package com.java_dragons.dnd_tenebres.domain.monster.entity;


import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.model.DiceType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "monsters")
@Getter
@NoArgsConstructor
public class Monster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "damage_bonus", nullable = false)
    private int damageBonus;

    @Column(name = "attack_name", nullable = false)
    private String attackName;

    @ElementCollection(fetch = FetchType.EAGER) // EAGER здесь допустим, так как стихий мало (1-3)
    @CollectionTable(name = "monster_elements", joinColumns = @JoinColumn(name = "monster_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "element")
    private Set<DamageType> elements = new HashSet<>();

    public Monster(String name, int level, int maxHp,int armorClass, int xpReward, int goldReward, DiceType damageDice, int damageBonus, String attackName, Set<DamageType> elements) {
        this.name = name;
        this.level = level;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.armorClass = armorClass;
        this.xpReward = xpReward;
        this.goldReward = goldReward;
        this.elements = elements;
        this.damageDice = damageDice;
        this.damageBonus = damageBonus;
        this.attackName = attackName;
    }

    public void takeDamage(int damage){
        this.currentHp = Math.max(0, this.currentHp - damage);

    }

    public  boolean isDead(){
        return currentHp <= 0;
    }

    @PrePersist
    public void initHp() {
        if (this.currentHp == 0) {
            this.currentHp = this.maxHp;
        }
    }



}
