package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record UberReservedRideWithFlexibleReturn(
    @JsonProperty("guest") UberGuest guest,
    @JsonProperty("pickup") CoordinatesWithPlace pickup,
    @JsonProperty("dropoff") CoordinatesWithPlace dropoff,
    @JsonProperty("note_for_driver") String noteForDriver,
    @JsonProperty("additional_guests") List<UberGuest> additionalGuests,
    @JsonProperty("communication_channel") String communicationChannel,
    @JsonProperty("product_id") String productId,
    @JsonProperty("fare_id") String fareId,
    @JsonProperty("policy_uuid") String policyUuid,
    @JsonProperty("expense_code") String expenseCode,
    @JsonProperty("expense_memo") String expenseMemo,
    @JsonProperty("sender_display_name") String senderDisplayName,
    @JsonProperty("call_enabled") Boolean callEnabled,
    @JsonProperty("contacts_to_notify") List<ContactToNotify> contactsToNotify,
    @JsonProperty("return_trip_params") @JsonAlias({"return_trip","returnTrip"}) FlexibleReturnTrip returnTripParams,
    @JsonProperty("stops") List<Stop> stops,
    @JsonProperty("scheduling") Scheduling scheduling
) {}
