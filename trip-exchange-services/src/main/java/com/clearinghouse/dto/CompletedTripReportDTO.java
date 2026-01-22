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
public class CompletedTripReportDTO {

    private String providerName;
    private long completedTicketCount;
    private long totalNoOfTickets;

    public CompletedTripReportDTO(String providerName, long completedTicketCount, int totalNoOfTickets) {
        this.providerName = providerName;
        this.completedTicketCount = completedTicketCount;
        this.totalNoOfTickets = totalNoOfTickets;
    }

    public CompletedTripReportDTO(String providerName, int completedTicketCount, long totalNoOfTickets) {
        this.providerName = providerName;
        this.completedTicketCount = completedTicketCount;
        this.totalNoOfTickets = totalNoOfTickets;
    }

    public CompletedTripReportDTO(String providerName, int completedTicketCount, int totalNoOfTickets) {
        this.providerName = providerName;
        this.completedTicketCount = completedTicketCount;
        this.totalNoOfTickets = totalNoOfTickets;
    }


    @Override
    public String toString() {
        return "CompletedTripReportDTO{" + "providerName=" + providerName + ", completedTicketCount=" + completedTicketCount + ", totalNoOfTickets=" + totalNoOfTickets + '}';
    }

}
