package com.java_dragons.dnd_tenebres.domain.player.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlayerCreationRequest(
        @NotBlank(message = "Имя персонажа не может быть пустым")
        @Size(min = 2, max = 20, message = "Имя должно содержать от 2 до 20 символов")
        String name,
        int strength,
        int dexterity,
        int constitution,
        int intelligence,
        int wisdom,
        int charisma
) {
}
