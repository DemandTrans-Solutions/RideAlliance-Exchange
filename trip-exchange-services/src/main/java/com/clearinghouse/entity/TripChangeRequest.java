/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.entity;

import jakarta.persistence.*;

import java.io.Serializable;

/**
 * A request from one provider (originating or claimant) to the other to change a
 * claimed trip ticket. While a request is pending the trip remains locked; on
 * approval the requester is granted a one-time edit unlock.
 */
@Entity
@Table(name = "tripchangerequest")
public class TripChangeRequest extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TripChangeRequestID")
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TripTicketID")
    private TripTicket tripTicket;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RequestedByProviderID")
    private Provider requestedByProvider;

    @Column(name = "RequestedByUserID")
    private Integer requestedByUserId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TargetProviderID")
    private Provider targetProvider;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StatusID")
    private Status status;

    @Column(name = "Message")
    private String message;

    /** JSON map of proposed field -> new value (a proposal only; not auto-applied). */
    @Column(name = "ProposedChanges")
    private String proposedChanges;

    @Column(name = "ResponseMessage")
    private String responseMessage;

    @Column(name = "RespondedByUserID")
    private Integer respondedByUserId;

    public TripChangeRequest() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TripTicket getTripTicket() {
        return tripTicket;
    }

    public void setTripTicket(TripTicket tripTicket) {
        this.tripTicket = tripTicket;
    }

    public Provider getRequestedByProvider() {
        return requestedByProvider;
    }

    public void setRequestedByProvider(Provider requestedByProvider) {
        this.requestedByProvider = requestedByProvider;
    }

    public Integer getRequestedByUserId() {
        return requestedByUserId;
    }

    public void setRequestedByUserId(Integer requestedByUserId) {
        this.requestedByUserId = requestedByUserId;
    }

    public Provider getTargetProvider() {
        return targetProvider;
    }

    public void setTargetProvider(Provider targetProvider) {
        this.targetProvider = targetProvider;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getProposedChanges() {
        return proposedChanges;
    }

    public void setProposedChanges(String proposedChanges) {
        this.proposedChanges = proposedChanges;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public Integer getRespondedByUserId() {
        return respondedByUserId;
    }

    public void setRespondedByUserId(Integer respondedByUserId) {
        this.respondedByUserId = respondedByUserId;
    }

    @Override
    public String toString() {
        return "TripChangeRequest [id=" + id
                + ", tripTicketId=" + (tripTicket != null ? tripTicket.getId() : null)
                + ", requestedByProviderId=" + (requestedByProvider != null ? requestedByProvider.getProviderId() : null)
                + ", targetProviderId=" + (targetProvider != null ? targetProvider.getProviderId() : null)
                + ", statusId=" + (status != null ? status.getStatusId() : null)
                + "]";
    }
}
