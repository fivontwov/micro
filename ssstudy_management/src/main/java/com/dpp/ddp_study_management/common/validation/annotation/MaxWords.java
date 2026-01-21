package com.dpp.ddp_study_management.common.validation.annotation;

import com.dpp.ddp_study_management.common.validation.validator.MaxWordsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaxWordsValidator.class)
public @interface MaxWords {
    int max() default 100;
    String message() default "Description must not exceed {max} words";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
