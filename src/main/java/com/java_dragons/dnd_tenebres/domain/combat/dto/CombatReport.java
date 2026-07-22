package com.java_dragons.dnd_tenebres.domain.combat.dto;

import java.util.List;

public record CombatReport(
        int round,
        List<CombatEvent> events,
        boolean isEnemyDead,
        boolean isPlayerDead
) {}