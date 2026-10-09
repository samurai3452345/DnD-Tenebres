package com.java_dragons.dnd_tenebres.domain.monster.strategy;

import com.java_dragons.dnd_tenebres.core.random.RandomSource;
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
    public Monster.MonsterAttackResult executeSkill(Monster monster, RandomSource randomSource) {
        int diceDamage = randomSource.roll(1, monster.getDamageDice().getSides());
        int totalDamage = (int) ((diceDamage + monster.getDamageBonus()) * 1.5);

        return new Monster.MonsterAttackResult("КРУГОВОЙ УДАР (Cleave)", totalDamage);
    }
}
