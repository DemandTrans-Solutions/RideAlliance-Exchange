package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record UberProduct(
	@JsonProperty("capacity") Integer capacity,
	@JsonProperty("description") String description,
	@JsonProperty("image") String image,
	@JsonProperty("shared") Boolean shared,
	@JsonProperty("cancellation") UberCancellation cancellation,
	@JsonProperty("advance_booking_type") String advanceBookingType,
	@JsonProperty("background_image") String backgroundImage,
	@JsonProperty("display_name") String displayName,
	@JsonProperty("parent_product_type_id") String parentProductTypeId,
	@JsonProperty("product_group") String productGroup,
	@JsonProperty("product_id") String productId,
	@JsonProperty("scheduling_enabled") Boolean schedulingEnabled,
	@JsonProperty("short_description") String shortDescription,
	@JsonProperty("upfront_fare_enabled") Boolean upfrontFareEnabled,
	@JsonProperty("vehicle_view_id") Integer vehicleViewId,
	@JsonProperty("reserve_info") UberReserveInfo reserveInfo
) {}
