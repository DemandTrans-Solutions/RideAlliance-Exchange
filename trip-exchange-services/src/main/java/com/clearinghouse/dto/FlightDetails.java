package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record FlightDetails(
    @JsonProperty("flight_number") String flightNumber,
    @JsonProperty("airline") Airline airline,
    @JsonProperty("arrival") FlightEndpoint arrival,
    @JsonProperty("departure") FlightEndpoint departure
) {}