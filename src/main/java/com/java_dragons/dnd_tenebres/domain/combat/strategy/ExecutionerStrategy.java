package com.java_dragons.dnd_tenebres.domain.combat.strategy;

import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.stereotype.Component;

@Component
public class ExecutionerStrategy implements ItemPassiveStrategy {
    @Override
    public ItemPassive getTargetPassive() { return ItemPassive.EXECUTIONER; }

    @Override
    public int modifyOutgoingDamage(Player player, Monster monster, int aliveEnemyCount, DamageType damageType, int currentDamage, StringBuilder log) {
        if (monster.getCurrentHp() < (monster.getMaxHp() * 0.1)) {
            log.append(" Сет Палача чувствует слабость жертвы! Урон удвоен! ");
            return currentDamage * 2;
        }
        return currentDamage;
    }
}