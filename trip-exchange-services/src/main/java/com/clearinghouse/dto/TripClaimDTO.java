/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
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

public class TripClaimDTO {


    private int id;
    @JsonProperty("claimant_provider_id")
    private int claimantProviderId;
    @JsonProperty("claimant_provider_name")
    private String claimantProviderName;
    @JsonProperty("claimant_service_id")
    private int claimantServiceId;

    @JsonProperty("claimant_trip_ticket_id")
    private String claimantTripId;

    private StatusDTO status;
    @JsonProperty("trip_ticket_id")
    private int tripTicketId;
    @JsonProperty("proposed_pickup_time")
    private String proposedPickupTime;
    @JsonProperty("proposed_fare")
    private float proposedFare;
    private String notes;
    @JsonProperty("expiration_date")
    private String expirationDate;
    @JsonProperty("is_expired")
    private boolean isExpired;
    private int version;

    //newly added
    private boolean ackStatus;
    private String ackStatusString;
    @JsonProperty("requester_fare")
    private Float requesterProviderFare;
    private float calculatedProposedFare;
    private boolean isNewRecord;

    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;

    private boolean overridePriceMismatch;

    @Override
    public String toString() {
        return "TripClaimDTO{" + "id=" + id + ", claimantProviderId=" + claimantProviderId + ", claimantProviderName=" + claimantProviderName + ", claimantServiceId=" + claimantServiceId + ", status=" + status + ", tripTicketId=" + tripTicketId + ", proposedPickupTime=" + proposedPickupTime + ", proposedFare=" + proposedFare + ", notes=" + notes + ", expirationDate=" + expirationDate + ", isExpired=" + isExpired + ", version=" + version + '}';
    }

}
