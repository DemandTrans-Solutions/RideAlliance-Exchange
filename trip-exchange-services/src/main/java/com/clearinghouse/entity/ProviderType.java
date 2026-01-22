package com.clearinghouse.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "providertype")
public class ProviderType implements Serializable {


    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProviderTypeId")
    private int providerTypeId;


    // Default constructor
    public ProviderType() {
    }

    // Constructor with ID parameter
    public ProviderType(int providerTypeId) {
        this.providerTypeId = providerTypeId;
    }

    @Column(name = "ProviderType")
    private String providerTypes;

    public int getProviderTypeId() {
        return providerTypeId;
    }

    public void setProviderTypeId(int providerTypeId) {
        this.providerTypeId = providerTypeId;
    }


    public String getProviderTypes() {
        return providerTypes;
    }

    public void setProviderTypes(String providerTypes) {
        this.providerTypes = providerTypes;
    }

    @Override
    public String toString() {
        return "ProviderType [providerTypeId=" + providerTypeId + ", providerTypes=" + providerTypes + "]";
    }

}
