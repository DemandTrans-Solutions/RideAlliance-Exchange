/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.enumentity;

/**
 *
 * @author chaitanyaP
 */
public enum TripTicketStatusConstants {

    approved(1) {
        @Override
        public int tripTicketStatusUpdate() {
            return 1;
        }
    }, available(2) {
        @Override
        public int tripTicketStatusUpdate() {
            return 2;
        }
    }, awaitingResult(3) {
        @Override
        public int tripTicketStatusUpdate() {
            return 3;
        }
    }, cancelled(4) {
        @Override
        public int tripTicketStatusUpdate() {
            return 4;
        }
    },
    claimPending(5) {
        @Override
        public int tripTicketStatusUpdate() {
            return 5;
        }
    },
    claimed(6) {
        @Override
        public int tripTicketStatusUpdate() {
            return 6;
        }
    },
    completed(7) {
        @Override
        public int tripTicketStatusUpdate() {
            return 7;
        }
    },
    declined(8) {
        @Override
        public int tripTicketStatusUpdate() {
            return 8;
        }
    },
    expired(9) {
        @Override
        public int tripTicketStatusUpdate() {
            return 9;
        }
    },
    noClaims(10) {
        @Override
        public int tripTicketStatusUpdate() {
            return 10;
        }
    },
    noShow(11) {
        @Override
        public int tripTicketStatusUpdate() {
            return 11;
        }
    },
    rescinded(12) {
        @Override
        public int tripTicketStatusUpdate() {
            return 12;
        }
    },
    unavailable(13) {
        @Override
        public int tripTicketStatusUpdate() {
            return 13;
        }
    };

    private final int value;

    TripTicketStatusConstants(int value) {
        this.value = value;
    }

    public abstract int tripTicketStatusUpdate();

}
