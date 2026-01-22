package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record Place(
    @JsonProperty("place_id") String placeId,
    @JsonProperty("provider") String provider
) {}