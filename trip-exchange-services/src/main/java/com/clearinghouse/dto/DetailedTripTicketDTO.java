/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 *
 * @author chaitanyaP
 */

@Getter
@Setter
@NoArgsConstructor
public class DetailedTripTicketDTO extends TripTicketDTO {

    private ProviderDTO originator;

    private ProviderDTO claimant;

    @JsonProperty("trip_Claims")
    private Set<TripClaimDTO> tripClaims;

    private Set<ClaimantTripTicketDTO> claimantTripTickets;

    @JsonProperty("trip_result")
    private TripResultDTO tripResult;

    @JsonProperty("trip_ticket_comments")
    private Set<TripTicketCommentDTO> tripTicketComments;

    private boolean isNewRecord;



    public void extractComments() {
        if ( tripTicketComments == null ) return;
        StringBuffer comments = new StringBuffer();
        for ( var comment : tripTicketComments ) {
            if ( comments.length() > 0 ) {
                comments.append("," );
            }
            comments.append(comment.getBody());
        }
        var existingNotes = getTripNotes();
        if ( existingNotes == null ) {
            setTripNotes(comments.toString());
        } else {
            setTripNotes(existingNotes + "," + comments.toString());
        }
    }


}
