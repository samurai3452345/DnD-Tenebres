package com.java_dragons.dnd_tenebres.infrastructure.security.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class StrongPasswordValidatorTest {
    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    @ParameterizedTest
    @ValueSource(strings = {"Пароль123", "secure-password-9", "A2345678"})
    void принимаетЕдинуюПолитику(String password) {
        assertThat(validator.isValid(password, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "корот1", "толькобуквы", "12345678"})
    void отклоняетСлабыеПароли(String password) {
        assertThat(validator.isValid(password, null)).isFalse();
    }
}
