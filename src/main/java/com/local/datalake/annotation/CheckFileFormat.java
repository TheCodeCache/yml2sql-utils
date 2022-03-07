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

import com.local.datalake.validator.Enums.FileFormat;
import com.local.datalake.validator.FileFormatValidator;

@Target({ METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = FileFormatValidator.class)
@Documented
public @interface CheckFileFormat {

    String message() default "{validation.constraints.checkfileformat.defaultMessage}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    
    boolean required() default true;

    FileFormat[] value();
}
