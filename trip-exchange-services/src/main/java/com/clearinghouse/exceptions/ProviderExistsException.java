/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.exceptions;

/**
 *
 * @author chaitanyaP
 */
public class ProviderExistsException extends RuntimeException {
    private String email;
    private String message;

    public ProviderExistsException(String email, String message) {
        this.email = email;
        this.message = message;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
