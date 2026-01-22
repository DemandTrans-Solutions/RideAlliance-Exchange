/**
 * Trip Acceptance Criteria Interface
 * Defines the structure for trip acceptance criteria configuration
 */
export interface TripAcceptanceCriteria {
  id?: number;
  futureDays: number;
  dayOfWeek: string;
  timeOfDay: string;
  fundingSourceId?: number;
  fundingSourceName?: string;
  isActive?: boolean;
}
