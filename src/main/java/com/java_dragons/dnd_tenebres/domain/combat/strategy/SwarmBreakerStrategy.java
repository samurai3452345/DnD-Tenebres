package com.java_dragons.dnd_tenebres.domain.combat.strategy;

import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SwarmBreakerStrategy implements ItemPassiveStrategy {
    @Override
    public ItemPassive getTargetPassive() {
        return ItemPassive.SWARM_BREAKER;
    }

    @Override
    public int modifyOutgoingDamage(Player player, Monster target, int aliveEnemyCount, DamageType type,
                                    int damage, List<CombatEvent> events) {
        if (aliveEnemyCount < 2) return damage;
        int bonus = Math.max(1, damage * Math.min(aliveEnemyCount - 1, 4) / 10);
        events.add(new CombatEvent(player.getName(), "PASSIVE_TRIGGER", target.getName(), bonus,
                "Разрушитель роя"));
        return damage + bonus;
    }
}
