package com.java_dragons.dnd_tenebres.domain.player.dto;

public record RestReport(
        String message,
        boolean isAmbushed,
        String biome,
        int level
) {
    public RestReport(String message, boolean isAmbushed) {
        this(message, isAmbushed, null, 0); // Он сам подставит null и 0
    }
}