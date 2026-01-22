package com.clearinghouse.util;

import com.clearinghouse.dto.DetailedTripTicketDTO;
import com.clearinghouse.entity.TripClaim;
import com.clearinghouse.entity.TripTicket;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

/**
 * Utility class for fixing TripClaim to TripClaimDTO mapping issues.
 * 
 * The ModelMapper sometimes fails to properly access lazy-loaded provider names
 * in nested TripClaim entities when mapping TripTicket to DetailedTripTicketDTO.
 * This utility provides methods to fix those mapping issues.
 */
@Slf4j
public class TripClaimMappingUtil {

    /**
     * Fixes the claimantProviderName field in TripClaimDTOs within a DetailedTripTicketDTO.
     * The ModelMapper sometimes fails to properly access lazy-loaded provider names,
     * so we explicitly set them from the source entities.
     * 
     * @param tripTicket The source TripTicket entity
     * @param detailedTicketDTO The target DetailedTripTicketDTO with potentially missing provider names
     */
    public static void fixTripClaimProviderNames(TripTicket tripTicket, DetailedTripTicketDTO detailedTicketDTO) {
        if (tripTicket.getTripClaims() != null && detailedTicketDTO.getTripClaims() != null) {
            // Create a map of claim ID to TripClaim entity for quick lookup
            var claimEntityMap = tripTicket.getTripClaims().stream()
                    .collect(Collectors.toMap(TripClaim::getId, claim -> claim));
            
            // Set the provider name for each TripClaimDTO from the corresponding entity
            detailedTicketDTO.getTripClaims().forEach(claimDTO -> {
                TripClaim claimEntity = claimEntityMap.get(claimDTO.getId());
                if (claimEntity != null && claimEntity.getClaimantProvider() != null) {
                    String providerName = claimEntity.getClaimantProvider().getProviderName();
                    claimDTO.setClaimantProviderName(providerName);
                    claimDTO.setClaimantProviderId(claimEntity.getClaimantProvider().getProviderId());
                    //log.debug("Fixed provider name for trip claim id {}: {}", claimDTO.getId(), providerName);
                }
            });
        }
    }
}