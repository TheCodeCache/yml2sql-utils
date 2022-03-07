package com.local.datalake.dto;

import com.local.datalake.annotation.NotBlank;

/**
 * parent class to dto
 * 
 * @author manoranjan
 */
public class BaseDO {

    // feed identifier;
    @NotBlank(fieldName = "Feed Identifier")
    private String identifier;

    // feed name
    @NotBlank(fieldName = "Feed Name")
    private String feedName;

    private String component;

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    @Override
    public String toString() {
        return "BaseDO [identifier=" + identifier + ", feedName=" + feedName + ", component=" + component + "]";
    }
}
