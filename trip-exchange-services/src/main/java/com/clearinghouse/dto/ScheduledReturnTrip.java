package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record ScheduledReturnTrip(
    @JsonProperty("pickup_location") Location pickupLocation,
    @JsonProperty("dropoff_location") Location dropoffLocation,
    @JsonProperty("product_id") String productId,
    @JsonProperty("fare_id") String fareId,
    @JsonProperty("note_for_driver") String noteForDriver
) {}