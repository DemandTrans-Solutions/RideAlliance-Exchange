package com.clearinghouse.testutil;

import com.clearinghouse.dto.ProviderDTO;
import com.clearinghouse.dto.StatusDTO;
import com.clearinghouse.dto.TripTicketDTO;
import com.clearinghouse.entity.Provider;
import com.clearinghouse.entity.Status;
import com.clearinghouse.entity.TripTicket;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class TripTicketBuilder {
    private TripTicket tripTicket;
    private TripTicketDTO tripTicketDTO;
    
    public TripTicketBuilder() {
        tripTicket = new TripTicket();
        tripTicketDTO = new TripTicketDTO();
        
        // Set default values
        tripTicket.setId(1);
        tripTicket.setCreatedAt(ZonedDateTime.now());
        tripTicket.setUpdatedAt(ZonedDateTime.now());
        
        Status status = new Status();
        status.setStatusId(1);
        tripTicket.setStatus(status);
        
        StatusDTO statusDTO = new StatusDTO();
        statusDTO.setStatusId(1);
        tripTicketDTO.setStatus(statusDTO);
    }
    
    public TripTicketBuilder withId(int id) {
        tripTicket.setId(id);
        tripTicketDTO.setId(id);
        return this;
    }
    
    public TripTicketBuilder withOriginProvider(Provider provider) {
        tripTicket.setOriginProvider(provider);
        ProviderDTO providerDTO = new ProviderDTO();
        providerDTO.setProviderId(provider.getProviderId());
        providerDTO.setProviderName(provider.getProviderName());
        providerDTO.setIsActive(provider.isActive());
        
        return this;
    }
    
    public TripTicketBuilder withPickupDate(LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();
        tripTicket.setRequestedPickupDate(date);
        tripTicketDTO.setRequestedPickupDate(date);
        return this;
    }
    
    public TripTicketBuilder withPickupTime(String time) {
        Time sqlTime = Time.valueOf(time + ":00");
        tripTicket.setRequestedPickupTime(sqlTime);
        tripTicketDTO.setRequestedPickupTime(sqlTime);
        return this;
    }
    
    public TripTicketBuilder withDropOffDate(LocalDate date) {
        tripTicket.setRequestedDropoffDate(date);
        tripTicketDTO.setRequestedDropoffDate(date);
        return this;
    }
    
    public TripTicketBuilder withDropOffTime(String time) {
        Time sqlTime = Time.valueOf(time + ":00");
        tripTicket.setRequestedDropOffTime(sqlTime);

        tripTicketDTO.setRequestedDropOffTime(sqlTime);
        return this;
    }
    
    public TripTicket build() {
        return tripTicket;
    }
    
    public TripTicketDTO buildDTO() {
        return tripTicketDTO;
    }
}