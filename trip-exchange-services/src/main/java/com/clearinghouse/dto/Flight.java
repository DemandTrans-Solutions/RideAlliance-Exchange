package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Flight information used for airport pickup estimates.
 * Mirrors OpenAPI fields: designator, arrival_airport_code, arrival_date
 */

import lombok.Builder;

@Builder
public record Flight(
	@JsonProperty("designator") String designator,
	@JsonProperty("arrival_airport_code") String arrivalAirportCode,
	@JsonProperty("arrival_date") String arrivalDate
) {
	// convenience accessor names match camelCase used in Java; JSON mapping handled by Jackson
}
