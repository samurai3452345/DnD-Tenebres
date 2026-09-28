package com.java_dragons.dnd_tenebres.domain.combat.dto;

import com.java_dragons.dnd_tenebres.domain.combat.model.CombatAction;
import jakarta.validation.constraints.NotNull;

public record CombatActionRequest(
        @NotNull
        CombatAction action,
        Long targetId,
        Long abilityId,
        Long itemId) {
}
