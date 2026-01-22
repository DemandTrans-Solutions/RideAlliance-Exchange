export interface CompletedRange {
  fromDate: string;
  toDate: string;
  providerId: number;
  reportTicketFilterStatus: string[];
  partnerProviderTicket?: boolean;
  myTicket?: boolean;
}

export interface TripCompletedReport {
  id: number;
  ticket_number: string;
  origin_provider_name: string;
  customer_first_name: string;
  customer_last_name: string;
  requested_pickup_date?: string;
  requested_pickup_time?: string;
  requested_dropoff_date?: string;
  requested_dropoff_time?: string;
  pickup_address: {
    street1: string;
    city: string;
    state: string;
    zipcode: string;
  };
  drop_off_address: {
    street1: string;
    city: string;
    state: string;
    zipcode: string;
  };
  trip_Claims?: any[];
  created_at?: string;
  attendant_mobility_factors?: string;
  guest_mobility_factors?: string;
  guests?: number;
  customer_service_animals?: string;
  customer_seats_required?: string;

  // Computed properties that will be added by component
  _customerName?: string;
  claimantProviders?: string;
  pickup?: string;
  dropoff?: string;
  _requested_pickup_time?: string;
  _requested_dropOff_time?: string;
  pickupDateTime?: string;
  dropoffDateTime?: string;
  submittedDateTime?: string;
  tripDetails?: string;
}

export interface ShowTicketOption {
  value: number;
  label: string;
}
