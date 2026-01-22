/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;

import com.clearinghouse.dao.*;
import com.clearinghouse.dto.*;
import com.clearinghouse.entity.*;
import com.clearinghouse.enumentity.NotificationStatus;
import com.clearinghouse.enumentity.NotificationTemplateCodeValue;
import com.clearinghouse.enumentity.TripClaimStatusConstants;
import com.clearinghouse.enumentity.TripTicketStatusConstants;
import com.clearinghouse.exceptions.HandlingExceptionForOKStatus;
import com.clearinghouse.exceptions.InvalidInputException;
import com.clearinghouse.listresponseentity.ProviderList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 *
 * @author chaitanyaP
 */
@org.springframework.stereotype.Service
@Transactional
@AllArgsConstructor
@Slf4j
public class TripTicketService implements IConvertBOToDTO, IConvertDTOToBO {


    private final UserService userService;

    private final TripTicketVectorStoreService tripTicketVectorStoreService;


    private final TripTicketDAO tripTicketDAO;


    private final ProviderDAO providerDAO;


    private final NotificationDAO notificationDAO;


    private final ActivityService activityService;


    private final UserNotificationDataDAO userNotificationDataDAO;


    private final ProviderPartnerDAO providerPartnerDAO;


    private final ListDAO listDAO;


    private final ServiceDAO serviceDAO;


    private final ModelMapper tripTicketModelMapper;


    private final ModelMapper claimantTripModelMapper;


    private final ModelMapper providerModelMapper;


    private final UserDAO userDAO;


    private final ConvertRequestToTripTicketDTOService convertRequestToTripTicketDTOService;


    private final ModelMapper modelMapper;


    private final WorkingHoursService workingHoursService;


    private final ProviderCostDAO providerCostDAO;


    private final TripTicketDistanceService tripTicketDistanceService;


    private final TripTicketDistanceDAO tripTicketDistanceDAO;


    private final TripClaimDAO tripClaimDAO;


    private final TripClaimService tripClaimService;


    private final ClaimantTripTicketDAO claimantTripticketDAO;

    private final DetailedTripTicketConverterService detailedTripTicketConverterService;

    private final TripResultService tripResultService;

    private final TripTicketCommentService tripTicketCommentService;


    private final ProviderService providerService;


    /**
     * Retrieves all trip tickets, sorted and grouped by pickup and drop-off times.
     *
     * @return List of TripTicketDTO objects
     */
    public List<TripTicketDTO> findAllTripTicket() {
        log.debug("Finding all trip tickets sorted and grouped by pickup and drop-off times");
        List<TripTicket> tripTickets = tripTicketDAO.findAllTripTickets();

        /*seperate list having pickupdate time as null*/
        List<TripTicket> pickupDatetimePresentTicketsList = new ArrayList<>();
        List<TripTicket> dropOffDatetimePresentTicketsList = new ArrayList<>();
        for (TripTicket tripTicket : tripTickets) {
            /*if ticket is avialabel and it has no claims then only*/
            if (tripTicket.getRequestedPickupDate() == null && tripTicket.getRequestedPickupTime() == null) {
                dropOffDatetimePresentTicketsList.add(tripTicket);
            } else {
                pickupDatetimePresentTicketsList.add(tripTicket);
            }
        }
        /*sort dropOffdatetime list*/
        /*sort list here on the basis of dropfoff date and time*/
        Collections.sort(dropOffDatetimePresentTicketsList, new Comparator<TripTicket>() {
            @Override
            public int compare(TripTicket t1, TripTicket t2) {

                int result = t1.getRequestedDropoffDate().compareTo(t2.getRequestedDropoffDate());

                if (result == 0) {
                    return t1.getRequestedDropOffTime().compareTo(t2.getRequestedDropOffTime());

                }

                return t1.getRequestedDropoffDate().compareTo(t2.getRequestedDropoffDate());
            }

        });

        /*we separate list into two part in unclaimed with availabel status and claimed */
        List<TripTicket> unclaimedTicketsList = new ArrayList<>();
        List<TripTicket> claimedTicketList = new ArrayList<>();

        for (TripTicket tripTicket : pickupDatetimePresentTicketsList) {
            /*if ticket is available and it has no claims then only*/
            if (tripTicket.getStatus().getStatusId() == TripTicketStatusConstants.available.tripTicketStatusUpdate() && tripTicket.getTripClaims().isEmpty()) {
                unclaimedTicketsList.add(tripTicket);
            } else {
                claimedTicketList.add(tripTicket);
            }
        }

        /*sort unclaimed tickets*/
        /*sort list here on the basis of pickup date and time*/
        Collections.sort(unclaimedTicketsList, new Comparator<TripTicket>() {
            @Override
            public int compare(TripTicket t1, TripTicket t2) {
                int result = t1.getRequestedPickupDate().compareTo(t2.getRequestedPickupDate());

                if (result == 0) {
                    return t1.getRequestedPickupTime().compareTo(t2.getRequestedPickupTime());

                }

                return t1.getRequestedPickupDate().compareTo(t2.getRequestedPickupDate());

            }

        });

        /*sort claimed tickets*/
        /*sort list here on the basis of pickup date and time*/
        Collections.sort(claimedTicketList, new Comparator<TripTicket>() {
            @Override
            public int compare(TripTicket t1, TripTicket t2) {
                int result = t1.getRequestedPickupDate().compareTo(t2.getRequestedPickupDate());

                if (result == 0) {
                    return t1.getRequestedPickupTime().compareTo(t2.getRequestedPickupTime());

                }

                return t1.getRequestedPickupDate().compareTo(t2.getRequestedPickupDate());

            }

        });

        /* combine to above lists*/
        if (!claimedTicketList.isEmpty()) {
            unclaimedTicketsList.addAll(claimedTicketList);
        }

        /* combine to dropoffDateTime  lists*/
        if (!dropOffDatetimePresentTicketsList.isEmpty()) {
            unclaimedTicketsList.addAll(dropOffDatetimePresentTicketsList);
        }

        List<TripTicketDTO> tripTicketDTOList = new java.util.ArrayList<>();
        for (TripTicket tripTicket : unclaimedTicketsList) {

            tripTicketDTOList.add((TripTicketDTO) toDTO(tripTicket));
        }

        tripTicketDTOList.forEach(tripTicketDTO -> {
            if ( tripTicketDTO.getStatus() != null ) {
                tripTicketDTO.getStatus().setDescription( tripTicketDTO.getStatus().getType());
            }
        });


        return tripTicketDTOList;

    }


    /**
     * Finds tickets for adapter by trip ticket ID and updated_since parameter.
     *
     * @param requestParamtereMap Map of request parameters
     * @param updated_since       String representing the last update timestamp
     * @return Map containing data and maxUpdatedDateTime
     */
    public Map<String, Object> findTicketsForAdapterByTripTicketId(Map<String, String[]> requestParamtereMap, String updated_since) {
        log.debug("Finding tickets for adapter with updated_since: {}", updated_since);
        Map<String, Object> returnFinalResult = new HashMap<String, Object>();
        AtomicReference<ZonedDateTime> maxDateTime = new AtomicReference<>();
        ZonedDateTime maxUpdatedDateTime = null;
        /*code for decrpypting token and taking user obj from it*/
      /*  String token = requestParamtereMap.get("api_key")[0];
        String[] tokenArray = token.split("\\.");
        String userPart = tokenArray[0];
        byte[] originalUserPart = Base64.getDecoder().decode(userPart);

        User user = convertRequestToTripTicketDTOService.fromJSON(originalUserPart);
        */
        User user = getCurrentUser();
        log.debug("user&&&&&==" + user.toString());

        List<TripTicket> tripTickets = new ArrayList<>();
        User originalUser = userDAO.findUserByUserId(user.getId());
        int providerId = originalUser.getProvider().getProviderId();
        try {
            tripTickets = tripTicketDAO.findAllDetailedTicketsForAdapter(updated_since, providerId);
        } catch (Exception e) {
            log.error("Error finding detailed tickets for adapter", e);
        }

        //AtomicReference<ZonedDateTime> maxDateTime = new AtomicReference<>();
        /*add code for sorting tickets */

        List<DetailedTripTicketDTO> detailedTripTicketDTOList = new java.util.ArrayList<>();
        for (TripTicket tripTicket : tripTickets) {
            Set<ClaimantTripTicketDTO> claimantTripTicketDTOs = new HashSet<ClaimantTripTicketDTO>();
            //following TODTO convesrion is done because of the data in the deatiledTripTicket is not as  tripTicketDTO
            DetailedTripTicketDTO detailedTicketDTO = tripTicketModelMapper.map(tripTicket, DetailedTripTicketDTO.class);

            if (tripTicket.getApprovedTripClaim() != null) {
                ProviderDTO providerDTO = providerModelMapper.map(tripTicket.getApprovedTripClaim().getClaimantProvider(), ProviderDTO.class);

                /*	if(!tripTicket.getTripClaims().isEmpty()) {
                		for (TripClaim tripClaim : tripTicket.getTripClaims()) {
                			if(tripClaim.isNewRecord()) {
                			if(providerDTO.getProviderId()==providerId) {
                				detailedTicketDTO.setNewRecord(true);
                			}
                		}
                	}
                }*/
                detailedTicketDTO.setClaimant(providerDTO);

            }//else {
            //handle for newRecord status only
            if (!tripTicket.getTripClaims().isEmpty()) {
                for (TripClaim tripClaim : tripTicket.getTripClaims()) {
                    if (tripClaim.isNewRecord()) {// && tripClaim.getStatus().getStatusId()==TripClaimStatusConstants.pending.tripClaimStatusUpdate()
                        if (tripClaim.getClaimantProvider().getProviderId() == providerId) {
                            detailedTicketDTO.setNewRecord(true);
                        }
                    }
                }
            }
            //}
            //get max updated datetime
            if (maxDateTime.get() == null
                    || ZonedDateTime.parse(tripTicket.getUpdatedAt().toString()).isAfter(maxDateTime.get())) {

                maxDateTime.getAndSet(ZonedDateTime.parse(tripTicket.getUpdatedAt().toString()));
                // (and do other stuff...)
            }

            maxUpdatedDateTime = maxDateTime.get();
            log.debug("----------------------------------------" + maxDateTime);
            log.debug("----------------------------------------" + maxUpdatedDateTime);

            ProviderDTO originatorDTO = providerModelMapper.map(tripTicket.getOriginProvider(), ProviderDTO.class);

            if (!tripTicket.getClaimantTripTicket().isEmpty()) {
                for (ClaimantTripTicket claimantTripTicket : tripTicket.getClaimantTripTicket()) {

                    ClaimantTripTicketDTO claimantTripTicketDTO = claimantTripticketDAO.convertToDTO(claimantTripTicket);
                    claimantTripTicketDTOs.add(claimantTripTicketDTO);
                }
            }

            detailedTicketDTO.setClaimantTripTickets(claimantTripTicketDTOs);
            detailedTicketDTO.setOriginator(originatorDTO);
            //set tripResult null each time
            detailedTicketDTO.setTripResult(null);

            detailedTripTicketDTOList.add(detailedTicketDTO);
        }

        returnFinalResult.put("data", detailedTripTicketDTOList);
        if (maxUpdatedDateTime != null) {
            returnFinalResult.put("maxUpdatedDateTime", maxUpdatedDateTime.withZoneSameInstant(ZoneId.of("GMT-7")).toString());
        } else {
            returnFinalResult.put("maxUpdatedDateTime", null);
        }
        return returnFinalResult;

    }


