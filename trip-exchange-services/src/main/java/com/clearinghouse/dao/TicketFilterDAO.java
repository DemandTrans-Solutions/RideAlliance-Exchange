/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;


import com.clearinghouse.dto.TicketFilterDTO;
import com.clearinghouse.entity.*;
import com.clearinghouse.enumentity.GeographicalFilterStatusConstants;
import com.clearinghouse.enumentity.TripTicketStatusConstants;
import com.clearinghouse.listresponseentity.AddressListBO;
import com.clearinghouse.listresponseentity.ProviderList;
import com.clearinghouse.service.ServiceService;
import jakarta.persistence.TypedQuery;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author chaitanya
 */
@Repository
@AllArgsConstructor
@Slf4j
public class TicketFilterDAO extends AbstractDAO<Integer, TicketFilter> {


    private final TripTicketDAO tripTicketDAO;


    private final UserDAO userDAO;


    private final ListDAO listDAO;


    private final ServiceAreaDAO serviceareaDAO;

    private final ServiceDAO serviceDAO;

    private final ServiceService serviceService;


    public List<TicketFilter> findAllFilters() {

        List<TicketFilter> filters = getEntityManager()
                .createQuery("SELECT f FROM TicketFilter f")
                .getResultList();
        return filters;

    }


    public TicketFilter findFilterByFilterId(int filterId) {
        return getByKey(filterId);
    }


    public TicketFilter createFilter(TicketFilter ticketFilter) {

        add(ticketFilter);
        return ticketFilter;

    }


    public TicketFilter updateFilter(TicketFilter ticketFilter) {
        return update(ticketFilter);

    }


    public void deleteFilterByFilterId(int filterId) {

        TicketFilter ticketFilter = (TicketFilter) getEntityManager()
                .createQuery("SELECT f FROM TicketFilter f WHERE f.filterId = :filterId")
                .setParameter("filterId", filterId)
                .getSingleResult();
        ticketFilter.setIsActive(false);

    }


    public List<TicketFilter> findAllFiltersByUserId(int userId) {

        List<TicketFilter> filters = getEntityManager()
                .createQuery("SELECT f FROM TicketFilter f where f.user.id=:userId AND f.isActive=true ")
                .setParameter("userId", userId)
                .getResultList();
        return filters;

    }


    public List<TripTicket> getTripTicketsByTicketFilterObject(TicketFilterDTO ticketFilterDTOObj) {
        // Extract parameters from DTO
        User user = userDAO.findUserByUserId(ticketFilterDTOObj.getUserId());
        List<String> ticketStatusList = ticketFilterDTOObj.getTicketFilterstatus();
        List<String> originatingProviderList = ticketFilterDTOObj.getOriginatingProviderName();
        List<String> claimingProviderList = ticketFilterDTOObj.getClaimingProviderName();
        List<String> advancedFilterList = ticketFilterDTOObj.getAdvancedFilterParameter();
        String rescindedStatus = ticketFilterDTOObj.getRescindedApplyStatusParameter();
        String maxSeats = ticketFilterDTOObj.getSeatsRequiredMax();
        String minSeats = ticketFilterDTOObj.getSeatsRequiredMin();
        String schedulingPriority = ticketFilterDTOObj.getSchedulingPriority();
        List<String> tripTimeList = ticketFilterDTOObj.getTripTime();
        List<String> operHoursList = ticketFilterDTOObj.getOperatingHours();
        List<String> fundingSourceList = ticketFilterDTOObj.getFundingSourceList();
        List<String> pickupTimeRange = ticketFilterDTOObj.getReqPickUpStartAndEndTime();
        List<String> geographicValues = ticketFilterDTOObj.getSelectedGeographicVal();
        List<String> customerEligibilityList = ticketFilterDTOObj.getCustomerEligibility();
        List<String> hospitalityServiceList = ticketFilterDTOObj.getHospitalityServiceArea();
        boolean applyServiceFilter = ticketFilterDTOObj.isServiceFilterApply();

        // Build the query
        StringBuilder queryString = new StringBuilder();
        StringBuilder claimantOnlyQuery = new StringBuilder();

        // Initialize query based on user role
        buildBaseQuery(user, queryString, claimantOnlyQuery);

        // Add filters to query
        addTicketStatusFilter(ticketStatusList, queryString);
        addFundingSourceFilter(fundingSourceList, queryString);
        addPickupTimeFilter(pickupTimeRange, queryString);
        addCustomerEligibilityFilter(customerEligibilityList, queryString);
        addOriginProviderFilter(originatingProviderList, queryString);
        addRescindedStatusFilter(rescindedStatus, queryString);
        addTripTimeFilter(tripTimeList, queryString);
        addOperHoursFilter(operHoursList, queryString);
        addAdvancedFilter(advancedFilterList, queryString);

        // Clean up query string (remove trailing AND)
        cleanupQueryString(queryString);

        // Add additional filters
        addSeatsRequiredFilter(minSeats, maxSeats, queryString, claimantOnlyQuery.toString(), advancedFilterList);
        addSchedulingPriorityFilter(schedulingPriority, queryString, advancedFilterList);

        // Final cleanup
        if (queryString.toString().trim().endsWith("AND")) {
            String query = queryString.toString().trim();
            queryString = new StringBuilder(query.substring(0, query.lastIndexOf("AND")).trim());
        } else if (queryString.toString().trim().endsWith("WHERE")) {
            String query = queryString.toString().trim();
            queryString = new StringBuilder(query.substring(0, query.lastIndexOf("WHERE")).trim());
        }

        // Add sorting if specified
        String sortField = ticketFilterDTOObj.getSortField();
        int sortOrder = ticketFilterDTOObj.getSortOrder();
        if (sortField != null && !sortField.isEmpty()) {
            String orderDirection = (sortOrder == 1) ? "ASC" : "DESC";
            queryString.append(" ORDER BY t.").append(sortField).append(" ").append(orderDirection);
        }

        // Log and execute query
        log.debug("TicketfilterDAO.getTripTicketsByTicketFilterObject() - Query: " + queryString);
        List<TripTicket> tripTickets = getEntityManager()
                .createQuery(queryString.toString())
                .getResultList();

        // Apply hospitality service filter
        if (hospitalityServiceList != null && !hospitalityServiceList.isEmpty()) {
            tripTickets = applyHospitalityServiceFilter(tripTickets, hospitalityServiceList);
        }

        // Apply geographic filter
        tripTickets = applyGeographicFilter(tripTickets, applyServiceFilter, geographicValues, user);

        // Apply claiming provider filter
        tripTickets = applyClaimingProviderFilter(tripTickets, claimingProviderList, queryString, claimantOnlyQuery);

        // Apply user role filters
        tripTickets = applyUserRoleFilters(tripTickets, user);

        return tripTickets;
    }

