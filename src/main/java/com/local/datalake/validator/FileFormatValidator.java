package com.local.datalake.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.local.datalake.annotation.CheckFileFormat;
import com.local.datalake.common.ViewHelper;
import com.local.datalake.validator.Enums.FileFormat;

/**
 * Swagger File Format Validator
 */
public class FileFormatValidator implements ConstraintValidator<CheckFileFormat, String> {

    @Override
    public boolean isValid(String object, ConstraintValidatorContext constraintContext) {

        boolean isValid = false;

        if (!(ViewHelper.getFileExtn(object) == null)) {
            isValid = true;
        } else
            isValid = FileFormat.exists(object);

        if (!isValid) {
            constraintContext.disableDefaultConstraintViolation();
            constraintContext
                    .buildConstraintViolationWithTemplate("{validation.constraints.checkfileformat.customMessage}")
                    .addConstraintViolation();
        }
        return isValid;
    }
}
