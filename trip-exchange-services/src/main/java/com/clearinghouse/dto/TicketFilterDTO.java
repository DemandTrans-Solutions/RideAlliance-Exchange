/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 *
 * @author chaitanyaP
 */

@Getter
@Setter
@NoArgsConstructor
public class TicketFilterDTO {

    private int filterId;
    private int userId;
    private String filterName;
    private String rescindedApplyStatusParameter;
    private List<String> originatingProviderName;
    private List<String> claimingProviderName;
    private List<String> advancedFilterParameter;
    private List<String> ticketFilterstatus;
    private List<String> tripTime;
    private List<String> operatingHours;
    private String seatsRequiredMax;
    private String seatsRequiredMin;
    private String schedulingPriority;
    private boolean isActive;
    // newly added
    private List<String> fundingSourceList;
    private List<String> reqPickUpStartAndEndTime;
    private List<String> selectedGeographicVal;
    private boolean isServiceFilterApply;
    private List<String> customerEligibility;
    private List<String> hospitalityServiceArea;

    private int sortOrder;
    private String sortField;
    private int pagenumber;
    private int pagesize;


    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }


    public boolean isServiceFilterApply() {
        return isServiceFilterApply;
    }

    public void setServiceFilterApply(boolean isServiceFilterApply) {
        this.isServiceFilterApply = isServiceFilterApply;
    }


    @Override
    public String toString() {
        return "TicketFilterDTO{" + "filterId=" + filterId + ", userId=" + userId + ", filterName=" + filterName + ", rescindedApplyStatusParameter=" + rescindedApplyStatusParameter + ", originatingProviderName=" + originatingProviderName + ", claimingProviderName=" + claimingProviderName + ", advancedFilterParameter=" + advancedFilterParameter + ", ticketFilterstatus=" + ticketFilterstatus + ", tripTime=" + tripTime + ", isServiceFilterApply=" + isServiceFilterApply + ", seatsRequiredMax=" + seatsRequiredMax + ", seatsRequiredMin=" + seatsRequiredMin + ", schedulingPriority=" + schedulingPriority + ", isActive=" + isActive + '}';
    }

}
