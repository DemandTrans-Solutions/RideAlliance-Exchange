/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Time;

/**
 *
 * @author chaitanyaP
 */

@Getter
@Setter
@NoArgsConstructor
public class ProviderDTO {
    private int providerId;
    private String providerName;
    private String contactEmail;
    private String apiKey;
    private String privateKey;
    private int tripTicketExpirationDaysBefore;
    private Time tripTicketExpirationTimeOfDay;
    private Time tripTicketProvisionalTime;

    private boolean isActive;
    private AddressDTO providerAddress;
    //newly added
    private int providerTypeId;
    private String providersType;
    private String lastSyncDateTime;

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean s) {
        this.isActive = s;
    }


    // Adding setActive method for consistency with builder
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }


    @Override
    public String toString() {
        return "ProviderDTO{" + "providerId=" + providerId + ", providerName=" + providerName + ", contactEmail=" + contactEmail + ", apiKey=" + apiKey + ", privateKey=" + privateKey + ", tripTicketExpirationDaysBefore=" + tripTicketExpirationDaysBefore + ", tripTicketExpirationTimeOfDay=" + tripTicketExpirationTimeOfDay + ", tripTicketProvisionalTime=" + tripTicketProvisionalTime + ", isActive=" + isActive + ", providerAddress=" + providerAddress + '}';
    }

}
