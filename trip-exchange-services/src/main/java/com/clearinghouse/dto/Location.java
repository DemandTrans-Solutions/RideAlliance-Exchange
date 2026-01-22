package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record Location(
    @JsonAlias({"lat","latitude"})
    @JsonProperty("lat") Double lat,
    @JsonAlias({"lng","longitude"})
    @JsonProperty("lng") Double lng
) {}