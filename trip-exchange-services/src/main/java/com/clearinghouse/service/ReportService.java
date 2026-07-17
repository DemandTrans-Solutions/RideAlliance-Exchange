/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;

import com.clearinghouse.dao.ProviderCostDAO;
import com.clearinghouse.dao.ReportDAO;
import com.clearinghouse.dao.TripTicketDAO;
import com.clearinghouse.dao.TripTicketDistanceDAO;
import com.clearinghouse.dto.*;
import com.clearinghouse.entity.Provider;
import com.clearinghouse.entity.ProviderCost;
import com.clearinghouse.entity.TripClaim;
import com.clearinghouse.entity.TripResult;
import com.clearinghouse.entity.TripTicket;
import com.clearinghouse.entity.TripTicketDistance;
import com.clearinghouse.enumentity.TripClaimStatusConstants;
import com.clearinghouse.enumentity.TripTicketStatusConstants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

import static java.util.stream.Collectors.joining;

/**
 *
 * @author chaitanyaP
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class ReportService implements IConvertBOToDTO, IConvertDTOToBO {

    private final ReportDAO reportDAO;

    private final TripTicketDAO tripTicketDAO;


    private final ModelMapper tripTicketModelMapper;


    private final ModelMapper providerModelMapper;

    private final ProviderPartnerService providerPartnerService;

    private final TripTicketService tripTicketService;

    private final ProviderCostDAO providerCostDAO;

    private final TripTicketDistanceDAO tripTicketDistanceDAO;

    private final DetailedTripTicketConverterService detailedTripTicketConverterService;


    public String findOldestCreatedDate(int providerId) {

        return reportDAO.findOldestCreatedDate(providerId);
    }


    private ZonedDateTime parseDateTime(String dateTimeRaw) {
        try {
            return ZonedDateTime.parse(dateTimeRaw);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(dateTimeRaw).atZone(ZoneId.systemDefault());
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Invalid date format: " + dateTimeRaw, ex);
            }
        }
    }

    public ReportSummaryDTO findSummaryReport(ReportFilterDTO reportFilterDTOObj) {

        // Parse and validate fromDate
        String fromDateRaw = reportFilterDTOObj.getFromDate();
        if (fromDateRaw == null || fromDateRaw.isEmpty()) {
            throw new IllegalArgumentException("Invalid fromDate format. Cannot be null or empty.");
        }
        ZonedDateTime fromDate = parseDateTime(fromDateRaw);
        reportFilterDTOObj.setFromDateTime(fromDate); // set parsed value
        // Do NOT overwrite the string field

        // Parse and validate toDate
        String toDateRaw = reportFilterDTOObj.getToDate();
        if (toDateRaw == null || toDateRaw.isEmpty()) {
            throw new IllegalArgumentException("Invalid toDate format. Cannot be null or empty.");
        }
        ZonedDateTime toDate = parseDateTime(toDateRaw);
        reportFilterDTOObj.setToDateTime(toDate); // set parsed value
        // Do NOT overwrite the string field

        // Handle reportTicketFilterStatus
        List<String> reportTicketFilterStatus = reportFilterDTOObj.getReportTicketFilterStatus();
        if (reportTicketFilterStatus == null || reportTicketFilterStatus.isEmpty() || reportTicketFilterStatus.get(0).trim().isEmpty()) {
            reportFilterDTOObj.setReportTicketFilterStatus(Collections.emptyList());
        }

        // this report should not include to / from time (at least for now)
        reportFilterDTOObj.setFromDateTime(null);
        reportFilterDTOObj.setToDateTime(null);

        // Proceed with DAO calls
        ReportSummaryDTO summaryDTO = new ReportSummaryDTO();
        summaryDTO.setTotalTicketCount(reportDAO.countOfTotalTickets(reportFilterDTOObj));
        summaryDTO.setRescindedTicketCount(reportDAO.countOfRescindedTickets(reportFilterDTOObj));
        summaryDTO.setAvailabeTicketCount(reportDAO.countOfAvaialbleTickets(reportFilterDTOObj));
        summaryDTO.setApprovedTicketCount(reportDAO.countOfApprovedTickets(reportFilterDTOObj));
        summaryDTO.setExpiredTicketCount(reportDAO.countOfExpiredTickets(reportFilterDTOObj));
        summaryDTO.setCompletedTicketCount(reportDAO.countOfCompletedTickets(reportFilterDTOObj));

        /*count for claims sumbmitted*/
        summaryDTO.setTotalCliamsSubmitted(reportDAO.countOfTotalClaimsSubmitted(reportFilterDTOObj));
        summaryDTO.setApprovedClaimSubmitted(reportDAO.countOfClaimApproved(reportFilterDTOObj));
        summaryDTO.setPendingClaimSubmitted(reportDAO.countOfClaimPending(reportFilterDTOObj));
        summaryDTO.setRescindedCaimSubmitted(reportDAO.countOfClaimRescinded(reportFilterDTOObj));
        summaryDTO.setDeclinedClaimSubmitted(reportDAO.countOfClaimDeclined(reportFilterDTOObj));

        /*count for the claim received */
        int totalClaimReceived = 0;
        int pendingClaimReceived = 0;
        int approvedClaimReceived = 0;
        int rescindedClaimReceived = 0;
        int declinedClaimReceived = 0;

        /*get all originatedTickets*/
        List<TripTicket> tripTickets = tripTicketDAO.findAllTripTicketsByOriginatorPrividerId(reportFilterDTOObj.getProviderId());

        if (!tripTickets.isEmpty()) {
            for (TripTicket tripTicket : tripTickets) {
                Set<TripClaim> tripClaims = tripTicket.getTripClaims();
                if (!tripClaims.isEmpty()) {

                    totalClaimReceived = totalClaimReceived + tripClaims.size();
                    for (TripClaim tripClaim : tripClaims) {
                        //count for pending status
                        if (tripClaim.getStatus() != null) {
                            if (tripClaim.getStatus().getStatusId() == TripClaimStatusConstants.pending.tripClaimStatusUpdate()) {
                                pendingClaimReceived++;
                            }
                            //count for approved status
                            if (tripClaim.getStatus().getStatusId() == TripClaimStatusConstants.approved.tripClaimStatusUpdate()) {
                                approvedClaimReceived++;
                            }

                            //count for declined status
                            if (tripClaim.getStatus().getStatusId() == TripClaimStatusConstants.declined.tripClaimStatusUpdate()) {
                                declinedClaimReceived++;
                            }
                            //count for rescinded status
                            if (tripClaim.getStatus().getStatusId() == TripClaimStatusConstants.rescined.tripClaimStatusUpdate()) {
                                rescindedClaimReceived++;
                            }
                        }
                    }

                }
            }

        }
        /*setting values for trip claim Recived*/
        summaryDTO.setTotalCliamsReceived(totalClaimReceived);
        summaryDTO.setRescindedCaimReceived(rescindedClaimReceived);
        summaryDTO.setApprovedClaimReceived(approvedClaimReceived);
        summaryDTO.setDeclinedClaimReceived(declinedClaimReceived);
        summaryDTO.setPendingClaimReceived(pendingClaimReceived);
        return summaryDTO;
    }


    public List<DetailedTripTicketDTO> findDetailedTripTicketByReportFilterOBJ(ReportFilterDTO reportFilterDTOObj) {
        /**
         * check for from date
         */
        String fromDateRaw = reportFilterDTOObj.getFromDate();
        if (fromDateRaw == null || !fromDateRaw.contains("T")) {
            throw new IllegalArgumentException("Invalid fromDate format. Expected format: 'YYYY-MM-DDTHH:mm:ss'");
        }

        String[] dateTime1 = fromDateRaw.split("T");
        if (dateTime1.length < 2) {
            throw new IllegalArgumentException("Invalid fromDate format. Missing time component.");
        }

        String fromDateTemp = dateTime1[0].trim();
        String fromTimeTemp = dateTime1[1];
        if (fromTimeTemp.contains("-")) {
            fromTimeTemp = fromTimeTemp.substring(0, fromTimeTemp.indexOf("-"));
        }
        reportFilterDTOObj.setFromDate(fromDateTemp + "T" + fromTimeTemp);

        /**
         * check for to date
         */
        String toDateRaw = reportFilterDTOObj.getToDate();
        /**
         * check if there is time zone in date
         */
        String[] dateTime2 = toDateRaw.split("T");
        String toDateTemp = dateTime2[0].trim();
        /*check if date string contains zone value*/
        String toTimeTemp = dateTime2[1];
        if (toTimeTemp.contains("-")) {
            toTimeTemp = toTimeTemp.substring(0, toTimeTemp.indexOf("-"));
        }
        //set value of to date
        reportFilterDTOObj.setToDate(toDateTemp + "T" + toTimeTemp);


        reportFilterDTOObj.setFromDateTime(parseDateTime(reportFilterDTOObj.getFromDate()));
        reportFilterDTOObj.setToDateTime(parseDateTime(reportFilterDTOObj.getToDate()));


        List<DetailedTripTicketDTO> detailedTripTicketDTOList = new ArrayList<>();
        List<TripTicket> tripTicketsByreportFilter = reportDAO.getTripTicketsByReportFilterObj(reportFilterDTOObj);

        /*seperate list having pickupdate time as null*/
        List<TripTicket> pickupDatetimePresentTicketsList = new ArrayList<>();
        List<TripTicket> dropOffDatetimePresentTicketsList = new ArrayList<>();
        for (TripTicket tripTicket : tripTicketsByreportFilter) {
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

        //sort list here on the basis of pickup date and time
        Collections.sort(pickupDatetimePresentTicketsList, new Comparator<TripTicket>() {
            @Override
            public int compare(TripTicket t1, TripTicket t2) {
                int result = t2.getRequestedPickupDate().compareTo(t1.getRequestedPickupDate());
                if (result == 0) {
                    return t1.getRequestedPickupTime().compareTo(t2.getRequestedPickupTime());
                }
                return t2.getRequestedPickupDate().compareTo(t1.getRequestedPickupDate());
            }
        });

        if (!dropOffDatetimePresentTicketsList.isEmpty()) {
            pickupDatetimePresentTicketsList.addAll(dropOffDatetimePresentTicketsList);
        }

        for (TripTicket tripTicket : pickupDatetimePresentTicketsList) {

            // Use the shared converter so each claim reliably carries its claimant_provider_name and
            // status (it runs loadLazyFields + fixTripClaimProviderNames), letting the Current Tickets
            // report show the approved-else-pending-else-cancelled claimant.
            DetailedTripTicketDTO detailedTicketDTO = detailedTripTicketConverterService.convertToDetailedTripTicketDTO(tripTicket);
            detailedTripTicketDTOList.add(detailedTicketDTO);

        }
        return detailedTripTicketDTOList;

    }


    public List<DetailedTripTicketDTO> getTripTicketsByReportFilterWithoutCompleted(ReportFilterDTO reportFilterDTOObj) {

        /**
         * check for from date
         */
        String fromDateRaw = reportFilterDTOObj.getFromDate();
        if (fromDateRaw == null || !fromDateRaw.contains("T")) {
            throw new IllegalArgumentException("Invalid fromDate format. Expected format: 'YYYY-MM-DDTHH:mm:ss'");
        }

        String[] dateTime1 = fromDateRaw.split("T");
        if (dateTime1.length < 2) {
            throw new IllegalArgumentException("Invalid fromDate format. Missing time component.");
        }

        String fromDateTemp = dateTime1[0].trim();
        String fromTimeTemp = dateTime1[1];
        if (fromTimeTemp.contains("-")) {
            fromTimeTemp = fromTimeTemp.substring(0, fromTimeTemp.indexOf("-"));
        }
        reportFilterDTOObj.setFromDate(fromDateTemp + "T" + fromTimeTemp);

        /**
         * check for to date
         */
        String toDateRaw = reportFilterDTOObj.getToDate();
        /**
         * check if there is time zone in date
         */
        String[] dateTime2 = toDateRaw.split("T");
        String toDateTemp = dateTime2[0].trim();
        /*check if date string contains zone value*/
        String toTimeTemp = dateTime2[1];
        if (toTimeTemp.contains("-")) {
            toTimeTemp = toTimeTemp.substring(0, toTimeTemp.indexOf("-"));
        }
        //set value of to date
        reportFilterDTOObj.setToDate(toDateTemp + "T" + toTimeTemp);

        List<DetailedTripTicketDTO> detailedTripTicketDTOList = new ArrayList<>();
        List<TripTicket> tripTicketsByreportFilter = reportDAO.getTripTicketsByReportFilterWithoutCompleted(reportFilterDTOObj);

        /*seperate list having pickupdate time as null*/
        List<TripTicket> pickupDatetimePresentTicketsList = new ArrayList<>();
        List<TripTicket> dropOffDatetimePresentTicketsList = new ArrayList<>();
        for (TripTicket tripTicket : tripTicketsByreportFilter) {
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

        //sort list here on the basis of pickup date and time
        Collections.sort(pickupDatetimePresentTicketsList, new Comparator<TripTicket>() {
            @Override
            public int compare(TripTicket t1, TripTicket t2) {
                int result = t2.getRequestedPickupDate().compareTo(t1.getRequestedPickupDate());

                if (result == 0) {
                    return t1.getRequestedPickupTime().compareTo(t2.getRequestedPickupTime());

                }

                return t2.getRequestedPickupDate().compareTo(t1.getRequestedPickupDate());
            }

        });

        /*combining both lists*/
        if (!dropOffDatetimePresentTicketsList.isEmpty()) {
            pickupDatetimePresentTicketsList.addAll(dropOffDatetimePresentTicketsList);
        }

        for (TripTicket tripTicket : pickupDatetimePresentTicketsList) {

            // Use the shared converter so each claim reliably carries its claimant_provider_name and
            // status (it runs loadLazyFields + fixTripClaimProviderNames), letting the Current Tickets
            // report show the approved-else-pending-else-cancelled claimant.
            DetailedTripTicketDTO detailedTicketDTO = detailedTripTicketConverterService.convertToDetailedTripTicketDTO(tripTicket);
            detailedTripTicketDTOList.add(detailedTicketDTO);

        }
        return detailedTripTicketDTOList;

    }


    public List<CompletedTripReportDTO> findCompletedReport(ReportFilterDTO reportFilterDTOObj) {
        /**
         * check for from date
         */
        String fromDateRaw = reportFilterDTOObj.getFromDate();
        /**
         * check if there is time zone in date
         */
        String[] dateTime1 = fromDateRaw.split("T");
        String fromDateTemp = dateTime1[0].trim();
        /*check if date string contains zone value*/
        String fromTimeTemp = dateTime1[1];
        if (fromTimeTemp.contains("-")) {
            fromTimeTemp = fromTimeTemp.substring(0, fromTimeTemp.indexOf("-"));
        }
        reportFilterDTOObj.setFromDate(fromDateTemp + "T" + fromTimeTemp);

        /**
         * check for to date
         */
        String toDateRaw = reportFilterDTOObj.getToDate();
        /**
         * check if there is time zone in date
         */
        String[] dateTime2 = toDateRaw.split("T");
        String toDateTemp = dateTime2[0].trim();
        /*check if date string contains zone value*/
        String toTimeTemp = dateTime2[1];
        if (toTimeTemp.contains("-")) {
            toTimeTemp = toTimeTemp.substring(0, toTimeTemp.indexOf("-"));
        }
        //set value of to date
        reportFilterDTOObj.setToDate(toDateTemp + "T" + toTimeTemp);

        reportFilterDTOObj.setFromDateTime(parseDateTime(reportFilterDTOObj.getFromDate()));
        reportFilterDTOObj.setToDateTime(parseDateTime(reportFilterDTOObj.getToDate()));


        List<CompletedTripReportDTO> completedTripReportDTOs = new ArrayList<>();
        List<CompletedTripReportDTO> totalNoOfTicketsForCompletedTripReportDTOs = new ArrayList<>();
        completedTripReportDTOs = reportDAO.getCompletedReportDTOList(reportFilterDTOObj);

        totalNoOfTicketsForCompletedTripReportDTOs = reportDAO.getTotalNoOfTicketsCompletedReportDTOList(reportFilterDTOObj);

        for (CompletedTripReportDTO totalTicketCountForCompletedTripReportDTOObj : totalNoOfTicketsForCompletedTripReportDTOs) {

            /* checking for the each total count of providerName */
            for (CompletedTripReportDTO completedTripReportDTO : completedTripReportDTOs) {
                if (totalTicketCountForCompletedTripReportDTOObj.getProviderName()
                        .equalsIgnoreCase(completedTripReportDTO.getProviderName())) {
                    totalTicketCountForCompletedTripReportDTOObj
                            .setCompletedTicketCount(completedTripReportDTO.getCompletedTicketCount());
                }
            }

        }

        return totalNoOfTicketsForCompletedTripReportDTOs;
    }

    @Override
    public Object toDTO(Object bo) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public Object toBO(Object dto) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    @Override
    public Object toDTOCollection(Object boCollection) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }


    public List<DetailedTripTicketDTO> findTripTicketDetailsByStatusesReportFilterOBJ(ReportFilterDTO reportFilterDTO) {
        // Default: rows scoped by the originating provider (completed/cancelled reports).
        return findTripTicketDetailsByStatusesReportFilterOBJ(reportFilterDTO, false);
    }

    /*
     * IF CHANGING THE PROVIDER MONTHLY REPORT TO BE CLAIMANT-BASED: uncomment this entry point and
     * have ReportController.providerMonthlyTripTicketReport call it instead of the originator-based
     * method above. It scopes rows by the CLAIMING provider (falling back to the originating
     * provider when there is no approved claim), so a trip appears under the provider that actually
     * performed it. Also switch resolveCostProvider to its claimant-based branch. NOT YET TESTED.
     */
    // public List<DetailedTripTicketDTO> findTripTicketDetailsByClaimantReportFilterOBJ(ReportFilterDTO reportFilterDTO) {
    //     return findTripTicketDetailsByStatusesReportFilterOBJ(reportFilterDTO, true);
    // }

    private List<DetailedTripTicketDTO> findTripTicketDetailsByStatusesReportFilterOBJ(ReportFilterDTO reportFilterDTO, boolean claimantScoped) {

        List<ProviderPartnerDTO> providerPartnerDTOList = new ArrayList<ProviderPartnerDTO>();

        if (reportFilterDTO.isPartnerProviderTicket()) {
            providerPartnerDTOList = providerPartnerService
                    .findAllProviderPartnersByRequesterProviderId(reportFilterDTO.getProviderId());
        }
        if (reportFilterDTO.isMyTicket()) {
            providerPartnerDTOList.add(new ProviderPartnerDTO(reportFilterDTO.getProviderId()));
        }

        String inClause = "";
        if (providerPartnerDTOList != null && !providerPartnerDTOList.isEmpty()) {
            inClause = " IN (" + providerPartnerDTOList.stream().map(x -> String.valueOf(x.getRequesterProviderId()))
                    .collect(joining(", ")) + ") ";
        }
        reportFilterDTO.setInClauseQuery(inClause);
        /**
         * check for from date
         */
        String fromDateRaw = reportFilterDTO.getFromDate();
        /**
         * check if there is time zone in date
         */
        String[] dateTime1 = fromDateRaw.split("T");
        String fromDateTemp = dateTime1[0].trim();
        /* check if date string contains zone value */
        String fromTimeTemp = dateTime1[1];
        if (fromTimeTemp.contains("-")) {
            fromTimeTemp = fromTimeTemp.substring(0, fromTimeTemp.indexOf("-"));
        }
        reportFilterDTO.setFromDate(fromDateTemp + "T" + fromTimeTemp);

        /**
         * check for to date
         */
        String toDateRaw = reportFilterDTO.getToDate();
        /**
         * check if there is time zone in date
         */
        String[] dateTime2 = toDateRaw.split("T");
        String toDateTemp = dateTime2[0].trim();
        /* check if date string contains zone value */
        String toTimeTemp = dateTime2[1];
        if (toTimeTemp.contains("-")) {
            toTimeTemp = toTimeTemp.substring(0, toTimeTemp.indexOf("-"));
        }
        // set value of to date
        reportFilterDTO.setToDate(toDateTemp + "T" + toTimeTemp);

        reportFilterDTO.setFromDateTime(parseDateTime(reportFilterDTO.getFromDate()));
        reportFilterDTO.setToDateTime(parseDateTime(reportFilterDTO.getToDate()));

        List<DetailedTripTicketDTO> detailedTripTicketDTOList = new ArrayList<>();
        // claimantScoped is currently always false (originator-based). The claimant-based branch is
        // wired and ready but not yet enabled — see the commented claimant entry point above. NOT YET TESTED.
        List<TripTicket> tripTicketsByreportFilter = claimantScoped
                ? reportDAO.getTripTicketsForStatusesReportFilterByClaimant(reportFilterDTO)
                : reportDAO.getTripTicketsForStatusesReportFilter(reportFilterDTO);

        /* seperate list having pickupdate time as null */
        List<TripTicket> pickupDatetimePresentTicketsList = new ArrayList<>();
        List<TripTicket> dropOffDatetimePresentTicketsList = new ArrayList<>();
        for (TripTicket tripTicket : tripTicketsByreportFilter) {
            /* if ticket is avialabel and it has no claims then only */
            if (tripTicket.getRequestedPickupDate() == null && tripTicket.getRequestedPickupTime() == null) {
                dropOffDatetimePresentTicketsList.add(tripTicket);
            } else {
                pickupDatetimePresentTicketsList.add(tripTicket);
            }
        }

        /* sort dropOffdatetime list */
        /* sort list here on the basis of dropfoff date and time */
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

        // sort list here on the basis of pickup date and time
        Collections.sort(pickupDatetimePresentTicketsList, new Comparator<TripTicket>() {
            @Override
            public int compare(TripTicket t1, TripTicket t2) {
                int result = t2.getRequestedPickupDate().compareTo(t1.getRequestedPickupDate());
                if (result == 0) {
                    return t1.getRequestedPickupTime().compareTo(t2.getRequestedPickupTime());
                }
                return t2.getRequestedPickupDate().compareTo(t1.getRequestedPickupDate());
            }
        });

        if (!dropOffDatetimePresentTicketsList.isEmpty()) {
            pickupDatetimePresentTicketsList.addAll(dropOffDatetimePresentTicketsList);
        }

        for (TripTicket tripTicket : pickupDatetimePresentTicketsList) {
            // following TODTO convesrion is done because of the data in the
            // deatiledTripTicket is not as tripTicketDTO
            DetailedTripTicketDTO detailedTicketDTO = tripTicketModelMapper.map(tripTicket,
                    DetailedTripTicketDTO.class);
            if (tripTicket.getApprovedTripClaim() != null) {
                ProviderDTO providerDTO = providerModelMapper
                        .map(tripTicket.getApprovedTripClaim().getClaimantProvider(), ProviderDTO.class);
                detailedTicketDTO.setClaimant(providerDTO);
            }
            ProviderDTO originatorDTO = providerModelMapper.map(tripTicket.getOriginProvider(), ProviderDTO.class);
            detailedTicketDTO.setOriginator(originatorDTO);

            // The real route distance lives in the tripticketdistance table (not the trip ticket's
            // estimated_trip_distance, which is normally 0). Load it once and use it for both the
            // mileage column and the cost formula.
            TripTicketDistance timeAndDistance = tripTicketDistanceDAO.getDistanceByTripTicketId(tripTicket.getId());
            detailedTicketDTO.setMileage(timeAndDistance != null ? timeAndDistance.getTripTicketDistance() : 0f);
            detailedTicketDTO.setTripCost(resolveTripCost(tripTicket, timeAndDistance));
            detailedTripTicketDTOList.add(detailedTicketDTO);

        }
        return detailedTripTicketDTOList;
    }


    /**
     * Resolves the "Trip Cost" column for the All-Trips-by-Provider report.
     *
     * <p>Rows are scoped by the originating provider, so the cost uses that same originating
     * provider's rate card (keeping the cost consistent with the provider the report is filtered
     * on).
     *
     * <ul>
     *   <li><b>Cancelled trip</b> &rarr; the originating provider's {@code CancelledTripCost} (the
     *       UseCostFromProvider flag is ignored for cancelled trips).</li>
     *   <li><b>Completed trip + UseCostFromProvider</b> &rarr; the trip result's {@code Fare}.</li>
     *   <li><b>Completed trip otherwise</b> &rarr; the rate-card formula
     *       {@code getTotalCostOfProvider}, using the originating provider's cost data and the
     *       trip's stored planned distance/time.</li>
     * </ul>
     *
     * When the originating provider has no providercost row (or its rates / the fare are zero) the
     * cost is computed as-is and yields {@code 0}.
     */
    private float resolveTripCost(TripTicket tripTicket, TripTicketDistance timeAndDistance) {
        if (tripTicket == null || tripTicket.getStatus() == null) {
            return 0f;
        }

        Provider costProvider = resolveCostProvider(tripTicket);
        if (costProvider == null) {
            return 0f;
        }

        ProviderCost providerCost = providerCostDAO.findCostByProviderId(costProvider.getProviderId());
        if (providerCost == null) {
            // No rate card configured for this provider: treat all cost data as zero.
            providerCost = new ProviderCost();
        }

        int statusId = tripTicket.getStatus().getStatusId();

        // Cancelled trips use the flat per-provider cancellation cost; the flag does not apply.
        if (isCancelledStatus(statusId)) {
            return providerCost.getCancelledTripCost();
        }

        // Completed trips: provider's reported fare, or the rate-card formula.
        if (statusId == TripTicketStatusConstants.completed.tripTicketStatusUpdate()) {
            if (providerCost.isUseCostFromProvider()) {
                TripResult tripResult = tripTicket.getTripResult();
                return tripResult != null ? tripResult.getFare() : 0f;
            }
            if (timeAndDistance == null) {
                return 0f;
            }
            Float formulaCost = tripTicketService.getTotalCostOfProvider(tripTicket, timeAndDistance, providerCost);
            return formulaCost != null ? formulaCost : 0f;
        }

        // Any other status carries no trip cost.
        return 0f;
    }

    /**
     * The provider whose rate card / flag / cancellation cost drives a row's Trip Cost.
     *
     * <p>Currently the ORIGINATING provider, because the report's rows are scoped by the
     * originating provider.
     *
     * <p>IF CHANGING THE PROVIDER MONTHLY REPORT TO BE CLAIMANT-BASED: switch to the
     * claimant-else-originator selection (commented below) so each row's cost uses the same
     * provider the row is attributed to. NOT YET TESTED.
     */
    private Provider resolveCostProvider(TripTicket tripTicket) {
        return tripTicket.getOriginProvider();

        // Claimant-based alternative (performing provider; falls back to originator if no claim).
        // NOT YET TESTED. Use together with the claimant-based row scoping (see
        // findTripTicketDetailsByClaimantReportFilterOBJ / getTripTicketsForStatusesReportFilterByClaimant).
        // if (tripTicket.getApprovedTripClaim() != null
        //         && tripTicket.getApprovedTripClaim().getClaimantProvider() != null) {
        //     return tripTicket.getApprovedTripClaim().getClaimantProvider();
        // }
        // return tripTicket.getOriginProvider();
    }

    private boolean isCancelledStatus(int statusId) {
        return statusId == TripTicketStatusConstants.cancelled.tripTicketStatusUpdate()
                || statusId == TripTicketStatusConstants.cancelledByClient.tripTicketStatusUpdate()
                || statusId == TripTicketStatusConstants.cancelledByProvider.tripTicketStatusUpdate();
    }

}
