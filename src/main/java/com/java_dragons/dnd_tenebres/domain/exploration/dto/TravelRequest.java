package com.java_dragons.dnd_tenebres.domain.exploration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TravelRequest(
        @NotBlank(message = "ID целевой локации не может быть пустым")
        @Size(max = 100, message = "ID целевой локации не должен быть длиннее 100 символов")
        String targetLocationId
) {}
