package com.local.datalake.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.JsonPath;
import com.local.datalake.annotation.TestJsonPath;
import com.local.datalake.common.ViewHelper;

/**
 * data_privacy json-path validator
 */
public class JsonPathValidator implements ConstraintValidator<TestJsonPath, String> {

    private static final Logger log = LoggerFactory.getLogger(JsonPathValidator.class);

    @Override
    public boolean isValid(String path, ConstraintValidatorContext constraintContext) {

        boolean isValid;
        try {
            if (!ViewHelper.isNull(path))
                JsonPath.compile(path);
            isValid = true;
        } catch (Exception ex) {
            log.error("{}", ex.getMessage(), ex);
            isValid = false;
        }

        if (!isValid) {
            constraintContext.disableDefaultConstraintViolation();
            constraintContext
                    .buildConstraintViolationWithTemplate("{validation.constraints.testjsonpath.customMessage}")
                    .addConstraintViolation();
        }
        return isValid;
    }
}
