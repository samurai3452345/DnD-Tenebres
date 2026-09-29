package com.java_dragons.dnd_tenebres.domain.monster.strategy;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
public interface MonsterAiStrategy { MonsterDecision decide(Player player, Monster monster, int round); }
