package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record SchedulingDetails(
    @JsonProperty("pickup_time") @JsonAlias({"pickup_time","pickupTime"}) Long pickupTime,
    @JsonProperty("dropoff_time") @JsonAlias({"dropoff_time","dropoffTime"}) Long dropoffTime
) {}