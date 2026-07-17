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

    /* Trip cost for the "All Trips by Provider" report. Resolved per-row in ReportService:
       cancelled -> claimant (else originator) CancelledTripCost; completed -> tripresult fare
       when the provider's UseCostFromProvider flag is set, otherwise the rate-card formula. */
    private float tripCost;

    /* Trip mileage for the "All Trips by Provider" report. Resolved per-row in ReportService
       from the tripticketdistance row (the real Azure-computed route distance, in miles).
       NOTE: this is distinct from the inherited estimated_trip_distance, which is not populated
       from the route computation and is normally 0. */
    private float mileage;



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
