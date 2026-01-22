package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record FlexibleReturnTrip(
    @JsonProperty("deferred_ride_options") DeferredRideOptions deferredRideOptions,
    @JsonProperty("start_location") CoordinatesWithPlace startLocation,
    @JsonProperty("end_location") CoordinatesWithPlace endLocation,
    @JsonProperty("product_id") String productId,
    @JsonProperty("fare_id") String fareId,
    @JsonProperty("note_for_driver") String noteForDriver
) {}