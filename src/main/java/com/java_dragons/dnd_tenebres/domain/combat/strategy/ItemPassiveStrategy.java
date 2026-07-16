package com.java_dragons.dnd_tenebres.domain.combat.strategy;

import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;

public interface ItemPassiveStrategy {

    ItemPassive getTargetPassive();

    default int modifyOutgoingDamage(Player player, Monster monster, int aliveEnemyCount, int currentDamage, StringBuilder log) {
        return currentDamage;
    }

    default void onRoundStart(Player player, StringBuilder log) {
    }
}