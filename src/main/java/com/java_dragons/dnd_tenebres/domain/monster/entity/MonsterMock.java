package com.java_dragons.dnd_tenebres.domain.monster.entity;

import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class MonsterMock {
    private String name;
    private int hp;
    private int armorClass;
    private Set<DamageType> elements;

    public void takeDamage(int damage) {
        this.hp = Math.max(0, this.hp - damage);
    }

    public boolean isDead() {
        return this.hp <= 0;
    }
}
