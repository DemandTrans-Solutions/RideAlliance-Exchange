package com.clearinghouse.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Shankar I
 */
@Getter
@Setter
@NoArgsConstructor
public class ProviderCostDTO {

    private int providerCostId;
    private int providerId;
    private float costPerHour;
    private float ambulatoryCostPerMile;
    private float wheelchairCostPerMile;
    private float ambulatoryCost;
    private float wheelchairCost;
    private float totalCost;
    private float cancelledTripCost;
    private boolean useCostFromProvider;


    @Override
    public String toString() {
        return "ProviderCostDTO [providerCostId=" + providerCostId + ", providerId=" + providerId + ", costPerHour="
                + costPerHour + ", ambulatoryCostPerMile=" + ambulatoryCostPerMile + ", wheelchairCostPerMile="
                + wheelchairCostPerMile + ", ambulatoryCost=" + ambulatoryCost + ", wheelchairCost="
                + wheelchairCost + ", totalCost=" + totalCost + ", cancelledTripCost=" + cancelledTripCost
                + ", useCostFromProvider=" + useCostFromProvider + "]";
    }


}
