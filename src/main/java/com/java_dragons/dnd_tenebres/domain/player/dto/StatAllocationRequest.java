package com.java_dragons.dnd_tenebres.domain.player.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public record StatAllocationRequest(
        @Min(value = 0, message = "Добавка к силе не может быть отрицательной")
        @Max(value = 30, message = "За один запрос нельзя добавить больше 30 к характеристике") int addStrength,
        @Min(value = 0, message = "Добавка к ловкости не может быть отрицательной")
        @Max(value = 30, message = "За один запрос нельзя добавить больше 30 к характеристике") int addDexterity,
        @Min(value = 0, message = "Добавка к телосложению не может быть отрицательной")
        @Max(value = 30, message = "За один запрос нельзя добавить больше 30 к характеристике") int addConstitution,
        @Min(value = 0, message = "Добавка к интеллекту не может быть отрицательной")
        @Max(value = 30, message = "За один запрос нельзя добавить больше 30 к характеристике") int addIntelligence,
        @Min(value = 0, message = "Добавка к мудрости не может быть отрицательной")
        @Max(value = 30, message = "За один запрос нельзя добавить больше 30 к характеристике") int addWisdom,
        @Min(value = 0, message = "Добавка к харизме не может быть отрицательной")
        @Max(value = 30, message = "За один запрос нельзя добавить больше 30 к характеристике") int addCharisma
) {}
