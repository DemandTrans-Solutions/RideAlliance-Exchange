package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UberCancellationResponse(
        @JsonProperty("result") boolean result
) {}
