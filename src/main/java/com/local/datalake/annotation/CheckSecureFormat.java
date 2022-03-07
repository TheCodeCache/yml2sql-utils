package com.local.datalake.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.local.datalake.validator.Enums.SecureDataFormat;
import com.local.datalake.validator.SecureFormatValidator;

@Target({ METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = SecureFormatValidator.class)
@Documented
public @interface CheckSecureFormat {

    String message() default "{validation.constraints.checksecuredataformat.defaultMessage}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean optional();

    SecureDataFormat[] values() default {};
}
