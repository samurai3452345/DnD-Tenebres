package com.java_dragons.dnd_tenebres.domain.combat.dto;

public record CombatEvent(
        String actor,
        String actionType,
        String target,
        int value,
        String description,
        Integer round
) {
    public CombatEvent(String actor, String actionType, String target, int value, String description) {
        this(actor, actionType, target, value, description, null);
    }
}
