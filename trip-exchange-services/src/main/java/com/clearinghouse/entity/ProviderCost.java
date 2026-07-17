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

    @Column(name = "AmbulatoryCostPerMile")
    private float ambulatoryCostPerMile;

    @Column(name = "WheelchairCostPerMile")
    private float wheelchairCostPerMile;

    @Column(name = "AmbulatoryCost")
    private float ambulatoryCost;

    @Column(name = "WheelchairCost")
    private float wheelchairCost;

    @Column(name = "TotalCost")
    private float totalCost;

    @Column(name = "CancelledTripCost", columnDefinition = "decimal(10,2)")
    private float cancelledTripCost;

    @Column(name = "UseCostFromProvider", columnDefinition = "tinyint(1) default 0")
    private boolean useCostFromProvider;

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

    public float getAmbulatoryCostPerMile() {
        return ambulatoryCostPerMile;
    }

    public void setAmbulatoryCostPerMile(float ambulatoryCostPerMile) {
        this.ambulatoryCostPerMile = ambulatoryCostPerMile;
    }

    public float getWheelchairCostPerMile() {
        return wheelchairCostPerMile;
    }

    public void setWheelchairCostPerMile(float wheelchairCostPerMile) {
        this.wheelchairCostPerMile = wheelchairCostPerMile;
    }

    public float getAmbulatoryCost() {
        return ambulatoryCost;
    }

    public void setAmbulatoryCost(float ambulatoryCost) {
        this.ambulatoryCost = ambulatoryCost;
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

    public float getCancelledTripCost() {
        return cancelledTripCost;
    }

    public void setCancelledTripCost(float cancelledTripCost) {
        this.cancelledTripCost = cancelledTripCost;
    }

    public boolean isUseCostFromProvider() {
        return useCostFromProvider;
    }

    public void setUseCostFromProvider(boolean useCostFromProvider) {
        this.useCostFromProvider = useCostFromProvider;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        return "ProviderCost [providerCostId=" + providerCostId + ", provider=" + provider + ", costPerHour="
                + costPerHour + ", ambulatoryCostPerMile=" + ambulatoryCostPerMile + ", wheelchairCostPerMile="
                + wheelchairCostPerMile + ", ambulatoryCost=" + ambulatoryCost + ", wheelchairCost="
                + wheelchairCost + ", totalCost=" + totalCost + ", cancelledTripCost=" + cancelledTripCost
                + ", useCostFromProvider=" + useCostFromProvider + "]";
    }


}
