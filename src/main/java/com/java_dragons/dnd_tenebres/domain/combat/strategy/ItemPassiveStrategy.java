package com.java_dragons.dnd_tenebres.domain.combat.strategy;

import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;

import java.util.List;

public interface ItemPassiveStrategy {

    ItemPassive getTargetPassive();

    default int modifyOutgoingDamage(Player player, Monster target, int aliveEnemyCount, DamageType damageType, int currentDamage, List<CombatEvent> events) {
        return currentDamage;
    }

    default int modifyIncomingDamage(Player player, Monster attacker, DamageType damageType, int incomingDamage, List<CombatEvent> events) {
        return incomingDamage;
    }

    default void onRoundStart(Player player, List<CombatEvent> events) {
    }
}