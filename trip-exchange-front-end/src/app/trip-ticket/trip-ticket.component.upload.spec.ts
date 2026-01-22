import { FormBuilder } from '@angular/forms';
import { TripTicketComponent } from './trip-ticket.component';
import { Subject } from 'rxjs';

describe('TripTicketComponent upload mapping', () => {
  let component: TripTicketComponent;

  beforeEach(() => {
    component = new TripTicketComponent(
      {} as any, // _tripTicketService
      {} as any, // _listService
      { confirm: () => {} } as any, // _confirmation
      {} as any, // _router
      { queryParams: new Subject() } as any, // _route
      {} as any, // _location
      { get: () => 'token' } as any, // _tokenService
      { header: new Subject() } as any, // _headerEmitterService
      { get: () => 'ROLE_ADMIN' } as any, // _localStorage
      {} as any, // _notification
      { getAllFundSources: () => new Subject() } as any, // _fundSourceService
      {} as any, // _providerPartnersService
      new FormBuilder(), // formBuilder
      {} as any, // _domSanitizer
      {} as any, // _exportService
      { state$: new Subject() } as any // _uploadService
    );
  });

  afterEach(() => {
    if (component && typeof component.ngOnDestroy === 'function') {
      component.ngOnDestroy();
    }
  });

  it('maps template-specific fields for new uploads', () => {
    const row = {
      'External Trip Id': '42',
      'Client - Name': 'Jane Doe',
      Device: 'Wheelchair',
      'Pick-up location - Name': 'Pick-up Place',
      'Pick-up location - Number': '123',
      'Pick-up location - Street': 'Main',
      'Pick-up location - City': 'Testville',
      'Pick-up location - State': 'VA',
      'Pick-up location - County': 'Metro',
      'Pick-up location - ZIP code': '12345',
      'Pick-up Lat': '39.1',
      'Pick-up Lon': '-77.2',
      'Drop-off location - Name': 'Drop-off Place',
      'Drop-off location - Number': '456',
      'Drop-off location - Street': 'Broadway',
      'Drop-off location - City': 'Sampletown',
      'Drop-off location - State': 'VA',
      'Drop-off location - County': 'Metro',
      'Drop-off location - ZIP code': '54321',
      'Drop-off Lat': '38.9',
      'Drop-off Lon': '-77.0',
      'Status': 'Scheduled',
      'Start stop - Estimated arrival': '08:30',
      'End stop - Estimated arrival': '09:15'
    } as Record<string, any>;

    const result = (component as any).transformToTripTicketDTO(row);

    expect(result.customerAddress).toBeNull();
    expect(result.customerMobilityFactors).toBe('Wheelchair');
    expect(result.pickUpAddress.state).toBe('VA');
    expect(result.requestedPickupTime).toBe('08:30');
    expect(result.requestedDropOffTime).toBe('09:15');
  });
});
