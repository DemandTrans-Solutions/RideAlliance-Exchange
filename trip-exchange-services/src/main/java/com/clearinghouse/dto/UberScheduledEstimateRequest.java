package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UberScheduledEstimateRequest(
	@JsonProperty("pickup") Coordinates pickup,
	@JsonProperty("dropoff") Coordinates dropoff,
	@JsonProperty("scheduling") PickupDropoffTimes scheduling
) {}
