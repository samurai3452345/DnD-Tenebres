package com.java_dragons.dnd_tenebres.domain.player.dto;

public record RestReport(
        String message,
        boolean isAmbushed,
        String locationId,
        com.java_dragons.dnd_tenebres.domain.combat.dto.CombatStateResponse encounter
) {
    public RestReport(String message, boolean isAmbushed) {
        this(message, isAmbushed, null, null);
    }

    public RestReport(String message, boolean isAmbushed, String locationId) { this(message, isAmbushed, locationId, null); }
}
