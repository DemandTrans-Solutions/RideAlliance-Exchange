package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record Scheduling(
    @JsonProperty("pickup_time") Long pickupTime,
    @JsonProperty("dropoff_time") Long dropoffTime
) {}