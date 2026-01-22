/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.exceptionentity;

/**
 *
 * @author chaitanyaP
 */
public class ServiceAreaActiveCheckEntity {

    String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ServiceAreaActiveCheckEntity{" + "message=" + message + '}';
    }

}
