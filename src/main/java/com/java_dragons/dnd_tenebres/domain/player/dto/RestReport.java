package com.java_dragons.dnd_tenebres.domain.player.dto;

public record RestReport(
        String message,
        boolean isAmbushed,
        String locationId
) {
    public RestReport(String message, boolean isAmbushed) {
        this(message, isAmbushed, null);
    }
}