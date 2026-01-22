package com.clearinghouse.service;


import com.clearinghouse.dao.StatusDAO;
import com.clearinghouse.dto.*;
import com.clearinghouse.entity.TripClaim;
import com.clearinghouse.entity.TripTicket;
import com.clearinghouse.enumentity.TripClaimStatusConstants;
import com.clearinghouse.enumentity.TripTicketStatusConstants;
import com.clearinghouse.util.TripClaimMappingUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class DetailedTripTicketConverterService {

    private final ModelMapper tripTicketModelMapper;
    private final ModelMapper providerModelMapper;
    private final WorkingHoursService workingHoursService;
    private final UserContextService userContextService;
    private final StatusDAO statusDAO;


    public List<DetailedTripTicketDTO> convertToDetailedTripTicketDTOs(List<TripTicket> tripTickets) {
        return tripTickets.stream()
                .map(this::convertToDetailedTripTicketDTO)
                .collect(Collectors.toList());
    }

    public DetailedTripTicketDTO convertToDetailedTripTicketDTO(TripTicket tripTicket) {
        var userContext = userContextService.extractUserContext();
        tripTicket.loadLazyFields();

        var claims = tripTicket.getTripClaims();
        claims.forEach( c ->
                {
                    var providerNane = c.getClaimantProvider().getProviderName();
                    //log.debug("provider name for trip ticket id " + tripTicket.getId() + " and trip Claim id " + c.getId() + "  is " + providerNane);
                }
        );
        //log.debug("About to map TripTicket {} with {} claims using tripTicketModelMapper", tripTicket.getId(), claims != null ? claims.size() : 0);
        DetailedTripTicketDTO detailedTicketDTO = tripTicketModelMapper.map(tripTicket, DetailedTripTicketDTO.class);
        /*
        log.debug("Mapped TripTicket {} to DetailedTripTicketDTO with {} claims",
                detailedTicketDTO.getId(), 
                detailedTicketDTO.getTripClaims() != null ? detailedTicketDTO.getTripClaims().size() : 0);
        */
        // Fix for provider name mapping issue: explicitly set claimantProviderName for each TripClaimDTO
        // The ModelMapper sometimes fails to properly access lazy-loaded provider names
        TripClaimMappingUtil.fixTripClaimProviderNames(tripTicket, detailedTicketDTO);
        


        
        setClaimantIfPresent(tripTicket, detailedTicketDTO);
        setTripResultIfPresent(tripTicket, detailedTicketDTO);
        setEligibilityForClaim(tripTicket, detailedTicketDTO, userContext);
        setOriginator(tripTicket, detailedTicketDTO);
        detailedTicketDTO.getTripClaims().forEach(c -> {
            var providerNamne = c.getClaimantProviderName();
            //log.debug("DTO provider name for trip ticket id " + detailedTicketDTO.getId() +  " trip claim id " + c.getId() + " is " + providerNamne);
        });

        return detailedTicketDTO;
    }


    private void setClaimantIfPresent(TripTicket tripTicket, DetailedTripTicketDTO detailedTicketDTO) {
        var tripClaim = tripTicket.getApprovedTripClaim();
        if (tripClaim != null && tripClaim.getStatus().getStatusId() != TripClaimStatusConstants.cancelled.tripClaimStatusUpdate() ) {
            ProviderDTO providerDTO = providerModelMapper.map(tripTicket.getApprovedTripClaim().getClaimantProvider(), ProviderDTO.class);
            detailedTicketDTO.setClaimant(providerDTO);
            if ( (tripTicket.getStatus() != null && tripTicket.getStatus().getStatusId() != TripTicketStatusConstants.completed.tripTicketStatusUpdate()) &&
                claimIsPending(tripClaim)) {
                var pendingStatus = statusDAO.findStatusById(TripTicketStatusConstants.claimPending.tripTicketStatusUpdate());
                var statusDTO = new StatusDTO();
                statusDTO.setStatusId(pendingStatus.getStatusId());
                statusDTO.setDescription(pendingStatus.getDescription());
                statusDTO.setType(pendingStatus.getType());
                detailedTicketDTO.setStatus(statusDTO);
            }
        }
    }

    private void setTripResultIfPresent(TripTicket tripTicket, DetailedTripTicketDTO detailedTicketDTO) {
        var tripResult = tripTicket.getTripResult();
        if (tripResult != null) {
            TripResultDTO tripResultDTO = tripTicketModelMapper.map(tripResult, TripResultDTO.class);
            detailedTicketDTO.setTripResult(tripResultDTO);
        }
    }

    private boolean claimIsPending(TripClaim tripClaim) {
        return tripClaim.getStatus().getStatusId() == TripClaimStatusConstants.priceMismatch.tripClaimStatusUpdate() ||
                tripClaim.getStatus().getStatusId() == TripClaimStatusConstants.pending.tripClaimStatusUpdate() ||
                tripClaim.getStatus().getStatusId() == TripClaimStatusConstants.pendingYourApproval.tripClaimStatusUpdate();
    }


    private void setEligibilityForClaim(TripTicket tripTicket, DetailedTripTicketDTO detailedTicketDTO, UserContextDTO userContext) {
        if (userContext.userRole().equalsIgnoreCase("ROLE_ADMIN")) {
            detailedTicketDTO.setIsEligibleForClaim(true);
            return;
        }

        int statusId = tripTicket.getStatus().getStatusId();
        boolean isAvailableOrApproved = statusId == TripTicketStatusConstants.available.tripTicketStatusUpdate()
                || statusId == TripTicketStatusConstants.approved.tripTicketStatusUpdate();

        if (!isAvailableOrApproved) {
            return;
        }

        if (tripTicket.getOriginProvider().getProviderId() == userContext.providerId()) {
            detailedTicketDTO.setIsEligibleForClaim(true);
        } else {
            checkWorkingHoursEligibility(tripTicket, detailedTicketDTO, userContext.providerId());
        }
    }

    private void checkWorkingHoursEligibility(TripTicket tripTicket, DetailedTripTicketDTO detailedTicketDTO, int providerId) {
        CheckWorkingHoursDTO checkWorkingHoursDTO = new CheckWorkingHoursDTO();
        checkWorkingHoursDTO.setClaimantProviderId(providerId);
        checkWorkingHoursDTO.setTripTicketId(tripTicket.getId());

        CheckWorkingHoursDTO result = workingHoursService.checkWorkingHours(checkWorkingHoursDTO);
        detailedTicketDTO.setIsEligibleForClaim(result.isEligibleForCreateClaim());
    }


    private void setOriginator(TripTicket tripTicket, DetailedTripTicketDTO detailedTicketDTO) {
        ProviderDTO originatorDTO = providerModelMapper.map(tripTicket.getOriginProvider(), ProviderDTO.class);
        detailedTicketDTO.setOriginator(originatorDTO);
    }




}


