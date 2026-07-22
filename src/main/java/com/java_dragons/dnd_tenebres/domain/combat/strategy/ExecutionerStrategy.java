package com.java_dragons.dnd_tenebres.domain.combat.strategy;

import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExecutionerStrategy implements ItemPassiveStrategy {

    @Override
    public ItemPassive getTargetPassive() {
        return ItemPassive.EXECUTIONER;
    }

    @Override
    public int modifyOutgoingDamage(Player player, Monster monster, int aliveEnemyCount, DamageType damageType, int currentDamage, List<CombatEvent> events) {
        if (monster.getCurrentHp() < (monster.getMaxHp() * 0.1)) {
            events.add(new CombatEvent(player.getName(), "PASSIVE_TRIGGER", monster.getName(), currentDamage, "Сет Палача (казнь)"));
            return currentDamage * 2;
        }
        return currentDamage;
    }
}