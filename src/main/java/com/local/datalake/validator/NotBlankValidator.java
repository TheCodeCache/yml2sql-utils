package com.local.datalake.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.local.datalake.annotation.NotBlank;
import com.local.datalake.common.ViewHelper;

/**
 * checks for fields value to be not-null
 */
public class NotBlankValidator implements ConstraintValidator<NotBlank, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintContext) {

        boolean isValid = ViewHelper.isNull(value) ? false : true;

        if (!isValid) {
            constraintContext.disableDefaultConstraintViolation();
            constraintContext.buildConstraintViolationWithTemplate("{validation.constraints.notblank.customMessage}")
                    .addConstraintViolation();
        }
        return isValid;
    }
}
