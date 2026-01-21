package com.dpp.ddp_study_management.common.validation.annotation;

import com.dpp.ddp_study_management.common.validation.validator.ValidMentorIdsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidMentorIdsValidator.class)
public @interface ValidMentorIds {
    String message() default "ID mentor not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}