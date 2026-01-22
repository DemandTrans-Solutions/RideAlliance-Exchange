package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record UberLocation(
    @JsonAlias({"latitude", "lat"})
    @JsonProperty("latitude") Double latitude,

    @JsonAlias({"longitude", "lng"})
    @JsonProperty("longitude") Double longitude
) {}
