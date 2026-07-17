package com.java_dragons.dnd_tenebres.domain.combat.strategy;

import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.stereotype.Component;

@Component
public class BerserkerRageStrategy implements ItemPassiveStrategy {

    @Override
    public ItemPassive getTargetPassive() {
        return ItemPassive.BERSERKER_RAGE;
    }

    @Override
    public int modifyOutgoingDamage(Player player, Monster monster, int aliveEnemyCount, DamageType damageType, int currentDamage, StringBuilder log) {
        double hpPercentage = (double) player.getCurrentHp() / player.getMaxHp();

        if (hpPercentage <= 0.20) {
            log.append(" КРОВАВОЕ БЕЗУМИЕ БЕРСЕРКА! Находясь на пороге смерти, вы наносите двойной урон! ");
            return currentDamage * 2;
        } else if (hpPercentage <= 0.50) {
            int bonusDamage = (int) (currentDamage * 0.3);
            log.append(String.format("Ярость Берсерка! Боль делает вас сильнее (+%d урона)! ", bonusDamage));
            return currentDamage + bonusDamage;
        }

        return currentDamage;
    }
}