package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record UberHourly(
    @JsonProperty("tiers") List<UberTier> tiers,
    @JsonProperty("overage_rates") UberOverageRates overageRates
) {}

