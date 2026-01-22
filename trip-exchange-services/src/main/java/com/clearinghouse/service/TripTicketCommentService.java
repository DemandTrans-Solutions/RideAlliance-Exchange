/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;

import com.clearinghouse.dao.*;
import com.clearinghouse.dto.ActivityDTO;
import com.clearinghouse.dto.TripTicketCommentDTO;
import com.clearinghouse.entity.*;
import com.clearinghouse.enumentity.NotificationStatus;
import com.clearinghouse.enumentity.NotificationTemplateCodeValue;
import com.clearinghouse.enumentity.TripTicketStatusConstants;
import com.clearinghouse.listresponseentity.ProviderList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.time.Year;
import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 *
 * @author chaitanyaP
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class TripTicketCommentService implements IConvertBOToDTO, IConvertDTOToBO {


    private final TripTicketCommentDAO tripTicketCommentDAO;


    private final TripTicketDAO tripTicketDAO;


    private final TripClaimService tripClaimService;


    private final UserDAO userDAO;


    private final ListDAO listDAO;


    private final ActivityService activityService;


    private final ProviderDAO providerDAO;


    private final UserNotificationDataDAO userNotificationDataDAO;


    private final NotificationDAO notificationDAO;


    private final ModelMapper tripTicketCommentModelMapper;

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
            templateMap.put("pickupDate", "Pickup date - No requested pickup date,");
            templateMap.put("pickupTime", buildDropoffFallback(tripTicket));
        }
    }

    private String buildDropoffFallback(TripTicket tripTicket) {
        String dropoffDate = formatDisplayDate(tripTicket.getRequestedDropoffDate());
        String dropoffTime = formatDisplayTime(tripTicket.getRequestedDropOffTime());
        return String.format("Pickup time - No requested pickup time,Dropoff date - %s Dropoff time - %s ", dropoffDate, dropoffTime);
    }


    public List<TripTicketCommentDTO> findAllTripTicketCommetstByTripTicketId(int trip_ticket_id) {

        List<TripTicketComment> tripTicketComments = tripTicketCommentDAO.findAllTripTicketCommentsByTripTicketId(trip_ticket_id);

        List<TripTicketCommentDTO> tripTicketCommentDTOList = new java.util.ArrayList<>();
        for (TripTicketComment tripTicketComment : tripTicketComments) {

            tripTicketCommentDTOList.add((TripTicketCommentDTO) toDTO(tripTicketComment));
        }

        return tripTicketCommentDTOList;

    }


    public TripTicketCommentDTO findTripTicketCommentById(int id) {

        return (TripTicketCommentDTO) toDTO(tripTicketCommentDAO.findTripTicketCommentById(id));
    }


    public TripTicketCommentDTO createTripTicketComment(int trip_ticket_id, TripTicketCommentDTO ticketCommentDTO) {

        User userFromDb = userDAO.findUserByUserId(ticketCommentDTO.getUserId());

        ticketCommentDTO.setTripTicketId(trip_ticket_id);
        String providerName = tripTicketCommentDAO.getProviderName(userFromDb.getProvider().getProviderId());
        ticketCommentDTO.setUserName(userFromDb.getName());
        ticketCommentDTO.setNameOfProvider(providerName);

        TripTicketComment tripTicketComment = (TripTicketComment) toBO(ticketCommentDTO);
        tripTicketCommentDAO.createTripTicketComment(tripTicketComment);

        /*calling method from trip claim service to update the update_at value of ticket*/
        String status = tripClaimService.updateTicketForClaimAction(trip_ticket_id);

        /*create activity for comment added*/
        createActivityForTripTicketComment(ticketCommentDTO);
        List<ProviderList> finalProviderList = new ArrayList<>();

        TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(tripTicketComment.getTripTicket().getId());
//        send mail notification to all the provider who are able to view this ticket by service area

        if (tripTicket.getStatus().getStatusId() != TripTicketStatusConstants.approved.tripTicketStatusUpdate()) {
            /*get all the partners of ticket originator*/
            List<ProviderList> providerLists = listDAO.getOriginatorProviderListByProviderId(tripTicket.getOriginProvider().getProviderId());

            /*selecting only those providers other than comment creator provider*/
            for (ProviderList providerListObj : providerLists) {
                if (providerListObj.getProviderId() != userFromDb.getProvider().getProviderId()) {
                    finalProviderList.add(providerListObj);
                }
            }
        } else if (tripTicket.getApprovedTripClaim().getClaimantProvider().getProviderId() == userFromDb.getProvider().getProviderId()) {
            ProviderList listObj = new ProviderList(tripTicket.getOriginProvider().getProviderId(), "OrginatorProvider");
            finalProviderList.add(listObj);
        } else {

            ProviderList listObj = new ProviderList(tripTicket.getApprovedTripClaim().getClaimantProvider().getProviderId(), "ClaimantProvider");
            finalProviderList.add(listObj);
        }

        for (ProviderList providerListObj : finalProviderList) {

//            fetch the users of that provider having flag notify the  claimed trip ticket rescinded
            //NotificationEnginePart.. for trip ticket originator

            /*fetching users of the provider*/
            List<User> users = userNotificationDataDAO.getUsersForTripCommentAdded(providerListObj.getProviderId());

            for (User user : users) {
                //fetching single obj of the user role
                List<UserAuthority> userAuthority = new ArrayList<>();

                userAuthority.addAll(user.getAuthorities());
                String userrole = userAuthority.get(0).getAuthority();
                if (user.isIsNotifyTripCommentAdded() ) {

                    Notification emailNotificationForClaimant = new Notification();
                    NotificationTemplate notificationTemplateForClaimant = new NotificationTemplate();

                    emailNotificationForClaimant.setIsEMail(true);
                    emailNotificationForClaimant.setStatusId(NotificationStatus.newStatus.status());
                    notificationTemplateForClaimant.setNotificationTemplateId(NotificationTemplateCodeValue.tripCommentAdded.templateCodeValue());
                    emailNotificationForClaimant.setNotificationTemplate(notificationTemplateForClaimant);
                    emailNotificationForClaimant.setNumberOfAttempts(0);
                    emailNotificationForClaimant.setIsActive(true);

                    emailNotificationForClaimant.setEmailTo(user.getEmail());

                    //   Setting parameter values in according to the template for email notofication.
                    Map rescindClaimantTemplateMap = new HashMap<String, String>();
                    rescindClaimantTemplateMap.put("nameOftheuser", user.getName());
                    rescindClaimantTemplateMap.put("commonTripTicketId", tripTicket.getCommonTripId());
                    rescindClaimantTemplateMap.put("customerName", tripTicket.getCustomerFirstName() + " " + tripTicket.getCustomerLastName());
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
                    emailNotificationForClaimant.setSubject("Trip comment is added");
                    notificationDAO.createNotification(emailNotificationForClaimant);
                }

            }

        }

        TripTicketCommentDTO ticketCommentDTOCreated = (TripTicketCommentDTO) toDTO(tripTicketComment);
        ticketCommentDTOCreated.setCreatedAt(ZonedDateTime.now().toString());

        if (status.equalsIgnoreCase("success")) {
            return ticketCommentDTOCreated;
        } else {
            return null;
        }
    }


    public TripTicketCommentDTO updateTripTicketComment(int trip_ticket_id, TripTicketCommentDTO tripTicketCommentDTO, int id) {

        TripTicketComment tripTicketComment = (TripTicketComment) toBO(tripTicketCommentDTO);
        tripTicketCommentDAO.updateTripTicketComment(tripTicketComment);
        return (TripTicketCommentDTO) toDTO(tripTicketComment);

    }

    public void createActivityForTripTicketComment(TripTicketCommentDTO tripTicketCommentDTO) {

        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setTripTicketId(tripTicketCommentDTO.getTripTicketId());
        activityDTO.setAction("Comment added");
        activityDTO.setActionDetails("body=" + tripTicketCommentDTO.getBody());
        activityDTO.setActionTakenBy(getActionBy());
        activityService.createActivity(activityDTO);
    }

    @Override
    public Object toDTO(Object bo) {

        TripTicketComment tripTicketCommentBO = (TripTicketComment) bo;

        TripTicketCommentDTO tripTicketCommentDTO = tripTicketCommentModelMapper.map(tripTicketCommentBO, TripTicketCommentDTO.class);

        return tripTicketCommentDTO;

    }

    @Override
    public Object toBO(Object dto) {

        TripTicketCommentDTO tripTicketCommentDTO = (TripTicketCommentDTO) dto;
        log.debug("tripTicketCommentDTO is " + tripTicketCommentDTO);
        TripTicketComment tripTicketCommentBO = tripTicketCommentModelMapper.map(tripTicketCommentDTO, TripTicketComment.class);
        log.debug("tripTicketCommentBO is  " + tripTicketCommentBO);
        return tripTicketCommentBO;
    }

    @Override
    public Object toDTOCollection(Object boCollection
    ) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
