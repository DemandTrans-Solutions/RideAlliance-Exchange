/**
 * Interface for Working Hour data model
 */
export interface WorkingHour {
  workingHoursId?: number;
  providerId: number;
  day: string; // Day of week (MONDAY, TUESDAY, etc.)
  startTime: string; // Format: HH:MM
  endTime: string; // Format: HH:MM
  isHoliday?: boolean;
  isActive?: boolean;
}

/**
 * Implementation class of Working Hour interface
 */
export class WorkingHourModel implements WorkingHour {
  workingHoursId: number = 0;
  providerId: number;
  day: string;
  startTime: string;
  endTime: string;
  isHoliday: boolean = false;
  isActive: boolean = true;
}
