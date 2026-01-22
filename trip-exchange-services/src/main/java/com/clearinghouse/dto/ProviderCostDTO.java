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
    private float costPerMile;
    private float ambularyCost;
    private float wheelchairCost;
    private float totalCost;


    @Override
    public String toString() {
        return "ProviderCostDTO [providerCostId=" + providerCostId + ", providerId=" + providerId + ", costPerHour="
                + costPerHour + ", costPerMile=" + costPerMile + ", ambularyCost=" + ambularyCost + ", wheelchairCost="
                + wheelchairCost + ", totalCost=" + totalCost + "]";
    }


}
