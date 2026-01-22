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
public class TripTicketCommentDTO {

    private int id;
    @JsonProperty("trip_ticket_id")
    private int tripTicketId;
    @JsonProperty("user_id")
    private int userId;

    private String body;
    @JsonProperty("user_name")
    private String userName;
    @JsonProperty("name_of_provider")
    private String nameOfProvider;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;


    @Override
    public String toString() {
        return "TripTicketCommentDTO{" + "id=" + id + ", tripTicketId=" + tripTicketId + ", userId=" + userId + ", body=" + body + ", userName=" + userName + '}';
    }

}
