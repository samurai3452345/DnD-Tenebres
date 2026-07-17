package com.java_dragons.dnd_tenebres.domain.combat.strategy;

import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.stereotype.Component;

@Component
public class OrichalcumStrategy implements ItemPassiveStrategy {

    @Override
    public ItemPassive getTargetPassive() {
        return ItemPassive.ORICHALCUM;
    }

    @Override
    public int modifyIncomingDamage(Player player, Monster attacker, DamageType damageType, int incomingDamage, StringBuilder log) {
        if (damageType == DamageType.PHYSICAL) {
            int reducedDamage = (int) (incomingDamage * 0.85); // Оставляем 85% от урона (снижение на 15%)
            int blockedAmount = incomingDamage - reducedDamage;

            if (blockedAmount > 0) {
                log.append(String.format("🛡️ Орихалковая броня поглощает часть удара (заблокировано: %d)! ", blockedAmount));
            }
            return reducedDamage;
        }
        return incomingDamage; // Магию орихалк пропускает полностью
    }
}