    /**
     * Retrieves all detailed trip tickets updated since a given timestamp.
     *
     * @param timestamp_updated_since Timestamp string
     * @return List of DetailedTripTicketDTO objects
     */
    public List<DetailedTripTicketDTO> findAllDeatiledTripTicket(String timestamp_updated_since) {
        log.debug("Finding all detailed trip tickets updated since: {}", timestamp_updated_since);
        List<TripTicket> tripTickets = tripTicketDAO.findAllDetailedTripTickets(timestamp_updated_since);

        /*seperate list having pickupdate time as null*/
        List<TripTicket> pickupDatetimePresentTicketsList = new ArrayList<>();
        List<TripTicket> dropOffDatetimePresentTicketsList = new ArrayList<>();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userRole = auth.getAuthorities().iterator().next().getAuthority();

        for (TripTicket tripTicket : tripTickets) {
            /*if ticket is avialabel and it has no claims then only*/
            if (tripTicket.getRequestedPickupDate() == null && tripTicket.getRequestedPickupTime() == null) {
                dropOffDatetimePresentTicketsList.add(tripTicket);
            } else {
                pickupDatetimePresentTicketsList.add(tripTicket);
            }
        }
        /*sort dropOffdatetime list*/
        /*sort list here on the basis of dropfoff date and time*/
        Collections.sort(dropOffDatetimePresentTicketsList, new Comparator<TripTicket>() {
            @Override
            public int compare(TripTicket t1, TripTicket t2) {

                int result = t1.getRequestedDropoffDate().compareTo(t2.getRequestedDropoffDate());

                if (result == 0) {
                    return t1.getRequestedDropOffTime().compareTo(t2.getRequestedDropOffTime());

                }

                return t1.getRequestedDropoffDate().compareTo(t2.getRequestedDropoffDate());
            }

        });

        /*we separate list into two part in unclaimed with availabel status and claimed */
        List<TripTicket> unclaimedTicketsList = new ArrayList<>();
        List<TripTicket> claimedTicketList = new ArrayList<>();

        for (TripTicket tripTicket : pickupDatetimePresentTicketsList) {
            /*if ticket is avialabel and it has no claims then only*/
            if (tripTicket.getStatus().getStatusId() == TripTicketStatusConstants.available.tripTicketStatusUpdate() && tripTicket.getTripClaims().isEmpty()) {
                unclaimedTicketsList.add(tripTicket);
            } else {
                claimedTicketList.add(tripTicket);
            }
        }

        /*sort unclaimed tickets*/
        /*sort list here on the basis of pickup date and time*/
        Collections.sort(unclaimedTicketsList, new Comparator<TripTicket>() {
            @Override
            public int compare(TripTicket t1, TripTicket t2) {
                int result = t1.getRequestedPickupDate().compareTo(t2.getRequestedPickupDate());

                if (result == 0) {
                    return t1.getRequestedPickupTime().compareTo(t2.getRequestedPickupTime());

                }

                return t1.getRequestedPickupDate().compareTo(t2.getRequestedPickupDate());

            }

        });

        /*sort claimed tickets*/
        /*sort list here on the basis of pickup date and time*/
        Collections.sort(claimedTicketList, new Comparator<TripTicket>() {
            @Override
            public int compare(TripTicket t1, TripTicket t2) {
                int result = t1.getRequestedPickupDate().compareTo(t2.getRequestedPickupDate());

                if (result == 0) {
                    return t1.getRequestedPickupTime().compareTo(t2.getRequestedPickupTime());

                }

                return t1.getRequestedPickupDate().compareTo(t2.getRequestedPickupDate());

            }

        });

        /* combine to above lists*/
        if (!claimedTicketList.isEmpty()) {
            unclaimedTicketsList.addAll(claimedTicketList);
        }

        /*combine dropoffdatetime list at the end*/
        if (!dropOffDatetimePresentTicketsList.isEmpty()) {
            unclaimedTicketsList.addAll(dropOffDatetimePresentTicketsList);
        }

        List<DetailedTripTicketDTO> detailedTripTicketDTOList = new java.util.ArrayList<>();
        for (TripTicket tripTicket : unclaimedTicketsList) {

            //following TODTO convesrion is done because of the data in the deatiledTripTicket is not as  tripTicketDTO
            DetailedTripTicketDTO detailedTicketDTO = tripTicketModelMapper.map(tripTicket, DetailedTripTicketDTO.class);
            
            // Fix for provider name mapping issue
            com.clearinghouse.util.TripClaimMappingUtil.fixTripClaimProviderNames(tripTicket, detailedTicketDTO);

            if (tripTicket.getApprovedTripClaim() != null) {
                ProviderDTO providerDTO = providerModelMapper.map(tripTicket.getApprovedTripClaim().getClaimantProvider(), ProviderDTO.class);
                detailedTicketDTO.setClaimant(providerDTO);
            }
            //newly added by shankar for check tripticket is in operatingHours /syncForUI api
            if (userRole.equalsIgnoreCase("ROLE_ADMIN")) {
                detailedTicketDTO.setIsEligibleForClaim(true);
            }
            ProviderDTO originatorDTO = providerModelMapper.map(tripTicket.getOriginProvider(), ProviderDTO.class);
            detailedTicketDTO.setOriginator(originatorDTO);
            detailedTripTicketDTOList.add(detailedTicketDTO);
        }

        return detailedTripTicketDTOList;
    }

    /**
     * Retrieves all detailed trip tickets with pagination.
     *
     * @param paginationDTO PaginationDTO object
     * @return Map containing totalRecords and data
     */
    public Map<String, Object> findAllDeatiledTripTicketWithpagination(PaginationDTO paginationDTO) {
        log.debug("Finding all detailed trip tickets with pagination: {}, ",
            paginationDTO);
        var tripTickets = tripTicketDAO.findAllTicketsWithpagination(paginationDTO);

        if ( paginationDTO.getSortField() == null || paginationDTO.getSortField().isEmpty() ) {
            tripTickets = sortTicketsByDateTime(tripTickets);
        }
        //List<DetailedTripTicketDTO> detailedTicketDTOs = convertToDetailedTripTicketDTOs(sortedTickets);
        var detailedTicketDTOs = detailedTripTicketConverterService.convertToDetailedTripTicketDTOs(tripTickets);
        return createPaginationResponse(detailedTicketDTOs);
    }

    private List<TripTicket> sortTicketsByDateTime(List<TripTicket> tripTickets) {
        log.debug("Sorting {} trip tickets by date and time", tripTickets.size());
        Map<Boolean, List<TripTicket>> partitionedTickets = tripTickets.stream()
                .collect(Collectors.partitioningBy(this::hasPickupDateTime));

        List<TripTicket> pickupTickets = partitionedTickets.get(true);
        List<TripTicket> dropOffOnlyTickets = partitionedTickets.get(false);

        pickupTickets.sort(createPickupDateTimeComparator());
        dropOffOnlyTickets.sort(createDropOffDateTimeComparator());

        // Combine lists: pickup tickets first, then drop-off only tickets
        List<TripTicket> sortedTickets = new ArrayList<>(pickupTickets);
        sortedTickets.addAll(dropOffOnlyTickets);

        return sortedTickets;
    }


    public List<DetailedTripTicketDTO> findTicketsByIds(TripTicketDownloadRequest tripTicketIds) {
        log.debug("Finding tickets by IDs: {}", tripTicketIds.getTicketIds());
        var tickets = tripTicketDAO.findAllTripTicketsByTripIds(tripTicketIds.getTicketIds());
        return convertToDetailedTripTicketDTOs(tickets);
    }


    private boolean hasPickupDateTime(TripTicket ticket) {
        log.debug("Checking if ticket {} has pickup date/time", ticket);
        return ticket.getRequestedPickupDate() != null && ticket.getRequestedPickupTime() != null;
    }

    private Comparator<TripTicket> createPickupDateTimeComparator() {
        log.debug("Creating pickup date/time comparator for sorting trip tickets");
        return (t1, t2) -> {
            int dateComparison = t1.getRequestedPickupDate().compareTo(t2.getRequestedPickupDate());
            return dateComparison != 0 ? dateComparison
                    : t1.getRequestedPickupTime().compareTo(t2.getRequestedPickupTime());
        };
    }

    private Comparator<TripTicket> createDropOffDateTimeComparator() {
        log.debug("Creating drop-off date/time comparator for sorting trip tickets");
        return (t1, t2) -> {
            int dateComparison = t1.getRequestedDropoffDate().compareTo(t2.getRequestedDropoffDate());
            return dateComparison != 0 ? dateComparison
                    : t1.getRequestedDropOffTime().compareTo(t2.getRequestedDropOffTime());
        };
    }

    private List<DetailedTripTicketDTO> convertToDetailedTripTicketDTOs(List<TripTicket> tripTickets) {
        log.debug("Converting {} trip tickets to detailed DTOs", tripTickets.size());
        return tripTickets.stream()
                .map(this::convertToDetailedTripTicketDTO)
                .collect(Collectors.toList());
    }

    // Helper to quickly log DAO date read mismatches for diagnostics (non-API)
    public List<com.clearinghouse.dto.DateReadMismatchDTO> logDateReadMismatches(int maxRows) {
        var mismatches = tripTicketDAO.findDateReadMismatches(maxRows);
        for (var m : mismatches) {
            log.warn("[DATE-MISMATCH] id={}, rpLocal={}, rpStr={}, rdLocal={}, rdStr={}, addedOn={}, updatedOn={}",
                    m.getTripTicketId(),
                    m.getRequestedPickupDateLocal(), m.getRequestedPickupDateString(),
                    m.getRequestedDropoffDateLocal(), m.getRequestedDropoffDateString(),
                    m.getAddedOn(), m.getUpdatedOn());
        }
        if (mismatches.isEmpty()) {
            log.info("[DATE-MISMATCH] No mismatches found in top {} recent rows", maxRows);
        }
        return mismatches;
    }

    public List<com.clearinghouse.dto.DateReadMismatchDTO> logDateReadMismatchesById(int tripTicketId) {
        var mismatches = tripTicketDAO.findDateReadMismatchesById(tripTicketId);
        for (var m : mismatches) {
            log.warn("[DATE-MISMATCH-ID] id={}, rpLocal={}, rpStr={}, rdLocal={}, rdStr={}, addedOn={}, updatedOn={}",
                    m.getTripTicketId(),
                    m.getRequestedPickupDateLocal(), m.getRequestedPickupDateString(),
                    m.getRequestedDropoffDateLocal(), m.getRequestedDropoffDateString(),
                    m.getAddedOn(), m.getUpdatedOn());
        }
        if (mismatches.isEmpty()) {
            log.info("[DATE-MISMATCH-ID] No mismatches for TripTicketID={}", tripTicketId);
        }
        return mismatches;
    }

