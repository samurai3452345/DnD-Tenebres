package com.java_dragons.dnd_tenebres.domain.player.dto;

public record PlayerDeathReport(
        long goldLost,
        String respawnLocationId,
        int restoredHp,
        int restoredMp) {
}
