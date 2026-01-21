package com.dpp.ddp_study_management.common.validation.validator;

import com.dpp.ddp_study_management.common.validation.annotation.ValidMentorIds;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ValidMentorIdsValidator implements ConstraintValidator<ValidMentorIds, List<Long>> {

    private static final Long MAX_ID_VALUE = 999999999999L;

    @Override
    public boolean isValid(List<Long> mentorIds, ConstraintValidatorContext context) {
        if (mentorIds == null || mentorIds.isEmpty()) {
            return true;
        }

        for (Long mentorId : mentorIds) {
            if (mentorId <= 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Mentor ID must be a positive number")
                        .addConstraintViolation();
                return false;
            }
            else if (mentorId > MAX_ID_VALUE) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Mentor ID must not exceed the maximum "+MAX_ID_VALUE+" value")
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}