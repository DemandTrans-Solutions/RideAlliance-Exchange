package com.clearinghouse.service;

import com.clearinghouse.dto.*;

public interface UberApiClient {
    String healthCheck();

    UberGetRequestZonesResponse getRequestZones(String latitude, String longitude);

    UberTripEstimatesResponse getOnDemandTripEstimates(UberOnDemandEstimateRequest request);

    UberTripEstimatesResponse getFlexibleTripEstimates(UberFlexibleEstimateRequest request);

    UberTripEstimatesResponse getScheduledTripEstimates(UberScheduledEstimateRequest request);

    UberTripEstimatesResponse getReserveAirportPickupTripEstimates(UberReserveAirportPickupEstimateRequest request);

    UberTripEstimatesResponse insertTestTrip(UberInsertTestTrip req);

    UberCreateGuestTripResponse createOnDemandTrip(UberRideRequest req);

    UberCreateGuestTripResponse createScheduledTripWithScheduledReturn(UberRideRequest req);

    UberCreateGuestTripResponse createScheduledTripWithFlexibleReturn(UberRideRequest req);

    UberCreateGuestTripResponse createReservedTripWithScheduledReturn(UberRideRequest req);

    UberCreateGuestTripResponse createReservedTripWithFlexibleReturn(UberRideRequest req);

    // New endpoints from API specification (consolidated input)
    UberCreateGuestTripResponse createScheduledTrip(UberRideRequest req);

    UberCreateGuestTripResponse createOnDemandTripWithScheduledReturn(UberRideRequest req);

    UberCreateGuestTripResponse createOnDemandTripWithFlexibleReturn(UberRideRequest req);

    TripSummary getTrip(String requestId);

    void cancelTrip(String requestId);

    void handleWebhook(Object payload);
}
