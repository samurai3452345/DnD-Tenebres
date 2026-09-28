package com.java_dragons.dnd_tenebres.domain.monster.strategy;

import com.java_dragons.dnd_tenebres.core.math.DiceRoller;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.model.MonsterSkill;
import org.springframework.stereotype.Component;

@Component
public class FireBreathSkillStrategy implements MonsterSkillStrategy {
    @Override
    public MonsterSkill getTargetSkill() {
        return MonsterSkill.FIRE_BREATH;
    }

    @Override
    public Monster.MonsterAttackResult executeSkill(Monster monster) {
        int damage = DiceRoller.roll(Math.max(2, monster.getDiceCount()), monster.getDamageDice().getSides())
                + monster.getDamageBonus();
        return new Monster.MonsterAttackResult("Огненное дыхание", damage);
    }
}
