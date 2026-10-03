package com.java_dragons.dnd_tenebres.domain.player.dto;

import com.java_dragons.dnd_tenebres.domain.player.entity.PlayerStats;

public record CharacterSummaryResponse(
        long playerId,
        String playerName,
        int level,
        String locationId,
        String locationName,
        long gold,
        PlayerStats stats,
        int statPoints
) {
}
