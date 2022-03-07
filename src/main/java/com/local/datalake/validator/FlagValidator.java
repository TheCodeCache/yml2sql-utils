package com.local.datalake.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.local.datalake.annotation.CheckFlag;
import com.local.datalake.validator.Enums.Flag;

/**
 * Validates for Yes/No value for a given field
 */
public class FlagValidator implements ConstraintValidator<CheckFlag, Boolean> {

    private boolean optional;

    @Override
    public void initialize(final CheckFlag constraintAnnotation) {
        optional = constraintAnnotation.optional();
    }

    @Override
    public boolean isValid(Boolean flag, ConstraintValidatorContext constraintContext) {

        if (optional)
            return true;

        boolean isValid = Flag.exists(flag.toString());

        if (!isValid) {
            constraintContext.disableDefaultConstraintViolation();
            constraintContext.buildConstraintViolationWithTemplate("{validation.constraints.checkflag.customMessage}")
                    .addConstraintViolation();
        }
        return isValid;
    }
}
