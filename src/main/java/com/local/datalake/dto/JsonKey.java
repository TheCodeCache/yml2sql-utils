package com.local.datalake.dto;

import com.fasterxml.jackson.databind.node.JsonNodeType;

/**
 * Holds an individual json key and their Node Type
 */
public class JsonKey {

    private String       key;
    private JsonNodeType type;

    public JsonKey(String key, JsonNodeType type) {
        super();
        this.key = key;
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public JsonNodeType getType() {
        return type;
    }
}
