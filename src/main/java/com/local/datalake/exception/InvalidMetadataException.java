package com.local.datalake.exception;

/**
 * csv metadata file has some invalid details
 */
public class InvalidMetadataException extends ViewException {

    private static final long serialVersionUID = 3607847442258088792L;

    public InvalidMetadataException(String message) {
        super(message);
    }

    public InvalidMetadataException(String message, Throwable th) {
        super(message, th);
    }

    @Override
    public String toString() {
        return "InvalidMetadataException [toString()=" + super.toString() + "]";
    }
}
