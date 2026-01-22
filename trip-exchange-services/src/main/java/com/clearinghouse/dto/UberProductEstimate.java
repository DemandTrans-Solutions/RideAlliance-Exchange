package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record UberProductEstimate(
    @JsonProperty("fare") UberFare fare,
    @JsonProperty("product") UberProduct product,
    @JsonProperty("estimate_info") UberEstimateInfo estimateInfo,
    @JsonProperty("fare_id") String fareId,
    @JsonProperty("fulfillment_indicator") String fulfillmentIndicator
) {}
