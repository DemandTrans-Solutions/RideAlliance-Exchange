package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record Driver(
    @JsonProperty("id") String id,
    @JsonProperty("name") String name,
    @JsonProperty("phone_number") String phoneNumber,
    @JsonProperty("sms_number") String smsNumber,
    @JsonProperty("picture_url") String pictureUrl,
    @JsonProperty("rating") Double rating,
    @JsonProperty("regulatory_info") String regulatoryInfo,
    @JsonProperty("pin_based_communication") PinBasedCommunication pinBasedCommunication
) {}