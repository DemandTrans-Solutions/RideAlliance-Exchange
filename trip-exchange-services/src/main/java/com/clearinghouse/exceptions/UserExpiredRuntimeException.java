/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.exceptions;

/**
 *
 * @author chaitanyaP
 */
public class UserExpiredRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    String message;

    public UserExpiredRuntimeException() {
        super();
    }

    public UserExpiredRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    //    public UserExpiredRuntimeException(String message) {
//        super(message);
//    }
    public UserExpiredRuntimeException(String message) {
        this.message = message;

    }

    public UserExpiredRuntimeException(Throwable cause) {
        super(cause);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
