package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Pickup and dropoff times represented as epoch milliseconds (per OpenAPI int64).
 */

import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record PickupDropoffTimes(
	@JsonProperty("pickup_time") @JsonAlias({"pickup_time","pickupTime"}) Long pickupTime,
	@JsonProperty("dropoff_time") @JsonAlias({"dropoff_time","dropoffTime"}) Long dropoffTime
) {}
