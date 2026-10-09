package com.java_dragons.dnd_tenebres.infrastructure.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.java_dragons.dnd_tenebres.infrastructure.security.validation.StrongPassword;

public record RegisterRequest(
        @NotBlank(message = "Имя пользователя не может быть пустым")
        @Size(min = 3, max = 32, message = "Имя пользователя должно содержать от 3 до 32 символов")
        @Pattern(regexp = "[\\p{L}\\p{N}_.-]+", message = "Имя пользователя содержит недопустимые символы")
        String username,

        @StrongPassword
        String password
) {
}