    /**
     * Add seats required filter to query
     */
    private void addSeatsRequiredFilter(String minSeats, String maxSeats, StringBuilder queryString,
                                        String claimantOnlyQuery, List<String> advancedFilterParams) {
        if (isNotEmpty(minSeats) && isNotEmpty(maxSeats)) {
            String seatsQuery = " t.customerSeatsRequired BETWEEN " + minSeats.trim() + " AND " + maxSeats.trim();

            if (queryString.toString().trim().equalsIgnoreCase(claimantOnlyQuery.trim())) {
                if (queryString.toString().trim().endsWith("WHERE")) {
                    queryString.append(" (").append(seatsQuery).append(")");
                }
            } else if (isValidList(advancedFilterParams)) {
                queryString.append(" OR (").append(seatsQuery).append(")");
            } else {
                queryString.append(" AND (").append(seatsQuery).append(")");
            }
        }
    }

    /**
     * Add scheduling priority filter to query
     */
    private void addSchedulingPriorityFilter(String priority, StringBuilder queryString, List<String> advancedFilterParams) {
        if (isNotEmpty(priority)) {
            String priorityValue = priority.trim();

            if ((priorityValue.equalsIgnoreCase("PICKUP") || priorityValue.equalsIgnoreCase("DROPOFF"))) {
                String priorityQuery = " t.schedulingPriority Like '%" + priorityValue + "%'";

                if (queryString.toString().trim().endsWith("WHERE")) {
                    queryString.append(priorityQuery);
                } else if (isValidList(advancedFilterParams)) {
                    queryString.append(" OR (").append(priorityQuery).append(")");
                } else {
                    queryString.append(" AND (").append(priorityQuery).append(")");
                }
            }
        }
    }

