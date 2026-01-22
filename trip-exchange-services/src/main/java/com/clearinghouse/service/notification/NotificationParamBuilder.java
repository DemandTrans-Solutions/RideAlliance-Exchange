package com.clearinghouse.service.notification;

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
}
