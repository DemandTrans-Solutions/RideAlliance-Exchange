package com.clearinghouse.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Time;

@Getter
@Setter
@NoArgsConstructor
public class ProvidersWeeklyReportDTO {

    private Integer tripticketId;
    private Integer providerId;
    private String date;
    private String pickupDate;
    private Time pickupTime;
    private String pickupAddress;
    private String dropOffDate;
    private Time dropOffTime;
    private String dropOffAddress;
    private BigDecimal distance;
    private String time;
    private BigDecimal finalProposedCost;
    private String fundingSource;
    private String status;
    private String providerAs;

    @Override
    public String toString() {
        return "ProvidersWeeklyReportDTO [tripticketId=" + tripticketId + ", providerId=" + providerId + ", date="
                + date + ", pickupDate=" + pickupDate + ", pickupTime=" + pickupTime + ", pickupAddress="
                + pickupAddress + ", dropOffDate=" + dropOffDate + ", dropOffTime=" + dropOffTime + ", dropOffAddress="
                + dropOffAddress + ", distance=" + distance + ", time=" + time + ", finalProposedCost="
                + finalProposedCost + ", fundingSource=" + fundingSource + ", status=" + status + ", providerAs="
                + providerAs + "]";
    }

}
