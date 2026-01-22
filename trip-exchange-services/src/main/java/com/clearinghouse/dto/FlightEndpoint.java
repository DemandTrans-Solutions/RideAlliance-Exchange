package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record FlightEndpoint(
    @JsonProperty("airport_code") String airportCode,
    @JsonProperty("estimated_time_ms") String estimatedTimeMs
) {}