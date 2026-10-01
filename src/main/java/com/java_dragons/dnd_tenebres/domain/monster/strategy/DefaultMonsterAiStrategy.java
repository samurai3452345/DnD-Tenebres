package com.java_dragons.dnd_tenebres.domain.monster.strategy;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.model.MonsterSkill;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.stereotype.Component;
@Component
public class DefaultMonsterAiStrategy implements MonsterAiStrategy {
    @Override public MonsterDecision decide(Player player, Monster monster, int round) {
        boolean abilityReady = monster.getSpecialSkill() != MonsterSkill.NONE && monster.getSkillFrequency() > 0
                && round % monster.getSkillFrequency() == 0;
        if (abilityReady || (monster.getCurrentHp() * 100 / Math.max(1, monster.getMaxHp()) <= 30
                && monster.getSpecialSkill() != MonsterSkill.NONE))
            return new MonsterDecision(MonsterDecision.Action.SPECIAL_ABILITY, monster.getSpecialSkill().name());
        return new MonsterDecision(MonsterDecision.Action.BASIC_ATTACK, null);
    }
}
