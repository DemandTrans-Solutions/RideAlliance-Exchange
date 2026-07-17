/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.enumentity;

/**
 * Status values for a trip ticket change request.
 *
 * These IDs are rows in the shared `status` lookup table, in a dedicated high block (30-33) so they
 * do not collide with trip-ticket / claim statuses (which occupy 1-20). Seeded by
 * dbscript/ClearingHouse_changerequest_status_2026-06-15.sql.
 *
 * pending  - request created, awaiting the other provider's response
 * approved - the other provider approved; the proposed changes are applied to the trip automatically
 * denied   - the other provider denied the request
 * cancelled - the requester withdrew the request before the other provider responded
 */
public enum TripChangeRequestStatusConstants {

    pending(30) {
        @Override
        public int tripChangeRequestStatusValue() {
            return 30;
        }
    },
    approved(31) {
        @Override
        public int tripChangeRequestStatusValue() {
            return 31;
        }
    },
    denied(32) {
        @Override
        public int tripChangeRequestStatusValue() {
            return 32;
        }
    },
    cancelled(33) {
        @Override
        public int tripChangeRequestStatusValue() {
            return 33;
        }
    };

    private final int value;

    TripChangeRequestStatusConstants(int value) {
        this.value = value;
    }

    public abstract int tripChangeRequestStatusValue();
}
