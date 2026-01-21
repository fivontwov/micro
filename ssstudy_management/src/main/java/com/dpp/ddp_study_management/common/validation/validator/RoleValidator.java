package com.dpp.ddp_study_management.common.validation.validator;

import com.dpp.ddp_study_management.model.ERole;
import com.dpp.ddp_study_management.common.validation.annotation.ValidRole;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RoleValidator implements ConstraintValidator<ValidRole, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty())
            return  false;

        try {
            ERole.valueOf(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}