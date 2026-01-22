/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.enumentity;

/**
 *
 * @author chaitanyaP
 */
public enum NotificationTemplateCodeValue {

    //please add entry here as per requirement..
    createUserTemplateCode(10) {
        @Override
        public int templateCodeValue() {
            return 10;
        }
    },
    forgotPasswordTemplateCode(11) {
        @Override
        public int templateCodeValue() {
            return 11;
        }
    },
    addProviderPartnerTemplateCode(12) {
        @Override
        public int templateCodeValue() {
            return 12;
        }
    },
    claimDeclinedTemplateCode(13) {
        @Override
        public int templateCodeValue() {
            return 13;
        }
    },
    claimApprovedTemplateCode(14) {
        @Override
        public int templateCodeValue() {
            return 14;
        }
    },
    claimRescindedTemplateCode(15) {
        @Override
        public int templateCodeValue() {
            return 15;
        }
    },
    tripTicketRescindedTemplateCode(16) {
        @Override
        public int templateCodeValue() {
            return 16;
        }
    },
    partnerCreatesTicket(17) {
        @Override
        public int templateCodeValue() {
            return 17;
        }
    },
    tripCommentAdded(18) {
        @Override
        public int templateCodeValue() {
            return 18;
        }
    },
    tripResultSubmitted(19) {
        @Override
        public int templateCodeValue() {
            return 19;
        }
    },
    approvedTripClaimRescinded(20) {
        @Override
        public int templateCodeValue() {
            return 20;
        }
    },
    tripTicketReceived(21) {
        @Override
        public int templateCodeValue() {
            return 21;
        }
    },
    tripClaimAutoApproved(22) {
        @Override
        public int templateCodeValue() {
            return 22;
        }
    },
    userActivationByAdmin(23) {
        @Override
        public int templateCodeValue() {
            return 23;
        }
    },
    userDeactivationByAdmin(24) {
        @Override
        public int templateCodeValue() {
            return 24;
        }
    },
    ticketExpired(25) {
        @Override
        public int templateCodeValue() {
            return 25;
        }
    },
    claimUpdated(26) {
        @Override
        public int templateCodeValue() {
            return 26;
        }
    },
    invalidInput(27) {
        @Override
        public int templateCodeValue() {
            return 27;
        }
    },
    weeklyReportCreated(28) {
        @Override
        public int templateCodeValue() {
            return 28;
        }
    },
    claimCancelTemplateCode(29) {
        @Override
        public int templateCodeValue() {
            return 29;
        }
    }, claimCostMismatchTemplateCode(30) {
        @Override
        public int templateCodeValue() {
            return 30;
        }
    }, tripTicketCancelledTemplateCode(31) {
        @Override
        public int templateCodeValue() {
            return 31;
        }
    }, claimCancelledDueToTripCancelledTemplateCode(32) {
        @Override
        public int templateCodeValue() {
            return 32;
        }
    },
    tripResultCompleted(33) {
        @Override
        public int templateCodeValue() {
            return 33;
        }
    },
    ;
    private final int value;

    NotificationTemplateCodeValue(int value) {
        this.value = value;
    }

    public abstract int templateCodeValue();

}
