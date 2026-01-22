package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UberReserveAirportPickupEstimateRequest(
	@JsonProperty("pickup") Coordinates pickup,
	@JsonProperty("dropoff") Coordinates dropoff,
	@JsonProperty("flight") Flight flight
) {}
