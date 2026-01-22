package com.clearinghouse.dto;

import java.time.Instant;
import lombok.Builder;

@Builder
public record UberRequestDTO (
        long tripTicketId,

        float pickupLatitude,
        float pickupLongitude,

        float dropoffLatitude,
        float dropoffLongitude,


        Instant requestedPickupTime,
        boolean promisedDropOff
){
}
