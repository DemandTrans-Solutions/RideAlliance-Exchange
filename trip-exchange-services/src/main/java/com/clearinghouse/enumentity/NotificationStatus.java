/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.enumentity;

/**
 *
 * @author chaitanyaP
 */
public enum NotificationStatus {
    newStatus(1) {
        @Override
        public int status() {
            return 1;
        }
    }, inProgressStatus(2) {
        @Override
        public int status() {
            return 2;
        }
    }, successStatus(3) {
        @Override
        public int status() {
            return 3;
        }
    }, errorStatus(4) {
        @Override
        public int status() {
            return 4;
        }
    };
    private final int value;

    NotificationStatus(int value) {
        this.value = value;
    }

    public abstract int status();

}
//Add sepaartely...for code..
