package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record PinBasedCommunication(
    @JsonProperty("phone_number") String phoneNumber,
    @JsonProperty("pin") String pin
) {}