    /**
     * Apply hospitality service filter to trip tickets
     */
    private List<TripTicket> applyHospitalityServiceFilter(List<TripTicket> tripTickets, List<String> hospitalityServiceList) {
        if (hospitalityServiceList == null || hospitalityServiceList.isEmpty()) {
            return tripTickets;
        }

        Set<TripTicket> filteredTickets = new HashSet<>();

        for (TripTicket ticket : tripTickets) {
            List<Service> serviceAreaList = serviceService.findAllHosptalityServiceareabyIds(hospitalityServiceList);

            double pickupLat = ticket.getPickupAddress().getLatitude();
            double pickupLng = ticket.getPickupAddress().getLongitude();
            double dropoffLat = ticket.getDropOffAddress().getLatitude();
            double dropoffLng = ticket.getDropOffAddress().getLongitude();

            if (!serviceAreaList.isEmpty() && pickupLat != 0) {
                for (Service service : serviceAreaList) {
                    if (!service.isActive()) continue;

                    if (service.getHospitalServiceAreas() != null && !service.getHospitalServiceAreas().isEmpty()) {
                        for (ServiceArea area : service.getHospitalServiceAreas()) {
                            if (serviceDAO.checkAddressInServicearea(area, pickupLat, pickupLng) &&
                                    serviceDAO.checkAddressInServicearea(area, dropoffLat, dropoffLng)) {
                                filteredTickets.add(ticket);
                                break;
                            }
                        }
                    }
                }
            }
        }

        return new ArrayList<>(filteredTickets);
    }

    /**
     * Utility method to check if a string is not empty
     */
    private boolean isNotEmpty(String str) {
        return str != null && !str.trim().equalsIgnoreCase("");
    }

    /**
     * Utility method to check if a list is valid and not empty
     */
    private boolean isValidList(List<?> list) {
        return list != null && !list.isEmpty() && !(list.get(0).toString().trim().equalsIgnoreCase(""));
    }

    /**
     * Build base query string based on user role
     */
    private void buildBaseQuery(User user, StringBuilder queryString, StringBuilder queryForClaimantOnly) {
        if (user.getAuthorities().iterator().next().getAuthority().equalsIgnoreCase("ROLE_ADMIN")) {
            queryString.append("SELECT t FROM TripTicket t WHERE t.tripTicketInvisible=false AND ");
            queryForClaimantOnly.append("SELECT t FROM TripTicket t WHERE t.tripTicketInvisible=false AND ");
        } else {
            queryString.append("SELECT t FROM TripTicket t WHERE t.tripTicketInvisible=false AND ");
            queryForClaimantOnly.append("SELECT t FROM TripTicket t WHERE t.tripTicketInvisible=false AND ");

            List<ProviderList> providerList = listDAO.getOriginatorProviderListByProviderId(user.getProvider().getProviderId());
            StringBuilder providerFilter = new StringBuilder();

            int count = 0;
            for (ProviderList provider : providerList) {
                if (count > 0) {
                    providerFilter.append(" OR ");
                }
                providerFilter.append("t.originProvider.providerId=").append(provider.getProviderId());
                count++;
            }

            if (providerFilter.length() > 0) {
                queryString.append(" (").append(providerFilter).append(") AND ");
            }
        }
    }

    /**
     * Clean up query string by removing trailing AND/WHERE
     */
    private void cleanupQueryString(StringBuilder queryString) {
        String query = queryString.toString().trim();
        if (query.endsWith("AND")) {
            int lastIndex = query.lastIndexOf("AND");
            if (lastIndex > 0) {
                queryString.delete(lastIndex, queryString.length());
            }
        } else if (query.endsWith("WHERE")) {
            int lastIndex = query.lastIndexOf("WHERE");
            if (lastIndex > 0) {
                queryString.delete(lastIndex, queryString.length());
            }
        }
    }

    /**
     * Add ticket status filter to query
     */
    private void addTicketStatusFilter(List<String> ticketStatusList, StringBuilder queryString) {
        if (isValidList(ticketStatusList)) {
            StringBuilder statusFilter = new StringBuilder();

            for (int i = 0; i < ticketStatusList.size(); i++) {
                if (i > 0) {
                    statusFilter.append(" OR ");
                }
                statusFilter.append(" t.status.statusId = ").append(ticketStatusList.get(i).trim());
            }

            queryString.append("(").append(statusFilter).append(")").append("  AND  ");
        }
    }

    /**
     * Add funding source filter to query
     */
    private void addFundingSourceFilter(List<String> fundingSourceList, StringBuilder queryString) {
        if (isValidList(fundingSourceList)) {
            StringBuilder fundingFilter = new StringBuilder();

            for (int i = 0; i < fundingSourceList.size(); i++) {
                if (i > 0) {
                    fundingFilter.append(" OR ");
                }
                fundingFilter.append("FIND_IN_SET('").append(fundingSourceList.get(i))
                        .append("',t.tripFunders) > 0 ");
            }

            queryString.append(fundingFilter).append("  AND  ");
        }
    }

