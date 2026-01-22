/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 *
 * @author chaitanyaP
 */

@Getter
@Setter
@NoArgsConstructor
public class ServiceDTO {

    private int serviceId;
    private int providerId;
    private int fundingSourceId;
    private String serviceName;
    private float dropOffRate;
    private float pickupRate;
    private float costPerMinute;
    private float costPerMile;
    private float wheelchairSpaceCost;
    private String serviceArea;
    private boolean isActive;
    private String eligibility;
    private String serviceAreaType;
    // newly added
    private Set<Integer> providerIdList;
    private Set<HospitalityAreaProviderDTO> providerAreaList;
    private Set<ServiceAreaDTO> serviceAreaList;
    private UploadFile uploadFile;
    private boolean isHospitalityArea;
    private String uploadFilePath;
    private boolean isProviderSelected;
    private String fileName;


    public void setActive(boolean active) {
        this.isActive = active;
    }

    public void setIsActive(boolean active) {
        this.isActive = active;
    }


    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public String toString() {
        return "ServiceDTO{" + "serviceId=" + serviceId + ", providerId=" + providerId + ", fundingSourceId=" + fundingSourceId + ", serviceName=" + serviceName + ", dropOffRate=" + dropOffRate + ", pickupRate=" + pickupRate + ", costPerMinute=" + costPerMinute + ", costPerMile=" + costPerMile + ", wheelchairSpaceCost=" + wheelchairSpaceCost + ", serviceArea=" + serviceArea + ", isActive=" + isActive + ", eligibility=" + eligibility + ", serviceAreaType=" + serviceAreaType + '}';
    }

}
