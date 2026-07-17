/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Payload to create a change request on a claimed trip ticket.
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateChangeRequestRequest {

    /** Free-text message describing the requested change. */
    private String message;

    /** Optional structured proposed changes: field name -> proposed new value. */
    @JsonProperty("proposed_changes")
    private Map<String, String> proposedChanges;
}
