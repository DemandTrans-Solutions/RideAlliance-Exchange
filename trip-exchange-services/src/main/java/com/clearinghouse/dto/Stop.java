package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record Stop(
    @JsonProperty("coordinates") CoordinatesWithPlace coordinates,
    @JsonProperty("address") UberAddress address,
    @JsonProperty("place") Place place,
    @JsonProperty("timezone") String timezone,
    @JsonProperty("display_title") String displayTitle
) {}