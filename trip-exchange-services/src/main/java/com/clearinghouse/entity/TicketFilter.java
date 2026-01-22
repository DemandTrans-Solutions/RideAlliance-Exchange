/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.entity;

import jakarta.persistence.*;

import java.io.Serializable;

/**
 *
 * @author chaitanyaP
 */
@Entity
@Table(name = "ticketfilter")
public class TicketFilter extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FilterID")
    private int filterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID")
    private User user;

    @Column(name = "FilterName")
    private String filterName;

    @Column(name = "RescindedApplyStatusParameter")
    private String rescindedApplyStatusParameter;

    @Column(name = "OriginatingProviderName")
    private String originatingProviderName;

    @Column(name = "ClaimingProviderName")
    private String claimingProviderName;

    @Column(name = "AdvancedFilterParameter")
    private String advancedFilterParameter;

    @Column(name = "TicketStatus")
    private String ticketFilterstatus;

    @Column(name = "TripTime")
    private String tripTime;

    @Column(name = "SeatsRequiredMinimum")
    private String seatsRequiredMin;

    @Column(name = "SeatsRequiredMaximum")
    private String seatsRequiredMax;

    @Column(name = "SchedulingPriority")
    private String schedulingPriority;

    @Column(name = "IsServiceFilterApply")
    private Boolean isServiceFilterApply = false;

    @Column(name = "IsActive")
    private Boolean isActive = true;

    //newly added
    @Column(name = "TripPickUpTime")
    private String reqPickUpStartAndEndTime = "";

    @Column(name = "FundingSources")
    private String fundingSourceList = "";

    @Column(name = "AppliedGeographicFilter")
    private String selectedGeographicVal = "";

    @Column(name = "CustomerEligibility")
    private String customerEligibility = "";

    @Column(name = "HospitalityServiceArea")
    private String hospitalityServiceArea = "";


    public String getFundingSourceList() {
        return fundingSourceList;
    }

    public void setFundingSourceList(String fundingSourceList) {
        this.fundingSourceList = fundingSourceList;
    }

    public String getSchedulingPriority() {
        return schedulingPriority;
    }

    public String getReqPickUpStartAndEndTime() {
        return reqPickUpStartAndEndTime;
    }

    public void setReqPickUpStartAndEndTime(String reqPickUpStartAndEndTime) {
        this.reqPickUpStartAndEndTime = reqPickUpStartAndEndTime;
    }

    public String getSelectedGeographicVal() {
        return selectedGeographicVal;
    }

    public void setSelectedGeographicVal(String selectedGeographicVal) {
        this.selectedGeographicVal = selectedGeographicVal;
    }

    public void setSchedulingPriority(String schedulingPriority) {
        this.schedulingPriority = schedulingPriority;
    }

    public String getSeatsRequiredMin() {
        return seatsRequiredMin;
    }

    public void setSeatsRequiredMin(String seatsRequiredMin) {
        this.seatsRequiredMin = seatsRequiredMin;
    }

    public String getSeatsRequiredMax() {
        return seatsRequiredMax;
    }

    public void setSeatsRequiredMax(String seatsRequiredMax) {
        this.seatsRequiredMax = seatsRequiredMax;
    }

    public boolean isIsServiceFilterApply() {
        return isServiceFilterApply;
    }

    public void setIsServiceFilterApply(boolean isServiceFilterApply) {
        this.isServiceFilterApply = isServiceFilterApply;
    }

    public String getTicketFilterstatus() {
        return ticketFilterstatus;
    }

    public void setTicketFilterstatus(String ticketFilterstatus) {
        this.ticketFilterstatus = ticketFilterstatus;
    }

    public String getOriginatingProviderName() {
        return originatingProviderName;
    }

    public void setOriginatingProviderName(String originatingProviderName) {
        this.originatingProviderName = originatingProviderName;
    }

    public String getClaimingProviderName() {
        return claimingProviderName;
    }

    public void setClaimingProviderName(String claimingProviderName) {
        this.claimingProviderName = claimingProviderName;
    }

    public String getAdvancedFilterParameter() {
        return advancedFilterParameter;
    }

    public void setAdvancedFilterParameter(String advancedFilterParameter) {
        this.advancedFilterParameter = advancedFilterParameter;
    }

    public String getTripTime() {
        return tripTime;
    }

    public void setTripTime(String tripTime) {
        this.tripTime = tripTime;
    }

    public int getFilterId() {
        return filterId;
    }

    public void setFilterId(int filterId) {
        this.filterId = filterId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }


    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getRescindedApplyStatusParameter() {
        return rescindedApplyStatusParameter;
    }

    public void setRescindedApplyStatusParameter(String rescindedApplyStatusParameter) {
        this.rescindedApplyStatusParameter = rescindedApplyStatusParameter;
    }

    public String getCustomerEligibility() {
        return customerEligibility;
    }

    public void setCustomerEligibility(String customerEligibility) {
        this.customerEligibility = customerEligibility;
    }

    public String getHospitalityServiceArea() {
        return hospitalityServiceArea;
    }

    public void setHospitalityServiceArea(String hospitalityServiceArea) {
        this.hospitalityServiceArea = hospitalityServiceArea;
    }

    @Override
    public String toString() {
        return "TicketFilter [filterId=" + filterId + ", user=" + user + ", filterName=" + filterName
                + ", rescindedApplyStatusParameter=" + rescindedApplyStatusParameter + ", originatingProviderName="
                + originatingProviderName + ", claimingProviderName=" + claimingProviderName
                + ", advancedFilterParameter=" + advancedFilterParameter + ", ticketFilterstatus=" + ticketFilterstatus
                + ", tripTime=" + tripTime + ", seatsRequiredMin=" + seatsRequiredMin + ", seatsRequiredMax="
                + seatsRequiredMax + ", schedulingPriority=" + schedulingPriority + ", isServiceFilterApply="
                + isServiceFilterApply + ", isActive=" + isActive + ", reqPickUpStartAndEndTime="
                + reqPickUpStartAndEndTime + ", fundingSourceList=" + fundingSourceList + ", selectedGeographicVal="
                + selectedGeographicVal + ", customerEligibility=" + customerEligibility + ", hospitalityServiceArea="
                + hospitalityServiceArea + "]";
    }


}
