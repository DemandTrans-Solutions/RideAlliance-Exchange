export const TRIP_TICKET_TEMPLATE_HEADERS: string[] = [
  'External Trip Id',
  'Schedule - Run - Provider - Name',
  'Vehicle - ID',
  'Vehicle - Driver - First name',
  'Vehicle - Driver - Last name',
  'Trip date',
  'Start stop - Estimated arrival',
  'Start stop - Estimated departure',
  'Actual pick-up arrival',
  'Actual pick-up departure',
  'Pickup date',
  'End stop - Estimated arrival',
  'End stop - Estimated departure',
  'Actual drop-off arrival',
  'Actual drop-off departure',
  'Dropoff date',
  'Client - Name',
  'Device',
  'Vehicle Requirement',
  'Pick-up location - Name',
  'Pick-up location - Number',
  'Pick-up location - Street',
  'Pick-up location - City',
  'Pick-up location - State',
  'Pick-up location - County',
  'Pick-up location - ZIP code',
  'Pick-up Lat',
  'Pick-up Lon',
  'Drop-off location - Name',
  'Drop-off location - Number',
  'Drop-off location - Street',
  'Drop-off location - City',
  'Drop-off location - State',
  'Drop-off location - County',
  'Drop-off location - ZIP code',
  'Drop-off Lat',
  'Drop-off Lon',
  'Status',
  'No-show reason'
];

function normalizeHeader(header: string): string {
  return header ? header.trim() : header;
}

export interface TripTicketHeaderValidationResult {
  isValid: boolean;
  missingHeaders: string[];
  unexpectedHeaders: string[];
}

/**
 * Validates that the provided headers match the expected template headers.
 * Order is ignored, but all required headers must be present.
 */
export function validateTripTicketHeaders(
  headers: string[],
  expectedHeaders: string[] = TRIP_TICKET_TEMPLATE_HEADERS
): TripTicketHeaderValidationResult {
  const normalizedHeaders = headers.map(normalizeHeader).filter(header => !!header);
  const missingHeaders = expectedHeaders.filter(expected => !normalizedHeaders.includes(expected));
  const unexpectedHeaders = normalizedHeaders.filter(header => !expectedHeaders.includes(header));

  return {
    isValid: missingHeaders.length === 0,
    missingHeaders,
    unexpectedHeaders
  };
}
