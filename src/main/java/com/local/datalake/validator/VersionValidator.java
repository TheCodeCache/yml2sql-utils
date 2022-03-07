package com.local.datalake.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.local.datalake.annotation.CheckVersion;
import com.local.datalake.validator.Enums.Version;

/**
 * Validates Swagger File Version
 */
public class VersionValidator implements ConstraintValidator<CheckVersion, String> {

    @Override
    public boolean isValid(String version, ConstraintValidatorContext constraintContext) {

        boolean isValid = Version.exists(version);

        if (!isValid) {
            constraintContext.disableDefaultConstraintViolation();
            constraintContext
                    .buildConstraintViolationWithTemplate("{validation.constraints.checkversion.customMessage}")
                    .addConstraintViolation();
        }
        return isValid;
    }
}
