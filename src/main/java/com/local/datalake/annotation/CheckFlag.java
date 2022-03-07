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

import com.local.datalake.validator.Enums.Flag;
import com.local.datalake.validator.FlagValidator;

@Target({ METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = FlagValidator.class)
@Documented
public @interface CheckFlag {

    String message() default "{validation.constraints.checkflag.defaultMessage}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Flag[] value() default {};

    boolean optional();
}
