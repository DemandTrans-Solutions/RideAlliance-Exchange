package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record UberBoundingBox(
    @JsonProperty("northeast_lat") Double northeastLat,
    @JsonProperty("northeast_lng") Double northeastLng,
    @JsonProperty("southwest_lat") Double southwestLat,
    @JsonProperty("southwest_lng") Double southwestLng
) {}
