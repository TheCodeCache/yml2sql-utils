package com.local.datalake.validator;

import java.util.stream.Stream;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.local.datalake.annotation.IN;

/**
 * IN values validation logic
 * 
 * @author manoranjan
 */
public class InValidator implements ConstraintValidator<IN, String> {

    private String[] values;
    private boolean  required;

    @Override
    public void initialize(final IN constraintAnnotation) {
        values = constraintAnnotation.values();
        required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (!required)
            return true;
        else
            return Stream.of(values).anyMatch(x -> x.equalsIgnoreCase(value));
    }
}
