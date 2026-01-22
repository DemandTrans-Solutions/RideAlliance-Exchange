package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record EditableFields(
    @JsonProperty("additional_properties") Map<String, EditableField> additionalProperties
) {}