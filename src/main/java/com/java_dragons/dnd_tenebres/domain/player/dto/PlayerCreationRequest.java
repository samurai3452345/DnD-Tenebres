package com.java_dragons.dnd_tenebres.domain.player.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public record PlayerCreationRequest(
        @NotBlank(message = "Имя персонажа не может быть пустым")
        @Size(min = 2, max = 20, message = "Имя должно содержать от 2 до 20 символов")
        String name,
        @Min(value = 8, message = "Сила не может быть меньше 8") @Max(value = 15, message = "Сила не может быть больше 15") int strength,
        @Min(value = 8, message = "Ловкость не может быть меньше 8") @Max(value = 15, message = "Ловкость не может быть больше 15") int dexterity,
        @Min(value = 8, message = "Телосложение не может быть меньше 8") @Max(value = 15, message = "Телосложение не может быть больше 15") int constitution,
        @Min(value = 8, message = "Интеллект не может быть меньше 8") @Max(value = 15, message = "Интеллект не может быть больше 15") int intelligence,
        @Min(value = 8, message = "Мудрость не может быть меньше 8") @Max(value = 15, message = "Мудрость не может быть больше 15") int wisdom,
        @Min(value = 8, message = "Харизма не может быть меньше 8") @Max(value = 15, message = "Харизма не может быть больше 15") int charisma
) {}
