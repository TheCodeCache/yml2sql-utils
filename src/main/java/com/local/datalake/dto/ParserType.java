package com.local.datalake.dto;

/**
 * parser selection requires these three fields
 * <li>yaml version
 * <li>file format
 * <li>root object
 */
public class ParserType {

    private String  yamlVersion;
    private String  specFileFormat;
    private String  rootObject;
    private boolean isError;

    public ParserType(String yamlVersion, String specFileFormat, String rootObject) {
        super();
        this.yamlVersion = yamlVersion;
        this.specFileFormat = specFileFormat;
        this.rootObject = rootObject;
    }

    public String getYamlVersion() {
        return yamlVersion;
    }

    public String getSpecFileFormat() {
        return specFileFormat;
    }

    public String getRootObject() {
        return rootObject;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean isError) {
        this.isError = isError;
    }
}
