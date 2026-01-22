package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UberDriverLocation(
    @JsonProperty("bearing") Double bearing,
    @JsonProperty("latitude") Double latitude,
    @JsonProperty("longitude") Double longitude
) {}