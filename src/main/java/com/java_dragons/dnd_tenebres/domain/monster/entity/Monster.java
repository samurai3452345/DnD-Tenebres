package com.java_dragons.dnd_tenebres.domain.monster.entity;


import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "monsters")
@Getter
@Setter
public class Monster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "max_hp", nullable = false)
    private int maxHp;

    @Column(name = "current_hp", nullable = false)
    private int currentHp;

    @Column(name = "armor_class", nullable = false)
    private int armorClass;

    @Column(name = "xp_reward", nullable = false)
    private int xpReward;

    @Column(name = "gold_reward", nullable = false)
    private int goldReward;

    @ElementCollection(fetch = FetchType.EAGER) // EAGER здесь допустим, так как стихий мало (1-3)
    @CollectionTable(name = "monster_elements", joinColumns = @JoinColumn(name = "monster_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "element")
    private Set<DamageType> elements = new HashSet<>();


    public void takeDamage(int damage){
        this.currentHp = Math.max(0, this.currentHp - damage);

    }

    public  boolean isDead(){
        return currentHp <= 0;
    }
}
