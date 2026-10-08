package com.java_dragons.dnd_tenebres.infrastructure.security.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default "Пароль должен содержать от 8 до 72 символов, хотя бы одну букву и одну цифру";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
