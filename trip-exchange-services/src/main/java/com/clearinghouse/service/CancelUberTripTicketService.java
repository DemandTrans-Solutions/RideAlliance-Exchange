package com.clearinghouse.service;

import com.clearinghouse.dto.TripTicketDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class CancelUberTripTicketService {


    private final ProviderService providerService;

    private final UberService uberService;


    public boolean cancelTripTicket(int tripTicketId) {

        return uberService.cancelTripTicket(tripTicketId);

    }

}
