package com.java_dragons.dnd_tenebres.infrastructure.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Имя пользователя не может быть пустым")
        @Size(max = 32, message = "Имя пользователя не должно быть длиннее 32 символов")
        String username,

        @NotBlank(message = "Пароль не может быть пустым")
        @Size(max = 72, message = "Пароль не должен быть длиннее 72 символов")
        String password
) {
}
