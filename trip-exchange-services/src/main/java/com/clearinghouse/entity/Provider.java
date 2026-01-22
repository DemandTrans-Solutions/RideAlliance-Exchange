/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.sql.Time;

/**
 *
 * @author chaitanyaP
 */
@Entity
@Table(name = "provider")
public class Provider extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProviderID")
    private int providerId;

    @Column(name = "ProviderName")
    private String providerName;

    @Column(name = "contactEmail")
    private String contactEmail;

    @Column(name = "APIkey")
    private String apiKey;

    @Column(name = "PrivateKey")
    private String privateKey;

    @Column(name = "TripTicketExpirationDaysBefore")
    private int tripTicketExpirationDaysBefore;

    @Column(name = "TripTicketExpirationTime")
    private Time tripTicketExpirationTimeOfDay;

    @Column(name = "TripTicketProvisionalTime")
    private Time tripTicketProvisionalTime;

    @Column(name = "IsActive")
    private Boolean isActive;

    @Column(name = "LastSyncDateTime")
    private String lastSyncDateTime;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "AddressID")
    private Address address;


    //new added
    @OneToOne
    @JoinColumn(name = "ProviderTypeId")
    private ProviderType providerType;

    public int getProviderId() {
        return providerId;
    }

    public void setProviderId(int providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public int getTripTicketExpirationDaysBefore() {
        return tripTicketExpirationDaysBefore;
    }

    public void setTripTicketExpirationDaysBefore(int tripTicketExpirationDaysBefore) {
        this.tripTicketExpirationDaysBefore = tripTicketExpirationDaysBefore;
    }

    public Time getTripTicketExpirationTimeOfDay() {
        return tripTicketExpirationTimeOfDay;
    }

    public void setTripTicketExpirationTimeOfDay(Time tripTicketExpirationTimeOfDay) {
        this.tripTicketExpirationTimeOfDay = tripTicketExpirationTimeOfDay;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Time getTripTicketProvisionalTime() {
        return tripTicketProvisionalTime;
    }

    public void setTripTicketProvisionalTime(Time tripTicketProvisionalTime) {
        this.tripTicketProvisionalTime = tripTicketProvisionalTime;
    }


    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    //new added
    public ProviderType getProviderType() {
        return providerType;
    }

    public void setProviderType(ProviderType providerType) {
        this.providerType = providerType;
    }

    public Provider() {
        super();
        // TODO Auto-generated constructor stub
    }

    public Provider(int id) {
        super();
        this.providerId = id;
    }

    public String getLastSyncDateTime() {
        return lastSyncDateTime;
    }

    public void setLastSyncDateTime(String lastSyncDateTime) {
        this.lastSyncDateTime = lastSyncDateTime;
    }

    @Override
    public String toString() {
        return "Provider [providerId=" + providerId + ", providerName=" + providerName + ", contactEmail="
                + contactEmail + ", apiKey=" + apiKey + ", privateKey=" + privateKey
                + ", tripTicketExpirationDaysBefore=" + tripTicketExpirationDaysBefore
                + ", tripTicketExpirationTimeOfDay=" + tripTicketExpirationTimeOfDay + ", tripTicketProvisionalTime="
                + tripTicketProvisionalTime + ", isActive=" + isActive + ", lastSyncDateTime=" + lastSyncDateTime
                + ", address=" + address + ", providerTypeId="
                + (providerType != null ? providerType.getProviderTypeId() : null) + "]";
    }


}
