package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record UberMasterZone(
    @JsonProperty("id") String id,
    @JsonProperty("name") String name,
    @JsonProperty("address_line1") String addressLine1,
    @JsonProperty("categories") List<String> categories,
    @JsonProperty("polygon") String polygon,
    @JsonProperty("access_points") List<UberAccessPoint> accessPoints,
    @JsonProperty("sub_zones") List<UberSubZone> subZones,
    @JsonProperty("latitude") Double latitude,
    @JsonProperty("longitude") Double longitude
) {}

