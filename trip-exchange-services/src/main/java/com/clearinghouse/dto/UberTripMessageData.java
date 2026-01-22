package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UberTripMessageData(
    @JsonProperty("sender") String sender,
    @JsonProperty("message") String message
) {}