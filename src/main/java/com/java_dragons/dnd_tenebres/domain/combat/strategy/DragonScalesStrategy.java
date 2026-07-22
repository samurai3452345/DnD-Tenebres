package com.java_dragons.dnd_tenebres.domain.combat.strategy;

import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DragonScalesStrategy implements ItemPassiveStrategy {

    @Override
    public ItemPassive getTargetPassive() {
        return ItemPassive.DRAGON_SCALES;
    }

    @Override
    public int modifyIncomingDamage(Player player, Monster attacker, DamageType damageType, int incomingDamage, List<CombatEvent> events) {
        if (damageType != DamageType.PHYSICAL) {
            int reducedDamage = (int) (incomingDamage * 0.80);
            int blockedAmount = incomingDamage - reducedDamage;
            if (blockedAmount > 0) {
                events.add(new CombatEvent(player.getName(), "PASSIVE_DEFENSE", attacker.getName(), blockedAmount, "Драконья чешуя (защита)"));
            }
            return reducedDamage;
        }
        return incomingDamage;
    }

    @Override
    public int modifyOutgoingDamage(Player player, Monster target, int aliveEnemyCount, DamageType outgoingDamageType, int currentDamage, List<CombatEvent> events) {
        if (outgoingDamageType != DamageType.PHYSICAL) {
            int bonusDamage = (int) (currentDamage * 0.20);
            events.add(new CombatEvent(player.getName(), "PASSIVE_TRIGGER", target.getName(), bonusDamage, "Драконья чешуя (усиление магии)"));
            return currentDamage + bonusDamage;
        }
        return currentDamage;
    }
}