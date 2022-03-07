package com.local.datalake.alias;

/**
 * Rules used while computing alias
 */
public enum Rule {

    CAMEL_CASE(1), ALL_LOWER_CASE(2), ALL_UPPER_CASE(3);

    private int order;

    private Rule(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
