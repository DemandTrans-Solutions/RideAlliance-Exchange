package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UberDriverLocationData(
    @JsonProperty("location") UberDriverLocation location
) {}