/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.exceptions;

/**
 *
 * @author chaitanyaP
 */
public class ProviderPartnershipAlreadyExistsException extends RuntimeException {
    String providerName;
    private String message;

    public ProviderPartnershipAlreadyExistsException(String providerName, String message) {
        this.providerName = providerName;
        this.message = message;
    }


    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