    /**
     * Add pickup time range filter to query
     */
    private void addPickupTimeFilter(List<String> pickUpStartAndEndTime, StringBuilder queryString) {
        if (isValidList(pickUpStartAndEndTime)) {
            String inClause = " t.requestedPickupTime between '" +
                    pickUpStartAndEndTime.get(0) + "' and '" +
                    pickUpStartAndEndTime.get(1) + "' ";
            queryString.append(inClause).append("  AND  ");
        }
    }

    /**
     * Add customer eligibility filter to query
     */
    private void addCustomerEligibilityFilter(List<String> customerEligibilityList, StringBuilder queryString) {
        if (isValidList(customerEligibilityList)) {
            StringBuilder eligibilityFilter = new StringBuilder("t.customerEligibilityFactors IN(");

            for (int i = 0; i < customerEligibilityList.size(); i++) {
                if (i > 0) {
                    eligibilityFilter.append(", ");
                }
                eligibilityFilter.append("'").append(customerEligibilityList.get(i)).append("'");
            }

            eligibilityFilter.append(")");
            queryString.append(eligibilityFilter).append("  AND  ");
        }
    }

    /**
     * Add origin provider filter to query
     */
    private void addOriginProviderFilter(List<String> originatingProviderNameList, StringBuilder queryString) {
        if (isValidList(originatingProviderNameList)) {
            // Convert string list to comma-separated integer values
            StringBuilder inClause = new StringBuilder();
            inClause.append("t.originProvider.providerId in (");
            for (int i = 0; i < originatingProviderNameList.size(); i++) {
                String val = originatingProviderNameList.get(i).trim();
                try {
                    int intVal = Integer.parseInt(val);
                    if (i > 0) {
                        inClause.append(", ");
                    }
                    inClause.append(intVal);
                } catch (NumberFormatException e) {
                    // skip invalid entries
                }
            }
            inClause.append(")");
            queryString.append(inClause).append(" AND  ");
        }
    }

    /**
     * Add rescinded status filter to query
     */
    private void addRescindedStatusFilter(String rescindedStatus, StringBuilder queryString) {
        if (isNotEmpty(rescindedStatus)) {
            String status = rescindedStatus.trim();
            if (status.equalsIgnoreCase("SHOW")) {
                queryString.append(" ( t.status.statusId LIKE 12 OR t.status.statusId NOT LIKE 12) ").append("  AND   ");
            } else if (status.equalsIgnoreCase("HIDE")) {
                queryString.append(" ( t.status.statusId NOT LIKE 12) ").append("  AND  ");
            } else if (status.equalsIgnoreCase("ONLY")) {
                queryString.append(" ( t.status.statusId=12) ").append("  AND  ");
            }
        }
    }

    /**
     * Parse and add trip time filter to query
     */
    private void addTripTimeFilter(List<String> tripTimeList, StringBuilder queryString) {
        if (isValidList(tripTimeList)) {
            String[] timeRange = parseTripTimeRange(tripTimeList);
            String startDate = timeRange[0];
            String startTime = timeRange[1];
            String endDate = timeRange[2];
            String endTime = timeRange[3];

            String condition = " ( " +
                    "(t.requestedPickupDate > {d '" + startDate + "'} OR (t.requestedPickupDate = {d '" + startDate + "'} AND t.requestedPickupTime >= {t '" + startTime + "'})) AND " +
                    "(t.requestedPickupDate < {d '" + endDate + "'} OR (t.requestedPickupDate = {d '" + endDate + "'} AND t.requestedPickupTime <= {t '" + endTime + "'})) " +
                    " OR " +
                    "(t.requestedDropoffDate > {d '" + startDate + "'} OR (t.requestedDropoffDate = {d '" + startDate + "'} AND t.requestedDropOffTime >= {t '" + startTime + "'})) AND " +
                    "(t.requestedDropoffDate < {d '" + endDate + "'} OR (t.requestedDropoffDate = {d '" + endDate + "'} AND t.requestedDropOffTime <= {t '" + endTime + "'})) " +
                    ") ";
            queryString.append(condition).append("  AND  ");
        }
    }

    private void addOperHoursFilter(List<String> operHourList, StringBuilder queryString) {
        if (isValidList(operHourList)) {
            try {
                String pickupTime = operHourList.get(0).trim();
                String dropoffTime = operHourList.get(1).trim();

                // get everything after "pickUpTime="
                pickupTime = pickupTime.substring(pickupTime.indexOf("=") + 1);
                dropoffTime = dropoffTime.substring(dropoffTime.indexOf("=") + 1);

                String condition = " ( " +
                        "(t.requestedPickupTime >= {t '" + pickupTime + "'} AND t.requestedPickupTime <= {t '" + dropoffTime + "'}) " +
                        " OR " +
                        "(t.requestedDropOffTime >= {t '" + pickupTime + "'} AND t.requestedDropOffTime <= {t '" + dropoffTime + "'}) " +
                        ") ";
                queryString.append(condition).append("  AND  ");
            } catch ( Exception ex ) {
                log.error("Error parsing oper hours filter: " + ex.getMessage());
            }
        }
    }


