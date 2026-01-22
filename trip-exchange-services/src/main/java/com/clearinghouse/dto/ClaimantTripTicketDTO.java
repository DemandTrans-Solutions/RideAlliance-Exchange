package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClaimantTripTicketDTO {


    private int id;

    private int claimantProviderId;

    private String claimantTripId;

    @JsonProperty("trip_ticketId")
    private int tripTicketId;


    public void setClaimantProvider(int claimantProviderId) {
        this.claimantProviderId = claimantProviderId;
    }


    public void setTrip_ticket(int tripTicketId) {
        this.tripTicketId = tripTicketId;
    }

    @Override
    public String toString() {
        return "ClaimantTripTicketDTO [id=" + id + ", claimantProvider=" + claimantProviderId
                + ", claimantTripId=" + claimantTripId + ", tripTicketId=" + tripTicketId + "]";
    }

}
