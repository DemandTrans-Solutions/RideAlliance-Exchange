package com.clearinghouse.entity;

import jakarta.persistence.*;

import java.io.Serializable;

/**
 *
 * @author Shankar I
 */

@Table(name = "providercost")
@Entity
public class ProviderCost extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProviderCostId")
    private int providerCostId;

    @OneToOne
    @JoinColumn(name = "ProviderId", unique = true)
    private Provider provider;

    @Column(name = "CostPerHour")
    private float costPerHour;

    @Column(name = "CostPerMile")
    private float costPerMile;

    @Column(name = "AmbularyCost")
    private float ambularyCost;

    @Column(name = "WheelchairCost")
    private float wheelchairCost;

    @Column(name = "TotalCost")
    private float totalCost;

    public int getProviderCostId() {
        return providerCostId;
    }

    public void setProviderCostId(int providerCostId) {
        this.providerCostId = providerCostId;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public float getCostPerHour() {
        return costPerHour;
    }

    public void setCostPerHour(float costPerHour) {
        this.costPerHour = costPerHour;
    }

    public float getCostPerMile() {
        return costPerMile;
    }

    public void setCostPerMile(float costPerMile) {
        this.costPerMile = costPerMile;
    }

    public float getAmbularyCost() {
        return ambularyCost;
    }

    public void setAmbularyCost(float ambularyCost) {
        this.ambularyCost = ambularyCost;
    }

    public float getWheelchairCost() {
        return wheelchairCost;
    }

    public void setWheelchairCost(float wheelchairCost) {
        this.wheelchairCost = wheelchairCost;
    }

    public float getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(float totalCost) {
        this.totalCost = totalCost;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        return "ProviderCost [providerCostId=" + providerCostId + ", provider=" + provider + ", costPerHour="
                + costPerHour + ", costPerMile=" + costPerMile + ", ambularyCost=" + ambularyCost + ", wheelchairCost="
                + wheelchairCost + ", totalCost=" + totalCost + "]";
    }


}
