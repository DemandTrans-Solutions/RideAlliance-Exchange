package com.clearinghouse.dto;

import lombok.Builder;

import java.sql.Time;

@Builder
public record CurrentRideStatus (

        double currentDriverLatitude,
        double currentDriverLongitude,
        Time currentRideETA,

        double pickupLatitude,
        double pickupLongitde,

        double dropoffLatitude,
        double dropoffLongitude,

        String driverRoute

){
}
