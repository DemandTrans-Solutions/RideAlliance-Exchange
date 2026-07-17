/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload for the target provider to approve or deny a change request.
 */
@Getter
@Setter
@NoArgsConstructor
public class ChangeRequestResponseRequest {

    /** Optional message from the approver/denier back to the requester. */
    @JsonProperty("response_message")
    private String responseMessage;
}
