package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UberRideOptionDTO {
    private String uberRideOptionId;
    private String fareId;
    private String productId;
    private String fareDisplay;
    private UberRideType uberRideType;
    private String imageUrl;
    private double fullPrice;
    private double price;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant estimatedPickupTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant estimatedDropoffTime;
    private double distance;
    private long etaMinutes;
    private double surgeMultiplier;



}
