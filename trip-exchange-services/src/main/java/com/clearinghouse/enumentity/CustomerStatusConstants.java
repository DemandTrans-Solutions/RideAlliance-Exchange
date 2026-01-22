/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.enumentity;

/**
 *
 * @author shankar I
 */

public enum CustomerStatusConstants {
    newCustomer(1) {
        @Override
        public Integer getCustomerStatus() {
            return 1;
        }
    },
    existingCustomer(2) {
        @Override
        public Integer getCustomerStatus() {
            return 2;
        }
    };
    private final int value;

    CustomerStatusConstants(int value) {
        this.value = value;
    }

    public abstract Integer getCustomerStatus();

}