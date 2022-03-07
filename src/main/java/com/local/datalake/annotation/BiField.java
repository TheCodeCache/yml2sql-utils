package com.local.datalake.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.local.datalake.validator.BiFieldValidator;

/**
 * cross-field validation involving a pair of fields
 * 
 * @author manoranjan
 */
@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = BiFieldValidator.class)
@Documented
public @interface BiField {

    String message() default "{validation.constraints.bifield.defaultMessage}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * @return The first field
     */
    String parent();

    /**
     * @return The second field
     */
    String child();

    /**
     * Defines several <code>@BiField</code> annotations on the same element
     *
     * @see BiField
     */
    @Target({ TYPE, ANNOTATION_TYPE })
    @Retention(RUNTIME)
    @Documented
    @interface List {
        BiField[] value();
    }
}
