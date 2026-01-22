package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UberCreateGuestTripResponse(
    @JsonProperty("eta") Long eta,
    @JsonProperty("request_id") String requestId,
    @JsonProperty("product_id") String productId,
    @JsonProperty("status") String status,
    @JsonProperty("surge_multiplier") Double surgeMultiplier,
    @JsonProperty("guest") UberGuest guest
) {}