    /**
     * Helper method to parse trip time range from the trip time list
     */
    private String[] parseTripTimeRange(List<String> tripTimeList) {
        String pickupDateFrom = "";
        String pickupDateTo = "";
        String pickupFromTime = "";
        String pickupToTime = "";

        for (String tripTimeParam : tripTimeList) {
            if (tripTimeParam == null || tripTimeParam.trim().isEmpty()) continue;
            String[] keyValuePair = tripTimeParam.split("=", 2);
            if (keyValuePair.length < 2) continue;

            String key = keyValuePair[0].trim();
            String value = keyValuePair[1].trim();

            if (key.equalsIgnoreCase("pickUpDateTime")) {
                String[] dateTime = value.split(" ");
                pickupDateFrom = dateTime[0];
                if (dateTime.length > 1 && !dateTime[1].isEmpty()) {
                    String rawTime = dateTime[1];
                    if (rawTime.contains("-")) {
                        rawTime = rawTime.substring(0, rawTime.indexOf("-"));
                    }
                    String[] timeElements = rawTime.split(":");
                    if (timeElements.length >= 2) {
                        pickupFromTime = timeElements[0] + ":" + timeElements[1] + ":00";
                    } else {
                        pickupFromTime = "00:00:00";
                    }
                } else {
                    pickupFromTime = "00:00:00";
                }
            } else if (key.equalsIgnoreCase("dropOffDateTime")) {
                String[] dateTime = value.split(" ");
                pickupDateTo = dateTime[0].trim();
                if (dateTime.length > 1 && !dateTime[1].isEmpty()) {
                    String rawTime = dateTime[1];
                    if (rawTime.contains("-")) {
                        rawTime = rawTime.substring(0, rawTime.indexOf("-"));
                    }
                    try {
                        LocalDateTime dateTimeObj = LocalDateTime.parse(pickupDateTo + "T" + rawTime);
                        dateTimeObj = dateTimeObj.plusMinutes(1);
                        String[] dateTimeParts = dateTimeObj.toString().split("T");
                        pickupDateTo = dateTimeParts[0].trim();
                        String[] timeElements = dateTimeParts[1].split(":");
                        if (timeElements.length >= 2) {
                            pickupToTime = timeElements[0] + ":" + timeElements[1] + ":00";
                        } else {
                            pickupToTime = "23:59:59";
                        }
                    } catch (Exception e) {
                        pickupToTime = "23:59:59";
                    }
                } else {
                    pickupToTime = "23:59:59";
                }
            }
        }
        // Fallback defaults if not set
        if (pickupFromTime.isEmpty()) pickupFromTime = "00:00:00";
        if (pickupToTime.isEmpty()) pickupToTime = "23:59:59";
        return new String[]{pickupDateFrom, pickupFromTime, pickupDateTo, pickupToTime};
    }

    /**
     * Add advanced filter parameters to query
     */
    private void addAdvancedFilter(List<String> advancedFilterParameterList, StringBuilder queryString) {
        if (isValidList(advancedFilterParameterList)) {
            StringBuilder advancedFilter = new StringBuilder();
            int validFilters = 0;

            for (String advancedFilterParam : advancedFilterParameterList) {
                String[] keyValue = advancedFilterParam.split(":");

                if (keyValue.length > 1 && keyValue[1] != null && !keyValue[1].equalsIgnoreCase(" ")) {
                    String field = keyValue[0].trim();
                    String value = keyValue[1].trim();

                    if (validFilters > 0) {
                        advancedFilter.append(" OR ");
                    }

                    if (field.equalsIgnoreCase("customerFirstName") || field.equalsIgnoreCase("customerIdentifiers")) {
                        value = "%" + value + "%";
                    }

                    if ( field.toLowerCase().contains(".addressid") ) {
                        // address filtering is special since you want to check for the ID OR the
                        // value of any of the address columns is like the value of those columns in
                        // the address row with the passed id
                        String addressField = field.substring(0, field.indexOf("."));
                        String idField = field.replace("addressid", "addressId");
                        advancedFilter.append(" t.").append(idField).append(" IN (SELECT a.addressId FROM Address a WHERE a.street1 = " +
                                "(SELECT street1 FROM Address WHERE addressId = ").append(value).append(") AND " +
                                "a.city = (SELECT city FROM Address WHERE addressId = ").append(value).append(") AND a.state = " +
                                "(SELECT state FROM Address WHERE addressId = ").append(value).append(") AND " +
                                "a.zipcode = (SELECT zipcode FROM Address WHERE addressId = ").append(value).append("))");
                    } else {
                        // If the field ends with 'Id', use '=' instead of LIKE
                        if (field.endsWith("Id")) {
                            advancedFilter.append(" t.").append(field).append(" = ").append(value);
                        } else {
                            advancedFilter.append(" t.").append(field).append(" LIKE '").append(value).append("'");
                        }
                    }
                    validFilters++;
                }
            }

            if (validFilters > 0) {
                queryString.append("(").append(advancedFilter).append(")").append(" AND ");
            }
        }
    }

