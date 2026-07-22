package com.java_dragons.dnd_tenebres.domain.combat.strategy;

import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LightbringerStrategy implements ItemPassiveStrategy {

    @Override
    public ItemPassive getTargetPassive() {
        return ItemPassive.LIGHTBRINGER;
    }

    @Override
    public int modifyOutgoingDamage(Player player, Monster monster, int aliveEnemyCount, DamageType damageType, int currentDamage, List<CombatEvent> events) {
        boolean isDarknessSpawn = monster.getElements().contains(DamageType.DARK);

        if (isDarknessSpawn) {
            int holyDamage = (int) (currentDamage * 0.3);
            events.add(new CombatEvent(player.getName(), "PASSIVE_TRIGGER", monster.getName(), holyDamage, "Светоносец (святой урон)"));
            return currentDamage + holyDamage;
        }
        return currentDamage;
    }
}