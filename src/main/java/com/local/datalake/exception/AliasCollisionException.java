package com.local.datalake.exception;

/**
 * Throw this in case of alias collision
 */
public class AliasCollisionException extends ViewException {

    public AliasCollisionException(String message) {
        super(message);
    }

    public AliasCollisionException(String message, Throwable th) {
        super(message, th);
    }

    @Override
    public String toString() {
        return "AliasCollisionException [toString()=" + super.toString() + "]";
    }

    private static final long serialVersionUID = -2460318744427196664L;
}
