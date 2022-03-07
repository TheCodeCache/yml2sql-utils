package com.local.datalake.privacy;

import com.local.datalake.annotation.TestJsonPath;
import com.local.datalake.dto.BaseDO;

/**
 * privacy-data bean
 */
public class PrivacyInfo extends BaseDO {

    // error root object
    private String errorRootObject;
    // root object
    private String rootObject;
    // json-path of the nested field
    @TestJsonPath
    private String fieldJsonPath;
    // indicates if yml(swagger) file is shared
    private String ymlAttached;
    // privacy classification of a field
    private String classification;

    public String getRootObject() {
        return rootObject;
    }

    public void setRootObject(String rootObject) {
        this.rootObject = rootObject;
    }

    public String getFieldJsonPath() {
        return fieldJsonPath;
    }

    public void setFieldJsonPath(String fieldJsonPath) {
        this.fieldJsonPath = fieldJsonPath;
    }

    public String getYmlAttached() {
        return ymlAttached;
    }

    public void setYmlAttached(String ymlAttached) {
        this.ymlAttached = ymlAttached;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getErrorRootObject() {
        return errorRootObject;
    }

    public void setErrorRootObject(String errorRootObject) {
        this.errorRootObject = errorRootObject;
    }

    @Override
    public String toString() {
        return "PrivacyInfo [errorRootObject=" + errorRootObject + ", rootObject=" + rootObject + ", fieldJsonPath="
                + fieldJsonPath + ", ymlAttached=" + ymlAttached + ", classification=" + classification
                + ", toString()=" + super.toString() + "]";
    }
}
