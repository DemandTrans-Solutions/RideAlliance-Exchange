/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @author chaitanyaP
 */
@Entity
@Table(name = "tripclaim")
public class TripClaim extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TripClaimID")
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ClaimantProviderID")
    private Provider claimantProvider; // was claimant_provider

    @Column(name = "ClaimantTripID")
    private String claimantTripId; // was claimant_trip_id

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ServiceID")
    private Service service;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StatusID")
    private Status status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TripTicketID")
    private TripTicket tripTicket; // was trip_ticket

    @Column(name = "ProposedPickupTime")
    private LocalDateTime proposedPickupTime; // was proposed_pickup_time

    @Column(name = "ProposedFare")
    private float proposedFare; // was proposed_fare

    @Column(name = "Notes")
    private String notes;

    @Column(name = "ExpirationDate")
    private LocalDateTime expirationDate; // was expiration_date

    @Column(name = "IsExpired")
    private Boolean isExpired; // was is_expired

    @Column(name = "Version")
    private int version;

    //newly added
    @Column(name = "AcknowledgementStatus")
    private Boolean ackStatus;

    @Column(name = "RequesterProviderFare")
    private float requesterProviderFare;

    @Column(name = "CalculatedProposedFare")
    private float calculatedProposedFare;

    @Column(name = "NewRecord")
    private Boolean isNewRecord;

    // Default constructor
    public TripClaim() {
    }

    // Constructor with ID parameter
    public TripClaim(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Provider getClaimantProvider() {
        return claimantProvider;
    }

    public void setClaimantProvider(Provider claimantProvider) {
        this.claimantProvider = claimantProvider;
    }

    public String getClaimantTripId() {
        return claimantTripId;
    }

    public void setClaimantTripId(String claimantTripId) {
        this.claimantTripId = claimantTripId;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public TripTicket getTripTicket() {
        return tripTicket;
    }

    public void setTripTicket(TripTicket tripTicket) {
        this.tripTicket = tripTicket;
    }

    public float getProposedFare() {
        return proposedFare;
    }

    public void setProposedFare(float proposedFare) {
        this.proposedFare = proposedFare;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getProposedPickupTime() {
        return proposedPickupTime;
    }

    public void setProposedPickupTime(LocalDateTime proposedPickupTime) {
        this.proposedPickupTime = proposedPickupTime;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean isExpired) {
        this.isExpired = isExpired;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isAckStatus() {
        return ackStatus;
    }

    public void setAckStatus(boolean ackStatus) {
        this.ackStatus = ackStatus;
    }


    public float getRequesterProviderFare() {
        return requesterProviderFare;
    }

    public void setRequesterProviderFare(float requesterProviderFare) {
        this.requesterProviderFare = requesterProviderFare;
    }

    public float getCalculatedProposedFare() {
        return calculatedProposedFare;
    }

    public void setCalculatedProposedFare(float calculatedProposedFare) {
        this.calculatedProposedFare = calculatedProposedFare;
    }


    public boolean isNewRecord() {
        return isNewRecord;
    }

    public void setNewRecord(boolean isNewRecord) {
        this.isNewRecord = isNewRecord;
    }

    @Override
    public String toString() {
        return "TripClaim [id=" + id +
                ", claimantProviderId=" + (claimantProvider != null ? claimantProvider.getProviderId() : null)
                + ", claimantTripId=" + claimantTripId +
                ", serviceId=" + (service != null ? service.getServiceId() : null) +
                ", statusId=" + (status != null ? status.getStatusId() : null) +
                // Remove the reference to tripTicket to break the circular dependency
                ", proposedPickupTime=" + proposedPickupTime +
                ", proposedFare=" + proposedFare +
                ", notes=" + notes +
                ", expirationDate=" + expirationDate +
                ", isExpired=" + isExpired +
                ", version=" + version +
                ", ackStatus=" + ackStatus +
                ", requesterProviderFare=" + requesterProviderFare +
                ", calculatedProposedFare=" + calculatedProposedFare +
                ", isNewRecord=" + isNewRecord + "]";
    }


}