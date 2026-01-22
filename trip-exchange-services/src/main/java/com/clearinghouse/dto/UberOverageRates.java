package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record UberOverageRates(
    @JsonProperty("overage_rate_per_temporal_unit") Double overageRatePerTemporalUnit,
    @JsonProperty("overage_rate_per_distance_unit") Double overageRatePerDistanceUnit,
    @JsonProperty("temporal_unit") String temporalUnit,
    @JsonProperty("distance_unit") String distanceUnit,
    @JsonProperty("pricing_explainer") String pricingExplainer
) {}

