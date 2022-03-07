package com.local.datalake.exception;

/**
 * This exception would be thrown, when application receives invalid file format
 */
public class UnsupportedFileFormatException extends IllegalArgumentException {

    public UnsupportedFileFormatException(String message) {
        super(message);
    }

    public UnsupportedFileFormatException(String message, Throwable th) {
        super(message, th);
    }

    private static final long serialVersionUID = 9090545910968856033L;
}
