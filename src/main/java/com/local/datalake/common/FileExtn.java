package com.local.datalake.common;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * File Extns
 */
public enum FileExtn {

    YML(".yml"), YAML(".yaml"), RTF(".rtf"), JSON(".json"), HQL(".hql");

    private static final Map<String, FileExtn> cache = new HashMap<String, FileExtn>();

    static {
        // populating the map
        for (FileExtn extn : values()) {
            cache.put(extn.getValue(), extn);
        }
    }

    private String value;

    private FileExtn(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    /**
     * Return one of the choice of the Enum by its value if found else null
     * 
     * @param value extn value
     * @return FileExtn
     */
    public static FileExtn getByValue(String value) {
        return cache.get(value);
    }

    /**
     * Mimic-ing sql IN clause
     * 
     * @param extns
     * @return true if the current extn "this" found in the supplied list of extns
     *         else false
     */
    public boolean in(FileExtn... extns) {
        return Stream.of(extns).anyMatch(extn -> this == extn);
    }
}
