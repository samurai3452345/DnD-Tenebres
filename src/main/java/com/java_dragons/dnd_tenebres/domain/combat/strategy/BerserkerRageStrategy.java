package com.java_dragons.dnd_tenebres.domain.combat.strategy;

import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BerserkerRageStrategy implements ItemPassiveStrategy {

    @Override
    public ItemPassive getTargetPassive() {
        return ItemPassive.BERSERKER_RAGE;
    }

    @Override
    public int modifyOutgoingDamage(Player player, Monster monster, int aliveEnemyCount, DamageType damageType, int currentDamage, List<CombatEvent> events) {
        double hpPercentage = (double) player.getCurrentHp() / player.getMaxHp();

        if (hpPercentage <= 0.20) {
            events.add(new CombatEvent(player.getName(), "PASSIVE_TRIGGER", monster.getName(), currentDamage, "Кровавое безумие"));            return currentDamage * 2;
        } else if (hpPercentage <= 0.50) {
            int bonusDamage = (int) (currentDamage * 0.3);
            events.add(new CombatEvent(player.getName(), "PASSIVE_TRIGGER", monster.getName(), bonusDamage, "Ярость берсерка"));            return currentDamage + bonusDamage;
        }

        return currentDamage;
    }
}