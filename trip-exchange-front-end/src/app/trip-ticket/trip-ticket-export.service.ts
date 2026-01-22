import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import {TripTicketExportData} from '../models/trip-export-data.dto';



@Injectable({
  providedIn: 'root'
})
export class TripTicketExportService {

  constructor() { }

  // Header list for Trip Exchange Excel template (first sheet)
  private static readonly TRIP_EXCHANGE_HEADERS: string[] = [
    'Case Number',
    'Actual Provider',
    'Trip Name',
    'Date & Time Of Trip',
    'Reason(s) for Trip',
    'Mobility Device',
    'Level of Service',
    'Origin Street',
    'Origin City',
    'Origin State',
    'Origin ZIP',
    'Final Stop Street',
    'Final Stop City',
    'Final Stop State',
    'Final Stop ZIP',
    'First Name*',
    'Nickname',
    'Middle Name',
    'Last Name*',
    'Date Of Birth*',
    'Gender*',
    'Poverty Level*',
    'Disability Type',
    'Primary Language',
    'Race*',
    'Ethnicity*',
    'Email',
    'Veteran Status*',
    'Home Address Line 1',
    'Home Phone',
    'Cell Phone*',
    'Billing Address',
    'Caregiver Details',
    'Primary Emergency Contact Name',
    'Primary Emergency Contact Phone Number',
    'Primary Emergency Contact Relationship',
    'Vehicle Type',
    'Pick-Up/Drop-Off Time',
    'If Cancelled, why?',
    'Scheduled Start',
    'Scheduled Ends',
    'Funding Source',
    'Cost of Trip',
    'Estimated Miles',
    'Pick-Up Lat',
    'Pick-up Long',
    'Drop-Off Lat',
    'Drop-Off Long',
    'Actual pick-up arrive time',
    'Actual pick-up depart time',
    'Actual drop-off arrive time',
    'Actual drop-off depart time',
    'Status'  ,
    'No-Show Reason',
    'Number of Guests',
    'Number of Passengers',
    'Fare Collected',
    'Vehicle ID',
    'Driver ID'

  ];

