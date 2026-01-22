package com.clearinghouse.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author shankarI
 */

@Getter
@Setter
@NoArgsConstructor
public class CheckWorkingHoursDTO {

    private int tripTicketId;
    private int claimantProviderId;
    private boolean isEligibleForCreateClaim;
    private String msg;
    private int workingHoursId;


    public void setIsEligibleForCreateClaim(boolean isEligibleForCreateClaim) {
        this.isEligibleForCreateClaim = isEligibleForCreateClaim;
    }


    @Override
    public String toString() {
        return "CheckWorkingHoursDTO [tripTicketId=" + tripTicketId + ", claimantProviderId=" + claimantProviderId
                + ", isEligibleForCreateClaim=" + isEligibleForCreateClaim + ", msg=" + msg + ", workingHoursId="
                + workingHoursId + "]";
    }

}
