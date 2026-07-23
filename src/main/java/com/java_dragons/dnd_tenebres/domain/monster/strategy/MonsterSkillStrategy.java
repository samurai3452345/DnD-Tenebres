package com.java_dragons.dnd_tenebres.domain.monster.strategy;

import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.model.MonsterSkill;

public interface MonsterSkillStrategy {
    MonsterSkill getTargetSkill();

    Monster.MonsterAttackResult executeSkill(Monster monster);
}