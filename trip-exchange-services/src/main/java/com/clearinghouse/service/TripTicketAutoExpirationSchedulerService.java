/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;

import com.clearinghouse.dao.NotificationDAO;
import com.clearinghouse.dao.ProviderDAO;
import com.clearinghouse.dao.TripTicketDAO;
import com.clearinghouse.dao.UserNotificationDataDAO;
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
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author chaitanyaP
 */
@Service
@AllArgsConstructor
@Slf4j
public class TripTicketAutoExpirationSchedulerService {


    private final UserNotificationDataDAO userNotificationDataDAO;


    private final ProviderDAO providerDAO;


    private final ActivityService activityService;


    private final NotificationDAO notificationDAO;


    private final TripTicketDAO tripTicketDAO;


    private final Configuration freemarkerConfiguration;

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<TripTicket> getAvailableTripTicketsWithNoClaims() {
        List<TripTicket> tripTickets = tripTicketDAO.getAvailableTripTickets();
        List<TripTicket> tripticketsWithNoClaims = new ArrayList<>();
        for (TripTicket tripTicketOld : tripTickets) {
            // filter tickets having no claims
            if (tripTicketOld.getTripClaims().isEmpty()) {
                tripticketsWithNoClaims.add(tripTicketOld);
            } else {
                //fetch latest claim sort claim by updatedDate then check status
                TripClaim tripClaim = tripTicketOld.getTripClaims().stream()
                        .sorted((e1, e2) -> e1.getUpdatedAt().compareTo(e2.getUpdatedAt()))
                        .collect(Collectors.toList()).get((tripTicketOld.getTripClaims().size()) - 1);

                // Only consider this ticket as having 'no claims' if the latest claim
                // explicitly has a declined or rescined status. If status is missing
                // or null, do not assume it is claim-free — skip adding.
                if (tripClaim != null && tripClaim.getStatus() != null) {
                    int statusId = tripClaim.getStatus().getStatusId();
                    if (statusId == TripClaimStatusConstants.declined.tripClaimStatusUpdate()
                            || statusId == TripClaimStatusConstants.rescined.tripClaimStatusUpdate()) {
                        tripticketsWithNoClaims.add(tripTicketOld);
                    }
                }
            }
        }

        return tripticketsWithNoClaims;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    @Scheduled(initialDelay = 60000, fixedRate = 3600000)//this is for 333.33333 min or  5.5555555556hr
    public void tripTicketAutoExpiration() {

        log.debug("tripTicketAutoExpiration: Started expiration scheduler-----------------------------------------------------------");
        //take trip tickets which are availbale check there expiration date time <currebt date time
//  if yes make that  ticket as expired and send mail to originator
        List<TripTicket> availbaleTripTickets = getAvailableTripTicketsWithNoClaims();
        if (!availbaleTripTickets.isEmpty()) {
            for (TripTicket tripTicket : availbaleTripTickets) {

                //expiration date time must not be empty
                if (tripTicket.getExpirationDate() != null) {

                    LocalDateTime currentDateTime = LocalDateTime.now(Clock.systemDefaultZone());//ZoneId.of("UTC-6") MDT
                    int result = tripTicket.getExpirationDate().compareTo(currentDateTime);

                    if (result < 0) {
                        log.debug("trip ticket id {} is expired", tripTicket.getId());
                        Status statusForTicketExpiration = new Status();
                        statusForTicketExpiration.setStatusId(TripTicketStatusConstants.expired.tripTicketStatusUpdate());

                        tripTicket.setStatus(statusForTicketExpiration);
                        tripTicket.setLastStatusChangedByProvider(tripTicket.getOriginProvider());
                        tripTicket.setExpired(true);
                        tripTicketDAO.updateTripTicket(tripTicket);

                        //send mail to originator that ticket is expired
                        sendMailtoOriginatorForTicketExpired(tripTicket);

                        //add activity records
                        addActivtiyForTicketExpired(tripTicket);
                    }
                }
            }
        }
        log.debug("tripTicketAutoExpiration: Ended expiration scheduler-----------------------------------------------------------");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected void sendMailtoOriginatorForTicketExpired(TripTicket tripTicket) {
        //        send mail for claim approved
        List<User> usersOfOriginator = userNotificationDataDAO.getUsersOfProvider(tripTicket.getOriginProvider().getProviderId());
        for (User user : usersOfOriginator) {
            //fetching single obj of the user role
            List<UserAuthority> userAuthority = new ArrayList<>();
            userAuthority.addAll(user.getAuthorities());
            userAuthority.addAll(user.getAuthorities());
            String userrole = userAuthority.get(0).getAuthority();

            if (user.isIsNotifyTripExpired()) {

                //NotificationEnginePart.....
                Notification emailNotification = new Notification();
                NotificationTemplate notificationTemplate = new NotificationTemplate();
                emailNotification.setEmailTo(user.getEmail());
                emailNotification.setIsEMail(true);
                emailNotification.setStatusId(NotificationStatus.newStatus.status());
                notificationTemplate.setNotificationTemplateId(NotificationTemplateCodeValue.ticketExpired.templateCodeValue());
                emailNotification.setNotificationTemplate(notificationTemplate);
                emailNotification.setNumberOfAttempts(0);
                emailNotification.setIsActive(true);

                //        Setting parameter values in according to the template.
                Map autoExpirdTicketTemplateMap = new HashMap<String, String>();

                autoExpirdTicketTemplateMap.put("name", user.getName());
                autoExpirdTicketTemplateMap.put("commonTripTicketId", tripTicket.getCommonTripId());
                autoExpirdTicketTemplateMap.put("year", Year.now().toString());

                //                 formatting the date
//                String rowDateFromDatabase = tripTicket.getRequestedPickupDate().toString();
//                String[] arrayOfDate = rowDateFromDatabase.split("-");
//                String finalDate = arrayOfDate[1] + "-" + arrayOfDate[2] + "-" + arrayOfDate[0];
//
//                approvedClaimTemplateMap.put("pickupDate", finalDate);
//                String pickupTime = tripTicket.getRequestedPickupTime().toString();
//                String[] arrayforPickupTime = pickupTime.split(":");
//
//                /* conversion of 24 hrs to 12 hrs*/
//                String AMOrPM = "AM";
//                int hrHnadValue = Integer.valueOf(arrayforPickupTime[0]);
//                if (hrHnadValue > 12) {
//                    hrHnadValue = hrHnadValue - 12;
//                    AMOrPM = "PM";
//                }
//
//                approvedClaimTemplateMap.put("pickupTime", hrHnadValue + ":" + arrayforPickupTime[1] + " " + AMOrPM);
                populatePickupDetails(autoExpirdTicketTemplateMap, tripTicket);

                String jsonValueOfTemplate = "";

                Iterator<Map.Entry<String, String>> entries = autoExpirdTicketTemplateMap.entrySet().iterator();
                while (entries.hasNext()) {

                    Map.Entry<String, String> entry = entries.next();
                    jsonValueOfTemplate = jsonValueOfTemplate + "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
                    if (entries.hasNext()) {
                        jsonValueOfTemplate = jsonValueOfTemplate + ",";
                    }

                }

                String FinaljsonValueOfTemplate = "{" + jsonValueOfTemplate + "}";

                emailNotification.setParameterValues(FinaljsonValueOfTemplate);

                emailNotification.setSubject("Trip ticket is expired");

                notificationDAO.createNotification(emailNotification);
            }
        }

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public synchronized void addActivtiyForTicketExpired(TripTicket tripTicket) {
        Provider provider = providerDAO.findProviderByProviderId(tripTicket.getOriginProvider().getProviderId());
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setAction("Ticket Expired");
        activityDTO.setTripTicketId(tripTicket.getId());
        activityDTO.setActionTakenBy(provider.getProviderName());
        activityDTO.setActionDetails("originatorProvider=" + provider.getProviderName() + "status=Expired");
        activityService.createActivity(activityDTO);
    }

}
