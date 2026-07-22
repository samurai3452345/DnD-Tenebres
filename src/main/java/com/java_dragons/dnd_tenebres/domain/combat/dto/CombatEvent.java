package com.java_dragons.dnd_tenebres.domain.combat.dto;

public record CombatEvent(
        String actor,
        String actionType,
        String target,
        int value,
        String description
) {}