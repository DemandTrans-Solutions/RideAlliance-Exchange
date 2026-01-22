package com.clearinghouse.exceptions;

@SuppressWarnings("serial")
public class HandlingExceptionForOKStatus extends RuntimeException {

    private String message;

    public HandlingExceptionForOKStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
