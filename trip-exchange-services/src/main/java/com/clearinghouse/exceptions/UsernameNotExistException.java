/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.exceptions;

/**
 *
 * @author chaitanyaP
 */
public class UsernameNotExistException extends RuntimeException {

    private String username;
    private String message;

    public UsernameNotExistException(String username, String message) {
        this.username = username;
        this.message = message;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