    /**
     * Apply geographic filter to trip tickets
     */
    private List<TripTicket> applyGeographicFilter(List<TripTicket> tripTickets,
                                                   boolean applyServiceFilter,
                                                   List<String> geographicValues,
                                                   User user) {
        if (!applyServiceFilter) {
            return tripTickets;
        }

        // Case 1: Service filter with no specific geographic values
        if (geographicValues == null || geographicValues.isEmpty()) {
            Set<TripTicket> filteredTickets = new HashSet<>();

            for (TripTicket ticket : tripTickets) {
                List<Service> serviceAreas = serviceDAO.findAllSeriveareaByProviderId(user.getProvider().getProviderId());

                double pickupLat = ticket.getPickupAddress().getLatitude();
                double pickupLng = ticket.getPickupAddress().getLongitude();
                double dropoffLat = ticket.getDropOffAddress().getLatitude();
                double dropoffLng = ticket.getDropOffAddress().getLongitude();

                if (!serviceAreas.isEmpty() && pickupLat != 0) {
                    for (Service service : serviceAreas) {
                        if (!service.isActive() || service.isHospitalityArea() || service.isProviderSelected()) {
                            continue;
                        }

                        for (ServiceArea area : service.getHospitalServiceAreas()) {
                            boolean pickupInArea = serviceDAO.checkAddressInServicearea(area, pickupLat, pickupLng);
                            boolean dropoffInArea = serviceDAO.checkAddressInServicearea(area, dropoffLat, dropoffLng);

                            if (pickupInArea || dropoffInArea) {
                                filteredTickets.add(ticket);
                                break;
                            }
                        }
                    }
                }
            }

            return new ArrayList<>(filteredTickets);
        }
        // Case 2: Service filter with specific geographic values
        else {
            return applyAdvancedGeographicFilter(tripTickets, geographicValues, user);
        }
    }

    /**
     * Apply advanced geographic filter based on selected geographic values
     */
    private List<TripTicket> applyAdvancedGeographicFilter(List<TripTicket> tripTickets,
                                                           List<String> geographicValues,
                                                           User user) {
        Set<TripTicket> filteredTickets = new HashSet<>();

        for (TripTicket ticket : tripTickets) {
            // Categorize tickets by location
            Set<TripTicket> outsideServiceArea = new HashSet<>();
            Set<TripTicket> pickupInServiceArea = new HashSet<>();
            Set<TripTicket> dropoffInServiceArea = new HashSet<>();

            List<Service> serviceAreas = serviceDAO.findAllSeriveareaByProviderId(user.getProvider().getProviderId());
            double pickupLat = ticket.getPickupAddress().getLatitude();
            double pickupLng = ticket.getPickupAddress().getLongitude();
            double dropoffLat = ticket.getDropOffAddress().getLatitude();
            double dropoffLng = ticket.getDropOffAddress().getLongitude();

            if (serviceAreas.isEmpty() || (pickupLat == 0 && dropoffLat == 0)) {
                continue;
            }

            for (Service service : serviceAreas) {
                if (!service.isActive() || service.isHospitalityArea() || service.isProviderSelected()) {
                    continue;
                }

                for (ServiceArea area : service.getHospitalServiceAreas()) {
                    boolean pickupInArea = serviceDAO.checkAddressInServicearea(area, pickupLat, pickupLng);
                    boolean dropoffInArea = serviceDAO.checkAddressInServicearea(area, dropoffLat, dropoffLng);

                    if (pickupInArea && dropoffInArea) {
                        // Both inside, do nothing as we're looking for specific cases
                    } else if (pickupInArea && !dropoffInArea) {
                        pickupInServiceArea.add(ticket);
                        break;
                    } else if (!pickupInArea && dropoffInArea) {
                        dropoffInServiceArea.add(ticket);
                        break;
                    } else if (!pickupInArea && !dropoffInArea) {
                        outsideServiceArea.add(ticket);
                        break;
                    }
                }
            }

            // Apply filtering based on selected geographic values
            applyGeographicValueOptions(filteredTickets, outsideServiceArea, pickupInServiceArea,
                    dropoffInServiceArea, geographicValues);
        }

        return new ArrayList<>(filteredTickets);
    }

