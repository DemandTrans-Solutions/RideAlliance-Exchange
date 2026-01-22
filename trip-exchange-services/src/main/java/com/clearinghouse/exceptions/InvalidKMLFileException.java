package com.clearinghouse.exceptions;

@SuppressWarnings("serial")
public class InvalidKMLFileException extends RuntimeException {

    private String message;

    public InvalidKMLFileException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
