package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record UberTripDistanceEstimate(
    @JsonProperty("distance_estimate") Double distanceEstimate,
    @JsonProperty("distance_unit") String distanceUnit,
    @JsonProperty("duration_estimate") Integer durationEstimate,
    @JsonProperty("travel_distance_estimate") Double travelDistanceEstimate,
    @JsonProperty("pickup_time") Long pickupTime,
    @JsonProperty("dropoff_time") Long dropoffTime
) {}