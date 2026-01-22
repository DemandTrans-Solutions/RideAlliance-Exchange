package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record TripSummary(
    // Base trip properties
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
    @JsonProperty("return_trip_params") ReturnTrip returnTripParams,
    @JsonProperty("stops") List<Stop> stops,
    
    // Trip status and identification
    @JsonProperty("request_id") String requestId,
    @JsonProperty("status") String status,
    @JsonProperty("request_time") Long requestTime,
    @JsonProperty("rider_tracking_url") String riderTrackingUrl,
    @JsonProperty("surge_multiplier") Double surgeMultiplier,
    @JsonProperty("requester_uuid") String requesterUuid,
    @JsonProperty("location_uuid") String locationUuid,
    
    // Fare and cost information
    @JsonProperty("currency_code") String currencyCode,
    @JsonProperty("client_fare") String clientFare,
    @JsonProperty("client_fare_numeric") Double clientFareNumeric,
    @JsonProperty("client_fare_without_tip") String clientFareWithoutTip,
    @JsonProperty("can_tip") Boolean canTip,
    
    // Trip metrics
    @JsonProperty("trip_distance_miles") Double tripDistanceMiles,
    @JsonProperty("trip_duration_seconds") Long tripDurationSeconds,
    @JsonProperty("trip_leg_number") Integer tripLegNumber,
    @JsonProperty("total_trip_legs") Integer totalTripLegs,
    
    // Optional trip details
    @JsonProperty("final_destination") CoordinatesWithPlace finalDestination,
    @JsonProperty("status_detail") String statusDetail,
    @JsonProperty("editable_fields") EditableFields editableFields,
    @JsonProperty("vehicle_location") VehicleLocation vehicleLocation,
    @JsonProperty("driver") Driver driver,
    @JsonProperty("vehicle") Vehicle vehicle,
    @JsonProperty("begin_trip_time") Long beginTripTime,
    @JsonProperty("dropoff_time") Long dropoffTime,
    @JsonProperty("spend_cap") SpendCap spendCap,
    @JsonProperty("scheduling_details") SchedulingDetails schedulingDetails,
    @JsonProperty("follow_up_trip") FollowUpTrip followUpTrip,
    @JsonProperty("flight_details") FlightDetails flightDetails
) {}
