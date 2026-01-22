package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record UberCancellation(
    @JsonProperty("min_cancellation_fee") Integer minCancellationFee,
    @JsonProperty("cancellation_grace_period_threshold_sec") Integer cancellationGracePeriodThresholdSec
) {}
