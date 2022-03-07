package com.local.datalake.exception;

/**
 * Throw when Root-Object is not found in swagger yaml file
 */
public class InvalidRootObjectException extends ViewException {

    public InvalidRootObjectException(String message) {
        super(message);
    }

    public InvalidRootObjectException(String message, Throwable th) {
        super(message, th);
    }

    @Override
    public String toString() {
        return "InvalidRootObjectException [toString()=" + super.toString() + "]";
    }

    private static final long serialVersionUID = -6443008264922586429L;
}
