package com.clearinghouse.enumentity;

/**
 *
 * @author shankar I
 */

public enum GeographicalFilterStatusConstants {
    CheckForEntireServiceArea(4) {
        @Override
        public Integer getGeographicalFilterStatus() {
            return 4;
        }
    },
    CheckForOnlyPickUpIsInSA(2) {
        @Override
        public Integer getGeographicalFilterStatus() {
            return 2;
        }
    },
    CheckForOnlyDropOffIsInSA(3) {
        public Integer getGeographicalFilterStatus() {
            return 3;
        }
    }, CheckForoutsideserviceArea(1) {
        public Integer getGeographicalFilterStatus() {
            return 1;
        }
    };
    private final int value;

    GeographicalFilterStatusConstants(int value) {
        this.value = value;
    }

    public abstract Integer getGeographicalFilterStatus();

}
