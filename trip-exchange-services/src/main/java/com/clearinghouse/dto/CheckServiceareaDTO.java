/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author chaitanyap
 */

@Getter
@Setter
@NoArgsConstructor
public class CheckServiceareaDTO {

    int tripTicketId;
    int claimantProviderId;
    boolean isEligibleForService;
    boolean isAlredyClaimed;
    int serviceId;


    public boolean isEligibleForService() {
        return isEligibleForService;
    }

    public boolean isAlredyClaimed() {
        return isAlredyClaimed;
    }

    public void setIsAlredyClaimed(boolean b) {
        isAlredyClaimed = b;
    }

    public void setIsEligibleForService(boolean b) {
        isEligibleForService = b;
    }


    @Override
    public String toString() {
        return "CheckServiceareaDTO{" + "tripTicketId=" + tripTicketId + ", claimantProviderId=" + claimantProviderId + ", isEligibleForService=" + isEligibleForService + ", isAlredyClaimed=" + isAlredyClaimed + ", serviceId=" + serviceId + '}';
    }

}
