package com.local.datalake.dto;

import static com.local.datalake.common.Constants.PIPE;
import static com.local.datalake.validator.Enums.FileFormat.RTF;
import static com.local.datalake.validator.Enums.FileFormat.YAML;
import static com.local.datalake.validator.Enums.FileFormat.YML;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.local.datalake.annotation.BiField;
import com.local.datalake.annotation.CheckFileFormat;
import com.local.datalake.annotation.CheckFlag;
import com.local.datalake.annotation.CheckPath;
import com.local.datalake.annotation.CheckSecureFormat;
import com.local.datalake.annotation.CheckVersion;
import com.local.datalake.annotation.NotBlank;
import com.local.datalake.common.ViewHelper;

/**
 * Input Metadata
 * 
 * @author manoranjan
 */
@BiField.List({ @BiField(parent = "completeMsgEncrypted", child = "secureDataFormat"),
        @BiField(parent = "specFileFormat", child = "yamlFilePath") })
public class Input extends BaseDO {
    /**
     * case-sensitive
     */
    private String       errorRootObject;
    /**
     * case-sensitive
     */
    private String       rootObject;
    @CheckFileFormat({ RTF, YAML, YML })
    @CheckPath
    private String       yamlFilePath;
    @CheckVersion
    private String       yamlVersion;
    @NotBlank(fieldName = "OutputViewPath")
    private String       outputViewPath;
    private boolean      completeMsgEncrypted;
    @CheckSecureFormat(optional = true)
    private String       secureDataFormat;
    private boolean      sensitive;
    @NotBlank(fieldName = "messageType")
    private String       messageType;
    @NotBlank(fieldName = "Table")
    private String       table;
    @NotBlank(fieldName = "View")
    private String       view;
    @NotBlank(fieldName = "DataBase")
    private String       database;
    @CheckFileFormat({ RTF, YAML, YML })
    private String       specFileFormat;
    private boolean      arrayMsg;
    private String       arrayMsgTag;
    @CheckFlag(optional = false)
    private boolean      placeholder;

    private List<String> rootObjects = new ArrayList<>();
    private boolean      hasMultipleRoots;

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getRootObject() {
        return rootObject;
    }

    public void setRootObject(String rootObject) {
        this.rootObject = rootObject;

        // breaks rootobject if it contains pipe separator
        if (ViewHelper.isNotNullOrBlank(rootObject) && rootObject.contains(PIPE)) {
            List<String> roots = Arrays.stream(rootObject.split(Pattern.quote(PIPE)))
                    .filter(ViewHelper::isNotNullOrBlank).collect(Collectors.toList());
            if (roots.size() > 1) {
                this.rootObjects = roots;
                this.hasMultipleRoots = true;
            }
        }
    }

    public boolean isCompleteMsgEncrypted() {
        return completeMsgEncrypted;
    }

    public void setCompleteMsgEncrypted(boolean completeMsgEncrypted) {
        this.completeMsgEncrypted = completeMsgEncrypted;
    }

    public String getSecureDataFormat() {
        return secureDataFormat;
    }

    public void setSecureDataFormat(String secureDataFormat) {
        this.secureDataFormat = secureDataFormat;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

    public String getOutputViewPath() {
        return outputViewPath;
    }

    public void setOutputViewPath(String outputViewPath) {
        this.outputViewPath = outputViewPath;
    }

    public String getYamlFilePath() {
        return yamlFilePath;
    }

    public void setYamlFilePath(String yamlFilePath) {
        this.yamlFilePath = yamlFilePath;
    }

    public String getYamlVersion() {
        return yamlVersion;
    }

    public void setYamlVersion(String yamlVersion) {
        this.yamlVersion = yamlVersion;
    }

    public String getSpecFileFormat() {
        return specFileFormat;
    }

    public void setSpecFileFormat(String specFileFormat) {
        this.specFileFormat = specFileFormat;
    }

    public boolean isArrayMsg() {
        return arrayMsg;
    }

    public void setArrayMsg(boolean arrayMsg) {
        this.arrayMsg = arrayMsg;
    }

    public String getArrayMsgTag() {
        return arrayMsgTag;
    }

    public void setArrayMsgTag(String arrayMsgTag) {
        this.arrayMsgTag = arrayMsgTag;
    }

    public boolean isPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(boolean placeholder) {
        this.placeholder = placeholder;
    }

    public String getErrorRootObject() {
        return errorRootObject;
    }

    public void setErrorRootObject(String errorRootObject) {
        this.errorRootObject = errorRootObject;
    }

    public List<String> getRootObjects() {
        return rootObjects;
    }

    public boolean hasMultipleRoots() {
        return hasMultipleRoots;
    }

    @Override
    public String toString() {
        return "Input [errorRootObject=" + errorRootObject + ", rootObject=" + rootObject + ", yamlFilePath="
                + yamlFilePath + ", yamlVersion=" + yamlVersion + ", outputViewPath=" + outputViewPath
                + ", completeMsgEncrypted=" + completeMsgEncrypted + ", secureDataFormat=" + secureDataFormat
                + ", sensitive=" + sensitive + ", messageType=" + messageType + ", table=" + table + ", view=" + view
                + ", database=" + database + ", specFileFormat=" + specFileFormat + ", arrayMsg=" + arrayMsg
                + ", arrayMsgTag=" + arrayMsgTag + ", placeholder=" + placeholder + ", toString()=" + super.toString()
                + "]";
    }
}
