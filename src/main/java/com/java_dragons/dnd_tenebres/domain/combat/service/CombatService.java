package com.java_dragons.dnd_tenebres.domain.combat.service;

import com.java_dragons.dnd_tenebres.domain.item.entity.Weapon;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;

public interface CombatService {
    String executeRound(Player attacker,  Monster defender, int aliveEnemyCount, int round);
}
