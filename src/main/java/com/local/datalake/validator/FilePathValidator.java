package com.local.datalake.validator;

import java.nio.file.Paths;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.local.datalake.annotation.CheckPath;

/**
 * Swagger yml file path validator
 */
public class FilePathValidator implements ConstraintValidator<CheckPath, String> {

    @Override
    public boolean isValid(String path, ConstraintValidatorContext constraintContext) {

        boolean isValid = Paths.get(path).toFile().exists();

        if (!isValid) {
            constraintContext.disableDefaultConstraintViolation();
            constraintContext.buildConstraintViolationWithTemplate("{validation.constraints.checkpath.customMessage}")
                    .addConstraintViolation();
        }
        return isValid;
    }
}
