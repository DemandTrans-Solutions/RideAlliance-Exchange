package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record VehicleLocation(
    @JsonProperty("bearing") Double bearing,
    @JsonProperty("latitude") Double latitude,
    @JsonProperty("longitude") Double longitude
) {}