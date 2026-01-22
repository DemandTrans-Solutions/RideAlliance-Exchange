/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.exceptions;

/**
 *
 * @author chaitanyaP
 */
public class ServiceAreaActiveCheckException extends RuntimeException {

    String message;

    public ServiceAreaActiveCheckException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
