package com.clearinghouse.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "claimanttripticket")
public class ClaimantTripTicket extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "claimantTripTicketId")
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ClaimantProviderID")
    private Provider claimantProvider;

    @Column(name = "ClaimantTripID")
    private String claimantTripId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TripTicketID")
    private TripTicket tripTicket;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClaimantTripId() {
        return claimantTripId;
    }

    public void setClaimantTripId(String claimantTripId) {
        this.claimantTripId = claimantTripId;
    }

    public TripTicket getTripTicket() {
        return tripTicket;
    }

    public void setTripTicket(TripTicket tripTicket) {
        this.tripTicket = tripTicket;
    }

    public Provider getClaimantProvider() {
        return claimantProvider;
    }

    public void setClaimantProvider(Provider claimantProvider) {
        this.claimantProvider = claimantProvider;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        return "ClaimantTripTicket [id=" + id + ", claimantProvider=" + claimantProvider.getProviderId() + ", claimantTripId="
                + claimantTripId + ", tripTicket=" + tripTicket.getId() + "]";
    }

}