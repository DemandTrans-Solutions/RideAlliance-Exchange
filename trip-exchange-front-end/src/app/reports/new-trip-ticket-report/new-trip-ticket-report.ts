export class Range {
  fromDate: string | null = null;
  toDate: string | null = null;
  reportTicketFilterStatus: Array<string> = [];
  providerId: number = 0;
  providerIds: Array<number> = [];
}

export class SummaryReport {
  claimDeclined: number = 0;
  claimPending: number = 0;
  claimRescinded: number = 0;
  completed: number = 0;
  expired: number = 0;
  noOfTicketCreated: number = 0;
  ticketsAvailable: number = 0;
}
