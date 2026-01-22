/**
 * Generated from api-docs.json DetailedTripTicketDTO schema
 * Minimal TypeScript interfaces matching the API contract.
 * Fields use the same keys as the backend (snake_case) to make mapping straightforward.
 */
import { AddressDTO } from './address.dto';

export interface StatusDTO {
	statusId?: number;
	type?: string;
	description?: string;
}



export interface ProviderDTO {
	providerId?: number;
	providerName?: string;
	contactEmail?: string;
	apiKey?: string;
	privateKey?: string;
	tripTicketExpirationDaysBefore?: number;
	tripTicketExpirationTimeOfDay?: string;
	tripTicketProvisionalTime?: string;
	providerAddress?: AddressDTO;
	providerTypeId?: number;
	providersType?: string;
	lastSyncDateTime?: string;
}

export interface ClaimantTripTicketDTO {
	id?: number;
	claimantProviderId?: number;
	claimantTripId?: string;
	claimantProvider?: number;
	trip_ticket?: number;
	trip_ticketId?: number;
}

export interface TripClaimDTO {
	id?: number;
	status?: StatusDTO;
	notes?: string;
	version?: number;
	ackStatus?: boolean;
	ackStatusString?: string;
	calculatedProposedFare?: number;
	overridePriceMismatch?: boolean;
	newRecord?: boolean;
	claimant_provider_id?: number;
	claimant_provider_name?: string;
	claimant_service_id?: number;
	claimant_trip_ticket_id?: string;
	trip_ticket_id?: number;
	proposed_pickup_time?: string;
	proposed_fare?: number;
	expiration_date?: string;
	is_expired?: boolean;
	requester_fare?: number;
	created_at?: string;
	updated_at?: string;
  formattedDate?: string;
  formattedTime?: string;
}

export interface TripResultDTO {
	id?: number;
	tripTicketId?: number;
	isNoShowFlag?: boolean;
	tripDate?: string;
	actualPickupArriveTime?: string;
	actualPickupDepartTime?: string;
	actualDropOffArriveTime?: string;
	actualDropOffDepartTime?: string;
	pickUpLatitude?: number;
	pickupLongitude?: number;
	dropOffLatitude?: number;
	dropOffLongitude?: number;
	fareCollected?: number;
	vehicleId?: string;
	driverId?: string;
	claimantProvider?: string;
	pickUpAddress?: string;
	dropOffAddress?: string;
	scheduledPickupTime?: string;
	scheduledDropOffTime?: string;
	numberOfGuests?: number;
	numberOfAttendants?: number;
	numberOfPassengers?: number;
	orgProviderId?: number;
	rate?: number;
	fare?: number;
	notes?: string;
	outcome?: string;
	version?: number;
	trip_claim_id?: number;
	driver_name?: string;
	rate_type?: string;
	vehicle_type?: string;
	vehicle_name?: string;
	fare_type?: string;
	base_fare?: number;
	miles_traveled?: number;
	odometer_start?: number;
	odometer_end?: number;
	billable_mileage?: number;
	extra_securement_count?: number;
	cancellation_reason?: string;
	no_show_reason?: string;
	created_at?: string;
	updated_at?: string;
}

export interface TripTicketCommentDTO {
	id?: number;
	body?: string;
	trip_ticket_id?: number;
	user_id?: number;
	user_name?: string;
	name_of_provider?: string;
	created_at?: string;
	updated_at?: string;
}

export interface DetailedTripTicketDTO {
	id?: number;

	boarding_time?: number;
	deboarding_time?: number;
	attendants?: number;
	guests?: number;
	purpose?: string;
	serviceLevel?: string;
	status?: StatusDTO;
	version?: string;
	isEligibleForClaim?: boolean;
	tripFundersList?: string[];
	isTripCancel?: boolean;
	originator?: ProviderDTO;
	claimant?: ProviderDTO;
	claimantTripTickets?: ClaimantTripTicketDTO[];
	newRecord?: boolean;
	eligibleForClaim?: boolean;
	expired?: boolean;
	tripCancel?: boolean;
	origin_provider_id?: number;
	origin_customer_id?: string;
	requester_trip_id?: string;
	common_trip_id?: string;
	approved_trip_claim_id?: number;
  customer_email?: string;
	customer_address?: AddressDTO;
	customer_internal_id?: number;
	customer_first_name: string;
	customer_middle_name?: string;
	customer_nick_name?: string;
	customer_last_name: string;
	customer_home_phone?: string;
	customer_emergency_phone?: string;
	customer_emergency_contact_name?: string;
	customer_emergency_contact_phone?: string;
	customer_mobile_phone?: string;
	customer_dob?: string;
	customer_gender?: string;
	customer_race?: string;
	customer_ethnicity?: string;
	impairment_description?: string;
	customer_information_withheld?: boolean;
	primary_language?: string;
	customer_mailing_billing_address?: string;
	customer_caregiver_name?: string;
	customer_caregiver_contact_info?: string;
	customer_emergency_contact_relationship?: string;
	customer_care_info?: string;
	customer_funding_billing_information?: string;
	funding_type?: string;
	customer_notes?: string;
	customer_seats_required?: number;
	pickup_address?: AddressDTO;
	drop_off_address: AddressDTO;
	scheduling_priority?: string;
	trip_notes?: string;
	customer_identifiers?: string;
	customer_eligibility_factors?: string;
	customer_mobility_factors?: string;
	customer_service_animals?: boolean;
	trip_funders?: string;
	customer_assistance_needs?: string;
	attendant_mobility_factors?: string;
	customer_disability?: string;
	customer_veteran?: boolean;
	customer_poverty_level?: string;
	guest_mobility_factors?: string;
	requested_pickup_date?: string;
	requested_pickup_time?: string;
	requested_dropoff_date?: string;
	requested_dropoff_time?: string;
	earliest_pickup_time?: string;
	appointment_time?: string;
	customerLoadTime?: string;
	customerUnloadTime?: string;
	estimated_trip_travel_time?: number;
	estimated_trip_distance?: number;
	is_trip_isolation?: boolean;
	is_outside_coreHours?: boolean;
	time_window_before?: number;
	time_window_after?: number;
	provider_white_list?: string;
	provider_black_list?: string;
	last_status_changed_by_providerId?: number;
	trip_ticket_provisional_time?: string;
	provisional_provider_id?: number;
	expiration_date?: string;
	is_expired?: boolean;
	customer_custom_fields?: string;
	trip_custom_fields?: string;
	trip_purpose?: string;
	vehicle_type?: string;
	customerStatusForDuplication?: string;
	isTripTicketInvisible?: boolean;
	created_at?: string;
	updated_at?: string;
	trip_Claims?: TripClaimDTO[];
	trip_result?: TripResultDTO;
	trip_ticket_comments?: TripTicketCommentDTO[];
}

// DetailedTripTicketDTO defined above (named export via interface)


// Extended interface for UI-enhanced trip tickets
export interface TripTicketWithUIProperties extends DetailedTripTicketDTO {
  // UI-specific computed properties
  _customerName?: string;
  _passenger?: string;
  _status?: string;
  _requested_pickupDateTime?: string;
  _requested_dropOffDateTime?: string;
  _requested_pickup_date?: string;
  _requested_pickup_time?: string;
  _requested_dropoff_date?: string;
  _requested_dropoff_time?: string;
  _mobility_device?: string;
  _attendants?: string;


  // UI state properties
  myTicket?: boolean;
  selected?: boolean;
  claimantProviders?: string;
}
