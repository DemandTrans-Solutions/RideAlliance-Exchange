package com.clearinghouse.service;


import com.clearinghouse.configuration.HttpLoggerFilterConfig;
import com.clearinghouse.dao.ActivityDAO;
import com.clearinghouse.dto.*;
import com.clearinghouse.enumentity.TripTicketStatusConstants;
import com.clearinghouse.entity.Activity;
import com.clearinghouse.entity.Status;
import com.clearinghouse.entity.TripTicket;
import com.clearinghouse.enumentity.TripClaimStatusConstants;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class UberService {



    private final UberApiClient uberApiClient;

    private final TripClaimService tripClaimService;

    private final TripTicketService tripTicketService;


    private final ProviderService providerService;

    private final TripResultService tripResultService;

    private final AddressService addressService;

    private final TripTicketDataService tripTicketDataService;

    private final ActivityDAO  activityDAO;

    private final ActivityService activityService;



    public UberCreateGuestTripResponse bookUberRide(UberRideRequest uberRideRequest) {

        Instant now = Instant.now();
        UberCreateGuestTripResponse guestResponse;



        var rideRequestTime = (uberRideRequest.scheduling() != null && uberRideRequest.scheduling().pickupTime() != null) ?
            Instant.ofEpochMilli(uberRideRequest.scheduling().pickupTime()) : now;

        // if the ride is 2 hours or less away, it should be sent to the on-demand endpoint. Otherwise it goes to the scheduled endpoint.
        if ( rideRequestTime != null &&
                rideRequestTime.isBefore(now.plus(2, ChronoUnit.HOURS))) {
            guestResponse = uberApiClient.createOnDemandTrip(uberRideRequest);
        } else {
            guestResponse = uberApiClient.createScheduledTrip(uberRideRequest);
            //guestResponse = UberCreateGuestTripResponse.builder().requestId(UUID.randomUUID().toString()).build();

        }

        if ( guestResponse != null && guestResponse.requestId() != null) {
            var tripTicket = tripTicketService.getTripTicketByTripTicketId(uberRideRequest.tripTicketId());
            var pickupLocation = tripTicket.getPickupAddress();
            var dropoffLocation = tripTicket.getDropOffAddress();

            // compare the pickup and dropoff locations with the ones in the uberRideRequest
            boolean locationMismatch = false;
            if ( uberRideRequest.pickup() != null && pickupLocation != null) {
                if ( !Objects.equals(uberRideRequest.pickup().latitude(), (double)pickupLocation.getLatitude()) ||
                     !Objects.equals(uberRideRequest.pickup().longitude(), (double)pickupLocation.getLongitude()) ) {
                    log.warn("Pickup location in UberRideRequest does not match TripTicket pickup location, creating a new Location and updating the trip ticket " + tripTicket.getId());
                    pickupLocation = addressService.cloneAddressNewCoords(pickupLocation, uberRideRequest.pickup().latitude().floatValue(), uberRideRequest.pickup().longitude().floatValue());
                    locationMismatch = true;
                }
            }
            if ( uberRideRequest.dropoff() != null && dropoffLocation != null) {
                if (!Objects.equals(uberRideRequest.dropoff().latitude(), (double) dropoffLocation.getLatitude()) ||
                        !Objects.equals(uberRideRequest.dropoff().longitude(), (double) dropoffLocation.getLongitude())) {
                    log.warn("Dropoff location in UberRideRequest does not match TripTicket dropoff location, creating a new Location and updating the trip ticket " + tripTicket.getId());
                    dropoffLocation = addressService.cloneAddressNewCoords(dropoffLocation, uberRideRequest.dropoff().latitude().floatValue(), uberRideRequest.dropoff().longitude().floatValue());
                    locationMismatch = true;
                }
            }

            // mark the trip ticket as booked with the uber provider and set the claimant trip id
            var uberProvider = providerService.findUberProvider();
            var claimStatus = new StatusDTO();
            claimStatus.setStatusId(TripClaimStatusConstants.pending.tripClaimStatusUpdate());

            var tripClaim = new TripClaimDTO();
            tripClaim.setClaimantProviderId(uberProvider.getProviderId());
            tripClaim.setTripTicketId(uberRideRequest.tripTicketId());
            tripClaim.setClaimantTripId(guestResponse.requestId());
            tripClaim.setClaimantProviderName(uberProvider.getProviderName());


            // convert ride request time into formatted string in America/Denver timezone

            tripClaim.setProposedPickupTime(formatUnixSeconds(rideRequestTime.getEpochSecond(), ZoneId.of("America/Denver"), "yyyy-MM-dd HH:mm:ss"));
            tripClaim.setRequesterProviderFare(tripTicket.getRequesterProviderFare());
            tripClaim.setProposedFare(uberRideRequest.uberFare().floatValue());
            tripClaim.setStatus(claimStatus);
            tripClaim.setOverridePriceMismatch(true); // Allows fare differences between proposed and actual for Uber
            if ( locationMismatch ) {
                tripClaim.setNotes("Pickup/Dropoff location updated due to mismatch with UberRideRequest. Booked Uber Ride Request " + guestResponse.requestId());
            } else {
                tripClaim.setNotes("Booked Uber Ride Request " + guestResponse.requestId());
            }
            tripClaimService.createTripClaim(uberRideRequest.tripTicketId(), tripClaim);

            tripTicket.setCommonTripId(guestResponse.requestId());
            if ( locationMismatch ) {
                tripTicket.setPickupAddress(pickupLocation);
                tripTicket.setDropOffAddress(dropoffLocation);
            }
            tripTicketService.updateTripTicket(tripTicket);

        } else {
            log.debug("bookUberRide failed for uberRideRequest={}", uberRideRequest);
        }
        return guestResponse;
    }





    public boolean cancelTripTicket(int tripTicketId) {
        var tripTicket = tripTicketService.findTripTicketByTripTicketId(tripTicketId);
        if ( tripTicket != null ) {

            // rescind Uber claim on the trip
            if ( tripTicket.getApprovedTripClaimId() != null ) {
                var tripClaim = tripClaimService.findTripClaimByTripClaimId(tripTicket.getApprovedTripClaimId());
                if ( tripClaim != null ) {
                    log.debug("cancelTripTicket: rescinding claim for tripClaimId={}", tripClaim.getId());
                    tripClaimService.rescindTripClaim(tripClaim.getTripTicketId(), tripClaim.getId());
                }
            }

            var uberTripId = tripTicket.getCommonTripId();
            if ( uberTripId != null ) {
                cancelTrip(uberTripId);

                var activity = new ActivityDTO();
                activity.setAction("Manual Cancel Uber Trip for ride " + uberTripId);
                activity.setTripTicketId(tripTicketId);
                activityService.createActivity(activity);

                tripClaimService.findAllTripClaims(tripTicketId).forEach(tripClaim -> {
                   if ( tripClaim.getStatus() != null && tripClaim.getStatus().getStatusId() == TripClaimStatusConstants.approved.tripClaimStatusUpdate() ) {

                       // rescind this claim
                        log.debug("Uber cancelTripTicket: rescinding claim for tripTicketId={} and tripClaim={}", tripTicketId, tripClaim.getId());
                        tripClaimService.rescindTripClaim(tripTicketId, tripClaim.getId());
                   }
                });
                return true;
            } else {
                log.debug("cancelTripTicket failed: no commonTripId for tripTicketId={}", tripTicketId);
            }
        }
        return false;
    }



    // Format unix seconds
    public static String formatUnixSeconds(Long unixSeconds, ZoneId zone, String pattern) {
        if (Objects.isNull(unixSeconds)) return null;
        Instant instant = Instant.ofEpochSecond(unixSeconds);
        ZonedDateTime zdt = instant.atZone(zone != null ? zone : ZoneId.systemDefault());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(pattern != null ? pattern : "yyyy-MM-dd HH:mm:ss z");
        return zdt.format(fmt);
    }

    // ---- Stubs for Uber API endpoints extracted from TDS middleware Swagger ----

    public String healthCheck() {
        try {
            return uberApiClient.healthCheck();
        } catch (Exception ex) {
            log.warn("healthCheck request failed", ex);
            return null;
        }
    }

    public UberGetRequestZonesResponse getRequestZones(String latitude, String longitude) {
        try {
            return uberApiClient.getRequestZones(latitude, longitude);
        } catch (Exception ex) {
            log.warn("getRequestZones failed", ex);
            return null;
        }
    }


    public UberResponseDTO getUberEstimates(UberRequestDTO uberRequestDTO) {
        // if the ride is 2 hours or less away, it should be sent to the on-demand endpoint. Otherwise, it goes to the scheduled endpoint.
        Instant now = Instant.now();
        UberResponseDTO uberConfirmation;
        if (uberRequestDTO.requestedPickupTime() != null &&
                uberRequestDTO.requestedPickupTime().isBefore(now.plus(2, ChronoUnit.HOURS))) {
            // Within 2 hours - use on-demand endpoint
            log.debug("Ride within 2 hours - using on-demand endpoint");
            uberConfirmation = getOnDemandTripEstimates(uberRequestDTO);
        } else {
            // More than 2 hours away - use scheduled endpoint
            log.debug("Ride more than 2 hours away - using scheduled endpoint");
            uberConfirmation = getScheduledOrReservedTripEstimates(uberRequestDTO);
        }
        return uberConfirmation;
    }

    public UberResponseDTO getOnDemandTripEstimates(UberRequestDTO req) {
        try {
            var resp =  uberApiClient.getOnDemandTripEstimates(toOnDemandEstimateRequest(req));
            return fromTripEstimatesResponse(resp);
        } catch (Exception ex) {
            log.warn("getGuestTripEstimates failed", ex);
            return null;
        }
    }


    /**
     * Convert a business-level UberRequestDTO into an on-demand estimate request for the TDS middleware.
     */
    public static UberOnDemandEstimateRequest toOnDemandEstimateRequest(UberRequestDTO req) {
        if (req == null) return null;
    Coordinates pickup = new Coordinates((double) req.pickupLatitude(), (double) req.pickupLongitude());
    Coordinates dropoff = new Coordinates((double) req.dropoffLatitude(), (double) req.dropoffLongitude());
        return new UberOnDemandEstimateRequest(pickup, dropoff);
    }

    /**
     * Convert a business-level UberRequestDTO into a scheduled estimate request for the TDS middleware.
     * The scheduling PickupDropoffTimes will be populated using the request's requestedPickupTime; dropoff time
     * will be left null since UberRequestDTO contains only a single requestedPickupTime.
     */

    // src/main/java/com/clearinghouse/service/UberService.java
    public static UberScheduledEstimateRequest toScheduledEstimateRequest(UberRequestDTO req) {
        if (req == null) return null;
        Coordinates pickup = new Coordinates((double) req.pickupLatitude(), (double) req.pickupLongitude());
        Coordinates dropoff = new Coordinates((double) req.dropoffLatitude(), (double) req.dropoffLongitude());

        var requestPickup = req.requestedPickupTime();

        // Interpret the requested pickup time as America/Denver and convert to epoch millis
        Long pickupMillis = null;
        if (requestPickup != null ) {
            pickupMillis = requestPickup.atZone(ZoneId.of("America/Denver")).toInstant().toEpochMilli();
        }

        Long dropoffMillis = null;
        if ( req.promisedDropOff() ) {
            dropoffMillis = pickupMillis;
            pickupMillis = null;
        }

        PickupDropoffTimes scheduling = new PickupDropoffTimes(pickupMillis, dropoffMillis);
        return new UberScheduledEstimateRequest(pickup, dropoff, scheduling);
    }



    /**
     * Convert a business-level UberRequestDTO into a generic UberRideRequest for creating a scheduled trip.
     * Fields that are not available in UberRequestDTO (guest, return trip params, etc.) are left null.
     */
    public static UberRideRequest toScheduledRideRequest(UberRequestDTO req) {
        if (req == null) return null;
        
        // Create scheduling details from requestedPickupTime
        Scheduling scheduling = (req.requestedPickupTime() == null) ? null :
            Scheduling.builder().pickupTime(req.requestedPickupTime().toEpochMilli()).build();
        
        // Create CoordinatesWithPlace objects for pickup and dropoff locations
        CoordinatesWithPlace pickup = CoordinatesWithPlace.builder()
            .latitude((double) req.pickupLatitude())
            .longitude((double) req.pickupLongitude())
            .build();
            
        CoordinatesWithPlace dropoff = CoordinatesWithPlace.builder()
            .latitude((double) req.dropoffLatitude())
            .longitude((double) req.dropoffLongitude())
            .build();
            
        // Build and return the generic UberRideRequest (scheduling present for scheduled trips)
        return UberRideRequest.builder()
            .pickup(pickup)
            .dropoff(dropoff)
            .scheduling(scheduling)
            .build();
    }

    public UberTripEstimatesResponse getFlexibleTripEstimates(UberFlexibleEstimateRequest req) {
        try {
            return uberApiClient.getFlexibleTripEstimates(req);
        } catch (Exception ex) {
            log.warn("getFlexibleTripEstimates failed", ex);
            return null;
        }
    }

    public UberResponseDTO getScheduledOrReservedTripEstimates(UberRequestDTO req) {
        try {
            var uberRequest = toScheduledEstimateRequest(req);

            var resp =  uberApiClient.getScheduledTripEstimates(uberRequest);
            return fromTripEstimatesResponse(resp);
        } catch (Exception ex) {
            log.warn("getScheduledOrReservedTripEstimates failed", ex);
            return null;
        }
    }

    public UberTripEstimatesResponse getReserveAirportPickupTripEstimates(UberReserveAirportPickupEstimateRequest req) {
        try {
            return uberApiClient.getReserveAirportPickupTripEstimates(req);
        } catch (Exception ex) {
            log.warn("getReserveAirportPickupTripEstimates failed", ex);
            return null;
        }
    }

    public UberTripEstimatesResponse insertTestTrip(UberInsertTestTrip req) {
        try {
            return uberApiClient.insertTestTrip(req);
        } catch (Exception ex) {
            log.warn("insertTestTrip failed", ex);
            return null;
        }
    }

    public UberCreateGuestTripResponse createOnDemandTrip(UberRideRequest req) {
        try {
            return uberApiClient.createOnDemandTrip(req);
        } catch (Exception ex) {
            log.warn("createOnDemandTrip failed", ex);
            return null;
        }
    }

    public UberCreateGuestTripResponse createScheduledTripWithScheduledReturn(UberRideRequest req) {
        try {
            return uberApiClient.createScheduledTripWithScheduledReturn(req);
        } catch (Exception ex) {
            log.warn("createScheduledTripWithScheduledReturn failed", ex);
            return null;
        }
    }

    public UberCreateGuestTripResponse createScheduledTripWithFlexibleReturn(UberRideRequest req) {
        try {
            return uberApiClient.createScheduledTripWithFlexibleReturn(req);
        } catch (Exception ex) {
            log.warn("createScheduledTripWithFlexibleReturn failed", ex);
            return null;
        }
    }

    public UberCreateGuestTripResponse createReservedTripWithScheduledReturn(UberRideRequest req) {
        try {
            return uberApiClient.createReservedTripWithScheduledReturn(req);
        } catch (Exception ex) {
            log.warn("createReservedTripWithScheduledReturn failed", ex);
            return null;
        }
    }

    public UberCreateGuestTripResponse createReservedTripWithFlexibleReturn(UberRideRequest req) {
        try {
            return uberApiClient.createReservedTripWithFlexibleReturn(req);
        } catch (Exception ex) {
            log.warn("createReservedTripWithFlexibleReturn failed", ex);
            return null;
        }
    }

    public void cancelTrip(String requestId) {
        try {
            uberApiClient.cancelTrip(requestId);
        } catch (Exception ex) {
            log.warn("cancelTrip uber failed for requestId={}", requestId, ex);
        }
    }

    public void handleWebhook(Object webhookPayload) {
        // The TDS middleware will POST webhook events to /api/v1/uber/webhook
        // This method is a client-side stub for invoking the webhook endpoint if needed for testing
        try {
            uberApiClient.handleWebhook(webhookPayload);
        } catch (Exception ex) {
            log.warn("handleWebhook invocation failed", ex);
        }
    }


    public List<TripTicket> findUpcomingUberTripTickets() {
        // find all trip tickets with a status of in progress and a provider of Uber
        var uberProvider = providerService.findUberProvider();
        var activeUberTickets = tripClaimService.findTripTicketsByProviderAndExceptStatus(uberProvider, TripTicketStatusConstants.completed.tripTicketStatusUpdate());
        return activeUberTickets;
    }



    public static UberResponseDTO fromTripEstimatesResponse(UberTripEstimatesResponse response) {
        if (response == null || response.productEstimates() == null) {
            return UberResponseDTO.builder().rideOptions(List.of()).build();
        }
        List<UberRideOptionDTO> rideOptions = response.productEstimates().stream().map(productEstimate -> {
            UberProduct product = productEstimate.product();
            UberEstimateInfo estimateInfo = productEstimate.estimateInfo();
            UberFare fare = estimateInfo != null ? estimateInfo.fare() : null;

            
            // Extract pickup time from estimate info if available
            Long pickupEstimate = null;
            Long dropoffEstimate = null;
            Double distance = null;
            Integer duration = null;
            
            if (estimateInfo != null) {
                pickupEstimate = estimateInfo.pickupEstimate();
                if ( pickupEstimate == null ) {


                }
                if (estimateInfo.trip() != null) {
                    distance = estimateInfo.trip().distanceEstimate();
                    duration = estimateInfo.trip().durationEstimate();
                    if ( estimateInfo.trip().pickupTime() != null ) {
                        pickupEstimate = estimateInfo.trip().pickupTime();
                    }
                    if ( estimateInfo.trip().dropoffTime() != null ) {
                        dropoffEstimate = estimateInfo.trip().dropoffTime();
                    }
                }
            }
            
            // Create ride type with more details
            UberRideType rideType = UberRideType.builder()
                    .uberRideTypeId(product != null ? product.productId() : null)
                    .displayName(product != null ? product.displayName() : null)
                    .capacity(product != null && product.capacity() != null ? product.capacity() : 0)
                    .mobilityOptions(null) // No info available
                    .build();
                    
            // Calculate estimated pickup time if estimate is available
            Instant estimatedPickupTime = null;
            Instant estimatedDropoffTime = null;
            int etaMinutesValue = 0;
            // pickupEstimate is an epoch timestamp in milliseconds (per TDS middleware). Convert to Instant.
            if (pickupEstimate != null) {
                try {
                    estimatedPickupTime = Instant.ofEpochMilli(pickupEstimate);
                    long mins = Duration.between(Instant.now(), estimatedPickupTime).toMinutes();
                    etaMinutesValue = (int) Math.max(0, mins);
                } catch (Exception e) {
                    // Fallback: if conversion fails, leave estimatedPickupTime null and etaMinutesValue 0
                    estimatedPickupTime = null;
                    etaMinutesValue = 0;
                }
            }
            if (dropoffEstimate != null) {
                try {
                    estimatedDropoffTime = Instant.ofEpochMilli(dropoffEstimate);
                } catch (Exception e) {
                    // Fallback: if conversion fails, leave estimatedPickupTime null and etaMinutesValue 0
                    estimatedDropoffTime = null;
                }
            }


            // Calculate surge multiplier if available in fare
            Double surgeMultiplier = 1.0; // Default value

            return UberRideOptionDTO.builder()
                     .uberRideOptionId(product != null ? product.productId() : null)
                     .uberRideType(rideType)
                     .imageUrl(product != null ? product.image() : null)
                     .fullPrice(fare != null && fare.value() != null ? fare.value() : 0.0)
                     .price(fare != null && fare.value() != null ? fare.value() : 0.0)
                     .estimatedPickupTime(estimatedPickupTime)
                     .estimatedDropoffTime(estimatedDropoffTime)
                     .distance(distance != null ? distance : 0.0)
                     .etaMinutes(etaMinutesValue)
                     .fareId(fare != null ? fare.fareId() : null)
                     .productId(product != null ? product.productId() : null)
                     .fareDisplay(fare != null ? fare.display() : null)
                     .surgeMultiplier(surgeMultiplier)
                     .build();
        }).collect(Collectors.toList());
        return UberResponseDTO.builder().rideOptions(rideOptions).build();
    }



    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateUberRideStatus(TripTicket tripTicket) {
        // call the TDS Middleware to get current ride status
        var uberProvider = providerService.findUberProvider();
        if ( tripTicket.getCommonTripId() == null || tripTicket.getCommonTripId().isEmpty()) {
            log.warn("updateUberRideStatus called for tripTicket with no commonTripId: {}", tripTicket.getId());
            return;
        }
        var uberTrip = uberApiClient.getTrip(tripTicket.getCommonTripId());

        // This trip is ended, create a trip result record
        Long dropoffTimeMillis = (uberTrip == null) ? null : uberTrip.dropoffTime();
        if ( uberTrip != null ) {

            var tripResults = tripResultService.findAllTripResultByTripTicketId(tripTicket.getId());
            var tripResult = new TripResultDTO();
            if (!tripResults.isEmpty()) {
                log.debug("Trip {} already has trip results, deleting old  trip results", tripTicket.getId());
                tripResults.forEach(tr -> {
                    tripResult.setId(tr.getId());
                });
            }
            tripResult.setTripTicketId(tripTicket.getId());
            tripResult.setClaimantProvider(uberProvider.getProviderName());

            if (dropoffTimeMillis != null && dropoffTimeMillis > 0) {

                // Coordinates may be null; convert Double -> float safely
                CoordinatesWithPlace dropoff = (uberTrip.dropoff() != null) ? uberTrip.dropoff() : null;
                CoordinatesWithPlace pickup = (uberTrip.pickup() != null) ? uberTrip.pickup() : null;

                if (dropoff != null && dropoff.latitude() != null) {
                    tripResult.setDropOffLatitude(dropoff.latitude().floatValue());
                }
                if (dropoff != null && dropoff.longitude() != null) {
                    tripResult.setDropOffLongitude(dropoff.longitude().floatValue());
                }
                if (pickup != null && pickup.latitude() != null) {
                    tripResult.setPickUpLatitude(pickup.latitude().floatValue());
                }
                if (pickup != null && pickup.longitude() != null) {
                    tripResult.setPickupLongitude(pickup.longitude().floatValue());
                }

                // Addresses - guard nulls
                tripResult.setDropOffAddress(dropoff != null && dropoff.place() != null ? dropoff.place().toString() : null);
                tripResult.setPickUpAddress(pickup != null && pickup.place() != null ? pickup.place().toString() : null);

                // Times: UberTrip exposes epoch millis as Long. Convert to java.sql.Time / java.util.Date
                Long beginMillis = uberTrip.beginTripTime();
                ZoneId denver = ZoneId.of("America/Denver");
                if (beginMillis != null) {
                    LocalTime denverPickup = Instant.ofEpochMilli(beginMillis).atZone(denver).toLocalTime();
                    tripResult.setActualPickupArriveTime(java.sql.Time.valueOf(denverPickup));
                    tripResult.setActualPickupDepartTime(java.sql.Time.valueOf(denverPickup));
                    tripResult.setTripDate(new java.util.Date(beginMillis));
                }
                // dropoffTimeMillis was already checked in the outer condition; convert directly
                LocalTime denverDropoff = Instant.ofEpochMilli(dropoffTimeMillis).atZone(denver).toLocalTime();
                tripResult.setActualDropOffArriveTime(java.sql.Time.valueOf(denverDropoff));
                tripResult.setActualDropOffDepartTime(java.sql.Time.valueOf(denverDropoff));

                // Fare (Double -> float)
                Double clientFareNumeric = uberTrip.clientFareNumeric();
                if (clientFareNumeric != null) {
                    tripResult.setFare(clientFareNumeric.floatValue());
                }


                // Driver name (null-safe)
                var driver = uberTrip.driver();
                tripResult.setDriverName(driver != null ? driver.name() : null);

                // Miles traveled (Double -> float)
                Double miles = uberTrip.tripDistanceMiles();
                if (miles != null) {
                    tripResult.setMilesTraveled(miles.floatValue());
                }

                // Vehicle name (null-safe)
                var vehicle = uberTrip.vehicle();
                tripResult.setVehicleName(vehicle != null ? vehicle.licensePlate() : null);

                tripResult.setOrgProviderId(uberProvider.getProviderId());

                Status status = new Status();
                status.setStatusId(TripTicketStatusConstants.completed.tripTicketStatusUpdate());
                tripTicket.setStatus(status);
                tripTicketService.updateTripTicket(tripTicket);
                tripResultService.updateTripResult(tripResult);
            } else if ( uberTrip.status() != null && uberTrip.status().toLowerCase().contains("cancel")) {
                // Uber trip was cancelled

                Long beginMillis = uberTrip.beginTripTime();
                ZoneId denver = ZoneId.of("America/Denver");
                if (beginMillis != null && beginMillis > 0) {
                    LocalTime denverPickup = Instant.ofEpochMilli(beginMillis).atZone(denver).toLocalTime();
                    tripResult.setActualPickupArriveTime(java.sql.Time.valueOf(denverPickup));
                    tripResult.setActualPickupDepartTime(java.sql.Time.valueOf(denverPickup));
                    tripResult.setTripDate(new java.util.Date(beginMillis));
                } else {
                    tripResult.setTripDate(tripTicket.getRequestedPickupDate() != null
                            ? java.util.Date.from(tripTicket.getRequestedPickupDate().atStartOfDay(denver).toInstant())
                            : (tripTicket.getRequestedDropoffDate() != null
                            ? java.util.Date.from(tripTicket.getRequestedDropoffDate().atStartOfDay(denver).toInstant())
                            : new Date()));
                }

                Status status = new Status();
                status.setStatusId(TripTicketStatusConstants.cancelled.tripTicketStatusUpdate());
                tripTicket.setStatus(status);
                tripResult.setCancellationReason(uberTrip.statusDetail() != null ? uberTrip.statusDetail() : uberTrip.status());

                tripTicketService.updateTripTicket(tripTicket);
                tripResultService.updateTripResult(tripResult);

            }
        }
    }


    public TripSummary getTripSummary(String requestId) {
        try {
            return uberApiClient.getTrip(requestId);
            // This was for testing return tripTicketDataService.generateTestTripSummary();

        } catch (Exception ex) {
            log.warn("getTripSummary failed for requestId={}", requestId, ex);
            return null;
        }
    }



    @Scheduled(initialDelay = 15000, fixedRate = 300000)
    public void updateUberRides() {
        log.debug("updating uber rides");
        var uberTickets = findUpcomingUberTripTickets();
        int updatedUberTickets = 0;
        for (TripTicket tripTicket : uberTickets) {
            try {
                updateUberRideStatus(tripTicket);
                updatedUberTickets++;
            } catch ( Exception ex ) {
                log.warn("failed to update uber ride for tripTicketId={}", tripTicket.getId(), ex);
            }
        }
        log.debug("updated {} uber rides", updatedUberTickets);
    }

}
