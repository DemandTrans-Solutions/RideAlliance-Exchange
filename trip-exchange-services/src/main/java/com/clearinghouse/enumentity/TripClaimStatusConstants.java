/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.enumentity;

/**
 *
 * @author chaitanyaP
 */
public enum TripClaimStatusConstants {
    approved(1) {
        @Override
        public int tripClaimStatusUpdate() {
            return 1;
        }
    }, pending(14) {
        @Override
        public int tripClaimStatusUpdate() {
            return 14;
        }
    },
    cancelled(4) {
        @Override
        public int tripClaimStatusUpdate() {
            return 4;
        }
    },
    declined(8) {
        @Override
        public int tripClaimStatusUpdate() {
            return 8;
        }

    },
    rescined(12) {
        @Override
        public int tripClaimStatusUpdate() {
            return 12;
        }
    },
    pendingYourApproval(15) {
        @Override
        public int tripClaimStatusUpdate() {
            return 15;
        }
    },
    priceMismatch(16) {
        @Override
        public int tripClaimStatusUpdate() {
            return 16;
        }
    },
    ;
    private final int value;

    TripClaimStatusConstants(int value) {
        this.value = value;
    }

    public abstract int tripClaimStatusUpdate();
}
