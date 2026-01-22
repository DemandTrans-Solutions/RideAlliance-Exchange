/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.exceptionentity;

/**
 *
 * @author chaitanyaP
 */
public class EmailExistCheckingEntity {

    String email;

    public EmailExistCheckingEntity(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "EmailExistCheckingEntity{" + "email=" + email + '}';
    }

}
