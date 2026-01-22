/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.exceptionentity;

/**
 *
 * @author chaitanyaP
 */
public class ProviderEmailExistsExceptionEntity {

    String contactEmail;

    public ProviderEmailExistsExceptionEntity(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    @Override
    public String toString() {
        return "ProviderEmailExistsExceptionEntity{" + "contactEmail=" + contactEmail + '}';
    }

}
