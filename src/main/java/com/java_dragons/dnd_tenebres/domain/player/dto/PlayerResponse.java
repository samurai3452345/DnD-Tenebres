package com.java_dragons.dnd_tenebres.domain.player.dto;

import com.java_dragons.dnd_tenebres.domain.player.entity.PlayerStats;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PlayerResponse {
    long playerId;
    String playerName;
    int level;
    long experience;
    int currentHp;
    int maxHp;
    int gold;
    PlayerStats stats;

}
