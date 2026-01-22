export class UberRequest {
  tripTicketId: number = 0;
  pickupLatitude: number = 0;
  pickupLongitude: number = 0;
  dropoffLatitude: number = 0;
  dropoffLongitude: number = 0;
  promisedDropOff: boolean = false;
  requestedPickupTime: Date | null = null;
}


export class UberRideOption {
  uberRideOptionId: string = '';
  imageUrl?: string;
  fullPrice: number = 0;
  price: number = 0;
  estimatedPickupTime: Date | null = null;
  estimatedDropoffTime: Date | null = null;
  uberRideType: UberRideType | null = null;
  etaMinutes: number = 0;
  fareId: string = '';
  productId: string = '';
  fareDisplay: string = ''
  distance: number = 0; // in kilometers
  surgeMultiplier: number = 1.0; // Default to 1.0 if
}

export class UberRideType {
  uberRideTypeId: string = '';
  displayName: string = '';
  capacity: number = 0;
  mobilityOptions: string[] = []
}


export class UberResponse {
  rideOptions: UberRideOption[] = [];

}

export class UberBookingResponse {
  uberConfirmationId: string = '';
}


export class UberCancellationResponse {
  result: boolean = false;
}

// TypeScript interfaces matching Java record definitions for the bookUberOption endpoint

export interface UberGuest {
  guest_id?: string;
  first_name: string;
  last_name: string;
  phone_number?: string;
  email?: string;
  locale?: string;
}

export interface Place {
  place_id?: string;
  name?: string;
  address?: string;
}

export interface CoordinatesWithPlace {
  latitude: number;
  longitude: number;
  place?: Place | null;
}

export interface ContactToNotify {
  // Add properties as needed - not specified in the provided Java records
}

export interface Scheduling {
  pickup_time: number; // UNIX timestamp
  dropoff_time?: number | null; // UNIX timestamp
}

export interface Stop {
  // Add properties as needed - not specified in the provided Java records
}

export interface UberRideRequest {
  guest: UberGuest;
  pickup: CoordinatesWithPlace;
  dropoff: CoordinatesWithPlace;
  note_for_driver?: string;
  additional_guests?: UberGuest[] | null;
  communication_channel: string;
  product_id: string;
  fare_id: string;
  trip_ticket_id: number;
  policy_uuid?: string;
  expense_code?: string;
  expense_memo?: string;
  sender_display_name: string;
  call_enabled: boolean;
  contacts_to_notify?: ContactToNotify[];
  return_trip_params?: any; // Object type to allow ScheduledReturnTrip or FlexibleReturnTrip
  stops?: Stop[];
  scheduling: Scheduling;
  uber_fare: number;
}

// Ride Status interfaces matching the Java TripSummary record structure

export interface VehicleLocation {
  bearing?: number;
  latitude: number;
  longitude: number;
}

export interface Vehicle {
  make?: string;
  model?: string;
  vehicle_color_name?: string;
  license_plate?: string;
  picture_url?: string;
}

export interface PinBasedCommunication {
  // Add properties if needed based on actual Java implementation
}

export interface Driver {
  id?: string;
  name?: string;
  phone_number?: string;
  sms_number?: string;
  picture_url?: string;
  rating?: number;
  regulatory_info?: string;
  pin_based_communication?: PinBasedCommunication;
}

export interface ReturnTrip {
  // Add properties if needed based on actual Java implementation
}

export interface EditableFields {
  // Add properties if needed based on actual Java implementation
}

export interface SpendCap {
  // Add properties if needed based on actual Java implementation
}

export interface SchedulingDetails {
  // Add properties if needed based on actual Java implementation
}

export interface FollowUpTrip {
  // Add properties if needed based on actual Java implementation
}

export interface FlightDetails {
  // Add properties if needed based on actual Java implementation
}

export interface TripSummary {
  // Base trip properties
  guest?: UberGuest;
  pickup?: CoordinatesWithPlace;
  dropoff?: CoordinatesWithPlace;
  note_for_driver?: string;
  additional_guests?: UberGuest[];
  communication_channel?: string;
  product_id?: string;
  fare_id?: string;
  policy_uuid?: string;
  expense_code?: string;
  expense_memo?: string;
  sender_display_name?: string;
  call_enabled?: boolean;
  contacts_to_notify?: ContactToNotify[];
  return_trip_params?: ReturnTrip;
  stops?: Stop[];

  // Trip status and identification
  request_id?: string;
  status?: string;
  request_time?: number; // UNIX timestamp
  rider_tracking_url?: string;
  surge_multiplier?: number;
  requester_uuid?: string;
  location_uuid?: string;

  // Fare and cost information
  currency_code?: string;
  client_fare?: string;
  client_fare_numeric?: number;
  client_fare_without_tip?: string;
  can_tip?: boolean;

  // Trip metrics
  trip_distance_miles?: number;
  trip_duration_seconds?: number;
  trip_leg_number?: number;
  total_trip_legs?: number;

  // Optional trip details
  final_destination?: CoordinatesWithPlace;
  status_detail?: string;
  editable_fields?: EditableFields;
  vehicle_location?: VehicleLocation;
  driver?: Driver;
  vehicle?: Vehicle;
  begin_trip_time?: number; // UNIX timestamp
  dropoff_time?: number; // UNIX timestamp
  spend_cap?: SpendCap;
  scheduling_details?: SchedulingDetails;
  follow_up_trip?: FollowUpTrip;
  flight_details?: FlightDetails;
}

export {}
