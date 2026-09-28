package com.java_dragons.dnd_tenebres.domain.player.dto;

import jakarta.validation.constraints.Min;

public record StatAllocationRequest(
        @Min(value = 0, message = "Добавка к силе не может быть отрицательной") int addStrength,
        @Min(value = 0, message = "Добавка к ловкости не может быть отрицательной") int addDexterity,
        @Min(value = 0, message = "Добавка к телосложению не может быть отрицательной") int addConstitution,
        @Min(value = 0, message = "Добавка к интеллекту не может быть отрицательной") int addIntelligence,
        @Min(value = 0, message = "Добавка к мудрости не может быть отрицательной") int addWisdom,
        @Min(value = 0, message = "Добавка к харизме не может быть отрицательной") int addCharisma
) {}
