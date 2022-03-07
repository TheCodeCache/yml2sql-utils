package com.local.datalake.exception;

/**
 * when alias fails to create, throw this
 */
public class AliasRuleNotFoundException extends RuntimeException {

    public AliasRuleNotFoundException(String message) {
        super(message);
    }

    public AliasRuleNotFoundException(String message, Throwable th) {
        super(message, th);
    }

    @Override
    public String toString() {
        return "AliasRuleNotFoundException [toString()=" + super.toString() + "]";
    }

    private static final long serialVersionUID = -3861416095656573388L;
}
