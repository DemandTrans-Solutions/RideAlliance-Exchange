/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.enumentity;

/**
 *
 * @author chaitanyaP
 */
public enum AuthanticationCheckStatusForLogin {
    usertypeAdpater("adapter") {
        @Override
        public String loginUsertypeValue() {
            return "adapter";
        }
    },
    usertypeRegular("regular") {
        @Override
        public String loginUsertypeValue() {
            return "regular";
        }
    },
    usertypeUnknown("unknown") {
        @Override
        public String loginUsertypeValue() {
            return "unknown";
        }
    },
    ;

    private final String value;

    AuthanticationCheckStatusForLogin(String value) {
        this.value = value;
    }

    public abstract String loginUsertypeValue();

}
