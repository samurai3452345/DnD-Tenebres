package com.java_dragons.dnd_tenebres.domain.exploration.dto;

import jakarta.validation.constraints.NotBlank;

public record TravelRequest(
        @NotBlank(message = "ID целевой локации не может быть пустым")
        String targetLocationId
) {}