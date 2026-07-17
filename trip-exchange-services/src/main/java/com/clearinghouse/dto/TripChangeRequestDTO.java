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

import java.util.Map;

/**
 * View of a trip ticket change request returned to the client.
 */
@Getter
@Setter
@NoArgsConstructor
public class TripChangeRequestDTO {

    private int id;

    @JsonProperty("trip_ticket_id")
    private int tripTicketId;

    @JsonProperty("requested_by_provider_id")
    private int requestedByProviderId;

    @JsonProperty("requested_by_provider_name")
    private String requestedByProviderName;

    @JsonProperty("requested_by_user_id")
    private Integer requestedByUserId;

    @JsonProperty("target_provider_id")
    private int targetProviderId;

    @JsonProperty("target_provider_name")
    private String targetProviderName;

    /** "Pending" | "Approved" | "Denied" | "Applied" */
    private String status;

    @JsonProperty("status_id")
    private int statusId;

    private String message;

    @JsonProperty("proposed_changes")
    private Map<String, String> proposedChanges;

    @JsonProperty("response_message")
    private String responseMessage;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;
}