    /**
     * Apply geographic value options to filter tickets
     */
    private void applyGeographicValueOptions(Set<TripTicket> filteredTickets,
                                             Set<TripTicket> outsideServiceArea,
                                             Set<TripTicket> pickupInServiceArea,
                                             Set<TripTicket> dropoffInServiceArea,
                                             List<String> geographicValues) {
        String outsideVal = GeographicalFilterStatusConstants.CheckForoutsideserviceArea
                .getGeographicalFilterStatus().toString();
        String pickupVal = GeographicalFilterStatusConstants.CheckForOnlyPickUpIsInSA
                .getGeographicalFilterStatus().toString();
        String dropoffVal = GeographicalFilterStatusConstants.CheckForOnlyDropOffIsInSA
                .getGeographicalFilterStatus().toString();

        boolean hasOutside = geographicValues.contains(outsideVal);
        boolean hasPickup = geographicValues.contains(pickupVal);
        boolean hasDropoff = geographicValues.contains(dropoffVal);

        // Apply filters based on selected combinations
        if (hasOutside && !hasPickup && !hasDropoff) {
            filteredTickets.addAll(outsideServiceArea);
        } else if (hasPickup && !hasOutside && !hasDropoff) {
            filteredTickets.addAll(pickupInServiceArea);
        } else if (hasDropoff && !hasOutside && !hasPickup) {
            filteredTickets.addAll(dropoffInServiceArea);
        } else if (hasOutside && hasPickup && hasDropoff) {
            filteredTickets.addAll(outsideServiceArea);
            filteredTickets.addAll(pickupInServiceArea);
            filteredTickets.addAll(dropoffInServiceArea);
        } else if (hasOutside && hasPickup) {
            filteredTickets.addAll(outsideServiceArea);
            filteredTickets.addAll(pickupInServiceArea);
        } else if (hasOutside && hasDropoff) {
            filteredTickets.addAll(outsideServiceArea);
            filteredTickets.addAll(dropoffInServiceArea);
        } else if (hasPickup && hasDropoff) {
            filteredTickets.addAll(pickupInServiceArea);
            filteredTickets.addAll(dropoffInServiceArea);
        }
    }

    /**
     * Apply claiming provider filter to trip tickets
     */
    private List<TripTicket> applyClaimingProviderFilter(List<TripTicket> tripTickets,
                                                         List<String> claimingProviderList,
                                                         StringBuilder queryString,
                                                         StringBuilder claimantOnlyQuery) {
        // Handle empty results with only claimant provider filter
        if (tripTickets.isEmpty() &&
                queryString.toString().trim().equalsIgnoreCase(claimantOnlyQuery.toString().trim())) {
            tripTickets = tripTicketDAO.findAllTripTickets();
        }

        if (!isValidList(claimingProviderList)) {
            return tripTickets;
        }

        List<TripTicket> filteredTickets = new ArrayList<>();

        for (String providerId : claimingProviderList) {
            for (TripTicket ticket : tripTickets) {
                if (ticket.getTripClaims().isEmpty()) continue;

                for (TripClaim claim : ticket.getTripClaims()) {
                    String claimantId = Integer.toString(claim.getClaimantProvider().getProviderId()).trim();
                    if (claimantId.equalsIgnoreCase(providerId.trim())) {
                        filteredTickets.add(ticket);
                        break;
                    }
                }
            }
        }

        return filteredTickets;
    }

