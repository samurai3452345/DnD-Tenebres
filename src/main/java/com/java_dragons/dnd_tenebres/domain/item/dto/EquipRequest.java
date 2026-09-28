package com.java_dragons.dnd_tenebres.domain.item.dto;

import com.java_dragons.dnd_tenebres.domain.item.model.EquipmentSlot;
import jakarta.validation.constraints.NotNull;

public record EquipRequest(
        @NotNull(message = "Слот экипировки обязателен") EquipmentSlot slot
) {}
