export class User {
  id: number;
  providerId: number;
  providerName: string;
  userRole: string;
  username: string;
  jobTitle: string;
  email: string;
  name: string;
  phoneNumber: string;
  lastLoginDate: number;
  currentLoginIp: string;
  lastLoginIp: string;
  currentLoginDate: number;
  isActive: boolean;
  isNotifyPartnerCreatesTicket: boolean;
  isNotifyPartnerUpdateTicket: boolean;
  isNotifyClaimedTicketRescinded: boolean;
  isNotifyClaimedTicketExpired: boolean;
  isNotifyNewTripClaimAwaitingApproval: boolean;
  isNotifyNewTripClaimAutoApproved: boolean;
  isNotifyTripClaimApproved: boolean;
  isNotifyTripClaimDeclined: boolean;
  isNotifyTripClaimRescinded: boolean;
  isNotifyTripCommentAdded: boolean;
  isNotifyTripResultSubmitted: boolean;
  isNotifyTripReceived: boolean;
  isNotifyTripCancelled: boolean;
  isNotifyTripExpired: boolean;
  isNotifyTripWeeklyReport: boolean;
  isNotifyTripClaimCancelled: boolean;
  isNotifyTripPriceMismatched
  accountLockedDate: string;
  actionType: string;
}
