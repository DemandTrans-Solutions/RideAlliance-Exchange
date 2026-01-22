package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Options for deferred / flexible ride requests. OpenAPI includes pickup_day;
 * we keep this minimal record in case extra fields are later added.
 */

import lombok.Builder;

@Builder
public record DeferredRideOptions(
	@JsonProperty("pickup_day") String pickupDay
) {}
