/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;

import com.clearinghouse.dao.*;
import com.clearinghouse.dto.TripResultDTO;
import com.clearinghouse.dto.TripResultRequestDTO;
import com.clearinghouse.dto.TripTicketRequestDTO;
import com.clearinghouse.entity.*;
import com.clearinghouse.enumentity.NotificationStatus;
import com.clearinghouse.enumentity.NotificationTemplateCodeValue;
import com.clearinghouse.enumentity.TripTicketStatusConstants;
import com.clearinghouse.exceptions.HandlingExceptionForOKStatus;
import com.clearinghouse.exceptions.InvalidInputCheckException;
import com.clearinghouse.exceptions.InvalidInputException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author chaitanyaP
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class TripResultService implements IConvertBOToDTO, IConvertDTOToBO {


    private final FileGenerateService fileGenerateService;


    private final UserDAO userDAO;

    private final TripResultDAO tripResultDAO;


    private final TripClaimService tripClaimService;


    private final UserNotificationDataDAO userNotificationDataDAO;


    private final NotificationDAO notificationDAO;


    private final TripTicketDAO tripTicketDAO;


    private final ModelMapper tripResultModelMapper;


    public TripResultDTO createTripResultDTO(TripTicketRequestDTO tripTicketRequestDTO) {
        var tripResultRequestDTO = convertToTripResultDTO(tripTicketRequestDTO);
        if ( tripResultRequestDTO != null ) {
            return createCompletedTripResult(tripResultRequestDTO);
        }
        return null;
    }


    public TripResultRequestDTO convertToTripResultDTO( TripTicketRequestDTO tripTicketRequestDTO) {
        if ( tripTicketRequestDTO.isComplete() ) {
            // convert a TripTicketRequestDTO to TripResultDTO
            var tripResultDTO = new TripResultRequestDTO();

            // Map basic identifiers
            if (tripTicketRequestDTO.getTripTicketId() != null) {
                tripResultDTO.setTripTicketId(tripTicketRequestDTO.getTripTicketId());
            } else {
                // find the trip ticket for the provider trip id
                var tripTicket = tripTicketDAO.findTripTicketsByClaimantProviderOriginTripId(tripTicketRequestDTO.getOriginProviderId(), tripTicketRequestDTO.getOriginTripId());
                if ( tripTicket != null ) {
                    tripResultDTO.setTripTicketId(tripTicket.getId());
                }
            }

            // Dates/times
            // Trip date: try to reuse existing flexible parser
            tripResultDTO.setTripDate(tripTicketRequestDTO.getTripDate());

            // Actual times (may be provided as java.sql.Time)
            if (tripTicketRequestDTO.getActualPickupArriveTime() != null) {
                tripResultDTO.setActualPickupArriveTime(tripTicketRequestDTO.getActualPickupArriveTime());
            }
            if (tripTicketRequestDTO.getActualPickupDepartTime() != null) {
                tripResultDTO.setActualPickupDepartTime(tripTicketRequestDTO.getActualPickupDepartTime());
            }
            if (tripTicketRequestDTO.getActualDropOffArriveTime() != null) {
                tripResultDTO.setActualDropOffArriveTime(tripTicketRequestDTO.getActualDropOffArriveTime());
            }
            if (tripTicketRequestDTO.getActualDropOffDepartTime() != null) {
                tripResultDTO.setActualDropOffDepartTime(tripTicketRequestDTO.getActualDropOffDepartTime());
            }

            // Location coordinates from addresses
            if (tripTicketRequestDTO.getPickUpAddress() != null) {
                tripResultDTO.setPickUpLatitude(tripTicketRequestDTO.getPickUpAddress().getLatitude());
                tripResultDTO.setPickupLongitude(tripTicketRequestDTO.getPickUpAddress().getLongitude());
                // build a printable pickup address
                tripResultDTO.setPickUpAddress(
                        (tripTicketRequestDTO.getPickUpAddress().getStreet1() == null ? "" : tripTicketRequestDTO.getPickUpAddress().getStreet1())
                                + " " + (tripTicketRequestDTO.getPickUpAddress().getCity() == null ? "" : tripTicketRequestDTO.getPickUpAddress().getCity())
                                + " " + (tripTicketRequestDTO.getPickUpAddress().getState() == null ? "" : tripTicketRequestDTO.getPickUpAddress().getState())
                                + " " + (tripTicketRequestDTO.getPickUpAddress().getZipcode() == null ? "" : tripTicketRequestDTO.getPickUpAddress().getZipcode())
                );
            }
            if (tripTicketRequestDTO.getDropOffAddress() != null) {
                tripResultDTO.setDropOffLatitude(tripTicketRequestDTO.getDropOffAddress().getLatitude());
                tripResultDTO.setDropOffLongitude(tripTicketRequestDTO.getDropOffAddress().getLongitude());
                tripResultDTO.setDropOffAddress(
                        (tripTicketRequestDTO.getDropOffAddress().getStreet1() == null ? "" : tripTicketRequestDTO.getDropOffAddress().getStreet1())
                                + " " + (tripTicketRequestDTO.getDropOffAddress().getCity() == null ? "" : tripTicketRequestDTO.getDropOffAddress().getCity())
                                + " " + (tripTicketRequestDTO.getDropOffAddress().getState() == null ? "" : tripTicketRequestDTO.getDropOffAddress().getState())
                                + " " + (tripTicketRequestDTO.getDropOffAddress().getZipcode() == null ? "" : tripTicketRequestDTO.getDropOffAddress().getZipcode())
                );
            }

            // Fare, vehicle, driver
            tripResultDTO.setFareCollected(tripTicketRequestDTO.getFareCollected());
            tripResultDTO.setVehicleId(tripTicketRequestDTO.getVehicleId());
            tripResultDTO.setDriverId(tripTicketRequestDTO.getDriverId());

            // No-show / cancellation reasons
            tripResultDTO.setNoShowReason(tripTicketRequestDTO.getNoShowReason());
            tripResultDTO.setCancellationReason(tripTicketRequestDTO.getCancelReason());

            // Counts
            // TripTicketRequestDTO uses numAttendants / numGuests; map to DTO equivalents
            tripResultDTO.setNumberOfAttendants(tripTicketRequestDTO.getNumAttendants() != null ? tripTicketRequestDTO.getNumAttendants() : 0);
            tripResultDTO.setNumberOfGuests(tripTicketRequestDTO.getNumGuests() != null ? tripTicketRequestDTO.getNumGuests() : 0);
            tripResultDTO.setNumberOfPassengers(tripResultDTO.getNumberOfAttendants() + tripResultDTO.getNumberOfGuests() + 1); // +1 for the customer

            return tripResultDTO;

        }
        return null;
    }

    public List<TripResultDTO> findAllTripResultByTripTicketId(int trip_ticket_id) {

        List<TripResult> tripResults = tripResultDAO.findAllTripResultByTripTicketId(trip_ticket_id);

        List<TripResultDTO> tripResultDTOList = new java.util.ArrayList<>();
        for (TripResult tripResult : tripResults) {

            tripResultDTOList.add((TripResultDTO) toDTO(tripResult));
        }

        return tripResultDTOList;

    }

    public void deleteTripResult(int tripResultId) {
        tripResultDAO.deletTripResult(tripResultId);
    }

	/*    @Override
        public TripResultDTO createTripResult(int trip_ticket_id, TripResultDTO tripResultDTO) {

           // tripResultDTO.setTrip_ticket_id(trip_ticket_id);
            TripResult tripResult = (TripResult) toBO(tripResultDTO);

            tripResultDAO.createTripResult(tripResult);

            TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(tripResult.getTripTicket().getId());

            //            fetch the users of that provider having flag notify the  claimed trip ticket rescinded
            List<User> users = userNotificationDataDAO.getUsersForTripResultSubmitted(tripTicket.getOriginProvider().getProviderId());
            for (User user : users) {
                //fetching single obj of the user role
                List<UserAuthority> userAuthority = new ArrayList<>();

                userAuthority.addAll(user.getAuthorities());
                String userrole = userAuthority.get(0).getAuthority();
                if (user.isIsNotifyTripResultSubmitted() && (userrole.equalsIgnoreCase("ROLE_PROVIDERADMIN") || userrole.equalsIgnoreCase("ROLE_PROVIDERUSER"))) {

                    //NotificationEnginePart.. for trip ticket originator
                    Notification emailNotificationForClaimant = new Notification();
                    NotificationTemplate notificationTemplateForClaimant = new NotificationTemplate();

                    emailNotificationForClaimant.setIsEMail(true);
                    emailNotificationForClaimant.setStatusId(NotificationStatus.newStatus.status());
                    notificationTemplateForClaimant.setNotificationTemplateId(NotificationTemplateCodeValue.tripResultSubmitted.templateCodeValue());
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

                    String[] rowDateFromDatabaseArray = tripResult.getActual_pickup_time().toString().split("T");

                    String rowDate = rowDateFromDatabaseArray[0];
                    String rowTime = rowDateFromDatabaseArray[1];

                    String[] arrayOfDate = rowDate.split("-");
                    String finalDate = arrayOfDate[2] + "-" + arrayOfDate[1] + "-" + arrayOfDate[0];

                    String[] arrayforPickupTime = rowTime.split(":");

                    /* conversion of 24 hrs to 12 hrs//
                    String AMOrPM = "AM";
                    int hrHnadValue = Integer.valueOf(arrayforPickupTime[0]);
                    if (hrHnadValue > 12) {
                        hrHnadValue = hrHnadValue - 12;
                        AMOrPM = "PM";
                    }

                    rescindClaimantTemplateMap.put("actualPickupDateAndTime", finalDate + " " + hrHnadValue + ":" + arrayforPickupTime[1] + " " + AMOrPM);
                    rescindClaimantTemplateMap.put("claimantProvider", tripTicket.getApprovedTripClaim().getClaimantProvider().getProviderName());

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
                    emailNotificationForClaimant.setSubject("Trip result is submitted");
                    notificationDAO.createNotification(emailNotificationForClaimant);
                }

            }

            String status = tripClaimService.updateTickcetForClaimAction(trip_ticket_id);
            if (status.equalsIgnoreCase("success")) {
                return (TripResultDTO) toDTO(tripResult);
            } else {
                return null;
            }
        }
    */

    public TripResultDTO findTripResultByTripResultId(int id) {
        return (TripResultDTO) toDTO(tripResultDAO.findTripResultByTripResultId(id));
    }


    public TripResultDTO updateTripResult( TripResultDTO tripResultDTO) {

        TripResult tripResult = (TripResult) toBO(tripResultDTO);
        tripResultDAO.updateTripResult(tripResult);

        return (TripResultDTO) toDTO(tripResult);
    }

    @Override
    public Object toDTO(Object bo) {

        TripResult tripResultBO = (TripResult) bo;

        TripResultDTO tripResultDTO = tripResultModelMapper.map(tripResultBO, TripResultDTO.class);

        return tripResultDTO;

    }

    @Override
    public Object toBO(Object dto) {

        TripResultDTO tripResultDTO = (TripResultDTO) dto;

        TripResult tripResultBO = tripResultModelMapper.map(tripResultDTO, TripResult.class);

        return tripResultBO;
    }

    @Override
    public Object toDTOCollection(Object boCollection) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    public TripResultDTO createCompletedTripResult(TripResultRequestDTO tripResultDTO) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = ((User) auth.getDetails());
        log.debug("user&&&&&==" + user.toString());

        TripResultDTO convertRequestedDTO = convertStringToActualDataType(tripResultDTO);

        TripResult tripResult = (TripResult) toBO(convertRequestedDTO);

        if (tripResult.getTripTicket().getId() != 0) {
            TripResult foundTripResult = tripResultDAO
                    .findTripResultByTripTicketId(tripResult.getTripTicket().getId());
            if (foundTripResult != null) {
                tripResult.setId(foundTripResult.getId());
                tripResult = tripResultDAO.updateTripResult(tripResult);
            } else {
                tripResult = tripResultDAO.createTripResult(tripResult);
            }
        } else {
            tripResult = tripResultDAO.createTripResult(tripResult);
        }

        TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(tripResult.getTripTicket().getId());

        //fetch the users of that provider having flag notify the  claimed trip ticket rescinded
	/*        List<User> usersOfOriginator = userNotificationDataDAO.getUsersOfProvider(tripTicket.getOriginProvider().getProviderId());
	        for (User user : usersOfOriginator) {
	            //fetching single obj of the user role
	            List<UserAuthority> userAuthority = new ArrayList<>();

	            userAuthority.addAll(user.getAuthorities());
	            String userrole = userAuthority.get(0).getAuthority();
	            if (userrole.equalsIgnoreCase("ROLE_PROVIDERADMIN") || userrole.equalsIgnoreCase("ROLE_PROVIDERUSER")) {

	                //NotificationEnginePart.. for trip ticket originator
	                Notification emailNotificationForOriginator = new Notification();
	                NotificationTemplate notificationTemplateForOriginator = new NotificationTemplate();

	                emailNotificationForOriginator.setIsEMail(true);
	                emailNotificationForOriginator.setStatusId(NotificationStatus.newStatus.status());
	                notificationTemplateForOriginator.setNotificationTemplateId(NotificationTemplateCodeValue.tripResultCompleted.templateCodeValue());
	                emailNotificationForOriginator.setNotificationTemplate(notificationTemplateForOriginator);
	                emailNotificationForOriginator.setNumberOfAttempts(0);
	                emailNotificationForOriginator.setIsActive(true);
	                emailNotificationForOriginator.setEmailTo(user.getEmail());

	                //   Setting parameter values in according to the template for email notofication.
	                Map tripResultCompletedTemplateMap = new HashMap<String, String>();
	                tripResultCompletedTemplateMap.put("message",
							"The attached CSV file has all of the completed trip data from the claimants of your trip tickets for the trip that were verified yesterday");
	                tripResultCompletedTemplateMap.put("year", Year.now().toString());

	                emailNotificationForOriginator.setSubject("Completed Trip Data for Your Trip Tickets");
	                notificationDAO.createNotification(emailNotificationForOriginator);
	            }
	        }
	  */
        Status status = new Status();
        status.setStatusId(TripTicketStatusConstants.completed.tripTicketStatusUpdate());
        tripTicket.setStatus(status);
        tripTicketDAO.updateTripTicket(tripTicket);
        convertRequestedDTO = (TripResultDTO) toDTO(tripResult);
        convertRequestedDTO.setPickUpAddress(tripTicket.getPickupAddress().getStreet1() + " " + tripTicket.getPickupAddress().getCity() + " " + tripTicket.getPickupAddress().getState() + " " + tripTicket.getPickupAddress().getZipcode());
        convertRequestedDTO.setDropOffAddress(tripTicket.getDropOffAddress().getStreet1() + " " + tripTicket.getDropOffAddress().getCity() + " " + tripTicket.getDropOffAddress().getState() + " " + tripTicket.getDropOffAddress().getZipcode());
        convertRequestedDTO.setClaimantProvider(tripTicket.getApprovedTripClaim().getClaimantProvider().getProviderName());
        convertRequestedDTO.setScheduledPickupTime( tripTicket.getRequestedPickupTime() != null ? tripTicket.getRequestedPickupTime().toString() : null );
        convertRequestedDTO.setScheduledDropOffTime(tripTicket.getRequestedDropOffTime() != null ? tripTicket.getRequestedDropOffTime().toString() : null);
        return convertRequestedDTO;
    }

    /// /////////////////////////////////////

    private TripResultDTO convertStringToActualDataType(TripResultRequestDTO tripResultRequestDTO) {

        TripResultDTO tripResultDTO = new TripResultDTO();
        List<String> messageList = new ArrayList<String>();

        //VehicleId
        if (tripResultRequestDTO.getVehicleId() != null) {
            tripResultDTO.setVehicleId(tripResultRequestDTO.getVehicleId());
        }

        //DriverId
        if (tripResultRequestDTO.getDriverId() != null) {
            tripResultDTO.setDriverId(tripResultRequestDTO.getDriverId());
        }

        //TripTicketId
        if (String.valueOf(tripResultRequestDTO.getTripTicketId()) == null || String.valueOf(tripResultRequestDTO.getTripTicketId()) == "" || tripResultRequestDTO.getTripTicketId() == 0) {
            throw new HandlingExceptionForOKStatus("TripTicketId: Must not be null or empty");
        } else {
            tripResultDTO.setTripTicketId(tripResultRequestDTO.getTripTicketId());
        }

        TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(tripResultDTO.getTripTicketId());
        tripResultDTO.setVersion(1);

        tripResultDTO.setCancellationReason(tripResultRequestDTO.getCancellationReason());
        tripResultDTO.setNoShowReason(tripResultRequestDTO.getNoShowReason());

        float tempDecimal = 0.0f;
        //PickUpLatitude
        if (tripResultRequestDTO.getPickUpLatitude() == 0) {
            tripResultDTO.setPickUpLatitude(tripTicket.getPickupAddress().getLatitude());
        } else {
            tripResultDTO.setPickUpLatitude(tripResultRequestDTO.getPickUpLatitude());
        }
        //PickupLongitude
        if (tripResultRequestDTO.getPickupLongitude() == 0) {
            tripResultDTO.setPickupLongitude(tripTicket.getPickupAddress().getLongitude());
        } else {
            tripResultDTO.setPickupLongitude(tripResultRequestDTO.getPickupLongitude());
        }
        //DropOffLatitude
        if (tripResultRequestDTO.getDropOffLatitude() == 0) {
            tripResultDTO.setDropOffLatitude(tripTicket.getDropOffAddress().getLatitude());
        } else {
            tripResultDTO.setDropOffLatitude(tripResultRequestDTO.getDropOffLatitude());
        }
        //DropOffLongitude
        if (tripResultRequestDTO.getDropOffLongitude() == 0) {
            tripResultDTO.setDropOffLongitude(tripTicket.getDropOffAddress().getLongitude());
        } else {
            tripResultDTO.setDropOffLongitude(tripResultRequestDTO.getDropOffLongitude());
        }

        // tripResultRequestDTO.isNoShowFlag() is primitive boolean; autobox to Boolean
        tripResultDTO.setIsNoShowFlag(tripResultRequestDTO.isNoShowFlag());

        if (tripResultRequestDTO.getFareCollected() != tempDecimal) {
            tripResultDTO.setFareCollected(tripResultRequestDTO.getFareCollected());
        }
        //Attendants
        if (tripResultRequestDTO.getNumberOfAttendants() != 0) {
            tripResultDTO.setNumberOfAttendants(tripResultRequestDTO.getNumberOfAttendants());
        } else {
            tripResultDTO.setNumberOfAttendants(tripTicket.getAttendants());
        }
        //NumberOfGuests
        if (tripResultRequestDTO.getNumberOfGuests() != 0) {
            tripResultDTO.setNumberOfGuests(tripResultRequestDTO.getNumberOfGuests());
        } else {
            tripResultDTO.setNumberOfGuests(tripTicket.getGuests());
        }
        //getNumberOfPassengers
        if (tripResultRequestDTO.getNumberOfPassengers() != 0) {
            tripResultDTO.setNumberOfPassengers(tripResultRequestDTO.getNumberOfPassengers());
        }
        //ActualPickupArriveTime
        if (tripResultRequestDTO.getActualPickupArriveTime() != null && tripResultRequestDTO.getActualPickupArriveTime() != "") {
            try {
                String timeStr = extractHhMmSs(tripResultRequestDTO.getActualPickupArriveTime());
                tripResultDTO.setActualPickupArriveTime(Time.valueOf(timeStr));
            } catch (IllegalArgumentException e) {
                messageList.add("ActualPickupArriveTime: must be in Format of HH:mm:ss");
            }
        }
        //ActualPickupDepartTime
        if (tripResultRequestDTO.getActualPickupDepartTime() != null && tripResultRequestDTO.getActualPickupDepartTime() != "") {
            try {
                String timeStr = extractHhMmSs(tripResultRequestDTO.getActualPickupDepartTime());
                tripResultDTO.setActualPickupDepartTime(Time.valueOf(timeStr));
            } catch (IllegalArgumentException e) {
                messageList.add("ActualPickupDepartTime: must be in Format of HH:mm:ss");
            }
        }
        //ActualDropOffArriveTime
        if (tripResultRequestDTO.getActualDropOffArriveTime() != null && tripResultRequestDTO.getActualDropOffArriveTime() != "") {
            try {
                String timeStr = extractHhMmSs(tripResultRequestDTO.getActualDropOffArriveTime());
                tripResultDTO.setActualDropOffArriveTime(Time.valueOf(timeStr));
            } catch (IllegalArgumentException e) {
                messageList.add("ActualDropOffArriveTime: must be in Format of HH:mm:ss");
            }
        }
        //ActualDropOffDepartTime
        if (tripResultRequestDTO.getActualDropOffDepartTime() != null && tripResultRequestDTO.getActualDropOffDepartTime() != "") {
            try {
                String timeStr = extractHhMmSs(tripResultRequestDTO.getActualDropOffDepartTime());
                tripResultDTO.setActualDropOffDepartTime(Time.valueOf(timeStr));
            } catch (IllegalArgumentException e) {
                messageList.add("ActualDropOffDepartTime: must be in Format of HH:mm:ss");
            }
        }
        //TripDate
        if (tripResultRequestDTO.getTripDate() != null && tripResultRequestDTO.getTripDate() != "") {
            // try parsing ISO date/time first (e.g., 2025-10-08T00:00:00 or 2025-10-08)
            Date parsed = null;
            try {
                parsed = parseFlexibleDate(tripResultRequestDTO.getTripDate());
            } catch (Exception e) {
                // ignore and fallback to existing checks below
            }
            if (parsed != null) {
                tripResultDTO.setTripDate(parsed);
            } else {
                String raw = tripResultRequestDTO.getTripDate().trim();
                // normalize inputs that may start with a stray 't' (e.g. "t 2016-10-30 08:12:00.0")
                if (raw.startsWith("t") || raw.startsWith("T")) {
                    raw = raw.replaceFirst("^[tT]\\s*", "");
                }

                // try a set of common patterns (order matters: more specific first)
                String[] patterns = new String[]{
                        "yyyy-MM-dd HH:mm:ss.SSS",
                        "yyyy-MM-dd HH:mm:ss.S",
                        "yyyy-MM-dd HH:mm:ss",
                        "yyyy/MM/dd HH:mm:ss.SSS",
                        "yyyy/MM/dd HH:mm:ss.S",
                        "yyyy/MM/dd HH:mm:ss",
                        "yyyy-MM-dd",
                        "yyyy/MM/dd",
                        "MM-dd-yyyy HH:mm:ss.SSS",
                        "MM-dd-yyyy HH:mm:ss.S",
                        "MM-dd-yyyy HH:mm:ss",
                        "MM-dd-yyyy HH:mm",
                        "MM-dd-yyyy",
                        "MM-dd-yy",
                        "MM/dd/yyyy HH:mm:ss.SSS",
                        "MM/dd/yyyy HH:mm:ss.S",
                        "MM/dd/yyyy HH:mm:ss",
                        "MM/dd/yyyy HH:mm",
                        "MM/dd/yyyy",
                        "MM/dd/yy"
                };

                boolean success = false;
                for (String pattern : patterns) {
                    try {
                        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
                        formatter.setLenient(false);
                        // If the pattern uses two-digit year ("yy" but not "yyyy"), force the 100-year window to start at 2000
                        if (pattern.contains("yy") && !pattern.contains("yyyy")) {
                            formatter.set2DigitYearStart(new GregorianCalendar(2000, Calendar.JANUARY, 1).getTime());
                        }
                        Date d = formatter.parse(raw);
                        if ( ConvertRequestToTripTicketDTOService.isWithinDays(d, 120) ) {
                            tripResultDTO.setTripDate(d);
                            success = true;
                            break;
                        }
                    } catch (ParseException e) {
                        // try next
                    }
                }

                if (!success) {
                    // previous behavior attempted to guess between dash and slash patterns; keep helpful messages
                    if (tripResultRequestDTO.getTripDate().contains("-")) {
                        messageList.add("TripDate: Unrecognized format. Allowed examples: yyyy-MM-dd HH:mm:ss.S (e.g. 2016-10-30 08:12:00.0) or MM-dd-yyyy or MM-dd-yyyy HH:mm");
                    } else if (tripResultRequestDTO.getTripDate().contains("/")) {
                        messageList.add("TripDate: Unrecognized format. Allowed examples: yyyy/MM/dd HH:mm:ss.S (e.g. 2016/10/30 08:12:00.0) or MM/dd/yyyy or MM/dd/yyyy HH:mm");
                    } else {
                        messageList.add("TripDate: Unrecognized format. Allowed examples: yyyy-MM-dd, yyyy-MM-dd HH:mm:ss.S, MM-dd-yyyy, MM-dd-yyyy HH:mm, MM/dd/yyyy HH:mm");
                    }
                }
            }
        } else {
            messageList.add("TripDate: Must not be null or empty");
        }


        if (!messageList.isEmpty()) {
            throw new InvalidInputCheckException(messageList, 0);
        }
        return tripResultDTO;
    }

    /**
     * Extract first occurrence of HH:mm:ss or HH:mm from a string. If string already is in HH:mm:ss form, it is returned (normalized).
     * If HH:mm is found, seconds ":00" are appended. Fields are zero-padded to two digits.
     */
    private String extractHhMmSs(String s) {
        if (s == null) return s;
        s = s.trim();
        // If the input contains a 'T' try parsing as ISO datetime first (handles offsets, Z, etc.)
        if (s.contains("T")) {
            try {
                OffsetDateTime odt = OffsetDateTime.parse(s);
                return odt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            } catch (DateTimeParseException ex) {
                try {
                    LocalDateTime ldt = LocalDateTime.parse(s);
                    return ldt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                } catch (DateTimeParseException ex2) {
                    // fall through to regex-based extraction below
                }
            }
        }
        // try to find H:mm:ss or HH:mm:ss where minutes/seconds may be 1-2 digits; normalize to 2 digits
        // Use lookarounds to avoid requiring word boundaries; this lets times embedded after a 'T' or other
        // non-digit characters be matched (e.g. "2025-10-18T08:40:00"). Also ensure we are not matching
        // parts of longer digit sequences by asserting not preceded or followed by a digit.
        Pattern pFull = Pattern.compile("(?<!\\d)(\\d{1,2}:\\d{1,2}:\\d{1,2})(?!\\d)");
        Matcher mFull = pFull.matcher(s);
         if (mFull.find()) {
             String t = mFull.group(1);
             String[] parts = t.split(":");
             try {
                 int h = Integer.parseInt(parts[0]);
                 int m = Integer.parseInt(parts[1]);
                 int sec = Integer.parseInt(parts[2]);
                 return String.format("%02d:%02d:%02d", h, m, sec);
             } catch (NumberFormatException ex) {
                 // fallback to the raw match if parsing fails
                 return t;
             }
         }
         // try to find H:mm or HH:mm where minutes may be 1-2 digits and append :00
         Pattern pShort = Pattern.compile("(?<!\\d)(\\d{1,2}:\\d{1,2})(?!\\d)");
         Matcher mShort = pShort.matcher(s);
         if (mShort.find()) {
             String t = mShort.group(1);
             String[] parts = t.split(":");
             try {
                 int h = Integer.parseInt(parts[0]);
                 int m = Integer.parseInt(parts[1]);
                 return String.format("%02d:%02d:00", h, m);
             } catch (NumberFormatException ex) {
                 // fallback to append :00 if parsing fails
                 return t + ":00";
             }
         }
         return s;
     }

    /**
     * Parse flexible ISO date/time formats into java.util.Date. Accepts:
     * - yyyy-MM-dd
     * - yyyy-MM-dd'T'HH:mm:ss
     * - ISO_OFFSET_DATE_TIME
     */
    private Date parseFlexibleDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            if (s.contains("T")) {
                // try OffsetDateTime (with timezone)
                try {
                    OffsetDateTime odt = OffsetDateTime.parse(s);
                    return Date.from(odt.toInstant());
                } catch (DateTimeParseException ex) {
                    // try LocalDateTime
                    LocalDateTime ldt = LocalDateTime.parse(s);
                    return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
                }
            }
            // date only
            if (s.matches("\\d{4}-\\d{2}-\\d{2}")) {
                LocalDate ld = LocalDate.parse(s);
                return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
        } catch (DateTimeParseException e) {
            return null;
        }
        return null;
    }

     /// ///////////////////////////////////////////////////////////////


     public List<TripResultDTO> createCompletedTripResultList(List<TripResultRequestDTO> tripResultDTO) {

         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         User user = ((User) auth.getDetails());
         log.debug("user&&&&&==" + user.toString());
         Set<Integer> providersList = new HashSet<Integer>();

         User originalUser = userDAO.findUserByUserId(user.getId());
         int providerId;
         try {
             providerId = originalUser.getProvider().getProviderId();
         } catch (Exception e) {
             throw new InvalidInputException("api_key: Invalid api key");

         }


         List<TripResultDTO> tripResultDTOList = new ArrayList<TripResultDTO>();
         for (TripResultRequestDTO requestDTO : tripResultDTO) {
             TripResultDTO convertRequestedDTO = convertStringToActualDataType(requestDTO);

             TripResult tripResult = (TripResult) toBO(convertRequestedDTO);

             TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(tripResult.getTripTicket().getId());
             /*
             if (!(tripTicket.getStatus().getStatusId() == TripTicketStatusConstants.approved.tripTicketStatusUpdate() || (tripTicket.getStatus().getStatusId() == TripTicketStatusConstants.completed.tripTicketStatusUpdate()))) {
                 throw new HandlingExceptionForOKStatus("Tripticket claim is not yet approved");
             }*/

             //update or create tripResult
             if (tripResult.getTripTicket().getId() != 0) {
                 TripResult foundTripResult = tripResultDAO.findTripResultByTripTicketId(tripResult.getTripTicket().getId());
                 if (foundTripResult != null) {
                     tripResult.setId(foundTripResult.getId());
                     tripResult = tripResultDAO.updateTripResult(tripResult);
                 } else {
                     tripResult = tripResultDAO.createTripResult(tripResult);
                 }
             } else {
                 tripResult = tripResultDAO.createTripResult(tripResult);
             }

             providersList.add(tripTicket.getOriginProvider().getProviderId());

             Status status = new Status();
             status.setStatusId(TripTicketStatusConstants.completed.tripTicketStatusUpdate());
             tripTicket.setStatus(status);
             tripTicketDAO.updateTripTicket(tripTicket);
             convertRequestedDTO = (TripResultDTO) toDTO(tripResult);

             convertRequestedDTO.setPickUpAddress(tripTicket.getPickupAddress().getStreet1() + " " + tripTicket.getPickupAddress().getCity() + " " + tripTicket.getPickupAddress().getState() + " " + tripTicket.getPickupAddress().getZipcode());
             convertRequestedDTO.setDropOffAddress(tripTicket.getDropOffAddress().getStreet1() + " " + tripTicket.getDropOffAddress().getCity() + " " + tripTicket.getDropOffAddress().getState() + " " + tripTicket.getDropOffAddress().getZipcode());

             if ( tripTicket.getApprovedTripClaim() != null ) {
                 convertRequestedDTO.setClaimantProvider(tripTicket.getApprovedTripClaim().getClaimantProvider().getProviderName());
             }
             if ( tripTicket.getRequestedPickupTime() != null ) {
                 convertRequestedDTO.setScheduledPickupTime(tripTicket.getRequestedPickupTime().toString());
             }
             if ( tripTicket.getRequestedDropOffTime() != null ) {
                 convertRequestedDTO.setScheduledDropOffTime(tripTicket.getRequestedDropOffTime().toString());
             }
             convertRequestedDTO.setOrgProviderId(tripTicket.getOriginProvider().getProviderId());
             tripResultDTOList.add(convertRequestedDTO);
         }

         for (Integer orgId : providersList) {
             List<User> usersOfOriginator = userNotificationDataDAO.getUsersOfProvider(orgId);
             for (User user1 : usersOfOriginator) {
                 //fetching single obj of the user role
                 List<UserAuthority> userAuthority = new ArrayList<>();

                 userAuthority.addAll(user1.getAuthorities());
                 String userrole = userAuthority.get(0).getAuthority();
                 if (userrole.equalsIgnoreCase("ROLE_PROVIDERADMIN") || userrole.equalsIgnoreCase("ROLE_PROVIDERUSER")) {

                     //NotificationEnginePart.. for trip ticket originator
                     Notification emailNotificationForOriginator = new Notification();
                     NotificationTemplate notificationTemplateForOriginator = new NotificationTemplate();

                     emailNotificationForOriginator.setIsEMail(true);
                     emailNotificationForOriginator.setStatusId(NotificationStatus.newStatus.status());
                     notificationTemplateForOriginator.setNotificationTemplateId(NotificationTemplateCodeValue.tripResultCompleted.templateCodeValue());
                     emailNotificationForOriginator.setNotificationTemplate(notificationTemplateForOriginator);
                     emailNotificationForOriginator.setNumberOfAttempts(0);
                     emailNotificationForOriginator.setIsActive(true);
                     emailNotificationForOriginator.setEmailTo(user1.getEmail());
                     List<TripResultDTO> tripResultDTOs = new ArrayList<TripResultDTO>();
                     for (TripResultDTO tripResultDTO1 : tripResultDTOList) {
                         if (tripResultDTO1.getOrgProviderId() == orgId) {
                             tripResultDTOs.add(tripResultDTO1);
                         }
                     }
                     String filepath = fileGenerateService.createCSVForTripCompletedResultData(tripResultDTOs,
                             emailNotificationForOriginator);
                     emailNotificationForOriginator.setEmailAttachment(true);
                     List<String> filePathList = new ArrayList<String>();
                     List<String> fileNameList = new ArrayList<String>();
                     filePathList.add(filepath);
                     fileNameList.add("CompletedTripTickets");
                     //emailNotificationForOriginator.setFilePathList(filePathList);
                     //emailNotificationForOriginator.setFileNameList(fileNameList);
                     //   Setting parameter values in according to the template for email notofication.
                     Map tripResultCompletedTemplateMap = new HashMap<String, String>();
                     tripResultCompletedTemplateMap.put("name", originalUser.getProvider().getProviderName());
                     tripResultCompletedTemplateMap.put("message",
                             "The Trip Exchange now has complete trip data from the claimants of your trip tickets. Please login to the Trip Exchange to download your data.");
                     tripResultCompletedTemplateMap.put("year", Year.now().toString());

                     String jsonValueOfTemplate = "";

                     Iterator<Map.Entry<String, String>> entries = tripResultCompletedTemplateMap.entrySet().iterator();
                     while (entries.hasNext()) {

                         Map.Entry<String, String> entry = entries.next();
                         jsonValueOfTemplate = jsonValueOfTemplate + "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
                         if (entries.hasNext()) {
                             jsonValueOfTemplate = jsonValueOfTemplate + ",";
                         }

                     }
                     String FinaljsonValueOfTemplateForTicketOriginator = "{" + jsonValueOfTemplate + "}";

                     emailNotificationForOriginator.setParameterValues(FinaljsonValueOfTemplateForTicketOriginator);
                     emailNotificationForOriginator.setSubject("Completed Trip Data for Your Trip Tickets");
                     notificationDAO.createNotification(emailNotificationForOriginator);
                 }
             }
         }
         return tripResultDTOList;
     }


}
