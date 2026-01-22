/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author chaitanyaP
 */
@Entity
@Table(name = "providerpartner")
public class ProviderPartner extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProviderPartnerID")
    private int providerPartnerId;

    @ManyToOne
    @JoinColumn(name = "RequesterProviderID")
    private Provider requesterProvider;

    @ManyToOne(optional = false)
    @JoinColumn(name = "CoordinatorProviderID")
    private Provider coordinatorProvider;

    @ManyToOne(optional = false)
    @JoinColumn(name = "RequestStatusID")
    private ProviderPartnerStatus requestStatus;

    @Temporal(TemporalType.DATE)
    @Column(name = "RequesterApprovedDate")
    private Date requesterApprovedDate;

    @Column(name = "IsActive")
    private Boolean isActive;

    @Column(name = "IsTrustedPartnerForRequester")
    private Boolean isTrustedPartnerForRequester;

    @Column(name = "IsTrustedPartnerForCoordinator")
    private Boolean isTrustedPartnerForCoordinator;

    public boolean isIsTrustedPartnerForRequester() {
        return isTrustedPartnerForRequester;
    }

    public void setIsTrustedPartnerForRequester(boolean isTrustedPartnerForRequester) {
        this.isTrustedPartnerForRequester = isTrustedPartnerForRequester;
    }

    public boolean isIsTrustedPartnerForCoordinator() {
        return isTrustedPartnerForCoordinator;
    }

    public void setIsTrustedPartnerForCoordinator(boolean isTrustedPartnerForCoordinator) {
        this.isTrustedPartnerForCoordinator = isTrustedPartnerForCoordinator;
    }

    public int getProviderPartnerId() {
        return providerPartnerId;
    }

    public void setProviderPartnerId(int providerPartnerId) {
        this.providerPartnerId = providerPartnerId;
    }

    public Provider getRequesterProvider() {
        return requesterProvider;
    }

    public void setRequesterProvider(Provider requesterProvider) {
        this.requesterProvider = requesterProvider;
    }

    public Provider getCoordinatorProvider() {
        return coordinatorProvider;
    }

    public void setCoordinatorProvider(Provider coordinatorProvider) {
        this.coordinatorProvider = coordinatorProvider;
    }

    public Date getRequesterApprovedDate() {
        return requesterApprovedDate;
    }

    public void setRequesterApprovedDate(Date requesterApprovedDate) {
        this.requesterApprovedDate = requesterApprovedDate;
    }


    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public ProviderPartnerStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(ProviderPartnerStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    // Default constructor
    public ProviderPartner() {
    }

    // Constructor with ID parameter
    public ProviderPartner(int providerPartnerId) {
        this.providerPartnerId = providerPartnerId;
    }

    @Override
    public String toString() {
        return "ProviderPartner{" + "providerPartnerId=" + providerPartnerId + ", requesterProvider=" + requesterProvider + ", coordinatorProvider=" + coordinatorProvider + ", requestStatus=" + requestStatus + ", requesterApprovedDate=" + requesterApprovedDate + ", isActive=" + isActive + ", isTrustedPartnerForRequester=" + isTrustedPartnerForRequester + ", isTrustedPartnerForCoordinator=" + isTrustedPartnerForCoordinator + '}';
    }

}
