package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record UberFare(
	@JsonProperty("display") String display,
	@JsonProperty("value") Double value,
	@JsonProperty("hourly") UberHourly hourly,
	@JsonProperty("currency_code") String currencyCode,
	@JsonProperty("expires_at") Long expiresAt,
	@JsonProperty("fare_breakdown") List<UberFareBreakdown> fareBreakdown,
	@JsonProperty("fare_id") String fareId
) {}
