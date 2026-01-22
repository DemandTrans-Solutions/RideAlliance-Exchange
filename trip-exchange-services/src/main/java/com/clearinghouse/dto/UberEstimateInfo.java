package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record UberEstimateInfo(
    @JsonProperty("fare") UberFare fare,
    @JsonProperty("trip") UberTripDistanceEstimate trip,
    @JsonProperty("fare_id") String fareId,
    @JsonProperty("pickup_estimate") Long pickupEstimate,
    @JsonProperty("dropoff_estimate") Long dropoffEstimate,
    @JsonProperty("pricing_explanation") String pricingExplanation,
    @JsonProperty("no_cars_available") Boolean noCarsAvailable
) {}

