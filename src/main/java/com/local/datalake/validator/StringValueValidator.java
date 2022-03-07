package com.local.datalake.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.local.datalake.annotation.CheckForValue;

/**
 * validates against the values defined by IN annotation
 */
public class StringValueValidator implements ConstraintValidator<CheckForValue, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintContext) {

        boolean isValid = (value == null || value.isEmpty()) ? false : true;

        if (!isValid) {
            constraintContext.disableDefaultConstraintViolation();
            constraintContext
                    .buildConstraintViolationWithTemplate("{validation.constraints.checkforvalue.customMessage}")
                    .addConstraintViolation();
        }
        return isValid;
    }
}
