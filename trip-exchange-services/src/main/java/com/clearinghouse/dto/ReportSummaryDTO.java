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
 * @author chaitanyaP
 */

@Getter
@Setter
@NoArgsConstructor
public class ReportSummaryDTO {

    private int totalTicketCount;
    private int rescindedTicketCount;
    private int availabeTicketCount;
    private int approvedTicketCount;
    private int expiredTicketCount;
    private int completedTicketCount;

    private int totalCliamsReceived;
    private int rescindedCaimReceived;
    private int approvedClaimReceived;
    private int pendingClaimReceived;
    private int declinedClaimReceived;

    private int totalCliamsSubmitted;
    private int rescindedCaimSubmitted;
    private int approvedClaimSubmitted;
    private int pendingClaimSubmitted;
    private int declinedClaimSubmitted;


    @Override
    public String toString() {
        return "ReportSummaryDTO{" + "totalTicketCount=" + totalTicketCount + ", rescindedTicketCount=" + rescindedTicketCount + ", availabeTicketCount=" + availabeTicketCount + ", approvedTicketCount=" + approvedTicketCount + ", expiredTicketCount=" + expiredTicketCount + ", completedTicketCount=" + completedTicketCount + ", totalCliamsReceived=" + totalCliamsReceived + ", rescindedCaimReceived=" + rescindedCaimReceived + ", approvedClaimReceived=" + approvedClaimReceived + ", pendingClaimReceived=" + pendingClaimReceived + ", declinedClaimReceived=" + declinedClaimReceived + ", totalCliamsSubmitted=" + totalCliamsSubmitted + ", rescindedCaimSubmitted=" + rescindedCaimSubmitted + ", approvedClaimSubmitted=" + approvedClaimSubmitted + ", pendingClaimSubmitted=" + pendingClaimSubmitted + ", declinedClaimSubmitted=" + declinedClaimSubmitted + '}';
    }

}
