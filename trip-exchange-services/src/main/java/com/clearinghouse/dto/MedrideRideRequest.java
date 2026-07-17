package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record MedrideRideRequest(
    @JsonProperty(value="trip_ticket_id",access = JsonProperty.Access.WRITE_ONLY) Integer tripTicketId,
    @JsonProperty("pickup_time") Long pickupTime,
    @JsonProperty("dropoff_time") Long dropoffTime
) {}
