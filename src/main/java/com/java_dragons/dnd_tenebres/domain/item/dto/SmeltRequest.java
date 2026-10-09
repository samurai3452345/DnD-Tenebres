package com.java_dragons.dnd_tenebres.domain.item.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SmeltRequest(@NotBlank String oreName, @Min(1) @Max(1000) int amount) {}
