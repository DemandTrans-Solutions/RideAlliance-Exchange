package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record Airline(
    @JsonProperty("code") String code,
    @JsonProperty("name") String name,
    @JsonProperty("status") String status
) {}