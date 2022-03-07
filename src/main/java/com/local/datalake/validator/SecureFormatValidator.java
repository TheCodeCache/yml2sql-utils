package com.local.datalake.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.local.datalake.annotation.CheckSecureFormat;
import com.local.datalake.validator.Enums.SecureDataFormat;

/**
 * Secure-Data-Format vlaidator
 */
public class SecureFormatValidator implements ConstraintValidator<CheckSecureFormat, String> {

    private SecureDataFormat[] values;
    private boolean            optional;

    @Override
    public void initialize(final CheckSecureFormat constraintAnnotation) {
        values = constraintAnnotation.values();
        optional = constraintAnnotation.optional();
    }

    @Override
    public boolean isValid(String format, ConstraintValidatorContext constraintContext) {

        if (optional)
            return true;

        boolean isValid = (values == null || values.length == 0) ? SecureDataFormat.exists(format)
                : SecureDataFormat.exists(format, values);

        if (!isValid) {
            constraintContext.disableDefaultConstraintViolation();
            constraintContext.buildConstraintViolationWithTemplate(
                    "{validation.constraints.checksecuredataformat.customMessage}").addConstraintViolation();
        }
        return isValid;
    }
}
