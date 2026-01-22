/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author chaitanyaP
 */

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderPartnerDTO {

    private int providerPartnerId;
    private int requesterProviderId;
    private String requesterProviderName;
    private int coordinatorProviderId;
    private String coordinatorProviderName;
    private ProviderPartnerStatusDTO requestStatus;


    @JsonProperty("isActive")
    private boolean isActive;

    @JsonProperty("isTrustedPartnerForRequester")
    private boolean isTrustedPartnerForRequester;

    @JsonProperty("isTrustedPartnerForCoordinator")
    private boolean isTrustedPartnerForCoordinator;


    public ProviderPartnerDTO(int requesterProviderId) {
        super();
        this.requesterProviderId = requesterProviderId;
    }


    @Override
    public String toString() {

        return "ProviderPartnerDTO{" + "providerPartnerId=" + providerPartnerId + ", requesterProviderId=" + requesterProviderId + ", requesterProviderName=" + requesterProviderName + ", coordinatorProviderId=" + coordinatorProviderId + ", coordinatorProviderName=" + coordinatorProviderName + ", requestStatus=" + requestStatus + ", isActive=" + isActive + ", isTrustedPartnerForRequester=" + isTrustedPartnerForRequester + ", isTrustedPartnerForCoordinator=" + isTrustedPartnerForCoordinator + '}';
    }

}
