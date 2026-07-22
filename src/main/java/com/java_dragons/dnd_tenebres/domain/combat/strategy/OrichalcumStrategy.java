package com.java_dragons.dnd_tenebres.domain.combat.strategy;

import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrichalcumStrategy implements ItemPassiveStrategy {

    @Override
    public ItemPassive getTargetPassive() {
        return ItemPassive.ORICHALCUM;
    }

    @Override
    public int modifyIncomingDamage(Player player, Monster attacker, DamageType damageType, int incomingDamage, List<CombatEvent> events) {
        if (damageType == DamageType.PHYSICAL) {
            int reducedDamage = (int) (incomingDamage * 0.85);
            int blockedAmount = incomingDamage - reducedDamage;

            if (blockedAmount > 0) {
                events.add(new CombatEvent(player.getName(), "PASSIVE_DEFENSE", attacker.getName(), blockedAmount, "Орихалковая броня (защита)"));
            }
            return reducedDamage;
        }
        return incomingDamage;
    }
}