  /**
   * Process data for CSV export using trip-import-completion-upload.csv format
   */
  private processCSVExportData(tripData: TripTicketExportData[]): any[][] {
    // Headers from trip-import-completion-upload.csv (45 columns)
    const headers: string[] = [
      'Trip_id',
      'Provider Name',
      'appt_date',
      'REQ_PU',
      'REQ_DO',
      'VEHICLE_REQ',
      'Purpose',
      'DEVICE',
      'PU_NOTE',
      'pu_location_name',
      'pu_street',
      'pu_city',
      'pu_state',
      'pu_county',
      'pu_zipcode',
      'lat_pu',
      'lon_pu',
      'do_location_name',
      'DO_STREET',
      'do_city',
      'do_state',
      'do_county',
      'do_zipcode',
      'lat_do',
      'lon_do',
      'CLIENT_ID',
      'CLIENT_F_NAME',
      'CLIENT_L_NAME',
      'customer_dob',
      'Gender',
      'Language',
      'Ethnicity',
      'Email address',
      'customer_phone',
      'pu_phone',
      'Fare_type',
      'FUNDING_SOURCE',
      'Status',
      'No-Show Reason',
      'num_attendant',
      'Fare Collected',
      'Vehicle ID',
      'Driver ID',
      'Actual pick-up arrive time',
      'Actual pick-up depart time',
      'Actual drop-off arrive time',
      'Actual drop-off depart time'
    ];

    const aoa: any[][] = [];
    aoa.push(headers);

    for (const trip of tripData) {
      const pickup = trip.pickup_address || {};
      const dropoff = trip.drop_off_address || {};

      console.log('Processing trip for CSV export:', trip);

      const row: any[] = [
        // Trip_id
        trip.requester_trip_id ?? '',
        // Provider Name
        trip.originator?.providerName ?? trip.providerName ?? '',
        // appt_date - format as M/dd/yyyy
        this.formatDateToMDYFormat(trip._requested_pickup_date ?? trip.requested_pickup_date ?? trip.requested_dropoff_date  ) ?? '',
        // REQ_PU - map requested_pickup_time
        trip._requested_pickup_time ?? trip.requested_pickup_time ?? '',
        // REQ_DO - map requested_dropoff_time
        trip._requested_dropoff_time ?? trip.requested_dropOff_time ?? trip.requested_dropoff_time ?? '',
        // VEHICLE_REQ
        trip.vehicle_required ?? trip.VEHICLE_REQ ?? '',
        // Purpose
        trip.purpose ?? trip.tripPurpose ?? 'N/A',
        // DEVICE
        trip.device ?? trip.customer_mobility_factors ?? '',
        // PU_NOTE
        trip.pickup_note ?? trip.PU_NOTE ?? trip.trip_notes ?? '',
        // pu_location_name
        pickup.commonName ?? '',
        // pu_street
        pickup.street1 ?? '',
        // pu_city
        pickup.city ?? '',
        // pu_state
        pickup.state ?? '',
        // pu_county
        pickup.county ?? '',
        // pu_zipcode
        pickup.zipcode ?? '',
        // lat_pu
        pickup.latitude ?? '',
        // lon_pu
        pickup.longitude ?? '',
        // do_location_name
        dropoff.commonName ?? '',
        // DO_STREET
        dropoff.street1 ?? '',
        // do_city
        dropoff.city ?? '',
        // do_state
        dropoff.state ?? '',
        // do_county
        dropoff.county ?? '',
        // do_zipcode
        dropoff.zipcode ?? '',
        // lat_do
        dropoff.latitude ?? '',
        // lon_do
        dropoff.longitude ?? '',
        // CLIENT_ID
        trip.customer_internal_id ?? '',
        // CLIENT_F_NAME
        trip.customer_first_name ?? '',
        // CLIENT_L_NAME
        trip.customer_last_name ?? '',
        // customer_dob - format as M/dd/yyyy
        this.formatDateToMDYFormat(trip.customer_dob) ?? '',
        // Gender - skip (not available in TripTicketExportData)
        trip.customer_gender ?? '',
        // Language - skip (not available in TripTicketExportData)
        trip.primary_language ?? '',
        // Ethnicity - skip (not available in TripTicketExportData)
        trip.customer_ethnicity ??  '',
        // Email address - skip (not available in TripTicketExportData)
        trip.customer_email ?? '',

        // customer_phone
        trip.customer_mobile_phone ?? trip.customer_home_phone ?? '',
        // pu_phone
        pickup.phoneNumber ?? trip.customer_mobile_phone ?? trip.customer_home_phone ?? '',
        // Fare_type
        trip.fare_type ?? trip.fareType ?? trip.fare ?? 'NONE',
        // FUNDING_SOURCE
        trip.funding_source ?? trip.fundingSource ?? '',
        // Status
        trip.status?.type ?? '',
        // No-Show Reason
        trip.trip_result?.no_show_reason ?? '',
        // num_attendant
        trip.num_attendant ?? trip.customer_seats_required ?? '',

        trip.trip_result?.fare ?? trip.trip_result?.fareCollected ?? '',
        // Vehicle ID
        trip.trip_result?.vehicleId ?? trip.trip_result?.vehicleName ?? '',
        // Driver ID
        trip.trip_result?.driverName ?? '',
        // Actual pick-up arrive time
        trip.trip_result?.actualPickupArriveTime ?? '',
        // Actual pick-up depart time
        trip.trip_result?.actualPickupDepartTime ?? '',
        // Actual drop-off arrive time
        trip.trip_result?.actualDropOffArriveTime ?? '',
        // Actual drop-off depart time
        trip.trip_result?.actualDropOffDepartTime ?? ''
      ];

      aoa.push(row);
    }

    return aoa;
  }

