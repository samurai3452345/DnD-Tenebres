package com.java_dragons.dnd_tenebres.domain.combat.strategy;

import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.stereotype.Component;

@Component
public class DragonScalesStrategy implements ItemPassiveStrategy {

    @Override
    public ItemPassive getTargetPassive() {
        return ItemPassive.DRAGON_SCALES;
    }

    // ЗАЩИТА: Снижаем входящий магический урон
    @Override
    public int modifyIncomingDamage(Player player, Monster attacker, DamageType damageType, int incomingDamage, StringBuilder log) {
        if (damageType != DamageType.PHYSICAL) {
            int reducedDamage = (int) (incomingDamage * 0.80);
            int blockedAmount = incomingDamage - reducedDamage;

            if (blockedAmount > 0) {
                log.append(String.format("🐉 Драконья чешуя рассеивает магию стихий (заблокировано: %d)! ", blockedAmount));
            }
            return reducedDamage;
        }
        return incomingDamage;
    }

    // АТАКА: Увеличиваем исходящий магический урон
    @Override
    public int modifyOutgoingDamage(Player player, Monster target, int aliveEnemyCount, DamageType outgoingDamageType, int currentDamage, StringBuilder log) {
        // Если игрок бьет магией (НЕ физическим уроном)
        if (outgoingDamageType != DamageType.PHYSICAL) {
            int bonusDamage = (int) (currentDamage * 0.20); // +20% к урону
            log.append(String.format("🔥 Чешуя резонирует с вашей магией (+%d стихийного урона)! ", bonusDamage));
            return currentDamage + bonusDamage;
        }
        return currentDamage; // Физический удар мечом не усиливается
    }
}