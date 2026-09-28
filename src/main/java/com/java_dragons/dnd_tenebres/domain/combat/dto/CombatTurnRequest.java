package com.java_dragons.dnd_tenebres.domain.combat.dto;

import com.java_dragons.dnd_tenebres.domain.combat.model.CombatAction;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CombatTurnRequest(
        @NotNull(message = "ID монстра обязателен") Long monsterId,
        @Min(value = 1, message = "Номер раунда должен быть больше нуля") int round,
        @Min(value = 1, message = "Количество живых противников должно быть больше нуля") int aliveEnemyCount,
        @NotNull(message = "Боевое действие обязательно") CombatAction action,
        @Size(max = 100, message = "Название цели не должно быть длиннее 100 символов") String actionTargetName
) {}
