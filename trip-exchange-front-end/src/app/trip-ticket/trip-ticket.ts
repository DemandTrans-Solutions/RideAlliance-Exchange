import { Address } from '../shared/model/address';

export class TicketsList {
  providerId: number;
  customer_first_name: string;
  requested_pickup_date: string;
  requested_pickup_time: string;
  pickup_address: Address;
  drop_off_address: Address;
  status: string;
  claimantList: any;
}

 export class Range {
  fromDate: any;
  toDate: any;
  providerId: any;
  reportTicketFilterStatus: any;
}


export class SummaryReport{
  approvedClaimReceived: any;
  declinedClaimReceived: any;
  pendingClaimReceived: any;
  rescindedCaimReceived: any;
  totalCliamsReceived: any;

  approvedClaimSubmitted: any;
  declinedClaimSubmitted: any;
  pendingClaimSubmitted: any;
  rescindedCaimSubmitted: any;
  totalCliamsSubmitted: any;

  totalTicketCount: any;
  approvedTicketCount: any;
  completedTicketCount: any;
  availabeTicketCount: any;
  expiredTicketCount: any;
  rescindedTicketCount: any;
}
