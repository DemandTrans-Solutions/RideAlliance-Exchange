/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.exceptions;

/**
 *
 * @author manisha
 */
public class SpringAppRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SpringAppRuntimeException() {
        super();
    }

    public SpringAppRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpringAppRuntimeException(String message) {
        super(message);
    }

    public SpringAppRuntimeException(Throwable cause) {
        super(cause);
    }

}