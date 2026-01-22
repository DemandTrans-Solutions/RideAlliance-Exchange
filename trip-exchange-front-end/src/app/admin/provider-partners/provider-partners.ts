export class ProviderPartners {    
  providerId: number;
  coordinatorProviderId: number;
  coordinatorProviderName: string;
  isActive: boolean;
  isTrustedPartnerForCoordinator: boolean;
  isTrustedPartnerForRequester: boolean;
  providerPartnerId : number;
  requestStatus: RequestStatus;
  requestStatusId : number;
  requesterProviderId : number;
  requesterProviderName: string;
  trusted: string;
  providerPartnerStatusId: number;
  status: string;
  Login:any;
}

export class RequestStatus{
  description: string;
  providerPartnerStatusId: number;
  status: string;
  coordinatorProviderId: number;
  coordinatorProviderName: string;
  isActive: boolean;
  providerPartnerId : number;
  requestStatusId : number;
  requestStatus : string;
  requesterProviderId : number;
  requesterProviderName: string;
}