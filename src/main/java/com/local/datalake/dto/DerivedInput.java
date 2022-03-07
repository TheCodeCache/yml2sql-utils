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
 * DerivedInput Metadata
 * 
 * @author manoranjan
 */
@BiField.List({ @BiField(parent = "completeMsgEncrypted", child = "secureDataFormat"),
        @BiField(parent = "specFileFormat", child = "yamlFilePath") })
public class DerivedInput extends BaseDO {
    /**
     * case-sensitive
     */
	private String feedIdentifier;
	private String feedName;
    private String rootObject;
    @CheckFileFormat({ RTF, YAML, YML })
    @CheckPath
    private String yamlFilePath;
    private String privacyPath;
    private String outputDerivedPath;
    @CheckVersion
    private String yamlVersion;

    @NotBlank(fieldName = "messageType")
    private String messageType;
    @CheckFileFormat({ RTF, YAML, YML })
    private String specFileFormat;
    private boolean arrayMsg;
    private String  arrayMsgTag;
    @CheckFlag(optional = false)
    private boolean placeholder;

    private List<String> rootObjects = new ArrayList<>();
    private boolean hasMultipleRoots;
    
    public String getFeedIdentifier() {
    	return feedIdentifier;
    }
    
    public void setFeedIdentifier(String feedIdentifier) {
    	this.feedIdentifier = feedIdentifier;
    }
    
    public String getFeedName() {
    	return feedName;
    }
    
    public void setFeedName(String feedName) {
    	this.feedName = feedName;
    }   

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    
    
    public String getPrivacyPath() {
    	return privacyPath;
    }
    
    public void setPrivacyPath(String privacyPath) {
    	this.privacyPath = privacyPath;
    }
    
    public String getOutputDerivedPath() {
    	return outputDerivedPath;
    }
    
    public void setOutputDerivedPath(String outputDerivedPath) {
    	this.outputDerivedPath = outputDerivedPath;
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

    public List<String> getRootObjects() {
        return rootObjects;
    }

    public boolean hasMultipleRoots() {
        return hasMultipleRoots;
    }

    @Override
    public String toString() {
        return "Input [feedIdentifier=" + feedIdentifier + ", feedName=" + feedName + ", rootObject=" + rootObject + ", yamlFilePath="
                + yamlFilePath + ", privacyPath=" + privacyPath + ",outputDerivedPath=" + outputDerivedPath + ", yamlVersion=" + yamlVersion + ", messageType=" + messageType 
                + ", specFileFormat=" + specFileFormat + ", arrayMsg=" + arrayMsg
                + ", arrayMsgTag=" + arrayMsgTag + ", placeholder=" + placeholder + ", toString()=" + super.toString()
                + "]";
    }
}
