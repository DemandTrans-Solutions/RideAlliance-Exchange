/*
 * License to Clearing House Project
 * To be used for Clearing House project only
 */
package com.clearinghouse.service;

import com.clearinghouse.dao.NotificationDAO;
import com.clearinghouse.dao.ProviderDAO;
import com.clearinghouse.dao.TripChangeRequestDAO;
import com.clearinghouse.dao.TripTicketDAO;
import com.clearinghouse.dao.UserNotificationDataDAO;
import com.clearinghouse.dto.ActivityDTO;
import com.clearinghouse.dto.CreateChangeRequestRequest;
import com.clearinghouse.dto.TripChangeRequestDTO;
import com.clearinghouse.entity.*;
import com.clearinghouse.enumentity.NotificationTemplateCodeValue;
import com.clearinghouse.enumentity.TripChangeRequestStatusConstants;
import com.clearinghouse.enumentity.TripTicketStatusConstants;
import com.clearinghouse.exceptions.EditNotAllowedException;
import com.clearinghouse.exceptions.InvalidInputException;
import com.clearinghouse.service.notification.NotificationComposer;
import com.clearinghouse.service.notification.NotificationParamBuilder;
import com.clearinghouse.service.notification.NotificationRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Handles the change-request approval workflow for editing claimed trip tickets.
 *
 * <p>Rules:
 * <ul>
 *   <li>Super admin (ROLE_ADMIN) may edit anything, anytime (handled in the edit path).</li>
 *   <li>For a claimed trip, either the originating or claimant provider admin may send a
 *       change request to the other, but only when the trip is more than 24 hours away.</li>
 *   <li>Within 24 hours, only a super admin may change the trip (no change requests).</li>
 *   <li>On approval, the proposed changes are applied to the trip automatically and the request
 *       rests in {@code approved}. A claimed trip is never edited directly by a provider.</li>
 * </ul>
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class TripChangeRequestService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_PROVIDERADMIN = "ROLE_PROVIDERADMIN";
    private static final ZoneId APP_ZONE = ZoneId.of("UTC-6");
    private static final long LOCK_WINDOW_HOURS = 24;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DateTimeFormatter CR_DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);
    private static final DateTimeFormatter CR_TIME = DateTimeFormatter.ofPattern("hh:mm a", Locale.US);

    private final TripChangeRequestDAO tripChangeRequestDAO;
    private final TripTicketDAO tripTicketDAO;
    private final ProviderDAO providerDAO;
    private final UserNotificationDataDAO userNotificationDataDAO;
    private final NotificationDAO notificationDAO;
    private final FileGenerateService fileGenerateService;
    private final ActivityService activityService;
    private final UserContextService userContextService;

    // --------------------------------------------------------------------- API

    public List<TripChangeRequestDTO> findAllForTripTicket(int tripTicketId) {
        List<TripChangeRequestDTO> result = new ArrayList<>();
        for (TripChangeRequest r : tripChangeRequestDAO.findAllForTripTicket(tripTicketId)) {
            result.add(toDTO(r));
        }
        return result;
    }

    /**
     * Creates a change request from the current user's provider to the other party on a claimed trip.
     */
    public TripChangeRequestDTO createChangeRequest(int tripTicketId, CreateChangeRequestRequest request, User currentUser) {
        TripTicket trip = tripTicketDAO.findTripTicketByTripTicketId(tripTicketId);
        if (trip == null) {
            throw new InvalidInputException("Trip ticket not found [" + tripTicketId + "]");
        }
        currentUser = userContextService.hydrate(currentUser);
        if (currentUser == null || currentUser.getProvider() == null) {
            throw new EditNotAllowedException("Unable to determine the current user's provider.");
        }
        if (!isClaimed(trip)) {
            throw new InvalidInputException("Change requests apply only to claimed trips. Unclaimed trips can be edited directly by the originating provider.");
        }
        if (isWithinLockWindow(trip)) {
            throw new EditNotAllowedException("This trip is within 24 hours of pickup. Only a super admin can change it now.");
        }

        int currentProviderId = currentUser.getProvider().getProviderId();
        Provider originProvider = trip.getOriginProvider();
        Provider claimantProvider = trip.getApprovedTripClaim() != null ? trip.getApprovedTripClaim().getClaimantProvider() : null;
        if (claimantProvider == null) {
            throw new InvalidInputException("Trip has no approved claimant; nothing to coordinate.");
        }

        boolean isOriginator = originProvider != null && originProvider.getProviderId() == currentProviderId;
        boolean isClaimant = claimantProvider.getProviderId() == currentProviderId;
        if (!isOriginator && !isClaimant) {
            throw new EditNotAllowedException("You are not a party to this trip and cannot request changes.");
        }
        if (!isProviderAdmin(currentUser)) {
            throw new EditNotAllowedException("Only a provider admin can request changes.");
        }
        if (tripChangeRequestDAO.hasPendingRequest(tripTicketId)) {
            throw new InvalidInputException("There is already a pending change request for this trip.");
        }
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new InvalidInputException("A message describing the requested change is required.");
        }

        // The target is the other party.
        int targetProviderId = isOriginator ? claimantProvider.getProviderId() : originProvider.getProviderId();
        Provider targetProvider = providerDAO.findProviderByProviderId(targetProviderId);
        Provider requesterProvider = providerDAO.findProviderByProviderId(currentProviderId);

        TripChangeRequest req = new TripChangeRequest();
        req.setTripTicket(trip);
        req.setRequestedByProvider(requesterProvider);
        req.setRequestedByUserId(currentUser.getId());
        req.setTargetProvider(targetProvider);
        req.setStatus(statusOf(TripChangeRequestStatusConstants.pending));
        req.setMessage(request.getMessage().trim());
        req.setProposedChanges(toJson(request.getProposedChanges()));
        TripChangeRequest saved = tripChangeRequestDAO.createTripChangeRequest(req);

        recordActivity(trip.getId(), "Change request created",
                "from=" + safeName(requesterProvider) + ",to=" + safeName(targetProvider),
                safeName(requesterProvider));

        // Notify the target provider's admins. Pass the proposed-changes map so the email can list
        // each requested change on its own line alongside the trip's current values.
        String changesSummary = summarizeChanges(request.getProposedChanges());
        notifyProviderAdmins(targetProviderId, trip, NotificationTemplateCodeValue.tripChangeRequestReceived,
                "Change request received for trip " + safe(trip.getCommonTripId()),
                safeName(requesterProvider), safeName(targetProvider), req.getMessage(), changesSummary, "",
                request.getProposedChanges());

        return toDTO(saved);
    }

    /**
     * Edit an existing PENDING change request (message and/or proposed changes). Allowed for any
     * provider admin of the provider that created it (not necessarily the original author), or a
     * super admin. If nothing actually changed, this is a no-op
     * and no email is resent. Otherwise the request is updated and the "received" email is resent to
     * the target provider's admins.
     */
    public TripChangeRequestDTO updateChangeRequest(int tripTicketId, int requestId,
                                                    CreateChangeRequestRequest request, User currentUser) {
        TripChangeRequest req = tripChangeRequestDAO.findById(requestId);
        if (req == null || req.getTripTicket() == null || req.getTripTicket().getId() != tripTicketId) {
            throw new InvalidInputException("Change request not found [" + requestId + "]");
        }
        if (req.getStatus() == null
                || req.getStatus().getStatusId() != TripChangeRequestStatusConstants.pending.tripChangeRequestStatusValue()) {
            throw new InvalidInputException("Only a pending change request can be edited.");
        }
        currentUser = userContextService.hydrate(currentUser);
        if (currentUser == null || currentUser.getProvider() == null) {
            throw new EditNotAllowedException("Unable to determine the current user's provider.");
        }
        boolean isRequester = req.getRequestedByProvider() != null
                && req.getRequestedByProvider().getProviderId() == currentUser.getProvider().getProviderId();
        boolean isSuperAdmin = hasRole(currentUser, ROLE_ADMIN);
        if (!isRequester && !isSuperAdmin) {
            throw new EditNotAllowedException("Only a provider admin from the requesting provider can edit this request.");
        }
        if (isRequester && !isProviderAdmin(currentUser) && !isSuperAdmin) {
            throw new EditNotAllowedException("Only a provider admin can edit a change request.");
        }
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new InvalidInputException("A message describing the requested change is required.");
        }

        String newMessage = request.getMessage().trim();
        Map<String, String> newProposed = request.getProposedChanges() == null
                ? new LinkedHashMap<>() : request.getProposedChanges();
        Map<String, String> oldProposed = fromJson(req.getProposedChanges());

        // No-op: nothing changed -> don't write, don't resend.
        if (newMessage.equals(req.getMessage() == null ? "" : req.getMessage())
                && newProposed.equals(oldProposed)) {
            return toDTO(req);
        }

        req.setMessage(newMessage);
        req.setProposedChanges(toJson(newProposed));
        TripChangeRequest updated = tripChangeRequestDAO.updateTripChangeRequest(req);

        TripTicket trip = req.getTripTicket();
        String requesterName = safeName(req.getRequestedByProvider());
        String targetName = safeName(req.getTargetProvider());
        String changesSummary = summarizeChanges(newProposed);

        recordActivity(trip.getId(), "Change request updated", "request=" + requestId, requesterName);

        // Resend the "received" notification to the target provider's admins with the new details.
        if (req.getTargetProvider() != null) {
            notifyProviderAdmins(req.getTargetProvider().getProviderId(), trip,
                    NotificationTemplateCodeValue.tripChangeRequestReceived,
                    "Change request updated for trip " + safe(trip.getCommonTripId()),
                    requesterName, targetName, newMessage, changesSummary, "", newProposed);
        }

        return toDTO(updated);
    }

    /**
     * Cancel (withdraw) a PENDING change request. Allowed for any provider admin of the provider
     * that created it (not necessarily the original author), or a super admin. Marks the request
     * cancelled and notifies the target provider's admins that the request was withdrawn.
     */
    public TripChangeRequestDTO cancelChangeRequest(int tripTicketId, int requestId, User currentUser) {
        TripChangeRequest req = tripChangeRequestDAO.findById(requestId);
        if (req == null || req.getTripTicket() == null || req.getTripTicket().getId() != tripTicketId) {
            throw new InvalidInputException("Change request not found [" + requestId + "]");
        }
        if (req.getStatus() == null
                || req.getStatus().getStatusId() != TripChangeRequestStatusConstants.pending.tripChangeRequestStatusValue()) {
            throw new InvalidInputException("Only a pending change request can be cancelled.");
        }
        currentUser = userContextService.hydrate(currentUser);
        if (currentUser == null || currentUser.getProvider() == null) {
            throw new EditNotAllowedException("Unable to determine the current user's provider.");
        }
        boolean isRequester = req.getRequestedByProvider() != null
                && req.getRequestedByProvider().getProviderId() == currentUser.getProvider().getProviderId();
        boolean isSuperAdmin = hasRole(currentUser, ROLE_ADMIN);
        if (!isRequester && !isSuperAdmin) {
            throw new EditNotAllowedException("Only a provider admin from the requesting provider can cancel this request.");
        }
        if (isRequester && !isProviderAdmin(currentUser) && !isSuperAdmin) {
            throw new EditNotAllowedException("Only a provider admin can cancel a change request.");
        }

        req.setStatus(statusOf(TripChangeRequestStatusConstants.cancelled));
        TripChangeRequest updated = tripChangeRequestDAO.updateTripChangeRequest(req);

        TripTicket trip = req.getTripTicket();
        String requesterName = safeName(req.getRequestedByProvider());
        String targetName = safeName(req.getTargetProvider());
        Map<String, String> proposed = fromJson(req.getProposedChanges());
        String changesSummary = summarizeChanges(proposed);

        recordActivity(trip.getId(), "Change request cancelled", "request=" + requestId, requesterName);

        // Notify the target provider's admins that the request was withdrawn.
        if (req.getTargetProvider() != null) {
            notifyProviderAdmins(req.getTargetProvider().getProviderId(), trip,
                    NotificationTemplateCodeValue.tripChangeRequestCancelled,
                    "Change request withdrawn for trip " + safe(trip.getCommonTripId()),
                    requesterName, targetName, req.getMessage(), changesSummary, "", proposed);
        }

        return toDTO(updated);
    }

    public TripChangeRequestDTO approveChangeRequest(int tripTicketId, int requestId, String responseMessage, User currentUser) {
        return respond(tripTicketId, requestId, responseMessage, currentUser, true);
    }

    public TripChangeRequestDTO denyChangeRequest(int tripTicketId, int requestId, String responseMessage, User currentUser) {
        return respond(tripTicketId, requestId, responseMessage, currentUser, false);
    }

    private TripChangeRequestDTO respond(int tripTicketId, int requestId, String responseMessage, User currentUser, boolean approve) {
        TripChangeRequest req = tripChangeRequestDAO.findById(requestId);
        if (req == null || req.getTripTicket() == null || req.getTripTicket().getId() != tripTicketId) {
            throw new InvalidInputException("Change request not found [" + requestId + "]");
        }
        if (req.getStatus() == null
                || req.getStatus().getStatusId() != TripChangeRequestStatusConstants.pending.tripChangeRequestStatusValue()) {
            throw new InvalidInputException("This change request has already been responded to.");
        }
        currentUser = userContextService.hydrate(currentUser);
        if (currentUser == null || currentUser.getProvider() == null) {
            throw new EditNotAllowedException("Unable to determine the current user's provider.");
        }
        boolean isTarget = req.getTargetProvider() != null
                && req.getTargetProvider().getProviderId() == currentUser.getProvider().getProviderId();
        boolean isSuperAdmin = hasRole(currentUser, ROLE_ADMIN);
        if (!isTarget && !isSuperAdmin) {
            throw new EditNotAllowedException("Only the provider this request was sent to can respond to it.");
        }
        if (isTarget && !isProviderAdmin(currentUser) && !isSuperAdmin) {
            throw new EditNotAllowedException("Only a provider admin can respond to a change request.");
        }

        req.setResponseMessage(responseMessage == null ? null : responseMessage.trim());
        req.setRespondedByUserId(currentUser.getId());

        if (approve) {
            // Approval applies the proposed changes to the trip automatically (there is no separate
            // manual-edit step); the request rests in 'approved' to mean approved-and-applied.
            this.applyProposedTripChanges(req, currentUser);
            req.setStatus(statusOf(TripChangeRequestStatusConstants.approved));
        } else {
            req.setStatus(statusOf(TripChangeRequestStatusConstants.denied));
        }

        TripChangeRequest updated = tripChangeRequestDAO.updateTripChangeRequest(req);

        TripTicket trip = req.getTripTicket();
        String requesterName = safeName(req.getRequestedByProvider());
        String targetName = safeName(req.getTargetProvider());
        String changesSummary = summarizeChanges(fromJson(req.getProposedChanges()));

        recordActivity(trip.getId(), approve ? "Change request approved" : "Change request denied",
                "request=" + requestId, targetName);

        // Notify the original requester's admins of the outcome.
        int requesterProviderId = req.getRequestedByProvider().getProviderId();
        if (approve) {
            notifyProviderAdmins(requesterProviderId, trip, NotificationTemplateCodeValue.tripChangeRequestApproved,
                    "Your change request was approved for trip " + safe(trip.getCommonTripId()),
                    requesterName, targetName, req.getMessage(), changesSummary, req.getResponseMessage());
        } else {
            notifyProviderAdmins(requesterProviderId, trip, NotificationTemplateCodeValue.tripChangeRequestDenied,
                    "Your change request was denied for trip " + safe(trip.getCommonTripId()),
                    requesterName, targetName, req.getMessage(), changesSummary, req.getResponseMessage(),
                    fromJson(req.getProposedChanges()));
        }

        return toDTO(updated);
    }

    private void applyProposedTripChanges(TripChangeRequest req, User currentUser) {
        TripTicket trip = req.getTripTicket();
        Map<String, String> proposed = fromJson(req.getProposedChanges());

        if (trip == null) {
            throw new InvalidInputException("There is no trip associated with this change request.");
        } else if (proposed == null || proposed.isEmpty()) {
            throw new InvalidInputException("This change request has no proposed changes.");
        }

        List<String> changeLog = new ArrayList<>();

        String rawPUDate = trimmedOrNull(proposed.get("requested_pickup_date"));
        if (rawPUDate != null) {
            LocalDate oldVal = trip.getRequestedPickupDate();
            LocalDate newVal = parseDate("requested_pickup_date", rawPUDate);
            if (!Objects.equals(oldVal, newVal)) {
                trip.setRequestedPickupDate(newVal);
                changeLog.add("Requested pickup date: " + oldVal + " -> " + newVal);
            }
        }

        String rawPUTime = trimmedOrNull(proposed.get("requested_pickup_time"));
        if (rawPUTime != null) {
            Time oldVal = trip.getRequestedPickupTime();
            Time newVal = Time.valueOf(parseTime("requested_pickup_time", rawPUTime));
            if (!Objects.equals(oldVal, newVal)) {
                trip.setRequestedPickupTime(newVal);
                changeLog.add("Requested pickup time: " + oldVal + " -> " + newVal);
            }
        }

        String rawDODate = trimmedOrNull(proposed.get("requested_dropoff_date"));
        if (rawDODate != null) {
            LocalDate oldVal = trip.getRequestedDropoffDate();
            LocalDate newVal = parseDate("requested_dropoff_date", rawDODate);
            if (!Objects.equals(oldVal, newVal)) {
                trip.setRequestedDropoffDate(newVal);
                changeLog.add("Requested dropoff date: " + oldVal + " -> " + newVal);
            }
        }

        String rawDOTime = trimmedOrNull(proposed.get("requested_dropoff_time"));
        if (rawDOTime != null) {
            Time oldVal = trip.getRequestedDropOffTime();
            Time newVal = Time.valueOf(parseTime("requested_dropoff_time", rawDOTime));
            if (!Objects.equals(oldVal, newVal)) {
                trip.setRequestedDropOffTime(newVal);
                changeLog.add("Requested dropoff time: " + oldVal + " -> " + newVal);
            }
        }

        String rawSeats = trimmedOrNull(proposed.get("customer_seats_required"));
        if (rawSeats != null) {
            Integer oldVal = trip.getCustomerSeatsRequired();
            Integer newVal = parseInt("customer_seats_required", rawSeats);   // returns int, autoboxes
            if (!Objects.equals(oldVal, newVal)) {
                trip.setCustomerSeatsRequired(newVal);
                changeLog.add("Seats required: " + oldVal + " -> " + newVal);
            }
        }

        String rawNotes = trimmedOrNull(proposed.get("trip_notes"));
        if (rawNotes != null && !rawNotes.equals(trip.getTripNotes())) {
            changeLog.add("Trip notes: " + trip.getTripNotes() + " -> " + rawNotes);
            trip.setTripNotes(rawNotes);
        }

        if (req.getRequestedByUserId() != null) {
            trip.setUpdatedBy(req.getRequestedByUserId());
        }

        tripTicketDAO.updateTripTicket(trip);

        if (!changeLog.isEmpty()) {
            String approver = currentUser == null ? "?" : String.valueOf(currentUser.getId());
            String parties = "Requesting provider: " + safeName(req.getRequestedByProvider())
                    + " Requesting user: " + req.getRequestedByUserId()
                    + "; Approving provider: " + safeName(req.getTargetProvider())
                    + " Approving user: " + approver;
            String joined = String.join("; ", changeLog);

            log.info("Applied change request {} to trip {}: {}; {}", req.getId(), trip.getId(), joined, parties);
            recordActivity(trip.getId(), "Change request changes applied",
                    joined + " [" + parties + "]", safeName(req.getTargetProvider()));
        }
    }


    // --------------------------------------------------- edit-lock enforcement

    /**
     * Asserts that {@code currentUser} may edit {@code trip} directly. A claimed trip can never be
     * edited directly by a provider: changes must go through the change-request workflow, which
     * applies approved changes automatically. Super admins may always edit; the originating provider
     * may edit an unclaimed trip freely.
     *
     * @throws EditNotAllowedException if the edit is not permitted
     */
    public void assertCanEditDirectly(TripTicket trip, User currentUser) {
        if (trip == null) {
            return; // not-found handled elsewhere
        }
        // Super admin: always allowed.
        if (currentUser != null && hasRole(currentUser, ROLE_ADMIN)) {
            return;
        }
        currentUser = userContextService.hydrate(currentUser);
        if (currentUser == null || currentUser.getProvider() == null) {
            throw new EditNotAllowedException("Unable to determine the current user's provider.");
        }
        int currentProviderId = currentUser.getProvider().getProviderId();
        Provider originProvider = trip.getOriginProvider();
        boolean isOriginator = originProvider != null && originProvider.getProviderId() == currentProviderId;

        // Unclaimed trip: only the originating provider may edit (freely).
        if (!isClaimed(trip)) {
            if (isOriginator) {
                return;
            }
            throw new EditNotAllowedException("Only the originating provider can edit this trip.");
        }

        // Claimed trip: within 24h, only super admin (already returned above).
        if (isWithinLockWindow(trip)) {
            throw new EditNotAllowedException("This trip is within 24 hours of pickup. Only a super admin can change it now.");
        }

        Provider claimantProvider = trip.getApprovedTripClaim() != null
                ? trip.getApprovedTripClaim().getClaimantProvider() : null;
        boolean isClaimant = claimantProvider != null && claimantProvider.getProviderId() == currentProviderId;
        if (!isOriginator && !isClaimant) {
            throw new EditNotAllowedException("You are not a party to this trip and cannot edit it.");
        }

        // A claimed trip can never be edited directly, even by a party. Changes go through the
        // change-request workflow, which applies approved changes automatically.
        throw new EditNotAllowedException("This trip is claimed. Send a change request to the other provider; approved changes are applied automatically.");
    }

    // ------------------------------------------------------------- helpers

    private boolean isClaimed(TripTicket trip) {
        if (trip.getApprovedTripClaim() == null) {
            return false;
        }
        int statusId = trip.getStatus() != null ? trip.getStatus().getStatusId() : -1;
        // Editable-without-approval states: available, cancelled, completed, expired, rescinded.
        return statusId != TripTicketStatusConstants.available.tripTicketStatusUpdate()
                && statusId != TripTicketStatusConstants.cancelled.tripTicketStatusUpdate()
                && statusId != TripTicketStatusConstants.completed.tripTicketStatusUpdate()
                && statusId != TripTicketStatusConstants.expired.tripTicketStatusUpdate()
                && statusId != TripTicketStatusConstants.rescinded.tripTicketStatusUpdate();
    }

    /** True when the trip's requested pickup is less than 24 hours away (or already past). */
    private boolean isWithinLockWindow(TripTicket trip) {
        LocalDate date = trip.getRequestedPickupDate();
        if (date == null) {
            // Fall back to dropoff date if pickup is absent.
            date = trip.getRequestedDropoffDate();
        }
        if (date == null) {
            return false; // no schedule info; do not lock on time
        }
        LocalTime time = LocalTime.MIDNIGHT;
        if (trip.getRequestedPickupTime() != null) {
            time = trip.getRequestedPickupTime().toLocalTime();
        }
        ZonedDateTime pickup = ZonedDateTime.of(LocalDateTime.of(date, time), APP_ZONE);
        ZonedDateTime now = ZonedDateTime.now(APP_ZONE);
        return pickup.isBefore(now.plusHours(LOCK_WINDOW_HOURS));
    }

    private boolean isProviderAdmin(User user) {
        return hasRole(user, ROLE_PROVIDERADMIN) || hasRole(user, ROLE_ADMIN);
    }

    private boolean hasRole(User user, String role) {
        if (user == null || user.getAuthorities() == null) {
            return false;
        }
        for (UserAuthority a : user.getAuthorities()) {
            if (a != null && role.equalsIgnoreCase(a.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private Status statusOf(TripChangeRequestStatusConstants s) {
        Status status = new Status();
        status.setStatusId(s.tripChangeRequestStatusValue());
        return status;
    }

    private void notifyProviderAdmins(int providerId, TripTicket trip, NotificationTemplateCodeValue template,
                                      String subject, String requesterName, String targetName,
                                      String message, String changesSummary, String responseMessage) {
        notifyProviderAdmins(providerId, trip, template, subject, requesterName, targetName,
                message, changesSummary, responseMessage, null);
    }

    private void notifyProviderAdmins(int providerId, TripTicket trip, NotificationTemplateCodeValue template,
                                      String subject, String requesterName, String targetName,
                                      String message, String changesSummary, String responseMessage,
                                      Map<String, String> proposedChanges) {
        List<User> users = userNotificationDataDAO.getUsersOfProvider(providerId);
        for (User user : users) {
            if (!isProviderAdmin(user)) {
                continue;
            }
            Map<String, String> params = NotificationParamBuilder.changeRequestParams(
                    user.getName(), trip, requesterName, targetName, message, changesSummary, responseMessage,
                    proposedChanges);
            NotificationRequest req = new NotificationRequest(user.getEmail(), template, subject, params, false, null);
            new NotificationComposer(notificationDAO, fileGenerateService).enqueue(req, trip);
        }
    }

    private void recordActivity(int tripTicketId, String action, String details, String actionBy) {
        try {
            ActivityDTO dto = new ActivityDTO();
            dto.setTripTicketId(tripTicketId);
            dto.setAction(action);
            dto.setActionDetails(details);
            dto.setActionTakenBy(actionBy);
            activityService.createActivity(dto);
        } catch (Exception e) {
            log.warn("Failed to record activity for trip {}: {}", tripTicketId, e.getMessage());
        }
    }

    private String summarizeChanges(Map<String, String> changes) {
        if (changes == null || changes.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : changes.entrySet()) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(humanizeField(e.getKey())).append(" -> ").append(e.getValue());
        }
        return sb.toString();
    }

    private String humanizeField(String key) {
        if (key == null) {
            return "";
        }
        String spaced = key.replace('_', ' ');
        return spaced.isEmpty() ? spaced : Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    private static String toJson(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(map);
        } catch (Exception e) {
            return null;
        }
    }

    private static Map<String, String> fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new LinkedHashMap<>();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<LinkedHashMap<String, String>>() {});
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private TripChangeRequestDTO toDTO(TripChangeRequest r) {
        TripChangeRequestDTO dto = new TripChangeRequestDTO();
        dto.setId(r.getId());
        if (r.getTripTicket() != null) {
            dto.setTripTicketId(r.getTripTicket().getId());
        }
        if (r.getRequestedByProvider() != null) {
            dto.setRequestedByProviderId(r.getRequestedByProvider().getProviderId());
            dto.setRequestedByProviderName(r.getRequestedByProvider().getProviderName());
        }
        dto.setRequestedByUserId(r.getRequestedByUserId());
        if (r.getTargetProvider() != null) {
            dto.setTargetProviderId(r.getTargetProvider().getProviderId());
            dto.setTargetProviderName(r.getTargetProvider().getProviderName());
        }
        if (r.getStatus() != null) {
            dto.setStatusId(r.getStatus().getStatusId());
            dto.setStatus(statusLabel(r.getStatus().getStatusId()));
        }
        dto.setMessage(r.getMessage());
        dto.setProposedChanges(fromJson(r.getProposedChanges()));
        dto.setResponseMessage(r.getResponseMessage());
        if (r.getCreatedAt() != null) {
            dto.setCreatedAt(r.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
        if (r.getUpdatedAt() != null) {
            dto.setUpdatedAt(r.getUpdatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
        return dto;
    }

    private String statusLabel(int statusId) {
        if (statusId == TripChangeRequestStatusConstants.pending.tripChangeRequestStatusValue()) return "Pending";
        if (statusId == TripChangeRequestStatusConstants.approved.tripChangeRequestStatusValue()) return "Approved";
        if (statusId == TripChangeRequestStatusConstants.denied.tripChangeRequestStatusValue()) return "Denied";
        if (statusId == TripChangeRequestStatusConstants.cancelled.tripChangeRequestStatusValue()) return "Cancelled";
        return "Unknown";
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String safeName(Provider p) {
        return p == null ? "" : safe(p.getProviderName());
    }

    private static String trimmedOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private LocalDate parseDate(String field, String value) {
        try {
            return LocalDate.parse(value, CR_DATE);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Invalid date for " + field + ": '" + value + "' (expected MM/DD/YYYY).");
        }
    }

    private LocalTime parseTime(String field, String value) {
        try {
            return LocalTime.parse(value, CR_TIME);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Invalid time for " + field + ": '" + value + "' (expected hh:mm AM/PM).");
        }
    }

    private int parseInt(String field, String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid number for " + field + ": '" + value + "'.");
        }
    }
}
