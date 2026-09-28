package com.java_dragons.dnd_tenebres.infrastructure.security.dto;

import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerCreationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Имя пользователя не может быть пустым")
        @Size(min = 3, max = 32, message = "Имя пользователя должно содержать от 3 до 32 символов")
        @Pattern(regexp = "[\\p{L}\\p{N}_.-]+", message = "Имя пользователя содержит недопустимые символы")
        String username,

        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 8, max = 72, message = "Пароль должен содержать от 8 до 72 символов")
        @Pattern(regexp = "(?=.*\\p{L})(?=.*\\d).+", message = "Пароль должен содержать хотя бы одну букву и одну цифру")
        String password,

        @NotNull(message = "Данные персонажа обязательны")
        @Valid PlayerCreationRequest playerRequest
) {
}
