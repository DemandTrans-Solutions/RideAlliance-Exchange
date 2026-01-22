package com.clearinghouse.testutil;

import com.clearinghouse.dto.StatusDTO;
import com.clearinghouse.dto.TripClaimDTO;
import com.clearinghouse.dto.TripTicketDTO;
import com.clearinghouse.entity.Status;
import com.clearinghouse.entity.TripClaim;
import com.clearinghouse.entity.TripTicket;
import java.time.ZonedDateTime;

public class TripClaimBuilder {
    private TripClaim tripClaim;
    private TripClaimDTO tripClaimDTO;
    
    public TripClaimBuilder() {
        tripClaim = new TripClaim();
        tripClaimDTO = new TripClaimDTO();
        
        // Set default values
        tripClaim.setId(1);
        tripClaim.setCreatedAt(ZonedDateTime.now());
        tripClaim.setUpdatedAt(ZonedDateTime.now());
        
        // Status defaults
        Status status = new Status();
        status.setStatusId(1);
        tripClaim.setStatus(status);
        
        StatusDTO statusDTO = new StatusDTO();
        statusDTO.setStatusId(1);
        tripClaimDTO.setStatus(statusDTO);
        
        tripClaimDTO.setId(tripClaim.getId());
    }
    
    public TripClaimBuilder withId(int id) {
        tripClaim.setId(id);
        tripClaimDTO.setId(id);
        return this;
    }
    
    public TripClaimBuilder withTripTicket(TripTicket tripTicket) {
        tripClaim.setTripTicket(tripTicket);
        TripTicketDTO tripTicketDTO = new TripTicketDTO();
        tripTicketDTO.setId(tripTicket.getId());
        tripClaimDTO.setTripTicketId(1);
        return this;
    }
    
    public TripClaimBuilder withStatus(Status status) {
        tripClaim.setStatus(status);
        StatusDTO statusDTO = new StatusDTO();
        statusDTO.setStatusId(status.getStatusId());
        statusDTO.setType(status.getType());
        statusDTO.setDescription(status.getDescription());
        tripClaimDTO.setStatus(statusDTO);
        return this;
    }
    
    public TripClaim build() {
        return tripClaim;
    }
    
    public TripClaimDTO buildDTO() {
        return tripClaimDTO;
    }
}