package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public record UberFlexibleEstimateRequest(
	@JsonProperty("pickup") Coordinates pickup,
	@JsonProperty("dropoff") Coordinates dropoff,
	@JsonProperty("deferred_ride_options") DeferredRideOptions deferredRideOptions
) {}
