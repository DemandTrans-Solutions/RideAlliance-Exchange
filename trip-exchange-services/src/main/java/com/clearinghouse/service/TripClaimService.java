
/*
 * License to Clearing House Project
 * To be used for Clearing House project only
 */
package com.clearinghouse.service;

import com.clearinghouse.dao.*;
import com.clearinghouse.dto.ActivityDTO;
import com.clearinghouse.dto.TripClaimDTO;
import com.clearinghouse.entity.*;
import com.clearinghouse.enumentity.NotificationTemplateCodeValue;
import com.clearinghouse.enumentity.ProviderTypeConstants;
import com.clearinghouse.enumentity.TripClaimStatusConstants;
import com.clearinghouse.enumentity.TripTicketStatusConstants;
import com.clearinghouse.service.notification.NotificationComposer;
import com.clearinghouse.service.notification.NotificationParamBuilder;
import com.clearinghouse.service.notification.NotificationRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * @author chaitanyaP
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class TripClaimService implements IConvertBOToDTO, IConvertDTOToBO {

    private final UserService userService;
    private final TripClaimDAO tripClaimDAO;
    private final TripTicketDAO tripTicketDAO;
    private final UserNotificationDataDAO userNotificationDataDAO;
    private final ModelMapper tripClaimModelMapper;
    private final NotificationDAO notificationDAO;
    private final ActivityService activityService;
    private final ProviderDAO providerDAO;
    private final FileGenerateService fileGenerateService;

    public List<TripClaimDTO> findAllTripClaims(int trip_ticket_id) {
        List<TripClaim> tripClaims = tripClaimDAO.findAllTripClaims(trip_ticket_id);
        List<TripClaimDTO> tripClaimDTOList = new ArrayList<>();
        for (TripClaim tripClaim : tripClaims) {
            tripClaimDTOList.add((TripClaimDTO) toDTO(tripClaim));
        }
        return tripClaimDTOList;
    }

    public boolean tripClaimExists(int id) {
        return tripClaimDAO.findTripClaimByTripClaimId(id) != null;
    }

    public TripClaimDTO findTripClaimByTripClaimId(int id) {
        return (TripClaimDTO) toDTO(tripClaimDAO.findTripClaimByTripClaimId(id));
    }


    public List<TripTicket> findTripTicketsByProviderAndExceptStatus(Provider provider, int statusId) {
        return tripClaimDAO.findTripTicketsByProviderAndExceptStatus(provider, statusId);
    }


    public TripClaimDTO createTripClaim(int trip_ticket_id, TripClaimDTO tripClaimDTO) {
        boolean allowPriceMismatch = tripClaimDTO.isOverridePriceMismatch();
        tripClaimDTO.setTripTicketId(trip_ticket_id);
        tripClaimDTO.setNewRecord(true);
        TripClaim tripClaim = (TripClaim) toBO(tripClaimDTO);
        TripClaim tripClaimCreated = tripClaimDAO.createTripTripClaim(tripClaim);
        boolean checkPrice = false;

        // For now disable price match check, all prices are approved for now
        if ( checkPrice && tripClaim.getProposedFare() < tripClaim.getRequesterProviderFare() && !allowPriceMismatch) {
            // send mail to originator for price mismatch
            TripClaimDTO updatedtripClaimDTO = sendMailForMismatchCostFoundDuringTripClaim(trip_ticket_id, tripClaimCreated.getId());
            String checkStatus = updateTicketForClaimAction(updatedtripClaimDTO.getTripTicketId(), TripTicketStatusConstants.claimPending);
            if (checkStatus.equalsIgnoreCase("success")) {
                return updatedtripClaimDTO;
            } else {
                return null;
            }
        } else {
            TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(trip_ticket_id);
            // add record for the activity of claim created
            createActivityForClaimCreated(tripClaim);

            // check if claimant is partner with originator and if trusted partner for the originator then automatically approved
            List<ProviderPartner> providerPartners = userNotificationDataDAO.getProviderPartners(tripTicket.getOriginProvider().getProviderId());
            for (ProviderPartner providerPartner : providerPartners) {
                if (providerPartner.getRequesterProvider().getProviderId() == tripClaim.getClaimantProvider().getProviderId()) {
                    if (providerPartner.isIsTrustedPartnerForCoordinator()) {
                        return approveTripClaim(trip_ticket_id, tripClaimCreated.getId());
                    }
                } else if (providerPartner.getCoordinatorProvider().getProviderId() == tripClaim.getClaimantProvider().getProviderId()) {
                    if (providerPartner.isIsTrustedPartnerForRequester()) {
                        return approveTripClaim(trip_ticket_id, tripClaimCreated.getId());
                    }
                }
            }
            // if partner is not trusted then status will be pending so update ticket field
            String status = updateTicketForClaimAction(trip_ticket_id, TripTicketStatusConstants.claimPending);
            if (status.equalsIgnoreCase("success")) {
                return (TripClaimDTO) toDTO(tripClaimCreated);
            } else {
                return null;
            }
        }
    }


    /**
     * Updates an existing trip claim for a given trip ticket.
     * <p>
     * Updates the notes, requester provider fare, proposed fare, and proposed pickup time for the trip claim.
     * If the proposed fare is less than the requester provider fare, a notification is sent for the price mismatch
     * and the ticket status is updated accordingly.
     * </p>
     *
     * @param trip_ticket_id the trip ticket ID
     * @param tripClaimDTO   the trip claim data transfer object with updated values
     * @return the updated TripClaimDTO, or null if the update failed
     */
    public TripClaimDTO updateTripClaim(int trip_ticket_id, TripClaimDTO tripClaimDTO) {
        var tripClaim = tripClaimDAO.findTripClaimByTripClaimId(tripClaimDTO.getId());
        tripClaim.setNotes(tripClaimDTO.getNotes());
        tripClaim.setRequesterProviderFare(tripClaimDTO.getRequesterProviderFare());
        tripClaim.setProposedFare(tripClaimDTO.getProposedFare());
        tripClaim.setProposedPickupTime(LocalDateTime.parse(tripClaimDTO.getProposedPickupTime()));

        tripClaim = tripClaimDAO.updateTripClaim(tripClaim);
        TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(trip_ticket_id);
        if (tripClaim.getProposedFare() < tripClaim.getRequesterProviderFare()) {
            // send mail to originator for price mismatch
            TripClaimDTO updatedtripClaimDTO = sendMailForMismatchCostFoundDuringTripClaim(trip_ticket_id, tripClaim.getId());
            String checkStatus = updateTicketForClaimAction(updatedtripClaimDTO.getTripTicketId(), TripTicketStatusConstants.claimPending);
            if (checkStatus.equalsIgnoreCase("success")) {
                return updatedtripClaimDTO;
            } else {
                return null;
            }
        } else {
            Status status = new Status();
            List<ProviderPartner> providerPartners = userNotificationDataDAO.getProviderPartners(tripTicket.getOriginProvider().getProviderId());
            for (ProviderPartner providerPartner : providerPartners) {
                if (providerPartner.getRequesterProvider().getProviderId() == tripClaim.getClaimantProvider().getProviderId()) {
                    if (providerPartner.isIsTrustedPartnerForCoordinator()) {
                        return approveTripClaim(trip_ticket_id, tripClaim.getId());
                    } else {
                        status.setStatusId(TripClaimStatusConstants.pending.tripClaimStatusUpdate());
                        tripClaim.setStatus(status);
                        TripClaim tripClaimUpdted = tripClaimDAO.updateTripClaim(tripClaim);
                        tripClaimDTO = (TripClaimDTO) toDTO(tripClaimUpdted);
                    }
                } else if (providerPartner.getCoordinatorProvider().getProviderId() == tripClaim.getClaimantProvider().getProviderId()) {
                    if (providerPartner.isIsTrustedPartnerForRequester()) {
                        return approveTripClaim(trip_ticket_id, tripClaim.getId());
                    } else {
                        status.setStatusId(TripClaimStatusConstants.pending.tripClaimStatusUpdate());
                        tripClaim.setStatus(status);
                        TripClaim tripClaimUpdted = tripClaimDAO.updateTripClaim(tripClaim);
                        tripClaimDTO = (TripClaimDTO) toDTO(tripClaimUpdted);
                    }
                }
            }

            // fetch the old claim and compare their values with new one
            TripClaim oldcClaim = tripClaimDAO.findTripClaimByTripClaimId(tripClaimDTO.getId());
            Map<String, String> changedValues = new HashMap<>();

            // possible values to be changed are proposed pickup time, fare, and notes
            if (oldcClaim.getRequesterProviderFare() != tripClaimDTO.getRequesterProviderFare()) {
                changedValues.put("requesterFare", String.valueOf(tripClaimDTO.getRequesterProviderFare()));
            } else {
                changedValues.put("requesterFare", String.valueOf(tripClaimDTO.getRequesterProviderFare()));
            }
            if (oldcClaim.getProposedFare() != tripClaimDTO.getProposedFare()) {
                changedValues.put("proposedFare", String.valueOf(tripClaimDTO.getProposedFare()));
            } else {
                changedValues.put("proposedFare", String.valueOf(tripClaimDTO.getProposedFare()));
            }
            if (!oldcClaim.getNotes().equalsIgnoreCase(tripClaimDTO.getNotes())) {
                changedValues.put("notes", tripClaimDTO.getNotes());
            } else {
                changedValues.put("notes", "No Change");
            }

            /* if new value is not null and old value is also not null value */
            String newProposedPickupTime = tripClaimDTO.getProposedPickupTime();
            String oldProposedPickupTime = null;
            if (oldcClaim.getProposedPickupTime() != null) {
                oldProposedPickupTime = oldcClaim.getProposedPickupTime().toString();
            }
            if (newProposedPickupTime != null && oldProposedPickupTime != null) {
                if (newProposedPickupTime.equalsIgnoreCase(oldProposedPickupTime)) {
                    // convert date time to time in 12 hr format
                    String[] pickupDateTimeArray = tripClaimDTO.getProposedPickupTime().split("T");
                    String pickupTime = pickupDateTimeArray[1];
                    String[] hoursMinuteSec = pickupTime.split(":");
                    int hours = Integer.valueOf(hoursMinuteSec[0]);
                    if (hours > 12) {
                        int hoursConverted = 24 - hours;
                        pickupTime = hoursConverted + ":" + hoursMinuteSec[1] + " PM";
                    } else {
                        pickupTime = hoursMinuteSec[0] + ":" + hoursMinuteSec[1] + " AM";
                    }
                    changedValues.put("proposedPickupTime", pickupTime);
                } else {
                    changedValues.put("proposedPickupTime", "No Change");
                }
            } else if (newProposedPickupTime == null && oldProposedPickupTime != null) {
                changedValues.put("proposedPickupTime", "no proposedPickuptime");
            } else if (newProposedPickupTime != null && oldProposedPickupTime == null) {
                if (newProposedPickupTime.equalsIgnoreCase(oldProposedPickupTime)) {
                    String[] pickupDateTimeArray = tripClaimDTO.getProposedPickupTime().split("T");
                    String pickupTime = pickupDateTimeArray[1];
                    String[] hoursMinuteSec = pickupTime.split(":");
                    int hours = Integer.valueOf(hoursMinuteSec[0]);
                    if (hours > 12) {
                        int hoursConverted = 24 - hours;
                        pickupTime = hoursConverted + ":" + hoursMinuteSec[1] + " PM";
                    } else {
                        pickupTime = hoursMinuteSec[0] + ":" + hoursMinuteSec[1] + " AM";
                    }
                    changedValues.put("proposedPickupTime", pickupTime);
                } else {
                    changedValues.put("proposedPickupTime", "No Change");
                }
            } else {
                changedValues.put("proposedPickupTime", "No Change");
            }
            if (!(changedValues.get("proposedFare").equalsIgnoreCase("No Change")
                    && changedValues.get("requesterFare").equalsIgnoreCase("No Change")
                    && changedValues.get("notes").equalsIgnoreCase("No Change")
                    && changedValues.get("proposedPickupTime").equalsIgnoreCase("No Change"))) {
                // send mail to originator
                sendMailForUpdationOfClaim(tripClaimDTO.getTripTicketId(), changedValues);
            }

            // update the trip ticket for trip claim action
            String checkStatus = updateTicketForClaimAction(trip_ticket_id);
            TripClaim tripClaimUpdated = tripClaimDAO.updateTripClaim(tripClaim);
            if (checkStatus.equalsIgnoreCase("success")) {
                return (TripClaimDTO) toDTO(tripClaimUpdated);
            } else {
                return null;
            }
        }
    }

    /**
     * Updates the trip ticket for a trip claim action.
     * <p>
     * This method updates the trip ticket's status and audit fields (updated by, updated at)
     * when a claim action occurs. If a status is provided, it updates the ticket's status accordingly.
     * </p>
     *
     * @param ticketId the ID of the trip ticket to update
     * @return "success" if the update was successful, "failed" otherwise
     */
    public String updateTicketForClaimAction(int ticketId) {
        return updateTicketForClaimAction(ticketId, null);
    }

    /**
     * Updates the trip ticket for a trip claim action, with an optional status update.
     * <p>
     * This method updates the trip ticket's status (if provided) and audit fields (updated by, updated at)
     * when a claim action occurs. It retrieves the current authenticated user to set the updated_by field.
     * </p>
     *
     * @param ticketId           the ID of the trip ticket to update
     * @param ticketUpdateStatus the new status to set for the trip ticket (nullable)
     * @return "success" if the update was successful, "failed" otherwise
     */
    public String updateTicketForClaimAction(int ticketId, TripTicketStatusConstants ticketUpdateStatus) {
        ZonedDateTime updated_at;
        try {
            TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(ticketId);
            if (ticketUpdateStatus != null) {
                var status = new Status();
                status.setStatusId(ticketUpdateStatus.tripTicketStatusUpdate());
                tripTicket.setStatus(status);
            }
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String principalName = "";
            if (auth != null && auth.getPrincipal() != null ) {
                principalName = (String) auth.getPrincipal();
            }
            int updated_by = 1;
            if (auth != null && (!principalName.equalsIgnoreCase("anonymousUser"))) {
                updated_by = ((User) auth.getDetails()).getId();
            }
            updated_at = ZonedDateTime.now(ZoneId.of("UTC-6"));
            tripTicket.setUpdatedBy(updated_by);
            tripTicket.setUpdatedAt(updated_at);
            tripTicketDAO.updateTripTicket(tripTicket);
            return "success";
        } catch (Exception e) {
            log.error("Exception in updateTickcetForClaimAction: {}", e.getMessage(), e);
            return "failed";
        }
    }


    /**
     * Rescinds a trip claim for a given trip ticket and claim ID.
     * <p>
     * Handles rescinding logic for both pending and approved claims, updates statuses, sends notifications,
     * and records activities. If the claim is pending or has a price mismatch, it is simply rescinded. If the claim
     * is approved, the associated trip ticket is also updated and notifications are sent to both claimant and originator.
     * </p>
     *
     * @param trip_ticket_id the trip ticket ID
     * @param id             the trip claim ID
     * @return the updated TripClaimDTO after rescind, or an empty DTO if not applicable
     */
    public TripClaimDTO rescindTripClaim(int trip_ticket_id, int id) {
        // There are two possible ways a trip claim gets rescinded:
        // 1. When claim is created and that provider wants to cancel his claim when the status is pending
        // 2. When claim is approved and that time he wants to rescind the claim then claim status will be changed and trip ticket is also rescinded
        TripClaim tripClaim = tripClaimDAO.findTripClaimByTripClaimId(id);

        // notifications are handled in branches below via centralized helper

        // Checking if status is pending
        if ((tripClaim.getStatus().getStatusId() == TripClaimStatusConstants.pending.tripClaimStatusUpdate())
                || (tripClaim.getStatus().getStatusId() == TripClaimStatusConstants.priceMismatch.tripClaimStatusUpdate())) {
            // here need not to send email notification because claimant himself is going to cancel
            Status status = new Status();
            status.setStatusId(TripClaimStatusConstants.rescined.tripClaimStatusUpdate());
            tripClaim.setStatus(status);
            TripClaim tripClaimUpdated = tripClaimDAO.updateTripClaim(tripClaim);
            // add record of activity for the trip claim rescind
            createActivityForClaimRescinded(tripClaim);
            return (TripClaimDTO) toDTO(tripClaimUpdated);
        } else if (tripClaim.getStatus().getStatusId() == TripClaimStatusConstants.approved.tripClaimStatusUpdate()
            || tripClaim.getStatus().getStatusId() == TripClaimStatusConstants.rescined.tripClaimStatusUpdate()
        ) {
            TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(tripClaim.getTripTicket().getId());
            tripTicket.setApprovedTripClaim(null);
            Status status = new Status();
            status.setStatusId(TripClaimStatusConstants.rescined.tripClaimStatusUpdate());
            tripClaim.setStatus(status);
            Status statusForTripTicket = new Status();
            statusForTripTicket.setStatusId(TripTicketStatusConstants.available.tripTicketStatusUpdate());
            tripTicket.setStatus(statusForTripTicket);
            tripTicket.setApprovedTripClaim(tripClaim);
            // added last status changed by clause to know who changed the status of trip ticket
            tripTicket.setLastStatusChangedByProvider(tripClaim.getClaimantProvider());
            TripTicket updatedTripTicket = tripTicketDAO.updateTripTicket(tripTicket);
            // add record of activity for the trip claim rescind + ticket rescind
            createActivityForClaimRescinded(tripTicket, tripClaim);

            // Notification for trip ticket originator
            List<User> usersOfOriginator = userNotificationDataDAO.getUsersOfProvider(tripTicket.getOriginProvider().getProviderId());

            for (User user : usersOfOriginator) {
                // fetching single obj of the user role
                List<UserAuthority> userAuthority = new ArrayList<>();
                userAuthority.addAll(user.getAuthorities());
                String userrole = userAuthority.get(0).getAuthority();
                if (userrole.equalsIgnoreCase("ROLE_PROVIDERADMIN") || userrole.equalsIgnoreCase("ROLE_PROVIDERUSER")) {
                    Map<String, String> params = NotificationParamBuilder.baseClaimParams(user.getName(), tripTicket, tripClaim);
                    enqueueNotification(
                            user.getEmail(),
                            NotificationTemplateCodeValue.approvedTripClaimRescinded,
                            "Trip ticket is rescinded",
                            params,
                            false,
                            null,
                            tripTicket
                    );
                }
            }



            TripClaim updatedTripClaim = updatedTripTicket.getApprovedTripClaim();
            return (TripClaimDTO) toDTO(updatedTripClaim);
        }

        TripClaimDTO tripClaimDTO = new TripClaimDTO();
        return tripClaimDTO;
    }


    /**
     * Declines a trip claim for a given trip ticket and claim ID.
     * <p>
     * Updates the claim status to declined, records the activity, and sends notifications to the claimant's users.
     * Handles special logic for restricted provider types and formats notification content accordingly.
     * </p>
     *
     * @param trip_ticket_id the trip ticket ID
     * @param id             the trip claim ID
     * @return the updated TripClaimDTO after decline
     */
    public TripClaimDTO declineTripClaim(int trip_ticket_id, int id) {
        TripClaim tripClaim = tripClaimDAO.findTripClaimByTripClaimId(id);
        Status status = new Status();
        status.setStatusId(TripClaimStatusConstants.declined.tripClaimStatusUpdate());
        tripClaim.setStatus(status);
        TripClaim tripClaimUpdated = tripClaimDAO.updateTripClaim(tripClaim);

        TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(tripClaim.getTripTicket().getId());
        // add record of activity for trip claim declined
        createActivityForClaimDeclined(tripTicket, tripClaim);
        List<User> usersOfClaimant = userNotificationDataDAO.getUsersForTripClaimDeclined(tripClaim.getClaimantProvider().getProviderId());
        for (User user : usersOfClaimant) {
            // fetching single obj of the user role
            List<UserAuthority> userAuthority = new ArrayList<>();
            userAuthority.addAll(user.getAuthorities());
            userAuthority.addAll(user.getAuthorities());
            String userrole = userAuthority.get(0).getAuthority();
            if (user.isIsNotifyTripClaimDeclined() ) {
                Map<String, String> declinedClaimTemplateMap = NotificationParamBuilder.baseClaimParams(user.getName(), tripTicket, tripClaim);
                enqueueNotification(
                        user.getEmail(),
                        NotificationTemplateCodeValue.claimDeclinedTemplateCode,
                        "Trip claim is declined",
                        declinedClaimTemplateMap,
                        user.getProvider().getProviderType().getProviderTypeId() == ProviderTypeConstants.restrictedProvider.getProviderTypeId(),
                        "DeclinedClaimTripTicket",
                        tripTicket
                );
            }
        }
        return (TripClaimDTO) toDTO(tripClaimUpdated);
    }


    public TripClaimDTO approveTripClaim(int trip_ticket_id, int id) {
        TripClaim tripClaim = tripClaimDAO.findTripClaimByTripClaimId(id);
        Status status = new Status();
        status.setStatusId(TripClaimStatusConstants.approved.tripClaimStatusUpdate());
        tripClaim.setStatus(status);

        // Once trip claim is approved, other claims for that ticket will be declined.
        // TripTicket status will be approved and entry for the approved trip claim.
        TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(tripClaim.getTripTicket().getId());
        tripTicket.setApprovedTripClaim(tripClaim);

        Status statusUpdateForTripTicket = new Status();
        statusUpdateForTripTicket.setStatusId(TripTicketStatusConstants.approved.tripTicketStatusUpdate());
        tripTicket.setStatus(statusUpdateForTripTicket);

        Set<TripClaim> tripClaimsUpdateRequired = tripTicket.getTripClaims();
        tripTicket.setLastStatusChangedByProvider(tripTicket.getOriginProvider());

        boolean mailSendingFlag = false;
        for (TripClaim tripClaimUpdated : tripClaimsUpdateRequired) {
            if (tripClaimUpdated.getStatus().getStatusId() != TripClaimStatusConstants.cancelled.tripClaimStatusUpdate()) {
                if (tripClaimUpdated.getId() != tripClaim.getId()) {
                    Status statusForClaim = new Status();
                    statusForClaim.setStatusId(TripClaimStatusConstants.declined.tripClaimStatusUpdate());
                    tripClaimUpdated.setStatus(statusForClaim);
                    // add record for activity of declined trip
                    createActivityForClaimDeclined(tripTicket, tripClaimUpdated);
                }
                mailSendingFlag = true;
            }
        }
        if (mailSendingFlag) {
            // send mail to other claimants for declined claim
            sendMailToClaimantsForDeclinedClaim(tripTicket);
        }

        tripTicket.setTripClaims(tripClaimsUpdateRequired);
        tripTicketDAO.updateTripTicket(tripTicket);

        // add record for activity of the approved trip claim
        createActivityForClaimApprovedAndTicketApproved(tripTicket, tripClaim);

        // send mail for claim approved
        List<User> usersOfClaimant = userNotificationDataDAO.getUsersForTripClaimApproved(
                tripTicket.getApprovedTripClaim().getClaimantProvider().getProviderId());
        for (User user : usersOfClaimant) {
            // fetching single obj of the user role
            List<UserAuthority> userAuthority = new ArrayList<>();
            userAuthority.addAll(user.getAuthorities());
            userAuthority.addAll(user.getAuthorities());
            String userrole = userAuthority.get(0).getAuthority();
            if (user.isIsNotifyTripClaimApproved() ) {
                Map<String, String> params = NotificationParamBuilder.baseClaimParams(user.getName(), tripTicket, tripClaim);
                enqueueNotification(
                        user.getEmail(),
                        NotificationTemplateCodeValue.claimApprovedTemplateCode,
                        "Trip claim is approved",
                        params,
                        user.getProvider().getProviderType().getProviderTypeId() == ProviderTypeConstants.restrictedProvider.getProviderTypeId(),
                        "ApprovedClaimTripTicket",
                        tripTicket
                );
            }
        }
        // updating claim status for other providers now..
        return (TripClaimDTO) toDTO(tripClaim);
    }

    public void sendMailToClaimantsForDeclinedClaim(TripTicket tripTicket) {
        Set<TripClaim> tripClaims = tripTicket.getTripClaims();
        List<TripClaim> tripClaimList = new ArrayList<>();
        for (TripClaim tripClaimFromSet : tripClaims) {
            if (tripClaimFromSet.getId() != tripTicket.getApprovedTripClaim().getId()) {
                tripClaimList.add(tripClaimFromSet);
            }
        }
        for (TripClaim tripClaim : tripClaimList) {
            List<User> usersOfClaimant = userNotificationDataDAO.getUsersForTripClaimDeclined(tripClaim.getClaimantProvider().getProviderId());
            for (User user : usersOfClaimant) {
                // Using composer below
                // fetching single obj of the user role
                List<UserAuthority> userAuthority = new ArrayList<>();
                userAuthority.addAll(user.getAuthorities());
                userAuthority.addAll(user.getAuthorities());
                String userrole = userAuthority.get(0).getAuthority();
                if (user.isIsNotifyTripClaimDeclined() ) {
                    Map<String, String> params = NotificationParamBuilder.baseClaimParams(user.getName(), tripTicket, tripClaim);
                    enqueueNotification(
                            user.getEmail(),
                            NotificationTemplateCodeValue.claimDeclinedTemplateCode,
                            "Trip claim is declined",
                            params,
                            user.getProvider().getProviderType().getProviderTypeId() == ProviderTypeConstants.restrictedProvider.getProviderTypeId(),
                            "DeclinedClaimTripTicket",
                            tripTicket
                    );
                }
            }
        }
    }

    // Helpers to compose and enqueue notifications in a consistent way
    private void enqueueNotification(String email,
                                     NotificationTemplateCodeValue template,
                                     String subject,
                                     Map<String, String> params,
                                     boolean attachCsv,
                                     String csvBaseName,
                                     TripTicket ticket) {
        NotificationRequest req = new NotificationRequest(email, template, subject, params, attachCsv, csvBaseName);
        new NotificationComposer(notificationDAO, fileGenerateService).enqueue(req, ticket);
    }

    public void sendMailForUpdationOfClaim(int tripTicketId, Map<String, String> changedValues) {
        TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(tripTicketId);
        // new added to get tripclaim
        Set<TripClaim> tripClaims = tripTicket.getTripClaims();
        for (TripClaim tripClaim : tripClaims) {
            List<User> usersOfOriginator = userNotificationDataDAO.getUsersOfProvider(tripTicket.getOriginProvider().getProviderId());
            for (User user : usersOfOriginator) {
                if (user.getAuthorities().iterator().next().getAuthority().equalsIgnoreCase("ROLE_PROVIDERADMIN")
                        || user.getAuthorities().iterator().next().getAuthority().equalsIgnoreCase("ROLE_PROVIDERUSER")) {
                    // fetching single obj of the user role
                    List<UserAuthority> userAuthority = new ArrayList<>();
                    userAuthority.addAll(user.getAuthorities());
                    String userrole = userAuthority.get(0).getAuthority();
                    if (userrole.equalsIgnoreCase("ROLE_PROVIDERADMIN") || userrole.equalsIgnoreCase("ROLE_PROVIDERUSER")) {
                        Map<String, String> params = NotificationParamBuilder.baseClaimParams(user.getName(), tripTicket, tripClaim);
                        params.putAll(changedValues);
                        enqueueNotification(
                                user.getEmail(),
                                NotificationTemplateCodeValue.claimUpdated,
                                "Trip claim is updated",
                                params,
                                false,
                                null,
                                tripTicket
                        );
                    }
                }
            }
        }
    }

    /* method for the adding activity record for claim created */
    public void createActivityForClaimCreated(TripClaim tripClaim) {
        Provider provider = providerDAO.findProviderByProviderId(tripClaim.getClaimantProvider().getProviderId());
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setTripTicketId(tripClaim.getTripTicket().getId());
        activityDTO.setAction("Claim created");
        activityDTO.setActionDetails("status=Pending,claimant_provider=" + provider.getProviderName()
                + ",proposed_fare =" + tripClaim.getProposedFare() + ",proposed_pickup_time()="
                + tripClaim.getProposedPickupTime());
        activityDTO.setActionTakenBy(provider.getProviderName());
        activityService.createActivity(activityDTO);
    }

    /* method for the adding activity record for claim approved */
    @Transactional(propagation = Propagation.NESTED)
    public void createActivityForClaimApprovedAndTicketApproved(TripTicket tripTicket,
                                                                TripClaim tripClaim) {
        Provider provider = providerDAO.findProviderByProviderId(tripTicket.getLastStatusChangedByProvider().getProviderId());
        Provider claimant_providername = providerDAO.findProviderByProviderId(tripClaim.getClaimantProvider().getProviderId());

        ActivityDTO activityDTOForTripClaim = new ActivityDTO();
        activityDTOForTripClaim.setTripTicketId(tripClaim.getTripTicket().getId());
        activityDTOForTripClaim.setAction("Claim approved");
        activityDTOForTripClaim.setActionDetails("status=approved,claimant_providername=" + claimant_providername.getProviderName()
                + ",proposedFare=" + tripClaim.getProposedFare() + ",proposedPickupTime=" + tripClaim.getProposedPickupTime());
        activityDTOForTripClaim.setActionTakenBy(provider.getProviderName());
        activityService.createActivity(activityDTOForTripClaim);

        // activity for trip ticket approved
        ActivityDTO activityDTOForTripTicket = new ActivityDTO();
        activityDTOForTripTicket.setTripTicketId(tripClaim.getTripTicket().getId());
        activityDTOForTripTicket.setAction("Ticket approved");
        activityDTOForTripTicket.setActionDetails("status=approved,originator_provider=" + provider.getProviderName()
                + ",requested_pickup_time=" + tripTicket.getRequestedPickupTime() + ",requested_pickup_date=" + tripTicket.getRequestedPickupDate());
        activityDTOForTripTicket.setActionTakenBy(provider.getProviderName());
        activityService.createActivity(activityDTOForTripTicket);

    }

    /* method for the adding activity record for claim declined */
    @Transactional(propagation = Propagation.NESTED)
    public void createActivityForClaimDeclined(TripTicket tripTicket, TripClaim tripClaim) {
        int providerId = tripTicket.getLastStatusChangedByProvider() == null
                ? tripTicket.getOriginProvider().getProviderId()
                : tripTicket.getLastStatusChangedByProvider().getProviderId();

        Provider providerForTripTicket = providerDAO.findProviderByProviderId(providerId);
        Provider providerFortripClaim = providerDAO.findProviderByProviderId(tripClaim.getClaimantProvider().getProviderId());
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setTripTicketId(tripClaim.getTripTicket().getId());
        activityDTO.setAction("Claim declined");
        activityDTO.setActionDetails("status=declined,claimant_provider=" + providerFortripClaim.getProviderName());
        activityDTO.setActionTakenBy(providerForTripTicket.getProviderName());
        activityService.createActivity(activityDTO);
    }

    /* method for the adding activity record for claim rescinded */
    public void createActivityForClaimRescinded(TripClaim tripClaim) {
        Provider provider = providerDAO.findProviderByProviderId(tripClaim.getClaimantProvider().getProviderId());
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setTripTicketId(tripClaim.getTripTicket().getId());
        activityDTO.setAction("Claim rescinded");
        activityDTO.setActionDetails("status=rescinded,claimant_provider=" + provider.getProviderName());
        activityDTO.setActionTakenBy(provider.getProviderName());
        activityService.createActivity(activityDTO);
    }

    /*
     * method for the adding activity record for claim rescinded overloaded method
     */
    @Transactional(propagation = Propagation.NESTED)
    public void createActivityForClaimRescinded(TripTicket tripTicket, TripClaim tripClaim) {
        Provider provider = providerDAO.findProviderByProviderId(tripTicket.getLastStatusChangedByProvider().getProviderId());
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setTripTicketId(tripClaim.getTripTicket().getId());
        activityDTO.setAction("Claim rescinded");
        activityDTO.setActionDetails("status=rescinded,claimant_provider=" + tripClaim.getClaimantProvider().getProviderName());
        activityDTO.setActionTakenBy(provider.getProviderName());
        activityService.createActivity(activityDTO);

        // activity for trip ticket rescinded
        Provider originatorProvider = providerDAO.findProviderByProviderId(tripTicket.getOriginProvider().getProviderId());
        ActivityDTO activityDTOForTripTicket = new ActivityDTO();
        activityDTOForTripTicket.setTripTicketId(tripClaim.getTripTicket().getId());
        activityDTOForTripTicket.setAction("Ticket rescinded");
        activityDTOForTripTicket.setActionDetails("status=approved,originatorProvider=" + originatorProvider.getProviderName()
                + "requested_pickup_time=" + tripTicket.getRequestedPickupTime() + ",requested_pickup_date=" + tripTicket.getRequestedPickupDate());
        activityDTOForTripTicket.setActionTakenBy(provider.getProviderName());
        activityService.createActivity(activityDTOForTripTicket);

    }


    public Object toDTO(Object bo) {
        TripClaim tripClaimBO = (TripClaim) bo;
        Provider claimantProvider = providerDAO.findProviderByProviderId(tripClaimBO.getClaimantProvider().getProviderId());
        TripClaimDTO tripClaimDTO = tripClaimModelMapper.map(tripClaimBO, TripClaimDTO.class);
        tripClaimDTO.setClaimantProviderName(claimantProvider.getProviderName());
        if (tripClaimDTO.isAckStatus()) {
            tripClaimDTO.setAckStatusString("Yes");
        } else {
            tripClaimDTO.setAckStatusString("No");
        }
        // add for org claimant claim status
        if (tripClaimDTO.getStatus().getStatusId() == TripClaimStatusConstants.pending.tripClaimStatusUpdate()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            // String userRole = auth.getAuthorities().iterator().next().getAuthority();
            String username = auth.getName();
            int providerId = userService.findProviderIdByUsername(username);
            TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(tripClaimDTO.getTripTicketId());
            if (tripTicket.getOriginProvider().getProviderId() == providerId) {
                tripClaimDTO.getStatus().setStatusId(TripClaimStatusConstants.pendingYourApproval.tripClaimStatusUpdate());
                tripClaimDTO.getStatus().setType("Pending Your Approval");
            }
        }
        return tripClaimDTO;
    }

    @Override
    public Object toBO(Object dto) {
        TripClaimDTO tripClaimDTO = (TripClaimDTO) dto;
        TripClaim tripClaimBO = tripClaimModelMapper.map(tripClaimDTO, TripClaim.class);
        if (tripClaimDTO.getClaimantServiceId() == 0) {
            tripClaimBO.setService(null);
        }
        if (tripClaimDTO.getProposedPickupTime() != null) {
            tripClaimBO.setProposedPickupTime(
                    StringToLocalDateTimeConverter.converterStringToLocalDate(tripClaimDTO.getProposedPickupTime()));
        }
        if (tripClaimDTO.getExpirationDate() != null) {
            tripClaimBO.setExpirationDate(
                    StringToLocalDateTimeConverter.converterStringToLocalDate(tripClaimDTO.getExpirationDate()));
        }
        if (tripClaimDTO.getClaimantProviderId() != 0) {
            Provider claimantProvider = providerDAO.findProviderByProviderId(tripClaimDTO.getClaimantProviderId());
            tripClaimBO.setClaimantProvider(claimantProvider);
        } else {
            tripClaimBO.setClaimantProvider(null);
        }
        if (tripClaimDTO.getTripTicketId() != 0) {
            TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(tripClaimDTO.getTripTicketId());
            tripClaimBO.setTripTicket(tripTicket);
        } else {
            tripClaimBO.setTripTicket(null);
        }
        if (tripClaimDTO.getStatus() != null) {
            Status status = new Status();
            status.setStatusId(tripClaimDTO.getStatus().getStatusId());
            tripClaimBO.setStatus(status);
        } else {
            tripClaimBO.setStatus(null);
        }
        return tripClaimBO;
    }

    @Override
    public Object toDTOCollection(Object boCollection) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }

    // newly added for claim cancellation

    public TripClaimDTO sendingMailForCancellationOfTripClaim(TripTicket tripTicket, int id, String actionBy) {
        TripClaim tripClaim = tripClaimDAO.findTripClaimByTripClaimId(id);
        Status status = new Status();
        status.setStatusId(TripClaimStatusConstants.cancelled.tripClaimStatusUpdate());
        tripClaim.setStatus(status);
        tripClaim.setNotes("This trip claim cancelled by " + actionBy + " due to update tripticket.");
        tripClaim = tripClaimDAO.updateTripClaim(tripClaim);

        // add record of activity for trip claim declined
        createActivityForClaimCancel(tripTicket, tripClaim);
        // fetch the users of that provider
        List<User> usersOfClaimantProvider = userNotificationDataDAO.getUsersForTripClaimCancel(tripClaim.getClaimantProvider().getProviderId());
        for (User user : usersOfClaimantProvider) {
            // checking for checkbox selected
            if (user.isIsNotifyTripClaimCancelled()) {
                Map<String, String> params = NotificationParamBuilder.baseClaimParams(user.getName(), tripTicket, tripClaim);
                // Behavior: show origin provider as last status changer
                params.put("lastStatusChangedByProviderName", tripTicket.getOriginProvider().getProviderName());
                enqueueNotification(
                        user.getEmail(),
                        NotificationTemplateCodeValue.claimCancelTemplateCode,
                        "Trip claim is cancelled",
                        params,
                        false,
                        null,
                        tripTicket
                );
            }
        }
        return (TripClaimDTO) toDTO(tripClaim);
    }

    /**
     * Creates an activity record for a claim cancellation.
     * <p>
     * Records an activity when a claim is cancelled, including details about the trip ticket and the provider involved.
     * </p>
     *
     * @param tripTicket the trip ticket associated with the claim
     * @param tripClaim  the trip claim being cancelled
     */
    @Transactional(propagation = Propagation.NESTED)
    public void createActivityForClaimCancel(TripTicket tripTicket, TripClaim tripClaim) {
        Provider providerForTripTicket = providerDAO.findProviderByProviderId(tripTicket.getLastStatusChangedByProvider().getProviderId());
        Provider providerFortripClaim = providerDAO.findProviderByProviderId(tripClaim.getClaimantProvider().getProviderId());
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setTripTicketId(tripClaim.getTripTicket().getId());
        activityDTO.setAction("Claim Cancel");
        activityDTO.setActionDetails("status=Abort,claimant_provider=" + providerFortripClaim.getProviderName());
        activityDTO.setActionTakenBy(providerForTripTicket.getProviderName());
        activityService.createActivity(activityDTO);
    }

    /**
     * Sends a notification email for a price mismatch found during trip claim creation.
     * <p>
     * Updates the claim status to price mismatch, updates the trip ticket, records an activity, and notifies users.
     * </p>
     *
     * @param trip_ticket_id the trip ticket ID
     * @param id             the trip claim ID
     * @return the updated TripClaimDTO
     */
    public TripClaimDTO sendMailForMismatchCostFoundDuringTripClaim(int trip_ticket_id, int id) {
        TripClaim tripClaim = tripClaimDAO.findTripClaimByTripClaimId(id);
        Status status = new Status();
        status.setStatusId(TripClaimStatusConstants.priceMismatch.tripClaimStatusUpdate());
        tripClaim.setStatus(status);
        tripClaim = tripClaimDAO.updateTripClaim(tripClaim);

        TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(trip_ticket_id);
        tripTicket.setLastStatusChangedByProvider(tripClaim.getClaimantProvider());
        tripTicket.setApprovedTripClaim(tripClaim);
        Set<TripClaim> tripClaimsUpdateRequired = tripTicket.getTripClaims();
        tripTicket.setTripClaims(tripClaimsUpdateRequired);
        tripTicketDAO.updateTripTicket(tripTicket);
        // added record for activity of the price mismatch
        createActivityForClaimCostMismatch(tripTicket, tripClaim);
        // send mail for price mismatch
        List<User> usersOfOriginator = userNotificationDataDAO.getUsersForTripPriceMismatch(tripTicket.getOriginProvider().getProviderId());
        for (User user : usersOfOriginator) {
            if (user.isIsNotifyTripPriceMismatched()) {
                Map<String, String> params = NotificationParamBuilder.baseClaimParams(user.getName(), tripTicket, tripClaim);
                params.put("message", "Your Proposed Trip Ticket Price is greater than the amount that the Claimant of your Trip Ticket,");
                params.put("nextMessage", "You can adjust your proposed payment amount in the Trip Exchange. The claim of your Trip Ticket will not be finalized until your proposed payment price is the same as the requested price of the Claimant.");
                enqueueNotification(
                        user.getEmail(),
                        NotificationTemplateCodeValue.claimCostMismatchTemplateCode,
                        "Trip claim price is mismatched",
                        params,
                        false,
                        null,
                        tripTicket
                );
            }
        }
        // updating claim status for other providers now..
        return (TripClaimDTO) toDTO(tripClaim);
    }

    /**
     * Creates an activity record for a claim cost mismatch.
     * <p>
     * Records an activity when a claim's proposed fare does not match the requester's fare.
     * </p>
     *
     * @param tripTicket the trip ticket associated with the claim
     * @param tripClaim  the trip claim with a cost mismatch
     */
    @Transactional(propagation = Propagation.NESTED)
    public void createActivityForClaimCostMismatch(TripTicket tripTicket, TripClaim tripClaim) {
        Provider provider = null;
        if (tripTicket.getLastStatusChangedByProvider() != null) {
            provider = providerDAO.findProviderByProviderId(tripTicket.getLastStatusChangedByProvider().getProviderId());
        } else {
            provider = tripClaim.getClaimantProvider();
        }

        String claimant_providername = null;
        if (tripClaim.getClaimantProvider() != null) {
            // if claimant provider is not null then only find the provider name
            // otherwise it will throw null pointer exception
            // this is for the case when trip claim is created by system
            // and claimant provider is not set.
            claimant_providername = providerDAO.findProviderByProviderId(tripClaim.getClaimantProvider().getProviderId()).getProviderName();
        }

        ActivityDTO activityDTOForTripClaim = new ActivityDTO();
        activityDTOForTripClaim.setTripTicketId(tripTicket.getId());
        activityDTOForTripClaim.setAction("Claim price Mismatch");
        activityDTOForTripClaim.setActionDetails("status=ClaimPriceMismatch,claimant_provider="
                + claimant_providername + ",proposed_fare =" + tripClaim.getProposedFare()
                + ",getRequesterProviderFare =" + tripClaim.getRequesterProviderFare() + ",proposed_pickup_time="
                + tripClaim.getProposedPickupTime());
        activityDTOForTripClaim.setActionTakenBy(provider.getProviderName());
        activityService.createActivity(activityDTOForTripClaim);

    }


    public void sendACKMailToClaimantForTripCancellation(TripTicket tripTicket, int id, String actionBy) {
        TripClaim tripClaim = tripClaimDAO.findTripClaimByTripClaimId(id);
        Status status = new Status();
        status.setStatusId(TripClaimStatusConstants.cancelled.tripClaimStatusUpdate());
        tripClaim.setStatus(status);
        tripClaim.setNotes("This claim cancelled due to trip has been cancelled by " + actionBy);
        tripClaim = tripClaimDAO.updateTripClaim(tripClaim);

        // add record of activity for trip claim declined
        createActivityForClaimCancel(tripTicket, tripClaim);
        List<User> usersOfClaimantProvider = userNotificationDataDAO.getUsersForTripClaimCancel(tripClaim.getClaimantProvider().getProviderId());
        for (User user : usersOfClaimantProvider) {
            if (user.isIsNotifyTripClaimCancelled()) {
                Map<String, String> params = NotificationParamBuilder.baseClaimParams(user.getName(), tripTicket, tripClaim);
                params.put("lastStatusChangedByProviderName", tripTicket.getOriginProvider().getProviderName());
                enqueueNotification(
                        user.getEmail(),
                        NotificationTemplateCodeValue.claimCancelledDueToTripCancelledTemplateCode,
                        "Trip claim is cancelled",
                        params,
                        false,
                        null,
                        tripTicket
                );
            }
        }
    }

}

