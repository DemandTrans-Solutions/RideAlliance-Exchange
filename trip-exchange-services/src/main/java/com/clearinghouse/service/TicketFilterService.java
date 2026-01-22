/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;

import com.clearinghouse.controller.rest.TicketFilterController;
import com.clearinghouse.controller.rest.TripTicketController;
import com.clearinghouse.dao.TicketFilterDAO;
import com.clearinghouse.dto.*;
import com.clearinghouse.entity.TicketFilter;
import com.clearinghouse.entity.TripTicket;
import com.clearinghouse.enumentity.TripTicketStatusConstants;
import com.clearinghouse.listresponseentity.AddressListBO;
import com.clearinghouse.listresponseentity.AddressListByString;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author chaitanyaP
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class TicketFilterService implements IConvertBOToDTO, IConvertDTOToBO {

    private final TicketFilterDAO filterDAO;
    private final UserService userService;
    private final ModelMapper filterModelMapper;
    private final ModelMapper tripTicketModelMapper;
    private final ModelMapper providerModelMapper;
    private final WorkingHoursService workingHoursService;
    private final UserContextService userContextService;
    private final DetailedTripTicketConverterService detailedTripTicketConverterService;

    /**
     * Retrieves all ticket filters from the database.
     *
     * @return List of TicketFilterDTO objects representing all filters.
     */
    public List<TicketFilterDTO> findAllFilters() {

        List<TicketFilter> filters = filterDAO.findAllFilters();

        List<TicketFilterDTO> filterDTOList = new java.util.ArrayList<>();
        for (TicketFilter ticketFilter : filters) {

            filterDTOList.add((TicketFilterDTO) toDTO(ticketFilter));
        }

        return filterDTOList;

    }

    /**
     * Finds a ticket filter by its filter ID.
     *
     * @param filterId the ID of the filter to retrieve
     * @return TicketFilterDTO object for the specified filter
     */
    public TicketFilterDTO findFilterByFilterId(int filterId) {

        return (TicketFilterDTO) toDTO(filterDAO.findFilterByFilterId(filterId));

    }

    /**
     * Creates a new ticket filter in the database.
     *
     * @param filterDTO the filter data to create
     * @return the created TicketFilterDTO
     */
    public TicketFilterDTO createFilter(TicketFilterDTO filterDTO) {

        TicketFilter ticketFilter = (TicketFilter) toBO(filterDTO);
        ticketFilter.setIsActive(true);
        TicketFilter filterBO = filterDAO.createFilter(ticketFilter);
        return (TicketFilterDTO) toDTO(filterBO);

    }

    /**
     * Updates an existing ticket filter in the database.
     *
     * @param filterDTO the filter data to update
     * @return the updated TicketFilterDTO
     */
    public TicketFilterDTO updateFilter(TicketFilterDTO filterDTO) {

        TicketFilter ticketFilter = (TicketFilter) toBO(filterDTO);
        filterDAO.updateFilter(ticketFilter);

        return (TicketFilterDTO) toDTO(ticketFilter);

    }

    /**
     * Deletes a ticket filter by its filter ID.
     *
     * @param filterId the ID of the filter to delete
     * @return true if deletion was successful
     */
    public boolean deleteFilterByFilterId(int filterId) {

        filterDAO.deleteFilterByFilterId(filterId);
        return true;

    }

    /**
     * Retrieves all ticket filters for a specific user.
     *
     * @param userId the user ID to filter by
     * @return List of TicketFilterDTO objects for the user
     */
    public List<TicketFilterDTO> findAllFiltersByUserId(int userId) {

        List<TicketFilter> filters = filterDAO.findAllFiltersByUserId(userId);

        List<TicketFilterDTO> filterDTOList = new java.util.ArrayList<>();
        for (TicketFilter ticketFilter : filters) {

            filterDTOList.add((TicketFilterDTO) toDTO(ticketFilter));
        }

        return filterDTOList;

    }

    /**
     * Filters trip tickets based on the provided filter object.
     *
     * @param filterDTO the filter criteria
     * @return Map containing totalRecords and data (list of DetailedTripTicketDTO objects matching the filter)
     */
    public Map<String, Object> filterTicketsByFilterObject(TicketFilterDTO filterDTO) {
        var userContext = userContextService.extractUserContext();
        if (filterDTO.getUserId() == 0) {
            filterDTO.setUserId(userContext.userId());
        }


        // Convert snake_case to camelCase
        if (filterDTO != null && filterDTO.getSortField() != null && filterDTO.getSortField().contains("_")) {
            String camelCaseField = TripTicketController.convertSnakeToCamelCase(filterDTO.getSortField());
            if (camelCaseField.equals("originProviderId")) {
                camelCaseField = "originProvider.providerId";
            } else if (camelCaseField.equals("originProviderName")) {
                camelCaseField = "originProvider.providerName";
            }
            filterDTO.setSortField(camelCaseField);
        }


        List<TripTicket> tripTickets = filterDAO.getTripTicketsByTicketFilterObject(filterDTO);
        //List<TripTicket> processedTickets = processAndSortTickets(tripTickets);
        var processedTickets = tripTickets; // Why would the filter process tickets, when the filter is doing this????

        List<DetailedTripTicketDTO> dtos = detailedTripTicketConverterService.convertToDetailedTripTicketDTOs(processedTickets);

        // Apply pagination if specified
        int pageSize = filterDTO.getPagesize();
        int pageNumber = filterDTO.getPagenumber();
        if (pageSize > 0 && pageNumber > 0) {
            int startIndex = (pageNumber - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, dtos.size());
            if (startIndex < dtos.size()) {
                dtos = dtos.subList(startIndex, endIndex);
            } else {
                dtos = new ArrayList<>();
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalRecords", processedTickets.size());
        response.put("data", dtos);

        return response;
    }


    private List<TripTicket> processAndSortTickets(List<TripTicket> tripTickets) {
        List<TripTicket> filteredTickets = filterOutNoShowTickets(tripTickets);
        return sortTicketsByPriorityAndDateTime(filteredTickets);
    }

    private List<TripTicket> filterOutNoShowTickets(List<TripTicket> tripTickets) {
        return tripTickets.stream()
                .filter(ticket -> ticket.getStatus().getStatusId() != TripTicketStatusConstants.noShow.tripTicketStatusUpdate())
                .collect(Collectors.toList());
    }

    private List<TripTicket> sortTicketsByPriorityAndDateTime(List<TripTicket> tickets) {
        Map<Boolean, List<TripTicket>> partitionedTickets = tickets.stream()
                .collect(Collectors.partitioningBy(this::hasPickupDateTime));

        List<TripTicket> pickupTickets = partitionedTickets.get(true);
        List<TripTicket> dropOffOnlyTickets = partitionedTickets.get(false);

        List<TripTicket> sortedPickupTickets = sortPickupTicketsByClaimStatus(pickupTickets);
        dropOffOnlyTickets.sort(createDropOffDateTimeComparator());

        List<TripTicket> result = new ArrayList<>(sortedPickupTickets);
        result.addAll(dropOffOnlyTickets);

        return result;
    }

    private boolean hasPickupDateTime(TripTicket ticket) {
        return ticket.getRequestedPickupDate() != null && ticket.getRequestedPickupTime() != null;
    }

    private List<TripTicket> sortPickupTicketsByClaimStatus(List<TripTicket> pickupTickets) {
        Map<Boolean, List<TripTicket>> claimPartition = pickupTickets.stream()
                .collect(Collectors.partitioningBy(this::isUnclaimedAvailableTicket));

        List<TripTicket> unclaimedTickets = claimPartition.get(true);
        List<TripTicket> claimedTickets = claimPartition.get(false);

        Comparator<TripTicket> pickupComparator = createPickupDateTimeComparator();
        unclaimedTickets.sort(pickupComparator);
        claimedTickets.sort(pickupComparator);

        unclaimedTickets.addAll(claimedTickets);
        return unclaimedTickets;
    }

    private boolean isUnclaimedAvailableTicket(TripTicket ticket) {
        return ticket.getStatus().getStatusId() == TripTicketStatusConstants.available.tripTicketStatusUpdate()
                && ticket.getTripClaims().isEmpty();
    }

    private Comparator<TripTicket> createPickupDateTimeComparator() {
        return (t1, t2) -> {
            int dateComparison = t1.getRequestedPickupDate().compareTo(t2.getRequestedPickupDate());
            return dateComparison != 0 ? dateComparison
                    : t1.getRequestedPickupTime().compareTo(t2.getRequestedPickupTime());
        };
    }

    private Comparator<TripTicket> createDropOffDateTimeComparator() {
        return (t1, t2) -> {
            int dateComparison = t1.getRequestedDropoffDate().compareTo(t2.getRequestedDropoffDate());
            return dateComparison != 0 ? dateComparison
                    : t1.getRequestedDropOffTime().compareTo(t2.getRequestedDropOffTime());
        };
    }

    private List<DetailedTripTicketDTO> convertToDetailedDTOs(List<TripTicket> tickets, UserContextDTO userContext) {
        return tickets.stream()
                .map(ticket -> convertToDetailedDTO(ticket, userContext))
                .collect(Collectors.toList());
    }

    private DetailedTripTicketDTO convertToDetailedDTO(TripTicket tripTicket, UserContextDTO userContext) {
        DetailedTripTicketDTO detailedTicketDTO = tripTicketModelMapper.map(tripTicket, DetailedTripTicketDTO.class);
        
        // Fix for provider name mapping issue
        com.clearinghouse.util.TripClaimMappingUtil.fixTripClaimProviderNames(tripTicket, detailedTicketDTO);

        setClaimantIfPresent(tripTicket, detailedTicketDTO);
        setEligibilityForClaim(tripTicket, detailedTicketDTO, userContext);
        setOriginator(tripTicket, detailedTicketDTO);

        return detailedTicketDTO;
    }

    private void setClaimantIfPresent(TripTicket tripTicket, DetailedTripTicketDTO detailedTicketDTO) {
        if (tripTicket.getApprovedTripClaim() != null) {
            ProviderDTO providerDTO = providerModelMapper.map(
                    tripTicket.getApprovedTripClaim().getClaimantProvider(), ProviderDTO.class);
            detailedTicketDTO.setClaimant(providerDTO);
        }
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


    /**
     * Retrieves address details by address ID.
     *
     * @param addressId the address ID
     * @return AddressListByString object for the address
     */
    public AddressListByString getAddressById(int addressId) {
        AddressListBO addressBO = filterDAO.getAllAddressById(addressId);
        AddressListByString addressByString = toAddressListByStringDTO(addressBO);
        return addressByString;
    }

    /**
     * Converts a business object to a DTO.
     *
     * @param bo the business object
     * @return the corresponding DTO object
     */
    @Override
    public Object toDTO(Object bo) {

        TicketFilter ticketFilterBO = (TicketFilter) bo;
        TicketFilterDTO ticketFilterDTO = filterModelMapper.map(ticketFilterBO, TicketFilterDTO.class);

        // Add null safety checks before calling string methods
        String ticketStatus = "";
        if (ticketFilterBO.getTicketFilterstatus() != null) {
            ticketStatus = (ticketFilterBO.getTicketFilterstatus().replaceAll("\\[", "")).trim().replaceAll("\\]", "").trim();
        }
        if (ticketStatus.trim().equalsIgnoreCase("")) {
            ticketStatus = ticketStatus + "".trim();
        }
        List<String> ticketStatusList = Arrays.asList(ticketStatus.split(","));

        String originatingProviderName = "";
        if (ticketFilterBO.getOriginatingProviderName() != null) {
            originatingProviderName = (ticketFilterBO.getOriginatingProviderName().replaceAll("\\[", "")).replaceAll("\\]", "").trim();
        }
        if (originatingProviderName.trim().equalsIgnoreCase("")) {
            originatingProviderName = originatingProviderName.trim();
        }

        List<String> originatingProviderNameList = Arrays.asList(originatingProviderName.trim().split(","));

        String claimingProviderName = "";
        if (ticketFilterBO.getClaimingProviderName() != null) {
            claimingProviderName = (ticketFilterBO.getClaimingProviderName().replaceAll("\\[", "")).replaceAll("\\]", "").trim();
        }
        if (claimingProviderName.trim().equalsIgnoreCase("")) {
            claimingProviderName = claimingProviderName.trim();
        }

        List<String> claimingProviderNameList = Arrays.asList(claimingProviderName.trim().split(","));

        String advancedFilterParameter = "";
        if (ticketFilterBO.getAdvancedFilterParameter() != null) {
            advancedFilterParameter = (ticketFilterBO.getAdvancedFilterParameter().trim().replaceAll("\\[", "")).replaceAll("\\]", "").trim();
        }
        String[] advancedFilterParameterList = advancedFilterParameter.trim().split(",");

        List<String> NewAdvFilterparameter = new ArrayList<>();

        for (String s : advancedFilterParameterList) {
            NewAdvFilterparameter.add(s.trim());

        }

        String tripTime = "";
        if (ticketFilterBO.getTripTime() != null) {
            tripTime = (ticketFilterBO.getTripTime().replaceAll("\\[", "")).replaceAll("\\]", "").trim();
        }
        List<String> tripTimeList = Arrays.asList(tripTime.split(","));

        //newly added by shankarI
        String tripFunders = (ticketFilterBO.getFundingSourceList().replaceAll("\\[", "")).replaceAll("\\]", "").trim();
        List<String> tripFundersList = Arrays.asList(tripFunders.split(","));

        String tripPickUpStartEndTime = (ticketFilterBO.getReqPickUpStartAndEndTime().replaceAll("\\[", "")).replaceAll("\\]", "").trim();
        List<String> tripPickUpStartEndTimeList = Arrays.asList(tripPickUpStartEndTime.split(","));

        String geographicSA = (ticketFilterBO.getSelectedGeographicVal().replaceAll("\\[", "")).replaceAll("\\]", "").trim();
        if (geographicSA.trim().equalsIgnoreCase("")) {
            geographicSA = geographicSA.trim();
        }

        List<String> geographicSAList = Arrays.asList(geographicSA.trim().split(","));


        String customerEligibility = (ticketFilterBO.getCustomerEligibility().replaceAll("\\[", "")).replaceAll("\\]", "").trim();
        List<String> customerEligibilityList = Arrays.asList(customerEligibility.split(","));

        String hospitalityServiceArea = (ticketFilterBO.getHospitalityServiceArea().replaceAll("\\[", "")).replaceAll("\\]", "").trim();
        List<String> hospitalityServiceAreaList = Arrays.asList(hospitalityServiceArea.split(","));

        ticketFilterDTO = filterModelMapper.map(ticketFilterBO, TicketFilterDTO.class
        );

        ticketFilterDTO.setTicketFilterstatus(ticketStatusList);
        ticketFilterDTO.setOriginatingProviderName(originatingProviderNameList);
        ticketFilterDTO.setClaimingProviderName(claimingProviderNameList);
        ticketFilterDTO.setAdvancedFilterParameter(NewAdvFilterparameter);
        ticketFilterDTO.setTripTime(tripTimeList);

        return ticketFilterDTO;

    }

    @Override
    public Object toBO(Object dto) {

        TicketFilterDTO filterDTO = (TicketFilterDTO) dto;
        TicketFilter ticketFilterBO = filterModelMapper.map(filterDTO, TicketFilter.class);
//        ticketFilterBO.setRescindedApplyStatusParameter(filterDTO.getRescindedApplyStatusParameter().toString());
        return ticketFilterBO;

    }

    public Object toDTOCustomizedForTripTicket(Object bo) {
        TripTicket tripTicketBO = (TripTicket) bo;
        TripTicketDTO ticketDTO = tripTicketModelMapper.map(tripTicketBO, TripTicketDTO.class
        );
        return ticketDTO;

    }

    @Override
    public Object toDTOCollection(Object boCollection) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public AddressListByString toAddressListByStringDTO(AddressListBO addressListBO) {
        AddressListByString addressListByString = new AddressListByString();
        addressListByString.setAddressId(addressListBO.getAddressId());
        addressListByString.setAddress(addressListBO.getStreet1() + "," + addressListBO.getStreet2() + "," + addressListBO.getCity() + "," + addressListBO.getCounty() + "," + addressListBO.getState() + "," + addressListBO.getZipcode() + "," + addressListBO.getPhoneNumber());
        return addressListByString;
    }

}
