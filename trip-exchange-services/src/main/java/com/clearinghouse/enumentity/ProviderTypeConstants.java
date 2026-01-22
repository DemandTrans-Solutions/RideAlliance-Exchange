/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.enumentity;

/**
 *
 * @author shankar I
 */
public enum ProviderTypeConstants {
    standardProvider(1) {
        @Override
        public Integer getProviderTypeId() {
            return 1;
        }
    },
    restrictedProvider(2) {
        @Override
        public Integer getProviderTypeId() {
            return 2;
        }
    };
    private final int value;

    ProviderTypeConstants(int value) {
        this.value = value;
    }

    public abstract Integer getProviderTypeId();
}
