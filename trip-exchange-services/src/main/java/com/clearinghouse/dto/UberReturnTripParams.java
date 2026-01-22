package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UberReturnTripParams(
    @JsonProperty("start_location") Coordinates startLocation,
    @JsonProperty("end_location") Coordinates endLocation,
    @JsonProperty("scheduling") UberScheduling scheduling,
    @JsonProperty("product_id") String productId,
    @JsonProperty("note_for_driver") String noteForDriver
) {}