    /**
     * Fix date mismatches by rewriting entity dates from DB CAST strings.
     * This ensures both JDBC reads and raw SQL queries see consistent values.
     * 
     * @param maxRows maximum number of rows to scan
     * @param dryRun if true, only log what would be fixed without saving
     * @return Map with "fixed" count, "dryRun" flag, and list of "affectedIds"
     */
    public Map<String, Object> fixDateMismatches(int maxRows, boolean dryRun) {
        var mismatches = tripTicketDAO.findDateReadMismatches(maxRows);
        int fixedCount = 0;
        List<Integer> affectedIds = new ArrayList<>();

        for (var m : mismatches) {
            int id = m.getTripTicketId();
            if (id == 1733) {
                TripTicket ticket = tripTicketDAO.findTripTicketByTripTicketId(id);
                if (ticket == null) {
                    log.warn("[FIX-DATE-MISMATCH] TripTicketID={} not found, skipping", id);
                    continue;
                }

                boolean changed = false;
                LocalDate correctPickup = null;
                LocalDate correctDropoff = null;

                // Parse DB string to get the "correct" LocalDate value
                if (m.getRequestedPickupDateLocal() != null && m.getRequestedPickupDateString() != null) {
                    String pickupStr = m.getRequestedPickupDateString();
                    if (!m.getRequestedPickupDateLocal().toString().equals(pickupStr)) {
                        correctPickup = LocalDate.parse(pickupStr);
                        changed = true;
                    }
                }

                if (m.getRequestedDropoffDateLocal() != null && m.getRequestedDropoffDateString() != null) {
                    String dropoffStr = m.getRequestedDropoffDateString();
                    if (!m.getRequestedDropoffDateLocal().toString().equals(dropoffStr)) {
                        correctDropoff = LocalDate.parse(dropoffStr);
                        changed = true;
                    }
                }

                if (changed) {
                    if (dryRun) {
                        log.info("[FIX-DATE-MISMATCH] [DRY-RUN] id={}, pickupFix={}, dropoffFix={}",
                                id, correctPickup, correctDropoff);
                    } else {
                        if (correctPickup != null) {
                            ticket.setRequestedPickupDate(correctPickup);
                        }
                        if (correctDropoff != null) {
                            ticket.setRequestedDropoffDate(correctDropoff);
                        }
                        tripTicketDAO.updateTripTicket(ticket);
                        log.info("[FIX-DATE-MISMATCH] id={}, pickupFix={}, dropoffFix={} SAVED",
                                id, correctPickup, correctDropoff);
                    }
                    fixedCount++;
                    affectedIds.add(id);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dryRun", dryRun);
        result.put("scanned", mismatches.size());
        result.put("fixed", fixedCount);
        result.put("affectedIds", affectedIds);
        
        if (dryRun) {
            log.info("[FIX-DATE-MISMATCH] DRY-RUN complete: scanned={}, would fix={}", mismatches.size(), fixedCount);
        } else {
            log.info("[FIX-DATE-MISMATCH] APPLIED: scanned={}, fixed={}", mismatches.size(), fixedCount);
        }
        
        return result;
    }

    /**
     * Verify what JDBC reads vs what's in the DB for a specific ticket.
     * Helps diagnose timezone shift issues.
     */
    public Map<String, Object> verifyTicketDates(int tripTicketId) {
        TripTicket ticket = tripTicketDAO.findTripTicketByTripTicketId(tripTicketId);
        if (ticket == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "TripTicketID not found");
            error.put("id", tripTicketId);
            return error;
        }

        // Get mismatch data (includes DB CAST strings)
        var mismatches = tripTicketDAO.findDateReadMismatchesById(tripTicketId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("tripTicketId", tripTicketId);
        result.put("jdbcPickupDate", ticket.getRequestedPickupDate());
        result.put("jdbcDropoffDate", ticket.getRequestedDropoffDate());
        
        if (!mismatches.isEmpty()) {
            var m = mismatches.get(0);
            result.put("dbPickupString", m.getRequestedPickupDateString());
            result.put("dbDropoffString", m.getRequestedDropoffDateString());
            result.put("pickupMismatch", m.getRequestedPickupDateLocal() != null && m.getRequestedPickupDateString() != null 
                    && !m.getRequestedPickupDateLocal().toString().equals(m.getRequestedPickupDateString()));
            result.put("dropoffMismatch", m.getRequestedDropoffDateLocal() != null && m.getRequestedDropoffDateString() != null 
                    && !m.getRequestedDropoffDateLocal().toString().equals(m.getRequestedDropoffDateString()));
        } else {
            // No mismatch means JDBC and DB agree
            result.put("dbPickupString", ticket.getRequestedPickupDate() != null ? ticket.getRequestedPickupDate().toString() : null);
            result.put("dbDropoffString", ticket.getRequestedDropoffDate() != null ? ticket.getRequestedDropoffDate().toString() : null);
            result.put("pickupMismatch", false);
            result.put("dropoffMismatch", false);
        }
        
        result.put("addedOn", ticket.getCreatedAt());
        result.put("updatedOn", ticket.getUpdatedAt());
        
        return result;
    }

    private DetailedTripTicketDTO convertToDetailedTripTicketDTO(TripTicket tripTicket) {
        log.debug("Converting trip ticket {} to detailed DTO", tripTicket);
        DetailedTripTicketDTO detailedTicketDTO = tripTicketModelMapper.map(tripTicket, DetailedTripTicketDTO.class);
        
        // Fix for provider name mapping issue
        com.clearinghouse.util.TripClaimMappingUtil.fixTripClaimProviderNames(tripTicket, detailedTicketDTO);

        setClaimantIfPresent(tripTicket, detailedTicketDTO);
        setOriginator(tripTicket, detailedTicketDTO);

        return detailedTicketDTO;
    }

    private void setClaimantIfPresent(TripTicket tripTicket, DetailedTripTicketDTO detailedTicketDTO) {
        log.debug("Setting claimant for trip ticket {} if approved claim exists", tripTicket);
        if (tripTicket.getApprovedTripClaim() != null) {
            ProviderDTO providerDTO = providerModelMapper.map(
                    tripTicket.getApprovedTripClaim().getClaimantProvider(), ProviderDTO.class);
            detailedTicketDTO.setClaimant(providerDTO);
        }
    }

    private void setOriginator(TripTicket tripTicket, DetailedTripTicketDTO detailedTicketDTO) {
        log.debug("Setting originator for trip ticket {}", tripTicket);
        ProviderDTO originatorDTO = providerModelMapper.map(tripTicket.getOriginProvider(), ProviderDTO.class);
        detailedTicketDTO.setOriginator(originatorDTO);
    }

    private Map<String, Object> createPaginationResponse(List<DetailedTripTicketDTO> detailedTicketDTOs) {
        log.debug("Creating pagination response with {} tickets", detailedTicketDTOs.size());
        long totalRecords = tripTicketDAO.getTotalcountOftickets();

        Map<String, Object> response = new HashMap<>();
        response.put("totalRecords", totalRecords);
        response.put("data", detailedTicketDTOs);

        return response;
    }


    /**
     * Finds detailed trip tickets by origin provider ID.
     *
     * @param providerId Provider ID
     * @return List of DetailedTripTicketDTO objects
     */
    public List<DetailedTripTicketDTO> findDetailedTripTicketByOriginProviderId(int providerId) {
        log.debug("Finding detailed trip tickets by origin provider ID: {}", providerId);
        List<ProviderList> providerListOfOriginator = listDAO.getOriginatorProviderListByProviderId(providerId);

        List<TripTicket> tripTicketsBOList = tripTicketDAO.findAllTripTicketsByOriginatorProviderList(providerListOfOriginator);
        Set<TripTicket> finalListForTripTicketWithoutApprovedStatus = new HashSet<TripTicket>();
        List<TripTicket> finalListForTripTicketWithApprovedStatus = new ArrayList<>();
        List<TripTicket> sortedTripTicketList = new ArrayList<TripTicket>();

        //skipped noShow status triptickets
        for (TripTicket tripTicketSort : tripTicketsBOList) {
            if (tripTicketSort.getStatus().getStatusId() != TripTicketStatusConstants.noShow.tripTicketStatusUpdate()) {
                sortedTripTicketList.add(tripTicketSort);
            }
        }
        //seperating tickets having approved status and expired
        for (TripTicket tripTicket : sortedTripTicketList) {
            if ((tripTicket.getStatus().getStatusId() == TripTicketStatusConstants.approved.tripTicketStatusUpdate())
                    || tripTicket.getStatus().getStatusId() == TripTicketStatusConstants.expired.tripTicketStatusUpdate()) {
                finalListForTripTicketWithApprovedStatus.add(tripTicket);

            } else {
                finalListForTripTicketWithoutApprovedStatus.add(tripTicket);

            }

        }

        for (TripTicket tripTicketObj : finalListForTripTicketWithApprovedStatus) {
            //sepearating expired ticekts from approved+expired
            if (tripTicketObj.getStatus().getStatusId() == TripTicketStatusConstants.approved.tripTicketStatusUpdate()) {
                if ((tripTicketObj.getOriginProvider().getProviderId() == providerId) || (tripTicketObj.getApprovedTripClaim().getClaimantProvider().getProviderId() == providerId)) {
                    finalListForTripTicketWithoutApprovedStatus.add(tripTicketObj);
                }
            } else if ((tripTicketObj.getOriginProvider().getProviderId() == providerId)) {
                finalListForTripTicketWithoutApprovedStatus.add(tripTicketObj);
            }
        }

        Set<TripTicket> finalTicketListForOriginator = new HashSet<TripTicket>();
        Set<TripTicket> finalTicketListOfpartnersForOriginator = new HashSet<TripTicket>();
        //applyfilterofserviceArea
//        1.fetch service arealist of originator providers
        List<Service> serviceareaListOfProvider = serviceDAO.findAllSeriveareaByProviderId(providerId);

//            checking for all trip tickets on by one
        for (TripTicket tripTicket : finalListForTripTicketWithoutApprovedStatus) {

            /*if ticket is of originator need not to check service area*/
            if (tripTicket.getOriginProvider().getProviderId() != providerId) {
                if (tripTicket.getStatus().getStatusId() == TripTicketStatusConstants.approved.tripTicketStatusUpdate() && (tripTicket.getApprovedTripClaim().getClaimantProvider().getProviderId() == providerId)) {
                    finalTicketListOfpartnersForOriginator.add(tripTicket);
                } else {

                    //                    check pickup and drop off belongs to servicearea
                    var pickupLatitude = tripTicket.getPickupAddress().getLatitude();
                    var pickupLongitude = tripTicket.getPickupAddress().getLongitude();
                    var dropOffLatitude = tripTicket.getDropOffAddress().getLatitude();
                    var dropOffLongitude = tripTicket.getDropOffAddress().getLongitude();
                    if ((!serviceareaListOfProvider.isEmpty()) && (pickupLatitude != 0 || dropOffLatitude != 0)) {
                        for (Service service : serviceareaListOfProvider) {

                            if (service.isActive() && !service.isHospitalityArea() && !service.isProviderSelected()) {
                                for (ServiceArea serviceArea : service.getHospitalServiceAreas()) {
                                    //String serviceArea = service.getServiceArea();
//serviceArea = service.getHospitalServiceAreas().stream().findFirst().get();
                                    /**
                                     * if lat longs of PU and DoF are null then SA
                                     * filter ll not be applied
                                     */
//                    check for PUA
                                    if (serviceDAO.checkAddressInServicearea(serviceArea, pickupLatitude, pickupLongitude)) {
//                        check for DOFFA
                                        if (serviceDAO.checkAddressInServicearea(serviceArea, dropOffLatitude, dropOffLongitude)) {

                                            finalTicketListOfpartnersForOriginator.add(tripTicket);

                                            /*if ticket is validate in service area criteria need nto to check for further more  service areas*/
                                            break;
                                        } else if (!tripTicket.getTripClaims().isEmpty()) {
                                            finalTicketListOfpartnersForOriginator.add(tripTicket);
                                            break;
                                        }

                                    } else if (!tripTicket.getTripClaims().isEmpty()) {
                                        finalTicketListOfpartnersForOriginator.add(tripTicket);
                                        break;
                                    }
                                }
                            }
                        } // end of for loop
                        /**
                         * if service area size and inactive service areas are
                         * same
                         */
//                        if (serviceareaListOfProvider.size() == serviceDAO.getCountOfInactiveServicearea(providerId)) {
//                            finalTicketListOfpartnersForOriginator.add(tripTicket);
//                        }
                    } else {
                        finalTicketListOfpartnersForOriginator.add(tripTicket);
                    }
                }
            } else {
                finalTicketListForOriginator.add(tripTicket);
            }

        }
        /*combining both lists*/
        finalTicketListForOriginator.addAll(finalTicketListOfpartnersForOriginator);

        if (finalTicketListForOriginator.isEmpty()) {
            finalTicketListForOriginator.addAll(finalListForTripTicketWithoutApprovedStatus);
        }

        /*seperate list having pickupdate time as null*/
        List<TripTicket> pickupDatetimePresentTicketsList = new ArrayList<>();
        List<TripTicket> dropOffDatetimePresentTicketsList = new ArrayList<>();
        for (TripTicket tripTicket : finalTicketListForOriginator) {
            /*if ticket is avialabel and it has no claims then only*/
            if (tripTicket.getRequestedPickupDate() == null && tripTicket.getRequestedPickupTime() == null) {
                dropOffDatetimePresentTicketsList.add(tripTicket);
            } else {
                pickupDatetimePresentTicketsList.add(tripTicket);
            }
        }
        /**
         * sort dropOffdatetime list sort list here on the basis of drop off
         * date and time
         */
        Collections.sort(dropOffDatetimePresentTicketsList, new Comparator<TripTicket>() {
            @Override
            public int compare(TripTicket t1, TripTicket t2) {

                int result = t1.getRequestedDropoffDate().compareTo(t2.getRequestedDropoffDate());

                if (result == 0) {
                    return t1.getRequestedDropOffTime().compareTo(t2.getRequestedDropOffTime());

                }

                return t1.getRequestedDropoffDate().compareTo(t2.getRequestedDropoffDate());
            }

        });

        /*we separate list into two part in unclaimed with availabel status and claimed */
        List<TripTicket> unclaimedTicketsList = new ArrayList<>();
        List<TripTicket> claimedTicketList = new ArrayList<>();

        for (TripTicket tripTicket : pickupDatetimePresentTicketsList) {
            /*if ticket is avialabel and it has no claims then only*/
            if (tripTicket.getStatus().getStatusId() == TripTicketStatusConstants.available.tripTicketStatusUpdate() && tripTicket.getTripClaims().isEmpty()) {
                unclaimedTicketsList.add(tripTicket);
            } else {
                claimedTicketList.add(tripTicket);
            }
        }

        /*sort unclaimed tickets*/
        /*sort list here on the basis of pickup date and time*/
        Collections.sort(unclaimedTicketsList, new Comparator<TripTicket>() {
            @Override
            public int compare(TripTicket t1, TripTicket t2) {
                int result = t1.getRequestedPickupDate().compareTo(t2.getRequestedPickupDate());

                if (result == 0) {
                    return t1.getRequestedPickupTime().compareTo(t2.getRequestedPickupTime());

                }

                return t1.getRequestedPickupDate().compareTo(t2.getRequestedPickupDate());

            }

        });

        /*sort claimed tickets*/
        /*sort list here on the basis of pickup date and time*/
        Collections.sort(claimedTicketList, new Comparator<TripTicket>() {
            @Override
            public int compare(TripTicket t1, TripTicket t2) {
                int result = t1.getRequestedPickupDate().compareTo(t2.getRequestedPickupDate());

                if (result == 0) {
                    return t1.getRequestedPickupTime().compareTo(t2.getRequestedPickupTime());

                }

                return t1.getRequestedPickupDate().compareTo(t2.getRequestedPickupDate());

            }

        });

        /* combine to above lists*/
        if (!claimedTicketList.isEmpty()) {
            unclaimedTicketsList.addAll(claimedTicketList);
        }

        /* combine to dropoffdatetime list lists*/
        if (!dropOffDatetimePresentTicketsList.isEmpty()) {
            unclaimedTicketsList.addAll(dropOffDatetimePresentTicketsList);
        }

        List<DetailedTripTicketDTO> tripTicketDTOList = new ArrayList<>();

        for (TripTicket tripTicketFinalOBJ : unclaimedTicketsList) {


            /*
            DetailedTripTicketDTO detailedTicketDTO = tripTicketModelMapper.map(tripTicketFinalOBJ, DetailedTripTicketDTO.class);
            
            // Fix for provider name mapping issue
            com.clearinghouse.util.TripClaimMappingUtil.fixTripClaimProviderNames(tripTicketFinalOBJ, detailedTicketDTO);

            if (tripTicketFinalOBJ.getApprovedTripClaim() != null) {
                ProviderDTO providerDTO = providerModelMapper.map(tripTicketFinalOBJ.getApprovedTripClaim().getClaimantProvider(), ProviderDTO.class);

                detailedTicketDTO.setClaimant(providerDTO);

            }
            //newly added by shankar for check tripticket bydefault is in operatingHours /providerIdWiseTickets/{providerId} api
            if (tripTicketFinalOBJ.getStatus().getStatusId() == TripTicketStatusConstants.available.tripTicketStatusUpdate()
                    || tripTicketFinalOBJ.getStatus().getStatusId() == TripTicketStatusConstants.approved.tripTicketStatusUpdate()) {//&& tripTicketFinalOBJ.getTripClaims().isEmpty()) {
                if(tripTicketFinalOBJ.getOriginProvider().getProviderId() == providerId) {
                    detailedTicketDTO.setIsEligibleForClaim(true);
                }else {
                    CheckWorkingHoursDTO checkWorkingHoursDTO=new CheckWorkingHoursDTO();
                    checkWorkingHoursDTO.setClaimantProviderId(providerId);
                    checkWorkingHoursDTO.setTripTicketId(tripTicketFinalOBJ.getId());
                    CheckWorkingHoursDTO checkWorkingHoursDTOResult=workingHoursService.checkWorkingHours(checkWorkingHoursDTO);
                    detailedTicketDTO.setIsEligibleForClaim(checkWorkingHoursDTOResult.isEligibleForCreateClaim());
                }
            }
            ProviderDTO originatorDTO = providerModelMapper.map(tripTicketFinalOBJ.getOriginProvider(), ProviderDTO.class);
            detailedTicketDTO.setOriginator(originatorDTO);

             */
            var detailedTicketDTO = convertToDetailedTripTicketDTO(tripTicketFinalOBJ, providerId);
            tripTicketDTOList.add(detailedTicketDTO);
        }

        return tripTicketDTOList;
    }


    /**
     * Finds detailed trip ticket by ID.
     *
     * @param tripTicketId Trip ticket ID
     * @param providerId   Provider ID
     * @return DetailedTripTicketDTO object
     */
    public DetailedTripTicketDTO findDetailedTripTicketById(int tripTicketId, int providerId) {

        TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(tripTicketId);
        if (tripTicket == null) {
            return null;
        }
        DetailedTripTicketDTO detailedTripTicketDTO = convertToDetailedTripTicketDTO(tripTicket, providerId);
        return detailedTripTicketDTO;
    }

    protected DetailedTripTicketDTO convertToDetailedTripTicketDTO(TripTicket tripTicket, int providerId) {
        /*check which feild are updated*/
        DetailedTripTicketDTO detailedTripTicketDTO = tripTicketModelMapper.map(tripTicket, DetailedTripTicketDTO.class);
        
        // Fix for provider name mapping issue
        com.clearinghouse.util.TripClaimMappingUtil.fixTripClaimProviderNames(tripTicket, detailedTripTicketDTO);
        if (tripTicket.getApprovedTripClaim() != null) {
            ProviderDTO providerDTO = providerModelMapper.map(tripTicket.getApprovedTripClaim().getClaimantProvider(), ProviderDTO.class);
            detailedTripTicketDTO.setClaimant(providerDTO);
        }
        //newly added by shankar for check tripticket bydefault is in operatingHours /providerIdWiseTickets/{providerId} api
        if (tripTicket.getStatus().getStatusId() == TripTicketStatusConstants.available.tripTicketStatusUpdate()
                || tripTicket.getStatus().getStatusId() == TripTicketStatusConstants.approved.tripTicketStatusUpdate()) {//&& tripTicketFinalOBJ.getTripClaims().isEmpty()) {
            if (tripTicket.getOriginProvider().getProviderId() == providerId) {
                detailedTripTicketDTO.setIsEligibleForClaim(true);
            } else {
                CheckWorkingHoursDTO checkWorkingHoursDTO = new CheckWorkingHoursDTO();
                checkWorkingHoursDTO.setClaimantProviderId(providerId);
                checkWorkingHoursDTO.setTripTicketId(tripTicket.getId());
                CheckWorkingHoursDTO checkWorkingHoursDTOResult = workingHoursService.checkWorkingHours(checkWorkingHoursDTO);
                detailedTripTicketDTO.setIsEligibleForClaim(checkWorkingHoursDTOResult.isEligibleForCreateClaim());
            }
        }
        ProviderDTO originatorDTO = providerModelMapper.map(tripTicket.getOriginProvider(), ProviderDTO.class);
        detailedTripTicketDTO.setOriginator(originatorDTO);
        return detailedTripTicketDTO;
    }


    /**
     * Finds trip ticket by trip ticket ID.
     *
     * @param id Trip ticket ID
     * @return TripTicketDTO object
     */
    public TripTicketDTO findTripTicketByTripTicketId(int id) {

        return (TripTicketDTO) toDTO(tripTicketDAO.findTripTicketByTripTicketId(id));
    }


    /**
     * Creates a new trip ticket.
     *
     * @param tripTicketDTO TripTicketDTO object
     * @return TripTicketDTO object
     */
    public TripTicketDTO createTripTicket(TripTicketDTO tripTicketDTO) {

        StatusDTO statusDTO = new StatusDTO();
        statusDTO.setStatusId(TripTicketStatusConstants.available.tripTicketStatusUpdate());
        tripTicketDTO.setStatus(statusDTO);
        //fetch originator provider..
        Provider originatorProviderObj = providerDAO.findProviderByProviderId(tripTicketDTO.getOriginProviderId());

        LocalDate pickupDate = tripTicketDTO.getRequestedPickupDate();
        LocalDate dropoffDate = tripTicketDTO.getRequestedDropoffDate();
        if (pickupDate != null) {
            LocalDate expirationBase = minusDaysSkippingWeekends(pickupDate, originatorProviderObj.getTripTicketExpirationDaysBefore());
            String expirationDateTimeString = expirationBase + "T" + originatorProviderObj.getTripTicketExpirationTimeOfDay().toString();
            tripTicketDTO.setExpirationDate(expirationDateTimeString);
        } else if (dropoffDate != null) {
            LocalDate expirationBase = minusDaysSkippingWeekends(dropoffDate, originatorProviderObj.getTripTicketExpirationDaysBefore());
            String expirationDateTimeString = expirationBase + "T" + originatorProviderObj.getTripTicketExpirationTimeOfDay().toString();
            tripTicketDTO.setExpirationDate(expirationDateTimeString);
        }
        int comman_trip_id_random = UUID.randomUUID().hashCode();
        String comman_trip_id = String.valueOf(Math.abs(comman_trip_id_random));
        tripTicketDTO.setCommonTripId(comman_trip_id);
        tripTicketDTO.setLastStatusChangedByProviderId(tripTicketDTO.getOriginProviderId());
        log.info("[SERVICE-CREATE] Before toBO: DTO pickupDate={}, class={}, dropoffDate={}, class={}",
            tripTicketDTO.getRequestedPickupDate(), 
            tripTicketDTO.getRequestedPickupDate() != null ? tripTicketDTO.getRequestedPickupDate().getClass().getName() : "null",
            tripTicketDTO.getRequestedDropoffDate(),
            tripTicketDTO.getRequestedDropoffDate() != null ? tripTicketDTO.getRequestedDropoffDate().getClass().getName() : "null");
        TripTicket tripTicket = (TripTicket) toBO(tripTicketDTO);
        log.info("[SERVICE-CREATE] After toBO: Entity pickupDate={}, class={}, dropoffDate={}, class={}",
            tripTicket.getRequestedPickupDate(),
            tripTicket.getRequestedPickupDate() != null ? tripTicket.getRequestedPickupDate().getClass().getName() : "null",
            tripTicket.getRequestedDropoffDate(),
            tripTicket.getRequestedDropoffDate() != null ? tripTicket.getRequestedDropoffDate().getClass().getName() : "null");
        TripTicket updatedDbObjTripTicket = tripTicketDAO.createTripTicket(tripTicket);
        log.info("[SERVICE-CREATE] After DB save: Entity pickupDate={}, dropoffDate={}",
            updatedDbObjTripTicket.getRequestedPickupDate(),
            updatedDbObjTripTicket.getRequestedDropoffDate());

        /*add record as  activty */
        createActivityForTripTicketForCreateTripTicket(updatedDbObjTripTicket);

        //Send acknowldge meail to the oroginator
        sendACKMailToOriginator(updatedDbObjTripTicket);

        //        we need to send mail notifcation according to the user obj notification flags
        sendMailNotificationAccordingToTheUserFlag(updatedDbObjTripTicket);
        //updateTripTicket(updatedDbObjTripTicket);

        return (TripTicketDTO) toDTO(tripTicket);

    }

    //newly added for ExpiryDate #to skipped weekendDays from PickupDate beforeExpiresDays
    public static LocalDate minusDaysSkippingWeekends(LocalDate date, int days) {
        LocalDate result = date;
        int subtractedDays = 0;
        while (subtractedDays < days) {
            result = result.minusDays(1);
            if (!(result.getDayOfWeek() == DayOfWeek.SATURDAY || result.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                ++subtractedDays;
            }
        }
        return result;
    }

    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    private String formatDisplayDate(LocalDate date) {
        return date != null ? date.format(DISPLAY_DATE_FORMATTER) : "";
    }

    private String formatDisplayTime(Time time) {
        return time != null ? time.toLocalTime().format(DISPLAY_TIME_FORMATTER) : "";
    }

    private void populatePickupDetails(Map<String, String> templateMap, TripTicket tripTicket) {
        if (tripTicket.getRequestedPickupDate() != null) {
            templateMap.put("pickupDate", formatDisplayDate(tripTicket.getRequestedPickupDate()));
            templateMap.put("pickupTime", formatDisplayTime(tripTicket.getRequestedPickupTime()));
        } else {
            templateMap.put("pickupDate", "Pickup Date - No requested pickup date,");
            templateMap.put("pickupTime", buildDropoffFallback(tripTicket));
        }
    }

    private String buildDropoffFallback(TripTicket tripTicket) {
        String dropoffDate = formatDisplayDate(tripTicket.getRequestedDropoffDate());
        String dropoffTime = formatDisplayTime(tripTicket.getRequestedDropOffTime());
        return String.format("Pickup Time - No requested pickup time,Dropoff date - %s Drop Off time - %s ", dropoffDate, dropoffTime);
    }


    /**
     * Updates an existing trip ticket.
     *
     * @param tripTicketDTO TripTicketDTO object
     * @return TripTicketDTO object
     */
    public TripTicketDTO updateTripTicket(TripTicketDTO tripTicketDTO) {

        TripTicket tripTicket = (TripTicket) toBO(tripTicketDTO);
        tripTicketDAO.updateTripTicket(tripTicket);
        var dto = (TripTicketDTO) toDTO(tripTicket);
        tripTicketVectorStoreService.updateTripTicket(dto);
        return dto;

    }


    public TripTicket updateTripTicket(TripTicket tripTicket) {

        var res = tripTicketDAO.updateTripTicket(tripTicket);
        var dto = (TripTicketDTO) toDTO(tripTicket);
        tripTicketVectorStoreService.updateTripTicket(dto);
        return res;
    }

    /**
     * Converts a TripTicket object to TripTicketDTO.
     *
     * @param tripTicket TripTicket object
     * @return TripTicketDTO object
     */
    public TripTicketDTO convertToDTO(TripTicket tripTicket) {
        /*check which feild are updated*/
        return (TripTicketDTO) toDTO(tripTicket);

    }




    /**
     * Rescinds a trip ticket by ID.
     *
     * @param id Trip ticket ID
     * @return TripTicketDTO object
     */
    public TripTicketDTO rescindTripTicket(int id) {
        TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(id);

        //NotificationEnginePart.. for trip ticket originator
        Notification emailNotificationForTripOriginator = new Notification();
        NotificationTemplate notificationTemplateForTripOriginator = new NotificationTemplate();
        emailNotificationForTripOriginator.setEmailTo(tripTicket.getOriginProvider().getContactEmail());
        emailNotificationForTripOriginator.setIsEMail(true);
        emailNotificationForTripOriginator.setStatusId(NotificationStatus.newStatus.status());
        notificationTemplateForTripOriginator.setNotificationTemplateId(NotificationTemplateCodeValue.tripTicketRescindedTemplateCode.templateCodeValue());
        emailNotificationForTripOriginator.setNotificationTemplate(notificationTemplateForTripOriginator);
        emailNotificationForTripOriginator.setNumberOfAttempts(0);
        emailNotificationForTripOriginator.setIsActive(true);

//        there are two cases for trip ticket  rescind and if no claims are there then ticket status will be cancelled.
//        1.when trip ticket status is pending and claim ststus for that is also pending
//        2.if tickt status is aaproved then originator provider wants to resind the ticket then ticket status and the claim ll be resined.
//1.1:checking if tripicket status is available and is there any claim for it
        if ((tripTicket.getStatus().getStatusId() == TripTicketStatusConstants.available.tripTicketStatusUpdate()) && (tripTicket.getTripClaims().isEmpty())) {

            Status status = new Status();
            status.setStatusId(TripTicketStatusConstants.cancelled.tripTicketStatusUpdate());
            tripTicket.setStatus(status);
            TripTicket tripTicketUpdted = tripTicketDAO.updateTripTicket(tripTicket);
            /*add record for Trip ticket rescinded*/
            createActivityForTripTicketForRescindTripTicket(tripTicket);

            return (TripTicketDTO) toDTO(tripTicketUpdted);

        } //       1.2:if status is available and the claim are pending
        else if ((tripTicket.getStatus().getStatusId() == TripTicketStatusConstants.available.tripTicketStatusUpdate()) && (tripTicket.getApprovedTripClaim() == null)) {

            Set<TripClaim> tripClaimsWithPendingStatus = tripTicket.getTripClaims();

//            updateLaststatus changes by feild in tripticket
            tripTicket.setLastStatusChangedByProvider(tripTicket.getOriginProvider());
            /*   send mail notification to the claimant provider */
            sendRescindedMailNotificationToAllClaimants(tripClaimsWithPendingStatus, tripTicket);

//            updating only those claims whose status is pending into  set
            for (TripClaim tripClaim : tripClaimsWithPendingStatus) {
                if (tripClaim.getStatus().getStatusId() == TripClaimStatusConstants.pending.tripClaimStatusUpdate()) {
                    Status updatedStatusForTripClaim = new Status();
                    updatedStatusForTripClaim.setStatusId(TripTicketStatusConstants.rescinded.tripTicketStatusUpdate());
                    tripClaim.setStatus(updatedStatusForTripClaim);
                    /*add record for the activity when claim rescinded*/
                    createActivityForClaimRescinded(tripTicket, tripClaim);

                }

            }
            tripTicket.setTripClaims(tripClaimsWithPendingStatus);

            Status statusForTripTicekt = new Status();
            statusForTripTicekt.setStatusId(TripTicketStatusConstants.rescinded.tripTicketStatusUpdate());
            tripTicket.setStatus(statusForTripTicekt);

//            updating ticket in database
            TripTicket tripTicketUpdatedForPendingClaims = tripTicketDAO.updateTripTicket(tripTicket);
            /*add record for Trip ticket rescinded*/
            createActivityForTripTicketForRescindTripTicket(tripTicket);

            return (TripTicketDTO) toDTO(tripTicketUpdatedForPendingClaims);
        } //if ticket status and claim status is approved
        else if ((tripTicket.getStatus().getStatusId() == TripTicketStatusConstants.approved.tripTicketStatusUpdate())) {



//            updating last status changes by provider
            tripTicket.setLastStatusChangedByProvider(tripTicket.getOriginProvider());

//            remaining code..
            TripClaim approvedTripClaim = tripTicket.getApprovedTripClaim();

            Status updatedStatusForTripClaim = new Status();
            updatedStatusForTripClaim.setStatusId(TripTicketStatusConstants.rescinded.tripTicketStatusUpdate());

            approvedTripClaim.setStatus(updatedStatusForTripClaim);
            tripTicket.setApprovedTripClaim(approvedTripClaim);

            Status updatedStatusForTripTicket = new Status();
            updatedStatusForTripTicket.setStatusId(TripTicketStatusConstants.rescinded.tripTicketStatusUpdate());
            tripTicket.setStatus(updatedStatusForTripClaim);

            TripTicket tripTicketUpdated = tripTicketDAO.updateTripTicket(tripTicket);

            /*add record for Trip ticket rescinded*/
            createActivityForTripTicketForRescindTripTicket(tripTicket);

            /*add record in activity for claim rescinded*/
            createActivityForClaimRescinded(tripTicket, approvedTripClaim);

            List<User> usersOfClaimant = userNotificationDataDAO.getUsersForClaimedTripticketRescinded(tripTicket.getApprovedTripClaim().getClaimantProvider().getProviderId());
            for (User user : usersOfClaimant) {
                //fetching single obj of the user role
                List<UserAuthority> userAuthority = new ArrayList<>();
                userAuthority.addAll(user.getAuthorities());
                userAuthority.addAll(user.getAuthorities());
                String userrole = userAuthority.get(0).getAuthority();
                if (user.isIsNotifyClaimedTicketRescinded() ) {

                    //   Setting parameter values in according to the template for email notofication.
                    Map tripTicketOriginatorMailTemplateMap = new HashMap<String, String>();
                    tripTicketOriginatorMailTemplateMap.put("originatorORClaimantProviderName", user.getName());
                    tripTicketOriginatorMailTemplateMap.put("commonTripTicketId", tripTicket.getCommonTripId());

                    tripTicketOriginatorMailTemplateMap.put("lastStatusChangedByProviderName", tripTicket.getLastStatusChangedByProvider().getProviderName());
                    tripTicketOriginatorMailTemplateMap.put("year", Year.now().toString());
                    String jsonValueOfTemplate = "";

                    Iterator<Map.Entry<String, String>> entries = tripTicketOriginatorMailTemplateMap.entrySet().iterator();
                    while (entries.hasNext()) {

                        Map.Entry<String, String> entry = entries.next();
                        jsonValueOfTemplate = jsonValueOfTemplate + "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
                        if (entries.hasNext()) {
                            jsonValueOfTemplate = jsonValueOfTemplate + ",";
                        }

                    }
                    String FinaljsonValueOfTemplateForTicketOriginator = "{" + jsonValueOfTemplate + "}";

                    //            creatingspeparate obj of notification for claimant
                    Notification emailNotificationForClaimantProvider = new Notification();
                    NotificationTemplate notificationTemplateForClaimantProvider = new NotificationTemplate();
                    emailNotificationForClaimantProvider.setEmailTo(tripTicket.getApprovedTripClaim().getClaimantProvider().getContactEmail());
                    emailNotificationForClaimantProvider.setIsEMail(true);
                    emailNotificationForClaimantProvider.setStatusId(NotificationStatus.newStatus.status());
                    notificationTemplateForClaimantProvider.setNotificationTemplateId(NotificationTemplateCodeValue.tripTicketRescindedTemplateCode.templateCodeValue());
                    emailNotificationForClaimantProvider.setNotificationTemplate(notificationTemplateForClaimantProvider);
                    emailNotificationForClaimantProvider.setNumberOfAttempts(0);
                    emailNotificationForClaimantProvider.setIsActive(true);

                    emailNotificationForTripOriginator.setParameterValues(FinaljsonValueOfTemplateForTicketOriginator);
                    emailNotificationForClaimantProvider.setSubject("Trip ticket is rescinded");

                    notificationDAO.createNotification(emailNotificationForClaimantProvider);
                }
            }
            return (TripTicketDTO) toDTO(tripTicketUpdated);
        }

//        else returning emptydto
        TripTicketDTO ticketDTOEmpty = new TripTicketDTO();
        return ticketDTOEmpty;

    }


    public Set<TripClaim> sendRescindedMailNotificationToAllClaimants(Set<TripClaim> tripClaims, TripTicket tripTicket) {

        //NotificationEnginePart.. for trip ticket originator


        for (TripClaim tripClaim : tripClaims) {

//            fetch the users of that provider having flag notify the  claimed trip ticket rescinded
            List<User> users = userNotificationDataDAO.getUsersForClaimedTripticketRescinded(tripClaim.getClaimantProvider().getProviderId());
            for (User user : users) {
                //fetching single obj of the user role
                List<UserAuthority> userAuthority = new ArrayList<>();

                userAuthority.addAll(user.getAuthorities());
                String userrole = userAuthority.get(0).getAuthority();
                if (user.isIsNotifyClaimedTicketRescinded() ) {
                    if (tripClaim.getStatus().getStatusId() == TripClaimStatusConstants.pending.tripClaimStatusUpdate()) {

                        Notification emailNotificationForClaimant = new Notification();
                        NotificationTemplate notificationTemplateForClaimant = new NotificationTemplate();

                        emailNotificationForClaimant.setIsEMail(true);
                        emailNotificationForClaimant.setStatusId(NotificationStatus.newStatus.status());
                        notificationTemplateForClaimant.setNotificationTemplateId(NotificationTemplateCodeValue.tripTicketRescindedTemplateCode.templateCodeValue());
                        emailNotificationForClaimant.setNotificationTemplate(notificationTemplateForClaimant);
                        emailNotificationForClaimant.setNumberOfAttempts(0);
                        emailNotificationForClaimant.setIsActive(true);
                        emailNotificationForClaimant.setEmailTo(user.getEmail());

                        //   Setting parameter values in according to the template for email notofication.
                        Map rescindClaimantTemplateMap = new HashMap<String, String>();
                        rescindClaimantTemplateMap.put("originatorORClaimantProviderName", user.getName());
                        rescindClaimantTemplateMap.put("commonTripTicketId", tripTicket.getCommonTripId());
                        rescindClaimantTemplateMap.put("lastStatusChangedByProviderName", tripTicket.getLastStatusChangedByProvider().getProviderName());
                        rescindClaimantTemplateMap.put("year", Year.now().toString());

                        populatePickupDetails(rescindClaimantTemplateMap, tripTicket);

                        String jsonValueOfTemplate = "";

                        Iterator<Map.Entry<String, String>> entries = rescindClaimantTemplateMap.entrySet().iterator();
                        while (entries.hasNext()) {

                            Map.Entry<String, String> entry = entries.next();
                            jsonValueOfTemplate = jsonValueOfTemplate + "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
                            if (entries.hasNext()) {
                                jsonValueOfTemplate = jsonValueOfTemplate + ",";
                            }

                        }
                        String FinaljsonValueOfTemplateForRescindClaimant = "{" + jsonValueOfTemplate + "}";

                        //            sending mail to claimant provider
                        emailNotificationForClaimant.setParameterValues(FinaljsonValueOfTemplateForRescindClaimant);
                        emailNotificationForClaimant.setSubject("Trip ticket is rescinded");
                        notificationDAO.createNotification(emailNotificationForClaimant);
                    }
                }
            }
        }

        return tripClaims;

    }

    /**
     * Sends mail notification according to the user flag for a trip ticket.
     *
     * @param tripTicket TripTicket object
     */
    public void sendMailNotificationAccordingToTheUserFlag(TripTicket tripTicket) {

//        if partner creates ticket flag  enabled
        List<ProviderPartner> providerPartners = userNotificationDataDAO.getProviderPartners(tripTicket.getOriginProvider().getProviderId());
        if (providerPartners != null) {

            Provider originProvider = providerDAO.findProviderByProviderId(tripTicket.getOriginProvider().getProviderId());
            for (ProviderPartner providerPartner : providerPartners) {
                if (providerPartner.getRequesterProvider().getProviderId() == tripTicket.getOriginProvider().getProviderId()) {
//                if orgin provider in the requester in provider partner relationship then wll user coordinator provider
                    List<User> partnersUsers = userNotificationDataDAO.getUsersForPartnerCreateTicket(providerPartner.getCoordinatorProvider().getProviderId());

                    if (partnersUsers != null) {
                        for (User user : partnersUsers) {
                            //fetching single obj of the user role
                            List<UserAuthority> userAuthority = new ArrayList<>();
                            userAuthority.addAll(user.getAuthorities());

                            if (user.isIsNotifyPartnerCreatesTicket() ) {
                                //send mail to that user

//            notification obj creation
                                Notification emailNotificationForUser = new Notification();
                                NotificationTemplate notificationTemplateForUser = new NotificationTemplate();

                                emailNotificationForUser.setIsEMail(true);
                                emailNotificationForUser.setStatusId(NotificationStatus.newStatus.status());
                                notificationTemplateForUser.setNotificationTemplateId(NotificationTemplateCodeValue.partnerCreatesTicket.templateCodeValue());
                                emailNotificationForUser.setNotificationTemplate(notificationTemplateForUser);
                                emailNotificationForUser.setNumberOfAttempts(0);
                                emailNotificationForUser.setIsActive(true);

                                emailNotificationForUser.setEmailTo(user.getEmail());

                                //   Setting parameter values in according to the template for email notofication.
                                Map userTemplateMap = new HashMap<String, String>();
                                userTemplateMap.put("name", user.getName());
                                userTemplateMap.put("originatorProviderName", originProvider.getProviderName());
                                userTemplateMap.put("year", Year.now().toString());

                                populatePickupDetails(userTemplateMap, tripTicket);
                                String jsonValueOfTemplate = "";

                                Iterator<Map.Entry<String, String>> entries = userTemplateMap.entrySet().iterator();

                                while (entries.hasNext()) {

                                    Map.Entry<String, String> entry = entries.next();
                                    jsonValueOfTemplate = jsonValueOfTemplate + "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
                                    if (entries.hasNext()) {
                                        jsonValueOfTemplate = jsonValueOfTemplate + ",";
                                    }

                                }

                                String FinaljsonValueOfTemplateForRescindClaimant = "{" + jsonValueOfTemplate + "}";

                                //            sending mail to user
                                emailNotificationForUser.setParameterValues(FinaljsonValueOfTemplateForRescindClaimant);
                                emailNotificationForUser.setSubject("Partner creates ticket");
                                notificationDAO.createNotification(emailNotificationForUser);

                            }
                        }
                    }
                } else if (providerPartner.getCoordinatorProvider().getProviderId() == tripTicket.getOriginProvider().getProviderId()) {
                    List<User> partnersUsersSecond = userNotificationDataDAO.getUsersForPartnerCreateTicket(providerPartner.getRequesterProvider().getProviderId());

                    if (partnersUsersSecond != null) {
                        for (User userSecond : partnersUsersSecond) {
                            //fetching single obj of the user role
                            List<UserAuthority> userAuthority = new ArrayList<>();
                            userAuthority.addAll(userSecond.getAuthorities());

                            if (userSecond.isIsNotifyPartnerCreatesTicket() ) {
                                //send mail to that user

                                    //            notification obj creation
                                Notification emailNotificationForUser = new Notification();
                                NotificationTemplate notificationTemplateForUser = new NotificationTemplate();

                                emailNotificationForUser.setIsEMail(true);
                                emailNotificationForUser.setStatusId(NotificationStatus.newStatus.status());
                                notificationTemplateForUser.setNotificationTemplateId(NotificationTemplateCodeValue.partnerCreatesTicket.templateCodeValue());
                                emailNotificationForUser.setNotificationTemplate(notificationTemplateForUser);
                                emailNotificationForUser.setNumberOfAttempts(0);
                                emailNotificationForUser.setIsActive(true);

                                emailNotificationForUser.setEmailTo(userSecond.getEmail());

                                //   Setting parameter values in according to the template for email notofication.
                                Map userTemplateMap = new HashMap<String, String>();
                                userTemplateMap.put("name", userSecond.getName());
                                userTemplateMap.put("originatorProviderName", originProvider.getProviderName());
                                userTemplateMap.put("year", Year.now().toString());

                                populatePickupDetails(userTemplateMap, tripTicket);

                                String jsonValueOfTemplate = "";

                                Iterator<Map.Entry<String, String>> entries = userTemplateMap.entrySet().iterator();

                                while (entries.hasNext()) {

                                    Map.Entry<String, String> entry = entries.next();
                                    jsonValueOfTemplate = jsonValueOfTemplate + "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
                                    if (entries.hasNext()) {
                                        jsonValueOfTemplate = jsonValueOfTemplate + ",";
                                    }

                                }

                                String FinaljsonValueOfTemplateForRescindClaimant = "{" + jsonValueOfTemplate + "}";

                                //            sending mail to user
                                emailNotificationForUser.setParameterValues(FinaljsonValueOfTemplateForRescindClaimant);
                                emailNotificationForUser.setSubject("Partner creates ticket");
                                notificationDAO.createNotification(emailNotificationForUser);
                            }
                        }
                    }

                }

            }

        }
    }

    /**
     * Sends an acknowledgment mail to the originator of a trip ticket.
     *
     * @param tripTicket TripTicket object
     */
    public void sendACKMailToOriginator(TripTicket tripTicket) {

        //            fetch the users of that provider
        List<User> users = userNotificationDataDAO.getUsersForTripReceived(tripTicket.getOriginProvider().getProviderId());
        for (User user : users) {
            //fetching single obj of the user role

            //checking for checkbox
            if (user.isIsNotifyTripReceived()) {

                //NotificationEnginePart.. for trip ticket originator
                Notification emailNotificationForOriginator = new Notification();
                NotificationTemplate notificationTemplateForOriginator = new NotificationTemplate();

                emailNotificationForOriginator.setIsEMail(true);
                emailNotificationForOriginator.setStatusId(NotificationStatus.newStatus.status());
                notificationTemplateForOriginator.setNotificationTemplateId(NotificationTemplateCodeValue.tripTicketReceived.templateCodeValue());
                emailNotificationForOriginator.setNotificationTemplate(notificationTemplateForOriginator);
                emailNotificationForOriginator.setNumberOfAttempts(0);
                emailNotificationForOriginator.setIsActive(true);
                emailNotificationForOriginator.setEmailTo(user.getEmail());

                //   Setting parameter values in according to the template for email notofication.
                Map ackToOriginatorTemplateMap = new HashMap<String, String>();
                ackToOriginatorTemplateMap.put("name", user.getName());
                ackToOriginatorTemplateMap.put("tripTicketnumber", tripTicket.getCommonTripId());
                ackToOriginatorTemplateMap.put("year", Year.now().toString());

                populatePickupDetails(ackToOriginatorTemplateMap, tripTicket);
                String jsonValueOfTemplate = "";
                Iterator<Map.Entry<String, String>> entries = ackToOriginatorTemplateMap.entrySet().iterator();
                while (entries.hasNext()) {

                    Map.Entry<String, String> entry = entries.next();
                    jsonValueOfTemplate = jsonValueOfTemplate + "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
                    if (entries.hasNext()) {
                        jsonValueOfTemplate = jsonValueOfTemplate + ",";
                    }

                }
                String FinaljsonValueOfTemplateForRescindClaimant = "{" + jsonValueOfTemplate + "}";

                //            sending mail to claimant provider
                emailNotificationForOriginator.setParameterValues(FinaljsonValueOfTemplateForRescindClaimant);
                emailNotificationForOriginator.setSubject("Trip ticket is received");
                notificationDAO.createNotification(emailNotificationForOriginator);
            }

        }
    }

    /**
     * Creates an activity record for a trip ticket when it is created.
     *
     * @param tripTicket TripTicket object
     */
    public void createActivityForTripTicketForCreateTripTicket(TripTicket tripTicket) {
        Provider provider = providerDAO.findProviderByProviderId(tripTicket.getOriginProvider().getProviderId());
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setTripTicketId(tripTicket.getId());
        activityDTO.setAction("Trip ticket created");
        activityDTO.setActionDetails("status=available,originator_provider=" + provider.getProviderName() + ",requested pickup date=" + tripTicket.getRequestedPickupDate() + ", requested_pickup_time=" + tripTicket.getRequestedPickupTime());
        activityDTO.setActionTakenBy(getActionBy());
        activityService.createActivity(activityDTO);
    }

    /**
     * Creates an activity record for a trip ticket when it is rescinded.
     *
     * @param tripTicket TripTicket object
     */
    public void createActivityForTripTicketForRescindTripTicket(TripTicket tripTicket) {
        int providerId = 0;
        if ( tripTicket.getLastStatusChangedByProvider() != null ) {
            providerId = tripTicket.getLastStatusChangedByProvider().getProviderId();
        } else {
            providerId = tripTicket.getOriginProvider().getProviderId();
        }

        Provider originatorProvider = providerDAO.findProviderByProviderId(tripTicket.getOriginProvider().getProviderId());
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setTripTicketId(tripTicket.getId());
        activityDTO.setAction("Trip ticket Rescinded");
        activityDTO.setActionDetails("status=rescinded,originator_provider=" + originatorProvider.getProviderName() + ",requested pickup date=" + tripTicket.getRequestedPickupDate() + ", requested_pickup_time=" + tripTicket.getRequestedPickupTime());
        activityDTO.setActionTakenBy(getActionBy());
        activityService.createActivity(activityDTO);
    }

    /*method for the adding activity record for claim rescinded havinf approved ticket*/
    public void createActivityForClaimRescinded(TripTicket tripTicket, TripClaim tripClaim) {
        Provider provider = providerDAO.findProviderByProviderId(tripClaim.getClaimantProvider().getProviderId());
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setTripTicketId(tripClaim.getTripTicket().getId());
        activityDTO.setAction("Claim rescinded");
        activityDTO.setActionDetails("status=rescinded,claimant_provider=" + provider.getProviderName());
        activityDTO.setActionTakenBy(getActionBy());
        activityService.createActivity(activityDTO);
    }


    public boolean deleteTripTickeByTripTicketId(int id) {
        var entity = tripTicketDAO.findTripTicketByTripTicketId(id);
        tripTicketVectorStoreService.deleteTripTicket(entity);
        tripTicketDAO.deleteTripTicketByTripTicketId(id);
        return true;
    }

    /*checking is provider exists or not */

    public boolean isOriginatorProviderExists(int providerId) {
        boolean resultValue = true;
        if (providerDAO.findProviderByProviderId(providerId) == null) {
            resultValue = false;
            return resultValue;
        }
        return resultValue;
    }


    public void sendMailToOriginatorForInvalidInput(List<String> messageList, int providerId) {

        //            fetch the users of that provider
        List<User> users = userNotificationDataDAO.getUsersOfProvider(providerId);
        for (User user : users) {
            //fetching single obj of the user role
            List<UserAuthority> userAuthority = new ArrayList<>();

            userAuthority.addAll(user.getAuthorities());
            String userrole = userAuthority.get(0).getAuthority();

            //checking for providerAdmin


            //NotificationEnginePart.. for trip ticket originator
            Notification emailNotificationForOriginator = new Notification();
            NotificationTemplate notificationTemplateForOriginator = new NotificationTemplate();

            emailNotificationForOriginator.setIsEMail(true);
            emailNotificationForOriginator.setStatusId(NotificationStatus.newStatus.status());
            notificationTemplateForOriginator.setNotificationTemplateId(NotificationTemplateCodeValue.invalidInput.templateCodeValue());
            emailNotificationForOriginator.setNotificationTemplate(notificationTemplateForOriginator);
            emailNotificationForOriginator.setNumberOfAttempts(0);
            emailNotificationForOriginator.setIsActive(true);
            emailNotificationForOriginator.setEmailTo(user.getEmail());

            //   Setting parameter values in according to the template for email notofication.
            Map ackToOriginatorTemplateMap = new HashMap<String, String>();
            ackToOriginatorTemplateMap.put("name", user.getName());

            String invalidFields = "";
            for (String message : messageList) {
                if (!invalidFields.contains(message.split(":")[0])) {
                    invalidFields = invalidFields + "&#8226;&nbsp;&nbsp;" + message.split(":")[0] + "<br>";
                }

            }
            invalidFields = invalidFields.trim();

            ackToOriginatorTemplateMap.put("invalidFields", invalidFields.substring(0, invalidFields.length() - 4).trim());
            ackToOriginatorTemplateMap.put("year", Year.now().toString());

            String jsonValueOfTemplate = "";

            Iterator<Map.Entry<String, String>> entries = ackToOriginatorTemplateMap.entrySet().iterator();
            while (entries.hasNext()) {

                Map.Entry<String, String> entry = entries.next();
                jsonValueOfTemplate = jsonValueOfTemplate + "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
                if (entries.hasNext()) {
                    jsonValueOfTemplate = jsonValueOfTemplate + ",";
                }

            }
            String FinaljsonValueOfTemplateForRescindClaimant = "{" + jsonValueOfTemplate + "}";

            //            sending mail to claimant provider
            emailNotificationForOriginator.setParameterValues(FinaljsonValueOfTemplateForRescindClaimant);
            emailNotificationForOriginator.setSubject("Trip exchange data issue");
            notificationDAO.createNotification(emailNotificationForOriginator);


        }
    }

    @Override
    public Object toDTO(Object bo) {

        TripTicket tripTicketBO = (TripTicket) bo;
        TripTicketDTO ticketDTO = tripTicketModelMapper.map(tripTicketBO, TripTicketDTO.class);
        ticketDTO.setEstimatedTripDistance(tripTicketBO.getEstimatedTripDistance());
        ticketDTO.setEstimatedTripTravelTime(tripTicketBO.getEstimatedTripTravelTime());

        return ticketDTO;

    }

    @Override
    public Object toBO(Object dto) {

        TripTicketDTO tripTicketDTO = (TripTicketDTO) dto;

//        stringtoLocalDateConversion
        TripTicket tripTicketBO = tripTicketModelMapper.map(tripTicketDTO, TripTicket.class);
        if (tripTicketDTO.getTripTicketProvisionalTime() != null) {
            tripTicketBO.setTripTicketProvisionalTime(StringToLocalDateTimeConverter.converterStringToLocalDate(tripTicketDTO.getTripTicketProvisionalTime()));
        }
        if (tripTicketDTO.getExpirationDate() != null) {
            tripTicketBO.setExpirationDate(StringToLocalDateTimeConverter.converterStringToLocalDate(tripTicketDTO.getExpirationDate()));
        }
        return tripTicketBO;

    }

    @Override
    public Object toDTOCollection(Object boCollection) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    public TripTicketDTO updateFundingSource(TripTicketDTO tripTicketDTO, List<String> fundingSources) {
        TripTicket tripTicket = tripTicketDAO.getFundingSourcebyTripTicketId(tripTicketDTO.getId());

        //jugad for storing multiple fundingSources into single column comma separated
        String storedFundingSource = "";
        for (String fundingSource : fundingSources) {
            storedFundingSource = storedFundingSource + "," + fundingSource;
        }
        String savedFund = storedFundingSource.substring(1);

        tripTicket.setTripFunders(savedFund);
        tripTicketDAO.updateTripTicket(tripTicket);
        TripTicketDTO tripTicketdDto = (TripTicketDTO) toDTO(tripTicket);
        return tripTicketdDto;
    }


    public ProviderTripCostDTO getTripticketCostForProvider(int tripticketId, int claimantProviderId) {
        // TODO Auto-generated method stub
        var totalCostOfOrgAndClaimantProvider = new ProviderTripCostDTO();

        try {
            TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(tripticketId);
            if (tripTicket == null) {
                totalCostOfOrgAndClaimantProvider.setErrorMessage("Incorrect TripticketId");
            }

            ProviderCost costForOriginatorProvider = providerCostDAO.findCostByProviderId(tripTicket.getOriginProvider().getProviderId());
            if (costForOriginatorProvider == null) {
                costForOriginatorProvider = new ProviderCost();
                totalCostOfOrgAndClaimantProvider.setErrorMessage("Cost For Originator Provider is not defined");
            }

            ProviderCost costForClaimantProvider = providerCostDAO.findCostByProviderId(claimantProviderId);

            if (costForClaimantProvider == null) {
                costForClaimantProvider = new ProviderCost();
                totalCostOfOrgAndClaimantProvider.setErrorMessage("Cost For Claimant Provider is not defined");
            }

            var timeAndDistance = tripTicketDistanceDAO.getDistanceByTripTicketId(tripTicket.getId());
            if (tripTicket.getRequesterProviderFare() != null) {
                totalCostOfOrgAndClaimantProvider.setTotalCostOfOrgProvider(tripTicket.getRequesterProviderFare());
            } else {
                var totalCostOfOrgProvider = getTotalCostOfProvider(tripTicket, timeAndDistance, costForOriginatorProvider);
                if (totalCostOfOrgProvider == null) {
                    totalCostOfOrgAndClaimantProvider.setErrorMessage("#totalCostOfOriginatorProvider response error");
                }
                totalCostOfOrgAndClaimantProvider.setTotalCostOfOrgProvider(totalCostOfOrgProvider);
            }

            var totalCostOfClaimantProvider = getTotalCostOfProvider(tripTicket, timeAndDistance, costForClaimantProvider);
            if (totalCostOfClaimantProvider == null) {
                totalCostOfOrgAndClaimantProvider.setErrorMessage("#totalCostOfClaimantProvider response error");
            } else {
                totalCostOfOrgAndClaimantProvider.setTotalCostOfClaimantProvider(totalCostOfClaimantProvider);
            }

        } catch (Exception e) {
            log.error("Error getting trip ticket cost for provider", e);
            return totalCostOfOrgAndClaimantProvider;
        }
        return totalCostOfOrgAndClaimantProvider;
    }


    public Float getTotalCostOfProvider(TripTicket tripTicket, TripTicketDistance getTimeAndDistance, ProviderCost costforProvider) {

        float miles = getTimeAndDistance.getTripTicketDistance();
        float hours = getTimeAndDistance.getTripTicketTime();
        float costPerMile = costforProvider.getCostPerMile();
        float costPerHr = costforProvider.getCostPerHour();
        float ambulatoryCost = costforProvider.getAmbularyCost();
        float wheelchairCost = 0;

        String serviceLevel = "AMB";
        if ( tripTicket.getServiceLevel() != null ) {
            serviceLevel = tripTicket.getServiceLevel();
        }
        if (serviceLevel.equalsIgnoreCase("wheelchair") ||
                serviceLevel.equalsIgnoreCase("wav") ||
                serviceLevel.equalsIgnoreCase("accessible") ) {

            wheelchairCost = costforProvider.getWheelchairCost();
            ambulatoryCost = 0;
        }
        log.debug("Calculate trip cost for trip ticket " + tripTicket.getId() + " with service level " + tripTicket.getServiceLevel() + " Miles=" + miles + ", Hours=" + hours + ", costPerMile=" + costPerMile + ", costPerHrs=" + costPerHr + ", ambulatoryCost=" + ambulatoryCost + ",  wheelchairCost=" + wheelchairCost);
        return (miles * costPerMile) + (hours * costPerHr) + ambulatoryCost + wheelchairCost;
    }


    // newly added

    public boolean checkForTicketExistsByTripId(TripTicketDTO tripTicketDTO) {
        boolean status = false;
        TripTicket existingTripTicket = tripTicketDAO.findTripTicketByTripTicketId(tripTicketDTO.getId());
        if (existingTripTicket != null) {
            if (existingTripTicket.getOriginProvider().getProviderId() != tripTicketDTO.getOriginProviderId()
                    && existingTripTicket.getRequesterTripId() != tripTicketDTO.getRequesterTripId()) {
                Set<ClaimantTripTicket> claimantTripTicketList = new HashSet<ClaimantTripTicket>();
                ClaimantTripTicket claimantTripTicket = new ClaimantTripTicket();
                claimantTripTicket.setTripTicket(existingTripTicket);
                claimantTripTicket.setClaimantTripId(tripTicketDTO.getRequesterTripId());
                Provider claimantProvider = providerDAO.findProviderByProviderId(tripTicketDTO.getOriginProviderId());
                claimantTripTicket.setClaimantProvider(claimantProvider);
                try {
                    claimantTripTicket = claimantTripticketDAO.createClaimantTripTicket(claimantTripTicket);
                } catch (Exception e) {
                    log.error("Error creating claimant trip ticket", e);
                    throw new HandlingExceptionForOKStatus("Combination of Claimant trip id = " + claimantTripTicket.getClaimantTripId() + " and trip id = " + claimantTripTicket.getTripTicket().getId() + " is found.");
                }
                claimantTripTicketList.add(claimantTripTicket);
                existingTripTicket.setClaimantTripTicket(claimantTripTicketList);
                tripTicketDAO.updateTripTicket(existingTripTicket);
                status = true;
            } else {
                if (existingTripTicket.getOriginProvider().getProviderId() == tripTicketDTO.getOriginProviderId()
                        && existingTripTicket.getId() == tripTicketDTO.getId()) {
                    status = true;
                } else {
                    throw new InvalidInputException("Something went wrong, please try with same provider");
                }
            }
        }
        return status;
    }


    public TripTicketDTO getUpdatedTripTicket(TripTicket availableTripTicket, TripTicketDTO tripTicketDTO) throws InvalidInputException {

        LocalDate pickUpDate = tripTicketDTO.getRequestedPickupDate();
        LocalDate dropOffDate = tripTicketDTO.getRequestedDropoffDate();

        // checking for claim cancellation
        if (availableTripTicket.getApprovedTripClaim() != null) {
            tripTicketDTO = methodForClaimCancellation(availableTripTicket, tripTicketDTO, pickUpDate, dropOffDate);
        } else {
            // handle for pending status
            boolean checkForPendingTripClaimExists = tripClaimDAO
                    .checkForTripTicketPresentWithPendingStatus(availableTripTicket.getId());

            if (checkForPendingTripClaimExists) {
                tripTicketDTO = methodForClaimCancellation(availableTripTicket, tripTicketDTO, pickUpDate, dropOffDate);
            } else {

                /* if tripticket not claimed,available tripticket update all json */
                Provider originatorProviderObj = providerDAO.findProviderByProviderId(tripTicketDTO.getOriginProviderId());

                if (pickUpDate != null) {
                    LocalDate localDate = minusDaysSkippingWeekends(pickUpDate,
                            originatorProviderObj.getTripTicketExpirationDaysBefore());
                    String expirationDateTimeString = localDate + "T" + originatorProviderObj.getTripTicketExpirationTimeOfDay().toString();
                    tripTicketDTO.setExpirationDate(expirationDateTimeString);
                } else if (dropOffDate != null) {
                    LocalDate localDate = minusDaysSkippingWeekends(dropOffDate,
                            originatorProviderObj.getTripTicketExpirationDaysBefore());
                    String expirationDateTimeString = localDate + "T" + originatorProviderObj.getTripTicketExpirationTimeOfDay().toString();
                    tripTicketDTO.setExpirationDate(expirationDateTimeString);
                }

                // newly added to update distance and time using azure api
                if (!(availableTripTicket.getPickupAddress().getLatitude() == (tripTicketDTO.getPickupAddress().getLatitude())
                        && availableTripTicket.getPickupAddress().getLongitude() == (tripTicketDTO.getPickupAddress().getLongitude())
                        && availableTripTicket.getDropOffAddress().getLatitude() == (tripTicketDTO.getDropOffAddress().getLatitude())
                        && availableTripTicket.getDropOffAddress().getLongitude() == (tripTicketDTO.getDropOffAddress().getLongitude()))) {

                    TripTicketDistanceDTO timeAndDistanceDTO = tripTicketDistanceService.checkDistanceTime(tripTicketDTO);
                    if (timeAndDistanceDTO == null) {
                        log.debug("tripticketDTO " + tripTicketDTO + " timeAndDistanceDTO is null");
                        throw new InvalidInputException(
                                "The Lat-Long is invalid line 1883, Ticket is Not Updated. Please try again.");
                    }
                    tripTicketDistanceService.updateDistanceTime(availableTripTicket.getId(), tripTicketDTO, timeAndDistanceDTO);
                }

                tripTicketDTO.setId(availableTripTicket.getId());

                StatusDTO statusDTO = new StatusDTO();
                statusDTO.setStatusId(availableTripTicket.getStatus().getStatusId());
                tripTicketDTO.setStatus(statusDTO);

                tripTicketDTO.setCommonTripId(availableTripTicket.getCommonTripId());
                tripTicketDTO.setLastStatusChangedByProviderId(tripTicketDTO.getOriginProviderId());
                TripTicket tripTicket = (TripTicket) toBO(tripTicketDTO);
                TripTicket updatedDbObjTripTicket = tripTicketDAO.updateTripTicket(tripTicket);
                tripTicketDTO = (TripTicketDTO) toDTO(updatedDbObjTripTicket);
            }
        }

        return tripTicketDTO;
    }


    private TripTicketDTO methodForClaimCancellation(TripTicket availableTripTicket, TripTicketDTO tripTicketDTO, LocalDate pickUpDate, LocalDate dropOffDate) {
        LocalDate pickDateBo = availableTripTicket.getRequestedPickupDate();
        LocalDate dropDateBo = availableTripTicket.getRequestedDropoffDate();
        Time pickTimeBo = availableTripTicket.getRequestedPickupTime();
        Time dropTimeBo = availableTripTicket.getRequestedDropOffTime();
        String tripFunderBo = (availableTripTicket.getTripFunders() != null && !availableTripTicket.getTripFunders().isEmpty()) ?
                availableTripTicket.getTripFunders() : null;
        String custEligibilityFactorsBo = (availableTripTicket.getCustomerEligibilityFactors() != null && !availableTripTicket.getCustomerEligibilityFactors().isEmpty()) ?
                availableTripTicket.getCustomerEligibilityFactors() : null;

        Time pickTimeDTO = tripTicketDTO.getRequestedPickupTime();
        Time dropTimeDTO = tripTicketDTO.getRequestedDropOffTime();

        log.debug("     pickDateBo-----" + pickDateBo + "    pickTimeBo------" + pickTimeBo + "    dropDate------" + dropDateBo + "    dropTime----" + dropTimeBo +
                "    fundingsource----" + tripFunderBo + "   custeligibilityfactor--- " + custEligibilityFactorsBo + tripTicketDTO.getCustomerEligibilityFactors());
        //for claim cancellation
        if (!(Objects.equals(availableTripTicket.getGuests(), tripTicketDTO.getGuests()) &&
                Objects.equals(availableTripTicket.getAttendants(), tripTicketDTO.getAttendants()) &&
                availableTripTicket.getPickupAddress().getAddressId() == tripTicketDTO.getPickupAddress().getAddressId() &&
                availableTripTicket.getDropOffAddress().getAddressId() == tripTicketDTO.getDropOffAddress().getAddressId() &&
                StringUtils.equalsIgnoreCase(custEligibilityFactorsBo, tripTicketDTO.getCustomerEligibilityFactors()) &&
                StringUtils.equalsIgnoreCase(tripFunderBo, tripTicketDTO.getTripFunders()) &&
                Objects.equals(pickDateBo, pickUpDate) &&
                Objects.equals(pickTimeBo, pickTimeDTO) &&
                Objects.equals(dropDateBo, dropOffDate) &&
                Objects.equals(dropTimeBo, dropTimeDTO) &&
                availableTripTicket.isCustomerServiceAnimals() == tripTicketDTO.isCustomerServiceAnimals() &&
                availableTripTicket.isOutsideCoreHours() == tripTicketDTO.isOutsideCoreHours() &&
                availableTripTicket.isTripIsolation() == tripTicketDTO.getIsTripIsolation() &&
                Objects.equals(availableTripTicket.getCustomerSeatsRequired(), tripTicketDTO.getCustomerSeatsRequired()) &&
                Objects.equals(availableTripTicket.getTimeWindowBefore(), tripTicketDTO.getTimeWindowBefore()) &&
                Objects.equals(availableTripTicket.getTimeWindowAfter(), tripTicketDTO.getTimeWindowAfter()))) {

            //sending mail for claim cancellation
            TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(availableTripTicket.getId());
            Set<TripClaim> tripClaims = tripTicket.getTripClaims();
            for (TripClaim tripClaim : tripClaims) {
                if (tripClaim.getStatus().getStatusId() != TripClaimStatusConstants.cancelled.tripClaimStatusUpdate()) {
                    var actionBy = getActionBy();
                    tripClaimService.sendingMailForCancellationOfTripClaim(tripTicket, tripClaim.getId(), actionBy);
                }
            }

            Provider originatorProviderObj = providerDAO
                    .findProviderByProviderId(tripTicketDTO.getOriginProviderId());

            if (pickDateBo != null || dropDateBo != null) {
                if (pickUpDate != null) {
                    LocalDate localDate = minusDaysSkippingWeekends(pickUpDate,
                            originatorProviderObj.getTripTicketExpirationDaysBefore());

                    String expirationDateTimeString = localDate + "T"
                            + originatorProviderObj.getTripTicketExpirationTimeOfDay().toString();
                    tripTicketDTO.setExpirationDate(expirationDateTimeString);
                } else if (dropOffDate != null) {
                    LocalDate localDate = minusDaysSkippingWeekends(dropOffDate,
                            originatorProviderObj.getTripTicketExpirationDaysBefore());

                    String expirationDateTimeString = localDate + "T"
                            + originatorProviderObj.getTripTicketExpirationTimeOfDay().toString();
                    tripTicketDTO.setExpirationDate(expirationDateTimeString);

                }
            }

            // newly added to update distance and time using azure api
            if (!(availableTripTicket.getPickupAddress().getLatitude() == (tripTicketDTO.getPickupAddress().getLatitude())
                    && availableTripTicket.getPickupAddress().getLongitude() == (tripTicketDTO.getPickupAddress().getLongitude())
                    && availableTripTicket.getDropOffAddress().getLatitude() == (tripTicketDTO.getDropOffAddress().getLatitude())
                    && availableTripTicket.getDropOffAddress().getLongitude() == (tripTicketDTO.getDropOffAddress().getLongitude()))) {

                TripTicketDistanceDTO timeAndDistanceDTO = tripTicketDistanceService.checkDistanceTime(tripTicketDTO);
                if (timeAndDistanceDTO == null) {
                    throw new InvalidInputException("The Lat-Long is invalid, Ticket is Not Updated. Please try again.");
                }
                tripTicketDistanceService.updateDistanceTime(availableTripTicket.getId(), tripTicketDTO, timeAndDistanceDTO);

            }

            tripTicketDTO.setId(availableTripTicket.getId());

            StatusDTO statusDTO = new StatusDTO();
            statusDTO.setStatusId(TripTicketStatusConstants.available.tripTicketStatusUpdate());
            tripTicketDTO.setStatus(statusDTO);

            tripTicketDTO.setCommonTripId(availableTripTicket.getCommonTripId());
            tripTicketDTO.setLastStatusChangedByProviderId(tripTicketDTO.getOriginProviderId());
            TripTicket tripTicketBo = (TripTicket) toBO(tripTicketDTO);
            TripTicket updatedDbObjTripTicket = tripTicketDAO.updateTripTicket(tripTicketBo);
            tripTicketDTO = (TripTicketDTO) toDTO(updatedDbObjTripTicket);
        } else {
            // if tripticket claimed then update note only
            availableTripTicket.setTripNotes(tripTicketDTO.getTripNotes());
            availableTripTicket = tripTicketDAO.updateTripTicket(availableTripTicket);
            tripTicketDTO = (TripTicketDTO) toDTO(availableTripTicket);
        }
        return tripTicketDTO;
    }


    public void changedTripTicketStatusToNoShow(TripTicket availableTripTicket) {
        Status status = new Status();
        status.setStatusId(TripTicketStatusConstants.noShow.tripTicketStatusUpdate());
        availableTripTicket.setStatus(status);
        tripTicketDAO.updateTripTicket(availableTripTicket);
    }


    public boolean isUberRide(int tripTicketId ) {
        var tripTicket = findTripTicketByTripTicketId(tripTicketId);
        if ( tripTicket != null ) {
            var approvedClaim = tripTicket.getApprovedTripClaimId();
            var uberProvider = providerService.findUberProvider();
            if ( approvedClaim != null ) {
                var claim = tripClaimService.findTripClaimByTripClaimId(approvedClaim);
                if ( claim != null && claim.getClaimantProviderId() != 0 ) {
                    if  ( uberProvider != null && claim.getClaimantProviderId() == (uberProvider.getProviderId()) ) {
                        return true;

                    }
                }
            }
            if (tripTicket.getProvisionalProviderId() != null) {
                return (uberProvider != null && tripTicket.getProvisionalProviderId().equals(uberProvider.getProviderId()));
            }
        }
        return false;
    }


    public void changeTripTicketStatusToCancel(TripTicket availableTripTicket, CancelRequest cancelRequest) {

        Status status = new Status();
        status.setStatusId(TripTicketStatusConstants.cancelled.tripTicketStatusUpdate());
        availableTripTicket.setStatus(status);
        tripTicketDAO.updateTripTicket(availableTripTicket);
        // sending ACK mail to requester and claimant provider for cancel trip
        sendACKMailToOriginatorForTripCancellation(availableTripTicket);
        rescindTripTicket(availableTripTicket.getId());

        // see if this already has a TripResult, it so use it
        AtomicReference<TripResultDTO> tripResult = new AtomicReference<>();
        tripResultService.findAllTripResultByTripTicketId(availableTripTicket.getId()).forEach(resultLookup -> {
            tripResult.set(resultLookup);
            tripResult.get().setVersion(2);
        });
        if ( tripResult.get() == null ) {
            tripResult.set(new TripResultDTO());
            tripResult.get().setVersion(1);
        }

        LocalDate localDate = LocalDate.now();
        LocalDateTime localDateTime = localDate.atStartOfDay(); // or localDate.atTime(12, 0) for noon
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        tripResult.get().setTripDate(date);

        tripResult.get().setTripTicketId(availableTripTicket.getId());
        tripResult.get().setCancellationReason(cancelRequest.reason());

        var comment = new TripTicketCommentDTO();
        comment.setTripTicketId(availableTripTicket.getId());
        comment.setUserName(userService.getCurrentUserName());
        comment.setBody(cancelRequest.reason());
        comment.setUserId(userService.getCurrentUserId());
        tripTicketCommentService.createTripTicketComment(availableTripTicket.getId(), comment);

        tripResultService.updateTripResult(tripResult.get());
    }


    public List<String> listOfDistinctCustomerEligibility() {
        return tripTicketDAO.listOfDistinctCustomerEligibility();
    }


    public void sendACKMailToOriginatorForTripCancellation(TripTicket availableTripTicket) {

        TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(availableTripTicket.getId());
        // sending ACK mail to claimant for claim cancellation
        Set<TripClaim> tripClaims = tripTicket.getTripClaims();
        var actionBy = getActionBy();
        for (TripClaim tripClaim : tripClaims) {
            if (tripClaim.getStatus().getStatusId() != TripTicketStatusConstants.cancelled.tripTicketStatusUpdate()) {
                tripClaimService.sendACKMailToClaimantForTripCancellation(tripTicket, tripClaim.getId(), actionBy);
            }
        }

        /*add record as  activity */
        createActivityForTripTicketForCancelledTripTicket(tripTicket);

        //            fetch the users of that provider
        List<User> users = userNotificationDataDAO.getUsersForTripCancel(tripTicket.getOriginProvider().getProviderId());
        for (User user : users) {

            //checking for checkbox selected
            if (user.isIsNotifyTripCancelled()) {

                //NotificationEnginePart.. for trip ticket originator
                Notification emailNotificationForOriginator = new Notification();
                NotificationTemplate notificationTemplateForOriginator = new NotificationTemplate();

                emailNotificationForOriginator.setIsEMail(true);
                emailNotificationForOriginator.setStatusId(NotificationStatus.newStatus.status());
                notificationTemplateForOriginator.setNotificationTemplateId(NotificationTemplateCodeValue.tripTicketCancelledTemplateCode.templateCodeValue());
                emailNotificationForOriginator.setNotificationTemplate(notificationTemplateForOriginator);
                emailNotificationForOriginator.setNumberOfAttempts(0);
                emailNotificationForOriginator.setIsActive(true);
                emailNotificationForOriginator.setEmailTo(user.getEmail());

                //   Setting parameter values in according to the template for email notofication.
                Map ackToOriginatorTemplateMap = new HashMap<String, String>();
                ackToOriginatorTemplateMap.put("name", user.getName());
                ackToOriginatorTemplateMap.put("tripTicketnumber", tripTicket.getCommonTripId());
                ackToOriginatorTemplateMap.put("year", Year.now().toString());


                String jsonValueOfTemplate = "";
                Iterator<Map.Entry<String, String>> entries = ackToOriginatorTemplateMap.entrySet().iterator();
                while (entries.hasNext()) {

                    Map.Entry<String, String> entry = entries.next();
                    jsonValueOfTemplate = jsonValueOfTemplate + "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
                    if (entries.hasNext()) {
                        jsonValueOfTemplate = jsonValueOfTemplate + ",";
                    }

                }
                String FinaljsonValueOfTemplateForRescindClaimant = "{" + jsonValueOfTemplate + "}";

                //            sending mail to claimant provider
                emailNotificationForOriginator.setParameterValues(FinaljsonValueOfTemplateForRescindClaimant);
                emailNotificationForOriginator.setSubject("Trip ticket is cancelled");
                notificationDAO.createNotification(emailNotificationForOriginator);
            }

        }
    }

    /**
     * Creates an activity record for a trip ticket when it is cancelled.
     *
     * @param tripTicket TripTicket object
     */
    public void createActivityForTripTicketForCancelledTripTicket(TripTicket tripTicket) {
        Provider provider = providerDAO.findProviderByProviderId(tripTicket.getOriginProvider().getProviderId());
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setTripTicketId(tripTicket.getId());
        activityDTO.setAction("Trip ticket cancelled");
        activityDTO.setActionDetails("status=Cancelled,originator_provider=" + provider.getProviderName() + ",requested pickup date=" + tripTicket.getRequestedPickupDate() + ", requested_pickup_time=" + tripTicket.getRequestedPickupTime());
        activityDTO.setActionTakenBy(getActionBy());
        activityService.createActivity(activityDTO);
    }


    public TripTicket getTripTicketByTripTicketId(int id) {
        // TODO Auto-generated method stub
        return tripTicketDAO.findTripTicketByTripTicketId(id);
    }


    public String updatedLastSyncDateAndTripStatus(InputDTO inputDTO) {
        User user = getCurrentUser();
        log.debug("user&&&&&==" + user.toString());
        User originalUser = userDAO.findUserByUserId(user.getId());
        Provider provider = providerDAO.findProviderByProviderId(originalUser.getProvider().getProviderId());
        String msg = null;
        boolean status = false;

        if (!inputDTO.getTripTicketIds().isEmpty()) {
            List<TripClaim> tripClaims = tripClaimDAO.findAllTripClaimsForProvider(inputDTO.getTripTicketIds(), provider.getProviderId());
            if (!tripClaims.isEmpty()) {


                for (TripClaim tripClaim : tripClaims) {
                    try {
                        tripClaim.setNewRecord(false);
                        tripClaim = tripClaimDAO.updateTripClaim(tripClaim);
                        status = true;
                    } catch (Exception e) {
                        log.error("Error updating trip claim record", e);
                    }
                }

            }
        }
        if (inputDTO.getLastSyncDateTime() != null && inputDTO.getLastSyncDateTime() != "") {
            //for '-' 3rd occurrence  lastSyncDateTime is "2021-01-08T03:56:52.103433-07:00[GMT-07:00]"
            String[] lastSyncDateTimeArr = inputDTO.getLastSyncDateTime().split("(?<=\\G.*-.*-.*)-");
            provider.setLastSyncDateTime(lastSyncDateTimeArr[0]);
            providerDAO.updateProvider(provider);
        }

        if (status) {
            msg = "Successfully updated tripTicketIds and lastSyncDateTime for the provider";
        } else {
            msg = "Successfully updated lastSyncDateTime for the provider";
        }
        return msg;
    }


    public boolean checkForTicketExistsWithRequesterTripIdForProvider(TripTicketDTO tripTicketDTO) {
        return tripTicketDAO.checkForTicketExistsByRequesterTripIdAndOriginProvider(tripTicketDTO);
    }

    /**
     * Get paginated trip tickets
     *
     * @param first The index of the first record to fetch
     * @param rows  The number of rows to fetch
     * @return List of paginated trip tickets
     */
    public Page<TripTicketDTO> getPaginatedTripTickets(int first, int rows) {
        Pageable pageable = PageRequest.of(first / rows, rows);
        Page<TripTicket> tripTicketPage = tripTicketDAO.findAll(pageable);


        var dtos = tripTicketPage.getContent().stream()
                .map(tripTicket -> modelMapper.map(tripTicket, TripTicketDTO.class))
                .collect(Collectors.toList());


        return new PageImpl<>(dtos, pageable, tripTicketPage.getTotalElements());
    }


    public User getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null &&  (authentication instanceof UserAuthentication)) {
            UserAuthentication userAuth = (UserAuthentication) authentication;
            return userAuth.getAuthenticatedUser(); // full User entity
        }
        return null;
    }


    public String getActionBy() {
        User currentUser = getCurrentUser();
        String providerName = currentUser != null && currentUser.getProvider() != null ? " [" + currentUser.getProvider().getProviderName() + "]" : "";
        return currentUser != null ? currentUser.getName() + providerName : "N/A";
    }


}

