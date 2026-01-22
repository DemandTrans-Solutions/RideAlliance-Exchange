/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 *
 * @author chaitanyaP
 */

@Getter
@Setter
@NoArgsConstructor
public class ReportFilterDTO {

    private int providerId;
    private List<String> reportTicketFilterStatus;

    private String fromDate;
    private String toDate;

    // Robust: also store parsed ZonedDateTime for type-safe queries
    private java.time.ZonedDateTime fromDateTime;
    private java.time.ZonedDateTime toDateTime;

    @JsonAlias({"partnerProviderTicket"})
    private boolean isPartnerProviderTicket;
    @JsonAlias({"myTicket"})
    private boolean isMyTicket;
    private String inClauseQuery;


    private List<Integer> providerIds;


    @Override
    public String toString() {
        return "ReportFilterDTO{" + "providerId=" + providerId + ", reportTicketFilterStatus=" + reportTicketFilterStatus + ", fromDate=" + fromDate + ", toDate=" + toDate + '}';
    }

}
