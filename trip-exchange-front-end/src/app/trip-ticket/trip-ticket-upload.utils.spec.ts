import { TRIP_TICKET_TEMPLATE_HEADERS, validateTripTicketHeaders } from './trip-ticket-upload.utils';

describe('trip-ticket-upload utils', () => {
  it('should validate when all expected headers are present', () => {
    const result = validateTripTicketHeaders([...TRIP_TICKET_TEMPLATE_HEADERS]);
    expect(result.isValid).toBeTrue();
    expect(result.missingHeaders).toEqual([]);
  });

  it('should flag missing headers', () => {
    const headers = TRIP_TICKET_TEMPLATE_HEADERS.filter(header => header !== 'Device');
    const result = validateTripTicketHeaders(headers);

    expect(result.isValid).toBeFalse();
    expect(result.missingHeaders).toContain('Device');
  });

  it('should ignore header order', () => {
    const reversed = [...TRIP_TICKET_TEMPLATE_HEADERS].reverse();
    const result = validateTripTicketHeaders(reversed);

    expect(result.isValid).toBeTrue();
  });

  it('should list unexpected headers', () => {
    const headers = [...TRIP_TICKET_TEMPLATE_HEADERS, 'Unexpected Column'];
    const result = validateTripTicketHeaders(headers);

    expect(result.unexpectedHeaders).toContain('Unexpected Column');
  });
});
