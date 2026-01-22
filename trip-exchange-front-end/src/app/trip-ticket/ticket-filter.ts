// Updated ticket-filter.ts for Angular 18
// This file includes all necessary interfaces and classes for the trip-ticket component

export interface FilterOption {
  value: string;
  label: string;
  toString(): string;
}

// Extended FilterOption class with toString implementation
export class ExtendedFilterOption implements FilterOption {
  value: string;
  label: string;

  constructor(value: string, label: string) {
    this.value = value;
    this.label = label;
  }

  toString(): string {
    return this.value;
  }
}

export interface FilterParameter {
  name: string;
  value: any;
}

export interface TimeRange {
  start: string;
  end: string;
  toString(): string;
}

// Extended TimeRange class with toString implementation
export class ExtendedTimeRange implements TimeRange {
  start: string;
  end: string;

  constructor(start: string, end: string) {
    this.start = start;
    this.end = end;
  }

  toString(): string {
    return `${this.start}-${this.end}`;
  }
}

export class TicketFilter {
  ticketFilterstatus: FilterOption[] = [];
  claimingProviderName: FilterOption[] = [];
  originatingProviderName: FilterOption[] = [];
  tripTime: FilterParameter[] = [];
  operatingHours: FilterParameter[] = [];
  advancedFilterParameter: FilterParameter[] = [];
  filterId: string = '';
  filterName: string = '';
  isActive: boolean = true;
  isServiceFilterApply: boolean = false;
  rescindedApplyStatusParameter: string = '';
  schedulingPriority: string = '';
  userId: number = 0;
  customer_identifiers: string = '';
  seatsRequiredMin: number = 0;
  seatsRequiredMax: number = 0;
  fundingSourceList: FilterOption[] = [];
  customerEligibility: FilterOption[] = [];
  selectedGeographicVal: FilterOption[] = [];
  reqPickUpStartAndEndTime: TimeRange[] = [];
  hospitalityServiceArea: FilterOption[] = [];
}

export class AdvanceFilter {
  customer_first_name: string = '';
  customer_address_addressId: any = null;
  pickup_address_addressId: any = null;
  drop_off_address_addressId: any = null;
  customer_identifiers: string = '';
  isServiceFilterApply: boolean = false;
  pickup_date?: string;
  pickup_time?: string;
  drop_off_date?: string;
  drop_off_time?: string;
  customer_last_name?: string;
  [key: string]: any;
}

export class TripTime {
  [key: string]: any; // Add index signature to fix template access
  pickUpDateTime: Date | null = null;
  dropOffDateTime: Date | null = null;
  pickupDateFrom: Date | null = null;
  pickupDateTo: Date | null = null;
  customerFirstName: string = '';
  seatsRequiredMin: number = 0;
  seatsRequiredMax: number = 0;
}

export class Range {
  fromDate: string = '';
  toDate: string = '';
  providerId: number = 0;
  reportTicketFilterStatus: string[] = [];
}

export class SummaryReport {
  [key: string]: any;
}

export interface Address {
  addressId: number;
  street1?: string;
  street2?: string;
  city?: string;
  state?: string;
  zipcode?: string;
  latitude?: string;
  longitude?: string;
  address?: string;
}
