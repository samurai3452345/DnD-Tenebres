package com.java_dragons.dnd_tenebres.domain.combat.strategy;


import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.stereotype.Component;


@Component
public class LightbringerStrategy implements ItemPassiveStrategy {

    @Override
    public ItemPassive getTargetPassive() {
        return ItemPassive.LIGHTBRINGER;
    }

    @Override
    public int modifyOutgoingDamage(Player player, Monster monster, int aliveEnemyCount, int currentDamage, StringBuilder log) {

        boolean isDarknessSpawn = monster.getElements().contains(DamageType.DARK);

        if (isDarknessSpawn) {
            int holyDamage = (int) (currentDamage * 0.3);
            log.append(String.format("✨ Броня Светоносца ослепляет порождение тьмы святым огнем (+%d урона)! ", holyDamage));
            return currentDamage + holyDamage;
        }

        return currentDamage;
    }
}