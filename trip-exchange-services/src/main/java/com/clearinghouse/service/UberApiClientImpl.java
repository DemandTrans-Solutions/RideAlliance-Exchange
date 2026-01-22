package com.clearinghouse.service;

import com.clearinghouse.config.UberProperties;
import com.clearinghouse.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.stream.Collectors;

@Slf4j
@Component

public class UberApiClientImpl implements UberApiClient {

    private final RestTemplate restTemplate;
    private final UberTokenService tokenService;

    private final ObjectMapper objectMapper;

    private final String baseUrl;

    /**
     * Primary constructor used when wiring via Spring: inject a RestTemplate (uberRestTemplate) and UberTokenService.
     * The uberRestTemplate should already have the UberAuthInterceptor registered (configured in UberRestTemplateConfig).
     */

    @Autowired
    public UberApiClientImpl(@Qualifier("uberRestTemplate") RestTemplate restTemplate, UberProperties uberProperties, UberTokenService tokenService, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.tokenService = tokenService;
        this.baseUrl = uberProperties.getBaseUrl();
        // ensure interceptor is present so outgoing requests get Authorization header
        this.restTemplate.getInterceptors().add(new UberAuthInterceptor(this::getAccessToken));
        this.objectMapper = objectMapper;
    }

    // Backwards-compatible constructor used in tests: accept a tokenRestTemplate and create a local UberTokenService instance.
    public UberApiClientImpl(RestTemplate restTemplate, UberProperties uberProperties, RestTemplate tokenRestTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.tokenService = new UberTokenService(tokenRestTemplate, uberProperties);
        this.baseUrl = uberProperties.getBaseUrl();
        // register interceptor on the provided restTemplate so tests using a plain RestTemplate will exercise token fetch
        this.restTemplate.getInterceptors().add(new UberAuthInterceptor(this::getAccessToken));
        this.objectMapper = objectMapper;
    }

    @Override
    public String healthCheck() {
        String uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/v1/uber/health-check").toUriString();
        return restTemplate.getForObject(uri, String.class);
    }

    @Override
    public UberGetRequestZonesResponse getRequestZones(String latitude, String longitude) {
    String uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/v1/uber/getRequestZones")
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .toUriString();
    ResponseEntity<UberGetRequestZonesResponse> resp = restTemplate.exchange(uri, HttpMethod.GET, null, UberGetRequestZonesResponse.class);
    return resp.getBody();
    }

