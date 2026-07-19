package com.java_dragons.dnd_tenebres.domain.monster.entity;


import com.java_dragons.dnd_tenebres.core.math.DiceRoller;
import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.model.DiceType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
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
    @Builder.Default
    private Set<DamageType> elements = new HashSet<>();

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


    public record MonsterAttackResult(String attackName, int totalDamage) {} // Удобная DTO для возврата двух значений

    public MonsterAttackResult performAttack(int round) {
        int diceDamage = DiceRoller.roll(1, this.damageDice.getSides());
        int totalDamage = diceDamage + this.damageBonus;
        String attackName = this.attackName;

        // Вся специфика монстров хранится внутри монстра! Сервис об этом не знает.
        if ("Скелет-страж".equals(this.name) && round % 3 == 0) {
            totalDamage = (int) (totalDamage * 1.5);
            attackName = "КРУГОВОЙ УДАР";
        }

        return new MonsterAttackResult(attackName, totalDamage);
    }

}