  /**
   * Process data for Excel export using trip-exchange-import-export-template.xlsx format
   * NOTE: Headers are sourced from EXCEL_TRIP_TEMPLATE_HEADERS to stay in lockstep
   * with the downloadable template and upload validation.
   */
  private processExcelExportData(tripData: TripTicketExportData[]): any[][] {
    const aoa: any[][] = [];
    aoa.push(TripTicketExportService.TRIP_EXCHANGE_HEADERS);

    for (const trip of tripData) {
      const pickup: any = trip.pickup_address || {};
      const dropoff: any = trip.drop_off_address || {};

      const pickupDate = trip._requested_pickup_date ?? trip.requested_pickup_date;
      const pickupTime = trip._requested_pickup_time ?? trip.requested_pickup_time;
      const dropTime = trip._requested_dropoff_time ?? trip.requested_dropOff_time ?? trip.requested_dropoff_time;

      const dateTimeOfTrip = this.composeDateTime(pickupDate, pickupTime) || this.composeDateTime(pickupDate, dropTime) || '';
      const puOrDoTimeOnly = pickupTime || dropTime || '';

      const row: any[] = [
        trip.requester_trip_id ?? '', // Case Number
        trip.originator?.providerName ?? trip.providerName ?? '', // Actual Provider
        trip.requester_trip_id ?? '', // trip number
        dateTimeOfTrip, // Date & Time Of Trip
        trip.tripPurpose ?? trip.purpose ?? '', // Reason(s) for Trip
        trip.device ?? trip.customer_mobility_factors ?? '', // Mobility Device
        trip.customer_mobility_factors ?? trip.device ?? '', // Level of Service
        pickup.street1 ?? '', // Origin Street
        pickup.city ?? '', // Origin City
        pickup.state ?? '', // Origin State
        pickup.zipcode ?? '', // Origin ZIP
        dropoff.street1 ?? '', // Final Stop Street
        dropoff.city ?? '', // Final Stop City
        dropoff.state ?? '', // Final Stop State
        dropoff.zipcode ?? '', // Final Stop ZIP
        trip.customer_first_name ?? '', // First Name*
        trip.customer_nick_name, // Nickname
        trip.customer_middle_initial ?? trip.customer_mi ?? '', // Middle Name
        trip.customer_last_name ?? '', // Last Name*
        this.formatDateToMDYFormat(trip.customer_dob) ?? '', // Date Of Birth*
        trip.customer_gender ?? '', // Gender*
        trip.customer_poverty_level ?? '', // Poverty Level*
        trip.customer_disability ?? '', // Disability Type
        trip.primary_language ?? '', // Primary Language
        trip.customer_race ?? '', // Race*
        trip.customer_ethnicity ?? '', // Ethnicity*
        trip.customer_email ?? '', // Email
        trip.customer_veteran ??'', // Veteran Status*
        trip.customer_address && trip.customer_address.street1 ? trip.customer_address.street1 : '', // Home Address Line 1
        trip.customer_home_phone ?? trip.pu_phone ?? pickup.phoneNumber ?? '', // Home Phone
        trip.customer_mobile_phone ?? trip.pu_phone ?? '', // Cell Phone*
        trip.customer_mailing_billing_address ?? '', // Billing Address
        trip.customer_caregiver_name ?? '', // Caregiver Details
        trip.customer_emergency_contact_name ?? '', // Primary Emergency Contact Name
        trip.customer_emergency_contact_phone ?? '', // Primary Emergency Contact Phone Number
        trip.customer_emergency_contact_relationship ?? '', // Primary Emergency Contact Relationship
        trip.vehicle_required ?? trip.VEHICLE_REQ ?? '', // Vehicle Type
        puOrDoTimeOnly ?? '', // Pick-Up/Drop-Off Time
        trip.trip_result?.cancellation_reason ?? '', // If Cancelled, why?
        pickupTime ?? '', // Scheduled Start
        dropTime ?? '', // Scheduled Ends
        trip.funding_source ?? trip.fundingSource ?? '', // Funding Source
        trip.trip_result?.fare ?? trip.trip_result?.fareCollected ?? trip.fare ?? '', // Cost of Trip
        trip.estimated_trip_distance ?? '', // Estimated Miles
        pickup.latitude ?? '', // Pick-Up Lat
        pickup.longitude ?? '', // Pick-up Long
        dropoff.latitude ?? '', // Drop-Off Lat
        dropoff.longitude ?? '', // Drop-Off Long
        trip.trip_result?.actualPickupArriveTime ?? '',
        // Actual pick-up depart time
        trip.trip_result?.actualPickupDepartTime ?? '',
        // Actual drop-off arrive time
        trip.trip_result?.actualDropOffArriveTime ?? '',
        // Actual drop-off depart time
        trip.trip_result?.actualDropOffDepartTime ?? '',
        trip.status?.type ?? '',

        // 'No-Show Reason'
        trip.trip_result?.no_show_reason ?? '',

        // 'Number of Guests'
        trip.num_attendant ?? '',

        // 'Number of Passengers'
        trip.num_attendant ?? trip.customer_seats_required ?? '',

        // 'Fare Collected'
        trip.trip_result?.fare ?? trip.trip_result?.fareCollected ?? trip.fare ?? '', // Cost of Trip
        // 'Vehicle ID'
        trip.trip_result?.vehicleId ?? trip.trip_result?.vehicleName ?? '',

        // Driver ID
        trip.trip_result?.driverName ?? '',



      ];
      //
      aoa.push(row);
    }

    return aoa;
  }


