/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.exceptionentity;

import java.util.List;

/**
 *
 * @author chaitanyaP
 */
public class InvalidInputCheckingEntity {

    List<String> message;

    public InvalidInputCheckingEntity(List<String> message) {
        this.message = message;
    }

    public List<String> getMessage() {
        return message;
    }

    public void setMessage(List<String> message) {
        this.message = message;
    }

}
