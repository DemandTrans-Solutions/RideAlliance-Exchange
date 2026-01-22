package com.clearinghouse.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProviderTripCostDTO {

    private Float totalCostOfOrgProvider;
    private Float totalCostOfClaimantProvider;

    private String errorMessage;


}
