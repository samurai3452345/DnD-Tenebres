package com.java_dragons.dnd_tenebres.domain.player.dto;

import jakarta.validation.constraints.Min;

public record StatAllocationRequest(
        @Min(8) int addStrength,
        @Min(8) int addDexterity,
        @Min(8) int addConstitution,
        @Min(8) int addIntelligence,
        @Min(8) int addWisdom,
        @Min(8) int addCharisma
) {}