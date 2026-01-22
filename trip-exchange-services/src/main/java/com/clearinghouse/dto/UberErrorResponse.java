package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UberErrorResponse(
    @JsonProperty("statusCode") String statusCode,
    @JsonProperty("code") String code,
    @JsonProperty("message") String message,
    @JsonProperty("metadata") UberErrorResponseMetadata metadata
) {}
