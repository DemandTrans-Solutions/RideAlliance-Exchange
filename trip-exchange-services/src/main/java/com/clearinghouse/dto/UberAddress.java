package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UberAddress(
    @JsonProperty("address") String address,
    @JsonProperty("place") Place place
) {}