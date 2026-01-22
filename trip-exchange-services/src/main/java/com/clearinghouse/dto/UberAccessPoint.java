package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record UberAccessPoint(
    @JsonProperty("id") String id,
    @JsonProperty("latitude") Double latitude,
    @JsonProperty("longitude") Double longitude,
    @JsonProperty("label") String label,
    @JsonProperty("rider_wayfinding_note") String riderWayfindingNote,
    @JsonProperty("invalid_product_ids") List<String> invalidProductIds
) {}

