package com.clearinghouse.testutil;

import com.clearinghouse.dto.TripResultDTO;
import com.clearinghouse.dto.TripTicketDTO;
import com.clearinghouse.entity.TripClaim;
import com.clearinghouse.entity.TripResult;
import com.clearinghouse.entity.TripTicket;
import java.time.ZonedDateTime;

public class TripResultBuilder {
    private TripResult tripResult;
    private TripResultDTO tripResultDTO;
    
    public TripResultBuilder() {
        tripResult = new TripResult();
        tripResultDTO = new TripResultDTO();
        
        // Set default values
        tripResult.setId(1);
        tripResult.setCreatedAt(ZonedDateTime.now());
        tripResult.setUpdatedAt(ZonedDateTime.now());
        
        // Mirror to DTO
        tripResultDTO.setId(tripResult.getId());

        tripResultDTO.setTripTicketId(1);

        tripResult.setTripTicket(new TripTicketBuilder().withId(1).build());

    }
    
    public TripResultBuilder withId(int id) {
        tripResult.setId(id);
        tripResultDTO.setId(id);
        return this;
    }
    
    public TripResultBuilder withTripTicket(TripTicket tripTicket) {
        tripResult.setTripTicket(tripTicket);
        TripTicketDTO tripTicketDTO = new TripTicketDTO();
        tripTicketDTO.setId(tripTicket.getId());
        tripResultDTO.setTripTicketId(tripTicket.getId());
        
        return this;
    }
    
    public TripResultBuilder withTripClaim(TripClaim claim) {
        tripResult.setTripClaim(claim);
        tripResultDTO.setTripClaimId(claim.getId());
        return this;
    }
    
    public TripResultBuilder withCancellation(boolean isCancelled, String reason) {
        tripResult.setOutcome("CANCELLED");
        

        return this;
    }
    
    public TripResultBuilder asCancelled(String reason) {
        return withCancellation(true, reason);
    }
    
    public TripResult build() {
        return tripResult;
    }
    
    public TripResultDTO buildDTO() {
        return tripResultDTO;
    }
}