/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.exceptionentity;

/**
 *
 * @author chaitanyaP
 */
public class UsernameExistCheckingEntity {

    String username;


    public UsernameExistCheckingEntity(String username) {
        this.username = username;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}
