package com.local.datalake.dto;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Column Details
 */
public class Column {

    private static AtomicInteger defaultOrder = new AtomicInteger(0);

    private String               name;
    private String               alias;
    // if true, it'll be included in view's projection list
    private boolean              required;
    // columns in view will appear in the same sequence as this order
    private int                  order;

    /**
     * Holds JPath value for columns with hive function/expression
     * 
     * this field will be null for simple static columns
     */
    private String               jsonPath;

    public Column(String name) {
        this(name, name, true);
    }

    public Column(String name, String alias) {
        this(name, alias, true);
    }

    public Column(String name, boolean required) {
        this(name, name, required);
    }

    public Column(String name, String alias, boolean required) {
        super();
        this.name = name;
        this.alias = alias;
        this.required = required;
        this.order = defaultOrder.incrementAndGet();
    }

    public String getName() {
        return name;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public boolean isRequired() {
        return required;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonPath, name.toLowerCase());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Column other = (Column) obj;
        return Objects.equals(jsonPath, other.jsonPath) && Objects.equals(name.toLowerCase(), other.name.toLowerCase());
    }
}
