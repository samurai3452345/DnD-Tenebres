package com.java_dragons.dnd_tenebres.domain.monster.strategy;

import com.java_dragons.dnd_tenebres.core.math.DiceRoller;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.model.MonsterSkill;
import org.springframework.stereotype.Component;

@Component
public class CleaveSkillStrategy implements MonsterSkillStrategy {

    @Override
    public MonsterSkill getTargetSkill() {
        return MonsterSkill.CLEAVE;
    }

    @Override
    public Monster.MonsterAttackResult executeSkill(Monster monster) {
        int diceDamage = DiceRoller.roll(1, monster.getDamageDice().getSides());
        int totalDamage = (int) ((diceDamage + monster.getDamageBonus()) * 1.5);

        return new Monster.MonsterAttackResult("КРУГОВОЙ УДАР (Cleave)", totalDamage);
    }
}