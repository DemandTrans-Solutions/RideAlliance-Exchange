package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record UberTripEstimatesResponse(
	@JsonProperty("etas_unavailable") Boolean etasUnavailable,
	@JsonProperty("fares_unavailable") Boolean faresUnavailable,
	@JsonProperty("product_estimates") List<UberProductEstimate> productEstimates
) {}
