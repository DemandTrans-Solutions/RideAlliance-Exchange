package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record UberGetRequestZonesResponse(
    @JsonProperty("bounding_box") UberBoundingBox boundingBox,
    @JsonProperty("master_zone") UberMasterZone masterZone,
    @JsonProperty("access_points") List<UberAccessPoint> accessPoints
) {}
