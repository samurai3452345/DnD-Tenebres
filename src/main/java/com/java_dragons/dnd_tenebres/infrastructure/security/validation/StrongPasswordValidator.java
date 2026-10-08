package com.java_dragons.dnd_tenebres.infrastructure.security.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.length() < 8 || password.length() > 72) return false;
        return password.codePoints().anyMatch(Character::isLetter)
                && password.codePoints().anyMatch(Character::isDigit);
    }
}
