package com.java_dragons.dnd_tenebres.domain.combat.dto;

import com.java_dragons.dnd_tenebres.domain.combat.model.CombatAction;

public record CombatTurnRequest(
        Long monsterId,
        int round,
        int aliveEnemyCount,
        CombatAction action,
        String actionTargetName
) {}