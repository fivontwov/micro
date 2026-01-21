package com.dpp.ddp_study_management.common.validation.validator;

import com.dpp.ddp_study_management.common.validation.annotation.MaxWords;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MaxWordsValidator implements ConstraintValidator<MaxWords, String> {

    private int maxWords;
    private String defaultMessage;

    @Override
    public void initialize(MaxWords constraintAnnotation) {
        this.maxWords = constraintAnnotation.max();
        this.defaultMessage = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        String[] words = value.trim().split("\\s+");

        if (words.length > maxWords) {
            context.disableDefaultConstraintViolation();
            String baseMessage = defaultMessage.replace("{max}", String.valueOf(maxWords));
            String customMessage = String.format("%s, but found %d words", baseMessage, words.length);
            context.buildConstraintViolationWithTemplate(customMessage)
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}