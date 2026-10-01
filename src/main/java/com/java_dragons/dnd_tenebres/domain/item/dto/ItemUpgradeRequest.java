package com.java_dragons.dnd_tenebres.domain.item.dto;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ItemUpgradeRequest(
        String operationId,
        @NotNull(message = "ID улучшаемого предмета обязателен")
        Long targetItemId,
        @NotEmpty(message = "Нужно выбрать хотя бы один предмет для поглощения")
        @Size(max = 100, message = "За один раз можно поглотить не более 100 предметов")
        List<@NotNull(message = "ID предмета для поглощения не может быть пустым") Long> foodItemIds
) {}