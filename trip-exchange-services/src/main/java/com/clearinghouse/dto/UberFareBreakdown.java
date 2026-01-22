package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record UberFareBreakdown(
    @JsonProperty("name") String name,
    @JsonProperty("type") String type,
    @JsonProperty("value") Double value
) {}

