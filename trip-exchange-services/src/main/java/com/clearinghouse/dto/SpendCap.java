package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record SpendCap(
    @JsonProperty("trip_creation_status") String tripCreationStatus,
    @JsonProperty("on_trip_status") String onTripStatus
) {}