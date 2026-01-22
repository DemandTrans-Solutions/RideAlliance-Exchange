package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UberInsertTestTrip(
    @JsonProperty("request") UberOnDemandRide request,
    @JsonProperty("response") UberCreateGuestTripResponse response
) {}