  /**
   * Format date from yyyy-MM-dd to M/dd/yyyy
   */
  private formatDateToMDYFormat(dateString?: string): string {
    if (!dateString) return '';

    try {
      // Parse yyyy-MM-dd manually to avoid timezone issues
      const parts = dateString.split('-');
      if (parts.length !== 3) return dateString;
      const year = parseInt(parts[0], 10);
      const month = parseInt(parts[1], 10);
      const day = parseInt(parts[2], 10);
      if (isNaN(year) || isNaN(month) || isNaN(day)) return dateString;

      return `${month}/${day}/${year}`;
    } catch (error) {
      console.warn('Error formatting date:', dateString, error);
      return dateString; // Return original string if formatting fails
    }
  }

  /**
   * Extract leading street number from a street string.
   * Examples:
   *  - '123 Main St' => '123'
   *  - 'PO Box 123' => '' (no street number)
   */
  private extractStreetNumber(street?: string): string {
    if (!street) return '';
    // Trim and collapse whitespace
    const s = street.trim();
    // Match a leading number (optionally with letter suffix, e.g., 123A)
    const m = s.match(/^\s*(\d+[A-Za-z]?)(?:\s+|$)/);
    if (m && m[1]) return m[1];
    return '';
  }

  /**
   * Extract the street name (without leading number) from a street string.
   * Examples:
   *  - '123 Main St' => 'Main St'
   *  - 'PO Box 123' => 'PO Box 123' (preserve when no leading number)
   */
  private extractStreetName(street?: string): string {
    if (!street) return '';
    const s = street.trim();
    // Remove leading number and optional letter suffix
    const withoutNumber = s.replace(/^\s*\d+[A-Za-z]?\s*/, '');
    return withoutNumber;
  }

  /** Compose "M/dd/yyyy hh:mm A" from separate date and time strings if possible */
  private composeDateTime(dateStr?: string, timeStr?: string): string {
    const datePart = this.formatDateToMDYFormat(dateStr);
    if (!datePart) return '';
    const timePart = timeStr ?? '';
    return timePart ? `${datePart} ${timePart}` : datePart;
  }

  /**
   * Generate Excel file from trip data using trip-exchange-import-export-template.xlsx format
   */
  generateExcelFile(tripData: TripTicketExportData[]): Blob {
    try {
      const aoa = this.processExcelExportData(tripData);

      const ws = XLSX.utils.aoa_to_sheet(aoa);
      const wb = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(wb, ws, 'Trip Exchange Template');

      // Generate Excel file as blob
      const excelBuffer = XLSX.write(wb, { bookType: 'xlsx', type: 'array' });
      return new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    } catch (error) {
      console.error('Excel generation error:', error);
      throw new Error('Failed to generate Excel file');
    }
  }

  /**
   * Generate CSV file from trip data using trip-import-completion-upload.csv format
   */
  generateCSVFile(tripData: TripTicketExportData[]): Blob {
    try {
      const aoa = this.processCSVExportData(tripData);

      // Convert array of arrays to CSV string
      const csvContent = aoa.map(row =>
        row.map(field => {
          // Handle fields that might contain commas or quotes
          const stringField = String(field ?? '');
          if (stringField.includes(',') || stringField.includes('"') || stringField.includes('\n')) {
            // Escape quotes by doubling them and wrap in quotes
            return `"${stringField.replace(/"/g, '""')}"`;
          }
          return stringField;
        }).join(',')
      ).join('\n');

      return new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    } catch (error) {
      console.error('CSV generation error:', error);
      throw new Error('Failed to generate CSV file');
    }
  }

  /**
   * Download a blob as a file
   */
  downloadFile(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }

  /**
   * Generate timestamp for filename
   */
  generateTimestamp(): string {
    return new Date().toISOString().slice(0, 19).replace(/:/g, '-');
  }
}
