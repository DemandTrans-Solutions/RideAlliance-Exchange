/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.enumentity;

/**
 *
 * @author chaitanyaP
 */
public enum ProviderPartnerStatusConstants {
    pending(1) {
        @Override
        public int providerPartnerStatusChek() {
            return 1;
        }
    }, approved(2) {
        @Override
        public int providerPartnerStatusChek() {
            return 2;
        }
    }, cancelled(3) {
        @Override
        public int providerPartnerStatusChek() {
            return 3;
        }
    }, denied(4) {
        @Override
        public int providerPartnerStatusChek() {
            return 4;
        }
    },
    breakPartnership(5) {
        @Override
        public int providerPartnerStatusChek() {
            return 5;
        }
    };
    private final int value;

    ProviderPartnerStatusConstants(int value) {
        this.value = value;
    }

    public abstract int providerPartnerStatusChek();

}
