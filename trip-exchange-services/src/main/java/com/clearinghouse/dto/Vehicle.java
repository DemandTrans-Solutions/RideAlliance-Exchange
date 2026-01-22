package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record Vehicle(
    @JsonProperty("make") String make,
    @JsonProperty("model") String model,
    @JsonProperty("vehicle_color_name") String vehicleColorName,
    @JsonProperty("license_plate") String licensePlate,
    @JsonProperty("picture_url") String pictureUrl
) {}