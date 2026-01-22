import { Address } from '../../shared/model/address';

export class Provider {
  providerId: number;
  providerName: string;
  contactEmail: string;
  apiKey: string;
  privateKey: string;
  tripTicketExpirationDaysBefore: number;
  tripTicketExpirationTimeOfDay: string;
  tripTicketProvisionalTime: string;
  isActive: boolean;
  providerAddress: Address;
}