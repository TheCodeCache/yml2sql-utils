package com.local.datalake.exception;

/**
 * View Exception - Parent class
 *
 * @author manoranjan
 */
public class ViewException extends Exception {

    private static final long serialVersionUID = -808928941369906226L;

    private String            message;
    private Throwable         th;

    public ViewException(String message) {
        super(message);
        this.message = message;
    }

    public ViewException(String message, Throwable th) {
        super(message, th);
        this.message = message;
        this.th = th;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ViewException [message=" + message + ", th=" + th + "]";
    }
}
