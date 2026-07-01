export class MedrideRideRequest {
  trip_ticket_id: number = 0;
  pickup_time?: number | null; // UNIX timestamp
  dropoff_time?: number | null; // UNIX timestamp
}

export {}
