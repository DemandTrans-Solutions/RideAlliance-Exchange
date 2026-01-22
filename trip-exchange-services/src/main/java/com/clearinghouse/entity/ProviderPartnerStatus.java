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
@Table(name = "providerpartnerstatus")
public class ProviderPartnerStatus extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProviderPartnerStatusID")
    private int providerPartnerStatusId;

    @Column(name = "Status")
    private String status;

    @Column(name = "Description")
    private String description;

    // Default constructor
    public ProviderPartnerStatus() {
    }

    // Constructor with ID parameter
    public ProviderPartnerStatus(int providerPartnerStatusId) {
        this.providerPartnerStatusId = providerPartnerStatusId;
    }

    public int getProviderPartnerStatusId() {
        return providerPartnerStatusId;
    }

    public void setProviderPartnerStatusId(int providerPartnerStatusId) {
        this.providerPartnerStatusId = providerPartnerStatusId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ProvidePartnerStatus{" + "providerPartnerStatusId=" + providerPartnerStatusId + ", status=" + status + ", description=" + description + '}';
    }

}
