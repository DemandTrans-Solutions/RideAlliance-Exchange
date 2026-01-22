package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UberErrorResponseMetadata(
    @JsonProperty("statusCode") String statusCode
) {}