    @Override
    public UberTripEstimatesResponse getOnDemandTripEstimates(UberOnDemandEstimateRequest request) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/v1/uber/getOnDemandTripEstimates").toUriString();
        return restTemplate.postForObject(uri, request, UberTripEstimatesResponse.class);
    }

    @Override
    public UberTripEstimatesResponse getFlexibleTripEstimates(UberFlexibleEstimateRequest request) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/v1/uber/getFlexibleTripEstimates").toUriString();
        return restTemplate.postForObject(uri, request, UberTripEstimatesResponse.class);
    }

    @Override
    public UberTripEstimatesResponse getScheduledTripEstimates(UberScheduledEstimateRequest request) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/v1/uber/getScheduledTripEstimates").toUriString();

        String requestJson = null;
        try {
            requestJson = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            log.warn("Failed to serialize Uber scheduled/reserved trip estimates request to JSON", e);
        }

        log.debug("Sending scheduled/reserved trip estimates request to {}: {}", uri, requestJson);

        var resp =  restTemplate.postForObject(uri, request, UberTripEstimatesResponse.class);

        // if a pickup estimate is null, populate it with the requested pickup time
        if ( resp != null && resp.productEstimates() != null) {
            // Determine requested time in ms (prefer pickup, fall back to dropoff)
            final Long requestTimeMs = (request != null && request.scheduling() != null)
                    ? (request.scheduling().pickupTime() != null ? request.scheduling().pickupTime() : request.scheduling().dropoffTime())
                    : null;
            if (requestTimeMs != null) {
                var updatedProducts = resp.productEstimates().stream().map(pe -> {
                    var ei = pe.estimateInfo();
                    Long pickupEstimate = null;
                    Long dropoffEstimate = null;
                    if ( ei != null && ei.trip() != null ) {
                        pickupEstimate = ei.trip().pickupTime();
                        dropoffEstimate = ei.trip().dropoffTime();
                    }
                    if ( pickupEstimate == null ) {
                        pickupEstimate = requestTimeMs;
                    }


                    if (ei == null || ei.pickupEstimate() != null) {
                        return pe; // leave as-is
                    }
                    var newEi = new UberEstimateInfo(
                            ei.fare(),
                            ei.trip(),
                            ei.fareId(),
                            pickupEstimate,
                            dropoffEstimate,
                            ei.pricingExplanation(),
                            ei.noCarsAvailable()
                    );
                    return new UberProductEstimate(
                            pe.fare(),
                            pe.product(),
                            newEi,
                            pe.fareId(),
                            pe.fulfillmentIndicator()
                    );
                }).collect(Collectors.toList());

                // clone the resp java record since java records are immutable, but set product Estimate's estimateInfo pickupEstimate to the requestTime
                resp = new UberTripEstimatesResponse(resp.etasUnavailable(), resp.faresUnavailable(), updatedProducts);
            }
        }

        return resp;
    }

    @Override
    public UberTripEstimatesResponse getReserveAirportPickupTripEstimates(UberReserveAirportPickupEstimateRequest request) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/v1/uber/getReserveAirportPickupTripEstimates").toUriString();
        return restTemplate.postForObject(uri, request, UberTripEstimatesResponse.class);
    }

    @Override
    public UberTripEstimatesResponse insertTestTrip(UberInsertTestTrip req) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/v1/uber/insertTestTrip").toUriString();
        return restTemplate.postForObject(uri, req, UberTripEstimatesResponse.class);
    }

    @Override
    public UberCreateGuestTripResponse createOnDemandTrip(UberRideRequest req) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/v1/uber/createOnDemandTrip").toUriString();
        return restTemplate.postForObject(uri, req, UberCreateGuestTripResponse.class);
    }

    @Override
    public UberCreateGuestTripResponse createScheduledTripWithScheduledReturn(UberRideRequest req) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/v1/uber/createScheduledTripWithScheduledReturn").toUriString();
        return restTemplate.postForObject(uri, req, UberCreateGuestTripResponse.class);
    }

    @Override
    public UberCreateGuestTripResponse createScheduledTripWithFlexibleReturn(UberRideRequest req) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/v1/uber/createScheduledTripWithFlexibleReturn").toUriString();
        return restTemplate.postForObject(uri, req, UberCreateGuestTripResponse.class);
    }

    @Override
    public UberCreateGuestTripResponse createReservedTripWithScheduledReturn(UberRideRequest req) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/v1/uber/createReservedTripWithScheduledReturn").toUriString();
        return restTemplate.postForObject(uri, req, UberCreateGuestTripResponse.class);
    }

    @Override
    public UberCreateGuestTripResponse createReservedTripWithFlexibleReturn(UberRideRequest req) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/v1/uber/createReservedTripWithFlexibleReturn").toUriString();
        return restTemplate.postForObject(uri, req, UberCreateGuestTripResponse.class);
    }

    @Override
    public UberCreateGuestTripResponse createScheduledTrip(UberRideRequest req) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/v1/uber/createScheduledTrip").toUriString();
        String requestJson = null;
        try {
            requestJson = objectMapper.writeValueAsString(req);
        } catch (Exception e) {
            log.warn("Failed to serialize Uber scheduled/reserved trip estimates request to JSON", e);
        }
        log.debug("Sending scheduled trip request to {}: {}", uri, requestJson);
        return restTemplate.postForObject(uri, req, UberCreateGuestTripResponse.class);
    }

    @Override
    public UberCreateGuestTripResponse createOnDemandTripWithScheduledReturn(UberRideRequest req) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/v1/uber/createOnDemandTripWithScheduledReturn").toUriString();
        return restTemplate.postForObject(uri, req, UberCreateGuestTripResponse.class);
    }

    @Override
    public UberCreateGuestTripResponse createOnDemandTripWithFlexibleReturn(UberRideRequest req) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/v1/uber/createOnDemandTripWithFlexibleReturn").toUriString();
        return restTemplate.postForObject(uri, req, UberCreateGuestTripResponse.class);
    }

    @Override
    public TripSummary getTrip(String requestId) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/v1/uber/trip/{requestId}").buildAndExpand(requestId).toUriString();
        return restTemplate.getForObject(uri, TripSummary.class);
    }

    @Override
    public void cancelTrip(String requestId) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/v1/uber/cancelTrip/{requestId}").buildAndExpand(requestId).toUriString();
        restTemplate.delete(uri);
    }

    @Override
    public void handleWebhook(Object payload) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/v1/uber/webhook").toUriString();
        restTemplate.postForObject(uri, payload, Void.class);
    }

    /*
     * Build HttpHeaders including Authorization Bearer token. The token is lazily fetched and refreshed when expired.
     */
    // Authorization header will be applied by UberAuthInterceptor registered on RestTemplate

     /**
      * Obtain a valid access token. If current token is missing or expired (with small buffer), fetch a new one.
      */
     private String getAccessToken() {
         return tokenService.getAccessToken();
     }

     // TokenResponse is mapped by the public TokenResponse class in the same package
 }
