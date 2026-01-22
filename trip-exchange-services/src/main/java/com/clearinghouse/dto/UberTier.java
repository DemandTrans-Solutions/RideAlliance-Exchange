package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record UberTier(
    @JsonProperty("amount") Double amount,
    @JsonProperty("distance") Integer distance,
    @JsonProperty("time_in_mins") Integer timeInMins,
    @JsonProperty("distance_unit") String distanceUnit,
    @JsonProperty("formatted_time_and_distance_package") String formattedTimeAndDistancePackage
) {}

