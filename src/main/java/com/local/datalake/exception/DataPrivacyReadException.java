package com.local.datalake.exception;

/**
 * Throw this in case of data_privacy excel read issue
 */
public class DataPrivacyReadException extends ViewException {

    public DataPrivacyReadException(String message) {
        super(message);
    }

    public DataPrivacyReadException(String message, Throwable th) {
        super(message, th);
    }

    @Override
    public String toString() {
        return "DataPrivacyReadException [toString()=" + super.toString() + "]";
    }

    private static final long serialVersionUID = 7968683891746057993L;
}
