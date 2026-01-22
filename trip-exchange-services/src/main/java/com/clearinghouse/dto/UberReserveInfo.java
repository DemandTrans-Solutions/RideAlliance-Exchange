package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record UberReserveInfo(
    @JsonProperty("enabled") Boolean enabled,
    @JsonProperty("scheduled_threshold_minutes") Integer scheduledThresholdMinutes,
    @JsonProperty("free_cancellation_threshold_minutes") Integer freeCancellationThresholdMinutes,
    @JsonProperty("add_ons") List<Object> addOns,
    @JsonProperty("valid_until_timestamp") Long validUntilTimestamp
) {}

