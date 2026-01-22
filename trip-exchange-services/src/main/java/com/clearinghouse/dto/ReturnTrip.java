package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record ReturnTrip(
    @JsonProperty("pickup_time") @JsonAlias({"pickup_time","pickupTime"}) Long pickupTime,
    @JsonProperty("pickup") CoordinatesWithPlace pickup,
    @JsonProperty("dropoff") CoordinatesWithPlace dropoff,
    @JsonProperty("product_id") String productId,
    @JsonProperty("fare_id") String fareId,
    @JsonProperty("note_for_driver") String noteForDriver
) {}