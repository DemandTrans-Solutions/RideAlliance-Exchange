package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record EditableField(
    @JsonProperty("editable") Boolean editable,
    @JsonProperty("max_radius_meters") Integer maxRadiusMeters
) {}