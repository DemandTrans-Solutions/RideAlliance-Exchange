package com.clearinghouse.service;

import com.clearinghouse.dto.*;
import com.clearinghouse.enumentity.TripClaimStatusConstants;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class MedrideService {

    private final TripClaimService tripClaimService;
    private final ProviderService providerService;


    public void claimRide(MedrideRideRequest medrideRideRequest) {

        Instant now = Instant.now();

        var rideRequestTime = (medrideRideRequest.pickupTime() != null) ?
            Instant.ofEpochMilli(medrideRideRequest.pickupTime()) : now;

        // mark the trip ticket as booked with the medride provider and set the claimant trip id
        var medrideProvider = providerService.findMedrideProvider();
        var claimStatus = new StatusDTO();
        claimStatus.setStatusId(TripClaimStatusConstants.pending.tripClaimStatusUpdate());

        var tripClaim = new TripClaimDTO();
        tripClaim.setClaimantProviderId(medrideProvider.getProviderId());
        tripClaim.setTripTicketId(medrideRideRequest.tripTicketId());
        tripClaim.setClaimantProviderName(medrideProvider.getProviderName());


        // convert ride request time into formatted string in America/Denver timezone
        tripClaim.setProposedPickupTime(formatUnixSeconds(rideRequestTime.getEpochSecond(), ZoneId.of("America/Denver"), "yyyy-MM-dd HH:mm:ss"));
        tripClaim.setStatus(claimStatus);

        tripClaimService.createTripClaim(medrideRideRequest.tripTicketId(), tripClaim);
    }



    // Format unix seconds
    public static String formatUnixSeconds(Long unixSeconds, ZoneId zone, String pattern) {
        if (Objects.isNull(unixSeconds)) return null;
        Instant instant = Instant.ofEpochSecond(unixSeconds);
        ZonedDateTime zdt = instant.atZone(zone != null ? zone : ZoneId.systemDefault());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern != null ? pattern : "yyyy-MM-dd HH:mm:ss z");
        return zdt.format(fmt);
    }
}
