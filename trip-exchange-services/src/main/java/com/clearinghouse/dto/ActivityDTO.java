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
 *
 * @author chaitanyaP
 */

@Getter
@Setter
@NoArgsConstructor
public class ActivityDTO {

    private int activityId;
    private int tripTicketId;
    private String action;
    private String actionTakenBy;
    private String actionDetails;

    @JsonProperty("created_at")
    private String createdAt;

    @Override
    public String toString() {
        return "ActivityDTO{" + "activityId=" + activityId + ", tripTicketId=" + tripTicketId + ", action=" + action + ", actionTakenBy=" + actionTakenBy + ", actionDetails=" + actionDetails + ", createdAt=" + createdAt + '}';
    }

}
