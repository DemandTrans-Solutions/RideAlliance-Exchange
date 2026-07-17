package com.clearinghouse.service.notification;

import com.clearinghouse.entity.Address;
import com.clearinghouse.entity.TripClaim;
import com.clearinghouse.entity.TripTicket;

import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Helpers to build the map of template parameters consistently across templates.
 */
public class NotificationParamBuilder {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NotificationParamBuilder.class);
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");
    // Change-request emails render the "Current Trip Details" section in the SAME format the client
    // sends proposed changes in (MM/dd/yyyy, hh:mm a, Locale.US) so the current values line up with
    // the "Requested Changes" values shown alongside them. Used only by buildCurrentTripDetailsHtml.
    private static final DateTimeFormatter CR_DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy", java.util.Locale.US);
    private static final DateTimeFormatter CR_DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.US);

    public static Map<String, String> baseClaimParams(String nameOfUser,
                                                      TripTicket tripTicket,
                                                      TripClaim tripClaim) {
        Map<String, String> map = new HashMap<>();
        map.put("nameOfUser", nameOfUser);
        map.put("commonTripTicketId", tripTicket.getCommonTripId());
        map.put("lastStatusChangedByProviderName",
                Optional.ofNullable(tripTicket.getLastStatusChangedByProvider())
                        .map(p -> p.getProviderName())
                        .orElseGet(() -> tripTicket.getOriginProvider().getProviderName()));
        // fares
        if (tripClaim != null) {
            map.put("requesterFare", String.valueOf(tripClaim.getRequesterProviderFare()));
            map.put("calculatedProposedFare", String.valueOf(tripClaim.getCalculatedProposedFare()));
            map.put("proposedFare", String.valueOf(tripClaim.getProposedFare()));
            map.put("ackStatus", tripClaim.isAckStatus() ?
                    "Please be aware that the claimed trip occurs outside of the specified operating hours for the Provider who claimed the trip, and both the owner of the trip and the claimant Provider have acknowledged this situation." :
                    "");
        }
        map.put("year", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        addPickupAndDropoffFormatting(map, tripTicket);
        return map;
    }

    /**
     * Parameters for the trip change-request notification templates.
     *
     * @param recipientName        name of the user receiving the email
     * @param tripTicket           the trip ticket the request concerns
     * @param requesterProviderName the provider that initiated the request
     * @param targetProviderName    the provider asked to approve/deny the request
     * @param message              free-text message from the requester
     * @param changesSummary       human-readable summary of the proposed changes (may be empty)
     * @param responseMessage      optional message from the approver/denier (may be empty)
     */
    public static Map<String, String> changeRequestParams(String recipientName,
                                                          TripTicket tripTicket,
                                                          String requesterProviderName,
                                                          String targetProviderName,
                                                          String message,
                                                          String changesSummary,
                                                          String responseMessage) {
        return changeRequestParams(recipientName, tripTicket, requesterProviderName, targetProviderName,
                message, changesSummary, responseMessage, null);
    }

    /**
     * @param proposedChanges raw field->value map of the requested changes (may be null/empty). When
     *                        present, drives the per-line {@code requestedChangesList} and is unused
     *                        otherwise. {@code currentTripDetails} is built from the trip regardless.
     */
    public static Map<String, String> changeRequestParams(String recipientName,
                                                          TripTicket tripTicket,
                                                          String requesterProviderName,
                                                          String targetProviderName,
                                                          String message,
                                                          String changesSummary,
                                                          String responseMessage,
                                                          Map<String, String> proposedChanges) {
        Map<String, String> map = new HashMap<>();
        map.put("name", recipientName == null ? "" : recipientName);
        map.put("nameOfUser", recipientName == null ? "" : recipientName);
        map.put("tripTicketnumber", tripTicket.getCommonTripId() == null ? "" : tripTicket.getCommonTripId());
        map.put("commonTripTicketId", tripTicket.getCommonTripId() == null ? "" : tripTicket.getCommonTripId());
        map.put("requesterProviderName", requesterProviderName == null ? "" : requesterProviderName);
        map.put("targetProviderName", targetProviderName == null ? "" : targetProviderName);
        map.put("message", message == null ? "" : message);
        map.put("changesSummary", changesSummary == null ? "" : changesSummary);
        map.put("responseMessage", responseMessage == null ? "" : responseMessage);
        map.put("customerName", buildCustomerName(tripTicket));
        map.put("currentTripDetails", buildCurrentTripDetailsHtml(tripTicket));
        map.put("requestedChangesList", buildRequestedChangesHtml(proposedChanges));
        map.put("year", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        addPickupAndDropoffFormatting(map, tripTicket);
        return map;
    }


    public static Map<String, String> tripCommentAddedParams(String recipientName, TripTicket tripTicket) {
        Map<String, String> map = new HashMap<>();
        map.put("name", recipientName == null ? "" : recipientName);
        map.put("nameOftheuser", recipientName == null ? "" : recipientName);
        map.put("tripTicketnumber", tripTicket.getCommonTripId() == null ? "" : tripTicket.getCommonTripId());
        map.put("commonTripTicketId", tripTicket.getCommonTripId() == null ? "" : tripTicket.getCommonTripId());
        map.put("customerName", buildCustomerName(tripTicket));
        map.put("currentTripDetails", buildCurrentTripDetailsHtml(tripTicket));
        map.put("year", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        addPickupAndDropoffFormatting(map, tripTicket);
        return map;
    }

    /** Full customer name from the trip's first/middle/last fields. */
    private static String buildCustomerName(TripTicket t) {
        StringBuilder sb = new StringBuilder();
        appendNamePart(sb, t.getCustomerFirstName());
        appendNamePart(sb, t.getCustomerMiddleName());
        appendNamePart(sb, t.getCustomerLastName());
        return sb.toString();
    }

    private static void appendNamePart(StringBuilder sb, String part) {
        if (part != null && !part.trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(part.trim());
        }
    }

    /**
     * HTML (one {@code <br>}-separated line per field) of the trip's CURRENT values for each field
     * the change-request form exposes. Empty values are shown as a dash so the approver sees the
     * full picture.
     */
    private static String buildCurrentTripDetailsHtml(TripTicket t) {
        StringBuilder sb = new StringBuilder();
        appendDetailLine(sb, "Pickup location", formatAddress(t.getPickupAddress()));
        appendDetailLine(sb, "Dropoff location", formatAddress(t.getDropOffAddress()));
        appendDetailLine(sb, "Pickup date", formatCrDisplayDate(t.getRequestedPickupDate()));
        appendDetailLine(sb, "Pickup time", formatCrDisplayTime(t.getRequestedPickupTime()));
        appendDetailLine(sb, "Dropoff date", formatCrDisplayDate(t.getRequestedDropoffDate()));
        appendDetailLine(sb, "Dropoff time", formatCrDisplayTime(t.getRequestedDropOffTime()));
        appendDetailLine(sb, "Seats required",
                t.getCustomerSeatsRequired() == null ? "" : String.valueOf(t.getCustomerSeatsRequired()));
        appendDetailLine(sb, "Trip notes", t.getTripNotes());
        return sb.toString();
    }

    /** A readable one-line address: prefer the common name, else street / city, state zip. */
    private static String formatAddress(Address a) {
        if (a == null) {
            return "";
        }
        String common = a.getCommonName();
        if (common != null && !common.trim().isEmpty()) {
            return common.trim();
        }
        StringBuilder sb = new StringBuilder();
        if (a.getStreet1() != null && !a.getStreet1().trim().isEmpty()) {
            sb.append(a.getStreet1().trim());
        }
        if (a.getCity() != null && !a.getCity().trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(a.getCity().trim());
        }
        if (a.getState() != null && !a.getState().trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(a.getState().trim());
        }
        if (a.getZipcode() != null && !a.getZipcode().trim().isEmpty()) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(a.getZipcode().trim());
        }
        return sb.toString();
    }

    private static void appendDetailLine(StringBuilder sb, String label, String value) {
        if (sb.length() > 0) {
            sb.append("<br>");
        }
        String shown = (value == null || value.trim().isEmpty()) ? "-" : value.trim();
        sb.append("<strong>").append(label).append(":</strong> ").append(shown);
    }

    /**
     * HTML (one {@code <br>}-separated line per requested change) listing only the fields the
     * requester actually filled in. Field keys are humanized (e.g. requested_pickup_date ->
     * "Pickup date"). Returns "" when there are no structured proposed changes.
     */
    private static String buildRequestedChangesHtml(Map<String, String> proposedChanges) {
        if (proposedChanges == null || proposedChanges.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : proposedChanges.entrySet()) {
            if (e.getValue() == null || e.getValue().trim().isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("<br>");
            }
            sb.append("<strong>").append(humanizeFieldLabel(e.getKey())).append(":</strong> ")
                    .append(e.getValue().trim());
        }
        return sb.toString();
    }

    /** Turn a snake_case field key into a friendly label, stripping a leading "Requested ". */
    private static String humanizeFieldLabel(String key) {
        if (key == null) {
            return "";
        }
        // Keep these labels identical to the "Current Trip Details" section and the client form so
        // the same field reads the same in both places (e.g. not "Customer seats required" here but
        // "Seats required" there).
        if ("customer_seats_required".equals(key)) {
            return "Seats required";
        }
        String spaced = key.replace('_', ' ').trim();
        if (spaced.isEmpty()) {
            return spaced;
        }
        String label = Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
        // "Requested pickup date" -> "Pickup date" to match the form labels.
        if (label.startsWith("Requested ")) {
            String rest = label.substring("Requested ".length());
            label = rest.isEmpty() ? label : Character.toUpperCase(rest.charAt(0)) + rest.substring(1);
        }
        return label;
    }

    public static void addPickupAndDropoffFormatting(Map<String, String> map, TripTicket tripTicket) {
        if (tripTicket.getRequestedPickupDate() != null) {
            map.put("pickupDate", formatDisplayDate(tripTicket.getRequestedPickupDate()));
            map.put("pickupTime", formatDisplayTime(tripTicket.getRequestedPickupTime()));
        } else {
            // use dropoff
            String finalDate = formatDisplayDate(tripTicket.getRequestedDropoffDate());
            map.put("pickupDate", "Pickup date - No requested pickup date,");
            String dropTime = formatDisplayTime(tripTicket.getRequestedDropOffTime());
            String pickupTimeString = "Pickup time - No requested pickup time,Dropoff date - " + finalDate
                    + ",Dropoff time - " + dropTime + " ";
            map.put("pickupTime", pickupTimeString);
        }
    }

    private static String formatDisplayDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        try {
            return date.format(DISPLAY_DATE_FORMATTER);
        } catch (Exception e) {
            log.error("Error formatting LocalDate: {}", date, e);
            return date.toString();
        }
    }

    private static String formatDisplayTime(Time time) {
        if (time == null) {
            return "";
        }
        try {
            return time.toLocalTime().format(DISPLAY_TIME_FORMATTER);
        } catch (Exception e) {
            log.error("Error formatting Time: {}", time, e);
            return time.toString();
        }
    }

    /** Date in the change-request display format (MM/dd/yyyy), matching the proposed-changes values. */
    private static String formatCrDisplayDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        try {
            return date.format(CR_DISPLAY_DATE_FORMATTER);
        } catch (Exception e) {
            log.error("Error formatting LocalDate: {}", date, e);
            return date.toString();
        }
    }

    /** Time in the change-request display format (hh:mm a), matching the proposed-changes values. */
    private static String formatCrDisplayTime(Time time) {
        if (time == null) {
            return "";
        }
        try {
            return time.toLocalTime().format(CR_DISPLAY_TIME_FORMATTER);
        } catch (Exception e) {
            log.error("Error formatting Time: {}", time, e);
            return time.toString();
        }
    }
}
