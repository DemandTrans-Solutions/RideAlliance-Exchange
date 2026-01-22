/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;

import com.clearinghouse.dao.ReportDAO;
import com.clearinghouse.dao.TripTicketDAO;
import com.clearinghouse.dto.*;
import com.clearinghouse.entity.TripClaim;
import com.clearinghouse.entity.TripTicket;
import com.clearinghouse.enumentity.TripClaimStatusConstants;
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

            //following TODTO convesrion is done because of the data in the deatiledTripTicket is not as  tripTicketDTO
            DetailedTripTicketDTO detailedTicketDTO = tripTicketModelMapper.map(tripTicket, DetailedTripTicketDTO.class);

            if (tripTicket.getApprovedTripClaim() != null) {
                ProviderDTO providerDTO = providerModelMapper.map(tripTicket.getApprovedTripClaim().getClaimantProvider(), ProviderDTO.class);

                detailedTicketDTO.setClaimant(providerDTO);

            }
            ProviderDTO originatorDTO = providerModelMapper.map(tripTicket.getOriginProvider(), ProviderDTO.class);
            detailedTicketDTO.setOriginator(originatorDTO);
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

            //following TODTO convesrion is done because of the data in the deatiledTripTicket is not as  tripTicketDTO
            DetailedTripTicketDTO detailedTicketDTO = tripTicketModelMapper.map(tripTicket, DetailedTripTicketDTO.class);

            if (tripTicket.getApprovedTripClaim() != null) {
                ProviderDTO providerDTO = providerModelMapper.map(tripTicket.getApprovedTripClaim().getClaimantProvider(), ProviderDTO.class);

                detailedTicketDTO.setClaimant(providerDTO);

            }
            ProviderDTO originatorDTO = providerModelMapper.map(tripTicket.getOriginProvider(), ProviderDTO.class);
            detailedTicketDTO.setOriginator(originatorDTO);
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


    public List<DetailedTripTicketDTO> findCancelTripTicketDetailsByReportFilterOBJ(ReportFilterDTO reportFilterDTO) {

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
        List<TripTicket> tripTicketsByreportFilter = reportDAO.getTripTicketsForCancelStatusReportFilter(reportFilterDTO);

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
            detailedTripTicketDTOList.add(detailedTicketDTO);

        }
        return detailedTripTicketDTOList;
    }


    public List<DetailedTripTicketDTO> findCompletedTripTicketDetailsByReportFilterOBJ(ReportFilterDTO reportFilterDTO) {

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
        List<TripTicket> tripTicketsByreportFilter = reportDAO.getTripTicketsForCompletedStatusReportFilter(reportFilterDTO);

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
            detailedTripTicketDTOList.add(detailedTicketDTO);

        }
        return detailedTripTicketDTOList;
    }


}
