/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;

import com.clearinghouse.dao.*;
import com.clearinghouse.dto.ActivityDTO;
import com.clearinghouse.entity.*;
import com.clearinghouse.enumentity.NotificationStatus;
import com.clearinghouse.enumentity.NotificationTemplateCodeValue;
import com.clearinghouse.enumentity.TripClaimStatusConstants;
import com.clearinghouse.enumentity.TripTicketStatusConstants;
import freemarker.template.Configuration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 *
 * @author chaitanyaP
 */
@Service
@Slf4j
@AllArgsConstructor
public class NotificationSchedularAutoApprovalClaimsService {


    private final Configuration freemarkerConfiguration;


    private final NotificationDAO notificationDAO;


    private final TicketFilterService filterService;


    private final TripTicketDAO tripTicketDAO;


    private final ActivityService activityService;


    private final ProviderDAO providerDAO;


    private final ApplicationSettingDAO applicationSettingDAO;


    private final UserNotificationDataDAO userNotificationDataDAO;

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
            templateMap.put("pickupDate", "Pickup Date- No requested pickup date,");
            templateMap.put("pickupTime", buildDropoffFallback(tripTicket));
        }
    }

    private String buildDropoffFallback(TripTicket tripTicket) {
        String dropoffDate = formatDisplayDate(tripTicket.getRequestedDropoffDate());
        String dropoffTime = formatDisplayTime(tripTicket.getRequestedDropOffTime());
        return String.format("Pickup Time - No requested pickup time,Dropoff date - %s Drop Off time - %s ", dropoffDate, dropoffTime);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<TripTicket> getsynchronizedAvailableTripTickets() {
        List<TripTicket> tripTickets = tripTicketDAO.getAvailableTripTickets();
        return tripTickets;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    @Scheduled(initialDelay = 100000, fixedRate = 1800000)//this is for 30 min or approx 0.5 hr 1800000 miliseconds
    public void autoApprovalClaim() {

//        filterService.filterTicketsByFilterObject(filterService.findFilterByFilterId(5));
        List<ApplicationSetting> applicationSettings = applicationSettingDAO.findAllApplicationSettings();

        int autoApprovalOfClaimDaysBeforeinhours = applicationSettings.get(0).getClaimApprovalTimeInHours();

        List<TripTicket> tripTickets = getsynchronizedAvailableTripTickets();

//        Fetch the trip tickehaving pending status and trip claims!=null
//       fetch the trip claim list
//if Trip date -cuurent date = system set date for auto approval
//if true the fetc the cliam from the list and make it appove and update trip tickety accordingly
        if (!tripTickets.isEmpty()) {
            for (TripTicket tripTicket : tripTickets) {

                if (!tripTicket.getTripClaims().isEmpty()) {
                    Set<TripClaim> tripClaims = tripTicket.getTripClaims();
                    List<TripClaim> tripClaimList = new ArrayList<>();
                    tripClaimList.addAll(tripClaims);
                    //fetching single trip claim..
                    for (TripClaim tripClaimFromList : tripClaimList) {

                        boolean isClaimantTrustedPartner = userNotificationDataDAO.isProviderPartnerTrustedForOrginator(tripTicket.getOriginProvider().getProviderId(), tripClaimFromList.getClaimantProvider().getProviderId());

                        if (!isClaimantTrustedPartner) {

                            ZonedDateTime dateTime = tripClaimFromList.getCreatedAt();
                            dateTime = dateTime.plusHours(autoApprovalOfClaimDaysBeforeinhours);

//                    ZoneId.of("UTC-8")..make all time into pst using this for production
                            ZonedDateTime currentDateTime = ZonedDateTime.now(Clock.systemDefaultZone());
                            //comparing two date time
                            int result = currentDateTime.compareTo(dateTime);

                            if (tripClaimFromList.getStatus().getStatusId() == TripClaimStatusConstants.pending.tripClaimStatusUpdate() && (result > 0)) {

//                        approve that claim and ticket send mail accordingly and decline other claims
                                Status statusUpdateForApprovedTripClaim = new Status();
                                statusUpdateForApprovedTripClaim.setStatusId(TripClaimStatusConstants.approved.tripClaimStatusUpdate());

                                tripClaimFromList.setStatus(statusUpdateForApprovedTripClaim);

                                tripTicket.setApprovedTripClaim(tripClaimFromList);

                                Status statusUpdateForTripTicket = new Status();
                                statusUpdateForTripTicket.setStatusId(TripTicketStatusConstants.approved.tripTicketStatusUpdate());
                                tripTicket.setStatus(statusUpdateForTripTicket);

                                Set<TripClaim> tripClaimsUpdateRequired = tripTicket.getTripClaims();
                                tripTicket.setLastStatusChangedByProvider(tripTicket.getOriginProvider());
                                //sending mail to the originator
                                sendMailToOriginatorCheckingAutoApproveFlag(tripTicket);
                                //send mail to other claimants for declined claim
                                if (tripClaimList.size() > 1) {
                                    sendMailToClaimantsForDeclinedAutoApprovalCase(tripTicket, tripClaimFromList);
                                }
                                for (TripClaim tripClaimUpdated : tripClaimsUpdateRequired) {
                                    if (tripClaimUpdated.getId() != tripClaimFromList.getId()) {
                                        Status statusForClaim = new Status();
                                        statusForClaim.setStatusId(TripClaimStatusConstants.declined.tripClaimStatusUpdate());
                                        tripClaimUpdated.setStatus(statusForClaim);
                                        /*add reord for activity when trip claim is declined*/
                                        createActivityForClaimDeclined(tripTicket, tripClaimUpdated);
                                    }

                                }
                                tripTicket.setTripClaims(tripClaimsUpdateRequired);

                                tripTicketDAO.updateTripTicket(tripTicket);
                                createActivityForClaimApprovedAndTicketApproved(tripTicket, tripClaimFromList);

                                //        send mail for claim approved
                                List<User> usersOfClaimant = userNotificationDataDAO.getUsersForTripClaimApproved(tripTicket.getApprovedTripClaim().getClaimantProvider().getProviderId());
                                for (User user : usersOfClaimant) {
                                    //fetching single obj of the user role
                                    List<UserAuthority> userAuthority = new ArrayList<>();
                                    userAuthority.addAll(user.getAuthorities());
                                    userAuthority.addAll(user.getAuthorities());
                                    String userrole = userAuthority.get(0).getAuthority();
                                    if (user.isIsNotifyTripClaimApproved() ) {

                                        //NotificationEnginePart.....
                                        Notification emailNotification = new Notification();
                                        NotificationTemplate notificationTemplate = new NotificationTemplate();
                                        emailNotification.setEmailTo(user.getEmail());
                                        emailNotification.setIsEMail(true);
                                        emailNotification.setStatusId(NotificationStatus.newStatus.status());
                                        notificationTemplate.setNotificationTemplateId(NotificationTemplateCodeValue.claimApprovedTemplateCode.templateCodeValue());
                                        emailNotification.setNotificationTemplate(notificationTemplate);
                                        emailNotification.setNumberOfAttempts(0);
                                        emailNotification.setIsActive(true);

                                        //        Setting parameter values in according to the template.
                                        Map approvedClaimTemplateMap = new HashMap<String, String>();

                                        approvedClaimTemplateMap.put("nameOfUser", user.getName());
                                        approvedClaimTemplateMap.put("commonTripTicketId", tripTicket.getCommonTripId());
                                        approvedClaimTemplateMap.put("lastStatusChangedByProviderName", tripTicket.getLastStatusChangedByProvider().getProviderName());
                                        //newly added for tripticketCost
                                        approvedClaimTemplateMap.put("requesterFare", String.valueOf(tripClaimFromList.getRequesterProviderFare()));
                                        approvedClaimTemplateMap.put("calculatedProposedFare", String.valueOf(tripClaimFromList.getCalculatedProposedFare()));
                                        approvedClaimTemplateMap.put("proposedFare", String.valueOf(tripClaimFromList.getProposedFare()));

                                        if (tripClaimFromList.isAckStatus()) {
                                            approvedClaimTemplateMap.put("ackStatus", "Please be aware that the claimed trip occurs outside of the specified operating hours for the Provider who claimed the trip, and both the owner of the trip and the claimant Provider have acknowledged this situation.");
                                        } else {
                                            approvedClaimTemplateMap.put("ackStatus", "");
                                        }
                                        approvedClaimTemplateMap.put("year", Year.now().toString());

                                        //                 formatting the date
                                        populatePickupDetails(approvedClaimTemplateMap, tripTicket);
                                        String jsonValueOfTemplate = "";

                                        Iterator<Map.Entry<String, String>> entries = approvedClaimTemplateMap.entrySet().iterator();
                                        while (entries.hasNext()) {

                                            Map.Entry<String, String> entry = entries.next();
                                            jsonValueOfTemplate = jsonValueOfTemplate + "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
                                            if (entries.hasNext()) {
                                                jsonValueOfTemplate = jsonValueOfTemplate + ",";
                                            }

                                        }

                                        String FinaljsonValueOfTemplate = "{" + jsonValueOfTemplate + "}";

                                        emailNotification.setParameterValues(FinaljsonValueOfTemplate);

                                        emailNotification.setSubject("Trip claim is approved");

                                        notificationDAO.createNotification(emailNotification);
                                    }
                                }

                            }
                        }
                    }

                }

            }
        }

    }

    /*method for the adding activity record for claim approved*/
    @Transactional(propagation = Propagation.REQUIRED)
    public void createActivityForClaimApprovedAndTicketApproved(TripTicket tripTicket, TripClaim tripClaim) {
        Provider provider = providerDAO.findProviderByProviderId(tripTicket.getLastStatusChangedByProvider().getProviderId());
        Provider claimantProvider = providerDAO.findProviderByProviderId(tripClaim.getClaimantProvider().getProviderId());
        ActivityDTO activityDTOForTripClaim = new ActivityDTO();
        activityDTOForTripClaim.setTripTicketId(tripClaim.getTripTicket().getId());
        activityDTOForTripClaim.setAction("Claim approved");
        activityDTOForTripClaim.setActionDetails("status=approved,claimant_provider=" + claimantProvider.getProviderName() + ",proposed_fare =" + tripClaim.getProposedFare() + ",proposed_pickup_time=" + tripClaim.getProposedPickupTime()
        );
        activityDTOForTripClaim.setActionTakenBy(provider.getProviderName());
        activityService.createActivity(activityDTOForTripClaim);

        /* activity for trip ticket approved*/
        Provider originatorProvider = providerDAO.findProviderByProviderId(tripTicket.getOriginProvider().getProviderId());
        ActivityDTO activityDTOForTripTicket = new ActivityDTO();

        activityDTOForTripTicket.setTripTicketId(tripClaim.getTripTicket().getId());
        activityDTOForTripTicket.setAction("Ticket approved");
        activityDTOForTripTicket.setActionDetails("status=approved,originator_provider=" + originatorProvider.getProviderName() + ",requested_pickup_time=" + tripTicket.getRequestedPickupTime() + ",requested_pickup_date=" + tripTicket.getRequestedPickupDate()
        );
        activityDTOForTripTicket.setActionTakenBy(provider.getProviderName());
        activityService.createActivity(activityDTOForTripTicket);

    }

    /*method for the adding activity record for claim declined*/
    @Transactional(propagation = Propagation.REQUIRED)
    public void createActivityForClaimDeclined(TripTicket tripTicket, TripClaim tripClaim) {
        Provider providerForTripTicket = providerDAO.findProviderByProviderId(tripTicket.getLastStatusChangedByProvider().getProviderId());
        Provider providerFortripClaim = providerDAO.findProviderByProviderId(tripClaim.getClaimantProvider().getProviderId());
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setTripTicketId(tripClaim.getTripTicket().getId());
        activityDTO.setAction("Claim declined");
        activityDTO.setActionDetails("status=declined,claimant_provider=" + providerFortripClaim.getProviderName());
        activityDTO.setActionTakenBy(providerForTripTicket.getProviderName());
        activityService.createActivity(activityDTO);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void sendMailToOriginatorCheckingAutoApproveFlag(TripTicket tripTicket) {

        //NotificationEnginePart.....


        Set<TripClaim> tripClaims = tripTicket.getTripClaims();
        for (TripClaim tripclaim : tripClaims) {
            List<User> usersOfClaimant = userNotificationDataDAO.getUsersForAutoApprovalTripClaim(tripTicket.getOriginProvider().getProviderId());
            for (User user : usersOfClaimant) {

                Notification emailNotification = new Notification();
                NotificationTemplate notificationTemplate = new NotificationTemplate();

                emailNotification.setIsEMail(true);
                emailNotification.setStatusId(NotificationStatus.newStatus.status());
                notificationTemplate.setNotificationTemplateId(NotificationTemplateCodeValue.tripClaimAutoApproved.templateCodeValue());
                emailNotification.setNotificationTemplate(notificationTemplate);
                emailNotification.setNumberOfAttempts(0);
                emailNotification.setIsActive(true);


                emailNotification.setEmailTo(user.getEmail());
                //fetching single obj of the user role
                List<UserAuthority> userAuthority = new ArrayList<>();
                userAuthority.addAll(user.getAuthorities());
                userAuthority.addAll(user.getAuthorities());
                String userrole = userAuthority.get(0).getAuthority();
                if (user.isIsNotifyNewTripClaimAutoApproved() ) {

//        Setting parameter values in according to the template.
                    Map approvedClaimTemplateMap = new HashMap<String, String>();

                    approvedClaimTemplateMap.put("nameOfUser", user.getName());
                    approvedClaimTemplateMap.put("commonTripTicketId", tripTicket.getCommonTripId());
                    approvedClaimTemplateMap.put("passengerName", tripTicket.getCustomerFirstName() + " " + tripTicket.getCustomerLastName());
                    approvedClaimTemplateMap.put("providerName", tripTicket.getApprovedTripClaim().getClaimantProvider().getProviderName());

                    //newly added for tripticketCost
                    approvedClaimTemplateMap.put("requesterFare", String.valueOf(tripclaim.getRequesterProviderFare()));
                    approvedClaimTemplateMap.put("calculatedProposedFare", String.valueOf(tripclaim.getCalculatedProposedFare()));
                    approvedClaimTemplateMap.put("proposedFare", String.valueOf(tripclaim.getProposedFare()));

                    if (tripclaim.isAckStatus()) {
                        approvedClaimTemplateMap.put("ackStatus", "Please be aware that the claimed trip occurs outside of the specified operating hours for the Provider who claimed the trip, and both the owner of the trip and the claimant Provider have acknowledged this situation.");
                    } else {
                        approvedClaimTemplateMap.put("ackStatus", "");
                    }
                    approvedClaimTemplateMap.put("year", Year.now().toString());

                    populatePickupDetails(approvedClaimTemplateMap, tripTicket);

                    String jsonValueOfTemplate = "";

                    Iterator<Map.Entry<String, String>> entries = approvedClaimTemplateMap.entrySet().iterator();
                    while (entries.hasNext()) {

                        Map.Entry<String, String> entry = entries.next();
                        jsonValueOfTemplate = jsonValueOfTemplate + "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
                        if (entries.hasNext()) {
                            jsonValueOfTemplate = jsonValueOfTemplate + ",";
                        }

                    }

                    String FinaljsonValueOfTemplate = "{" + jsonValueOfTemplate + "}";

                    emailNotification.setParameterValues(FinaljsonValueOfTemplate);

                    emailNotification.setSubject("Trip claim is auto approved");

                    notificationDAO.createNotification(emailNotification);
                }
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void sendMailToClaimantsForDeclinedAutoApprovalCase(TripTicket tripTicket, TripClaim tripClaimFromList) {

        Set<TripClaim> tripClaims = tripTicket.getTripClaims();
        List<TripClaim> tripClaimList = new ArrayList<>();
        for (TripClaim tripClaimFromSet : tripClaims) {
            if (tripClaimFromSet.getId() != tripClaimFromList.getId()) {
                tripClaimList.add(tripClaimFromSet);
            }
        }
        for (TripClaim tripClaim : tripClaimList) {

            List<User> usersOfClaimant = userNotificationDataDAO.getUsersForTripClaimDeclined(tripClaim.getClaimantProvider().getProviderId());
            for (User user : usersOfClaimant) {
                //fetching single obj of the user role
                List<UserAuthority> userAuthority = new ArrayList<>();
                userAuthority.addAll(user.getAuthorities());
                userAuthority.addAll(user.getAuthorities());
                String userrole = userAuthority.get(0).getAuthority();
                if (user.isIsNotifyTripClaimDeclined() ) {

                    //NotificationEnginePart.....
                    Notification emailNotification = new Notification();
                    NotificationTemplate notificationTemplate = new NotificationTemplate();
                    emailNotification.setEmailTo(user.getEmail());
                    emailNotification.setIsEMail(true);
                    emailNotification.setStatusId(NotificationStatus.newStatus.status());
                    notificationTemplate.setNotificationTemplateId(NotificationTemplateCodeValue.claimDeclinedTemplateCode.templateCodeValue());
                    emailNotification.setNotificationTemplate(notificationTemplate);
                    emailNotification.setNumberOfAttempts(0);
                    emailNotification.setIsActive(true);

//        Setting parameter values in according to the template.
                    Map approvedClaimTemplateMap = new HashMap<String, String>();

                    approvedClaimTemplateMap.put("nameOfUser", user.getName());
                    approvedClaimTemplateMap.put("commonTripTicketId", tripTicket.getCommonTripId());
                    approvedClaimTemplateMap.put("lastStatusChangedByProviderName", tripTicket.getLastStatusChangedByProvider().getProviderName());
                    //newly added for tripticketCost
                    approvedClaimTemplateMap.put("requesterFare", String.valueOf(tripClaim.getRequesterProviderFare()));
                    approvedClaimTemplateMap.put("calculatedProposedFare", String.valueOf(tripClaim.getCalculatedProposedFare()));
                    approvedClaimTemplateMap.put("proposedFare", String.valueOf(tripClaim.getProposedFare()));
                    if (tripClaim.isAckStatus()) {
                        approvedClaimTemplateMap.put("ackStatus", "Please be aware that the claimed trip occurs outside of the specified operating hours for the Provider who claimed the trip, and both the owner of the trip and the claimant Provider have acknowledged this situation.");
                    } else {
                        approvedClaimTemplateMap.put("ackStatus", "");
                    }
                    approvedClaimTemplateMap.put("year", Year.now().toString());

                    populatePickupDetails(approvedClaimTemplateMap, tripTicket);

                    String jsonValueOfTemplate = "";

                    Iterator<Map.Entry<String, String>> entries = approvedClaimTemplateMap.entrySet().iterator();
                    while (entries.hasNext()) {

                        Map.Entry<String, String> entry = entries.next();
                        jsonValueOfTemplate = jsonValueOfTemplate + "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
                        if (entries.hasNext()) {
                            jsonValueOfTemplate = jsonValueOfTemplate + ",";
                        }

                    }

                    String FinaljsonValueOfTemplate = "{" + jsonValueOfTemplate + "}";

                    emailNotification.setParameterValues(FinaljsonValueOfTemplate);

                    emailNotification.setSubject("Trip claim is declined");

                    notificationDAO.createNotification(emailNotification);
                }
            }
        }

    }

}
