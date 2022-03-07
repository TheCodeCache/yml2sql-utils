package com.local.datalake.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.local.datalake.annotation.BiField;
import com.local.datalake.common.ViewHelper;
import com.local.datalake.validator.Enums.SecureDataFormat;

/**
 * cross-field validation logic
 * 
 * @author manoranjan
 */
public class BiFieldValidator implements ConstraintValidator<BiField, Object> {
    private static final Logger log = LoggerFactory.getLogger(BiFieldValidator.class);

    private String              parentFieldName;
    private String              childFieldName;
    private String              message;

    @Override
    public void initialize(final BiField constraintAnnotation) {
        parentFieldName = constraintAnnotation.parent();
        childFieldName = constraintAnnotation.child();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {

        boolean isValid = true;
        // To track if cross-field validations are present for a given pair of field
        boolean matched = false;

        try {
            final String parentFieldValue = BeanUtils.getProperty(value, parentFieldName);
            final String childFieldValue = BeanUtils.getProperty(value, childFieldName);

            // For CompleteMsgEncrypted and SecureDataFormat field pairs
            if ("completeMsgEncrypted".equalsIgnoreCase(parentFieldName)
                    && "secureDataFormat".equalsIgnoreCase(childFieldName)) {
                if (parentFieldValue.equalsIgnoreCase("true")) {
                    if (ViewHelper.isNull(childFieldValue) || !SecureDataFormat.exists(childFieldValue))
                        isValid = false;
                }
                matched = true;
            }

            // For specFileFormat and yamlFilePath field pairs
            if ("specFileFormat".equalsIgnoreCase(parentFieldName) && "yamlFilePath".equalsIgnoreCase(childFieldName)) {
                if (ViewHelper.isNull(parentFieldValue)) {
                    isValid = false;
                    useCustomFailureMsg("validation.constraints.bifield.parent.nullMessage", context, true);
                } else if (ViewHelper.isNull(childFieldValue)) {
                    isValid = false;
                    useCustomFailureMsg("validation.constraints.bifield.child.nullMessage", context, true);
                }
                if (ViewHelper.isNotNullOrBlank(parentFieldValue) && ViewHelper.isNotNullOrBlank(childFieldValue)
                        && !parentFieldValue.equalsIgnoreCase(ViewHelper.getPathExtn(childFieldValue))) {
                    isValid = false;
                }
                matched = true;
            }

            // ADD VALIDATION LOGIC FOR OTHER CROSS-FIELD PAIRs IF REQUIRED//

            if (!matched)
                useCustomFailureMsg(message = String.format(
                        "Missing implementation for cross-field validation between fields \"%s\" and \"%s\""
                                + "\nTry removing @BiField annotation from Input class for these fields",
                        parentFieldName, childFieldName), context, false);
        } catch (final Exception ex) {
            log.error("BiFieldValidation Failed: {}", ex.getMessage(), ex);
            isValid = false;
        }
        return isValid;
    }

    /**
     * use custom message/template
     * 
     * @param value
     * @param context
     * @param useTemplate
     */
    private void useCustomFailureMsg(String value, ConstraintValidatorContext context, boolean useTemplate) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(useTemplate ? String.format("%s%s%s", "{", value, "}") : message)
                .addConstraintViolation();
    }
}