    /**
     * Apply user role specific filters to trip tickets
     */
    private List<TripTicket> applyUserRoleFilters(List<TripTicket> tripTickets, User user) {
        String userRole = user.getAuthorities().iterator().next().getAuthority();

        if (!userRole.equalsIgnoreCase("ROLE_PROVIDERADMIN") &&
                !userRole.equalsIgnoreCase("ROLE_PROVIDERUSER")) {
            return tripTickets;
        }

        // Separate tickets based on status
        List<TripTicket> regularStatusTickets = new ArrayList<>();
        List<TripTicket> specialStatusTickets = new ArrayList<>();

        var approvedStatus = TripTicketStatusConstants.approved.tripTicketStatusUpdate();
        var expiredStatus = TripTicketStatusConstants.expired.tripTicketStatusUpdate();
        for (TripTicket ticket : tripTickets) {
            var statusId = ticket.getStatus().getStatusId();
            if (  ticket.getApprovedTripClaim() != null &&  (statusId == approvedStatus || statusId == expiredStatus) ) {
                specialStatusTickets.add(ticket);
            } else {
                regularStatusTickets.add(ticket);
            }
        }




        // Process special status tickets
        for (TripTicket ticket : specialStatusTickets) {
            int statusId = ticket.getStatus().getStatusId();
            int providerId = user.getProvider().getProviderId();
            // allow uber tickets for DRCOG
            boolean isDRCOGUser = user.getProvider() != null ? user.getProvider().getProviderName().equalsIgnoreCase("DRCOG") : false;
            boolean isUberClaim = ticket.getTripClaims().stream().anyMatch(tripClaim -> tripClaim.getClaimantProvider().getProviderName().equalsIgnoreCase("UBER"));
            if ( isDRCOGUser && isUberClaim ) {
                regularStatusTickets.add(ticket);
            } else {
                if (ticket.getApprovedTripClaim() != null && statusId == approvedStatus) {
                    if (ticket.getOriginProvider().getProviderId() == providerId ||
                            ticket.getApprovedTripClaim().getClaimantProvider().getProviderId() == providerId) {
                        regularStatusTickets.add(ticket);
                    }
                } else if (ticket.getOriginProvider().getProviderId() == providerId) {
                    regularStatusTickets.add(ticket);
                }
            }
        }

        // Apply service area filter
        List<TripTicket> originatorTickets = new ArrayList<>();
        List<TripTicket> partnerTickets = new ArrayList<>();
        List<Service> serviceAreas = serviceDAO.findAllSeriveareaByProviderId(user.getProvider().getProviderId());

        for (TripTicket ticket : regularStatusTickets) {
            int providerId = user.getProvider().getProviderId();

            // If user is ticket originator, keep the ticket
            if (ticket.getOriginProvider().getProviderId() == providerId) {
                originatorTickets.add(ticket);
                continue;
            }

            // If ticket is approved and user is claimant, keep the ticket
            if (ticket.getStatus().getStatusId() == approvedStatus &&
                    ticket.getApprovedTripClaim() != null &&
                    ticket.getApprovedTripClaim().getClaimantProvider().getProviderId() == providerId) {
                partnerTickets.add(ticket);
                continue;
            }

            // Otherwise, check service areas
            if (isTicketInServiceArea(ticket, serviceAreas, providerId)) {
                partnerTickets.add(ticket);
            }
        }

        // Combine lists
        originatorTickets.addAll(partnerTickets);

        return originatorTickets.isEmpty() ? regularStatusTickets : originatorTickets;
    }

    /**
     * Check if a ticket is in the service area of a provider
     */
    private boolean isTicketInServiceArea(TripTicket ticket, List<Service> serviceAreas, int providerId) {
        double pickupLat = ticket.getPickupAddress().getLatitude();
        double pickupLng = ticket.getPickupAddress().getLongitude();
        double dropoffLat = ticket.getDropOffAddress().getLatitude();
        double dropoffLng = ticket.getDropOffAddress().getLongitude();

        if (serviceAreas.isEmpty()) {
            return true;
        }

        if (pickupLat == 0) {
            return true;
        }

        for (Service service : serviceAreas) {
            if (!service.isActive() || service.isHospitalityArea() || service.isProviderSelected()) {
                continue;
            }

            for (ServiceArea area : service.getHospitalServiceAreas()) {
                if (serviceDAO.checkAddressInServicearea(area, pickupLat, pickupLng) &&
                        serviceDAO.checkAddressInServicearea(area, dropoffLat, dropoffLng)) {
                    return true;
                }
            }
        }

        // If all service areas are inactive, consider the ticket in range
        return serviceAreas.size() == serviceDAO.getCountOfInactiveServicearea(providerId);
    }

    public AddressListBO getAllAddressById(int addressId
    ) {

        TypedQuery<AddressListBO> query = entityManager.createQuery("SELECT NEW com.clearinghouse.listresponseentity.AddressListBO(ad.addressId, ad.street1,ad.street2,ad.city,ad.county,ad.state,ad.zipcode,ad.phoneNumber) FROM Address AS ad where  ad.addressId =:addressId", AddressListBO.class)
                .setParameter("addressId", addressId);
        AddressListBO ticketFilterList = query.getSingleResult();

        return ticketFilterList;
    }

    @Override
    public void add(TicketFilter entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        super.add(entity);
    }

}
