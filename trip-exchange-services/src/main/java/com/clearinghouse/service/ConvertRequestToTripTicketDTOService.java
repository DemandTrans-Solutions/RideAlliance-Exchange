/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;


import com.clearinghouse.dao.AddressDAO;
import com.clearinghouse.dao.CustomerDAO;
import com.clearinghouse.dao.TripTicketDAO;
import com.clearinghouse.dao.UserDAO;
import com.clearinghouse.dto.*;
import com.clearinghouse.entity.Address;
import com.clearinghouse.entity.Customer;
import com.clearinghouse.entity.User;
import com.clearinghouse.enumentity.CustomerStatusConstants;
import com.clearinghouse.enumentity.TripTicketStatusConstants;
import com.clearinghouse.exceptions.InvalidInputCheckException;
import com.clearinghouse.exceptions.InvalidInputException;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 *
 * @author chaitanyaP
 */
@Service
@Transactional
@Slf4j
public class ConvertRequestToTripTicketDTOService {

    private final String syncTimezone;
    private final ZoneId syncZoneId;

    private final UserDAO userDAO;

    private final AddressDAO addressDAO;

    private final AddressService addressService;

    private final TripTicketDistanceService tripTicketDistanceService;

    private final CustomerDAO customerDAO;

    private final TripTicketDAO tripTicketDAO;

    public ConvertRequestToTripTicketDTOService(
            @Value("${timezone.syncAPI}") String syncTimezone,
            UserDAO userDAO,
            AddressDAO addressDAO,
            AddressService addressService,
            TripTicketDistanceService tripTicketDistanceService,
            CustomerDAO customerDAO,
            TripTicketDAO tripTicketDAO) {
        this.syncTimezone = syncTimezone;
        this.syncZoneId = ZoneId.of(syncTimezone);
        this.userDAO = userDAO;
        this.addressDAO = addressDAO;
        this.addressService = addressService;
        this.tripTicketDistanceService = tripTicketDistanceService;
        this.customerDAO = customerDAO;
        this.tripTicketDAO = tripTicketDAO;
    }


    public User fromJSON(byte[] userBytes) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            User user = mapper.readValue(new ByteArrayInputStream(userBytes), User.class);
            return user;
        } catch (IOException e) {
            log.error("Error deserializing User from JSON: {}", e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    // Helper result class and parser moved to class scope to avoid duplicate code
    private static class DateTimeParseResult {
        boolean present;
        LocalDate date;
        Time time;
    }

    /**
     * Helper method to create SimpleDateFormat with the syncAPI timezone.
     * Input times are interpreted as being in the syncAPI timezone (America/Denver).
     * This must match the timezone used in @JsonFormat annotations in TripTicketDTO.
     */
    private SimpleDateFormat createDateFormat(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(TimeZone.getTimeZone(syncTimezone));
        return sdf;
    }

    // New helper: returns true if the given date is within +/- days of today
    public static boolean isWithinDays(Date date, int days) {
        if (date == null) {
            return false;
        }
        LocalDate localDate = Instant.ofEpochMilli(date.getTime())
                .atZone(TimeZone.getDefault().toZoneId())
                .toLocalDate();
        return isWithinDays(localDate, days);
    }

    public static boolean isWithinDays(LocalDate date, int days) {
        if (date == null) {
            return false;
        }
        LocalDate now = LocalDate.now(TimeZone.getDefault().toZoneId());
        LocalDate min = now.minusDays(days);
        LocalDate max = now.plusDays(days);
        return !date.isBefore(min) && !date.isAfter(max);
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(syncZoneId).toLocalDate();
    }

    private LocalDate tryParseTripDate(String tripDateStr, boolean rangeCheck) {
        if (tripDateStr == null || tripDateStr.trim().isEmpty()) {
            return null;
        }
        List<String> dateFormats = Arrays.asList(
                "MM/dd/yy",
                "yyyy-MM-dd",
                "yyyy/MM/dd",
                "MM/dd/yyyy",
                "yyyy-MM-dd HH:mm:ss.S",
                "yyyy/MM/dd HH:mm:ss.S");

        for (String df : dateFormats) {
            try {
                SimpleDateFormat sdf = createDateFormat(df);
                sdf.setLenient(false);
                Date candidate = sdf.parse(tripDateStr);
                if (df.equals("MM/dd/yy")) {
                    String[] parts = tripDateStr.split("/");
                    if (parts.length == 3) {
                        try {
                            int yy = Integer.parseInt(parts[2]);
                            int year = yy > 30 ? 1900 + yy : 2000 + yy;
                            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(syncTimezone));
                            cal.setTime(candidate);
                            cal.set(Calendar.YEAR, year);
                            candidate = cal.getTime();
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                LocalDate candidateLocalDate = toLocalDate(candidate);
                if (!rangeCheck || isWithinDays(candidateLocalDate, 180)) {
                    return candidateLocalDate;
                }
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private DateTimeParseResult parseRequestedDateTime(String req, String tripDateStr, String fieldName, List<String> messageList, boolean rangeCheck) {
        DateTimeParseResult result = new DateTimeParseResult();
        result.present = false;

        try {
            if (req != null && req.contains("T")) {
                String[] datetime = req.split("T");
                String date = datetime[0];
                String timeWithZone = datetime[1];
                String timeWithoutZone = timeWithZone.split("-")[0];

                SimpleDateFormat formatter = createDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date dateTimePart = formatter.parse(date + "T" + timeWithoutZone);
                LocalDate parsedDate = toLocalDate(dateTimePart);

                if (rangeCheck && !isWithinDays(parsedDate, 180)) {
                    messageList.add(fieldName + ": Date must be within +/-180 days of today");
                    result.present = false;
                } else {
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(syncTimezone));
                    cal.setTime(dateTimePart);
                    String timeStr = String.format("%02d:%02d:%02d",
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            cal.get(Calendar.SECOND));

                    result.date = parsedDate;
                    result.time = Time.valueOf(timeStr);
                    log.info("[DATE-PARSE-ISO] fieldName={}, input={}, parsed date={}", fieldName, req, parsedDate);
                    result.present = true;
                }
            } else if (req != null && req.matches("^\\d{1,2}:\\d{2}(:\\d{2})?$")) {
                String timeOnly = req.matches("^\\d{1,2}:\\d{2}$") ? req + ":00" : req;

                if (tripDateStr == null || tripDateStr.trim().isEmpty()) {
                    messageList.add("TripDate: Must be provided when " + fieldName + " is in HH:mm format");
                } else {
                    LocalDate parsedDate = tryParseTripDate(tripDateStr, rangeCheck);
                    if (parsedDate == null) {
                        messageList.add("TripDate: Must be in a valid format (e.g. MM/dd/yy, yyyy-MM-dd, yyyy/MM/dd, MM/dd/yyyy) and within +/-180 days of today");
                    } else {
                        result.date = parsedDate;
                        result.time = Time.valueOf(timeOnly);
                        log.info("[DATE-PARSE-TIME-ONLY] fieldName={}, time={}, tripDate={}, parsed date={}",
                                fieldName, req, tripDateStr, parsedDate);
                        result.present = true;
                    }
                }
            } else if (req == null) {
                if (tripDateStr == null || tripDateStr.trim().isEmpty()) {
                    messageList.add(fieldName + ": TripDate must be provided when req is null");
                } else {
                    LocalDate parsedDate = tryParseTripDate(tripDateStr, rangeCheck);
                    if (parsedDate == null) {
                        messageList.add("TripDate: Must be in a valid format (e.g. MM/dd/yy, yyyy-MM-dd, yyyy/MM/dd, MM/dd/yyyy) and within +/-180 days of today");
                    } else {
                        result.date = parsedDate;
                        result.time = Time.valueOf("00:00:00");
                        log.info("[DATE-PARSE-DATE-ONLY] fieldName={}, tripDate={}, parsed date={}",
                                fieldName, tripDateStr, parsedDate);
                        result.present = true;
                    }
                }
            } else {
                messageList.add(fieldName + ": Must be in Format yyyy-MM-ddTHH:mm:ss-Z, time only HH:mm, or null with tripDateStr");
            }
        } catch (Exception e) {
            log.error("Error parsing {} in convertRequestJsonToTicketDTO: {}", fieldName, e.getMessage(), e);
            messageList.add(fieldName + ": Must be in Format yyyy-MM-ddTHH:mm:ss-Z, time only HH:mm, or null with tripDateStr");
            result.present = false;
        }

        return result;
    }
    public Map<String, Object> convertRequestJsonToTicketDTO(TripTicketRequestDTO tripTicketRequestDTO,
                                                             BindingResult bindingResult) {
        List<String> messageList = new ArrayList<>();
        /* fetching fields from map and assign to the DTO fields */

        TripTicketDTO tripTicketDTO = new TripTicketDTO();
        AddressDTO customerAddressDTO = new AddressDTO();
        AddressDTO pickUpAddressDTO = new AddressDTO();
        AddressDTO dropOffAddressDTO = new AddressDTO();

        /* code for decrpypting token and taking user obj from it */
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = ((User) auth.getDetails());
        log.debug("user&&&&&==" + user.toString());

        /*
         * String token = api_key; String[] tokenArray = token.split("\\."); String
         * userPart = tokenArray[0]; byte[] originalUserPart =
         * Base64.getDecoder().decode(userPart);
         *
         * User user = fromJSON(originalUserPart);
         */
        Integer originProviderId = null;
        if ( tripTicketRequestDTO.getOriginProviderId() != null ) {
            originProviderId = tripTicketRequestDTO.getOriginProviderId();
            tripTicketDTO.setOriginProviderId(tripTicketRequestDTO.getOriginProviderId());
        } else {
            User originalUser = userDAO.findUserByUserId(user.getId());
            try {
                tripTicketDTO.setOriginProviderId(originalUser.getProvider().getProviderId());
                originProviderId = originalUser.getProvider().getProviderId();
            } catch (Exception e) {
                log.error("Error setting originProviderId in convertRequestJsonToTicketDTO: {}", e.getMessage(), e);
                messageList.add("api_key: Invalid api key");
            }
        }

        /* TripTicketId */
        if (tripTicketRequestDTO.getTripTicketId() != null) {
            Integer tripId = tripTicketRequestDTO.getTripTicketId();
            tripTicketDTO.setId(tripId);

        } else if ( tripTicketRequestDTO.getOriginTripId() != null ) {
            var tripTicket = tripTicketDAO.findByOriginTripId(tripTicketRequestDTO.getOriginTripId());
            if ( tripTicket != null ) {
                tripTicketDTO.setId(tripTicket.getId());
            }
        } else {
            tripTicketDTO.setId(0);
        }

        tripTicketDTO.setVehicleType(tripTicketRequestDTO.getVehicleType());

        /* appointmentTime */
        if (tripTicketRequestDTO.getAppointmentTime() != null && !Objects.equals(tripTicketRequestDTO.getAppointmentTime(), "")) {// &&
            // !appointmentTime.isEmpty())
            // {
            /* seperaing date and time */
            String appointmentTime = tripTicketRequestDTO.getAppointmentTime();
            String timeWithZone = appointmentTime.split("T")[1];

            String timeWithoutZone = timeWithZone.split("-")[0];
            /* setting time value by converting string to time */
            tripTicketDTO.setAppointmentTime(Time.valueOf(timeWithoutZone));
        }

        if ( tripTicketRequestDTO.getCustomerAddress() != null ) {
            /* customer address street 1... */

            if (tripTicketRequestDTO.getCustomerAddress().getStreet1() != null
                    && !Objects.equals(tripTicketRequestDTO.getCustomerAddress().getStreet1(), "")) {
                customerAddressDTO.setStreet1(tripTicketRequestDTO.getCustomerAddress().getStreet1());
            } else {
                log.warn("CustomerAddress.Street1: Must not be null or empty for " + tripTicketRequestDTO.toString() + " trip ticket" );
                //messageList.add("CustomerAddress.Street1: Must not be null or empty");
            }

            /* customer address street 2... */

            if (tripTicketRequestDTO.getCustomerAddress().getStreet2() != null
                    && !Objects.equals(tripTicketRequestDTO.getCustomerAddress().getStreet2(), "")) {
                customerAddressDTO.setStreet2(tripTicketRequestDTO.getCustomerAddress().getStreet2());
            }

            /* customer address address_type... */

            if (tripTicketRequestDTO.getCustomerAddress().getAddressType() != null
                    && !Objects.equals(tripTicketRequestDTO.getCustomerAddress().getAddressType(), "")) {
                customerAddressDTO.setAddressType(tripTicketRequestDTO.getCustomerAddress().getAddressType());
            }

            /* customer address city... */

            if (tripTicketRequestDTO.getCustomerAddress().getCity() != null
                    && !Objects.equals(tripTicketRequestDTO.getCustomerAddress().getCity(), "")) {
                customerAddressDTO.setCity(tripTicketRequestDTO.getCustomerAddress().getCity());
            } else {
                //messageList.add("CustomerAddress.City: Must not be null or empty");
                log.warn("CustomerAddress.City: Must not be null or empty for " + tripTicketRequestDTO.toString() + " trip ticket" );
            }

            /* customer address comman name... */

            if (tripTicketRequestDTO.getCustomerAddress().getCommonName() != null
                    && !Objects.equals(tripTicketRequestDTO.getCustomerAddress().getCommonName(), "")) {
                customerAddressDTO.setCommonName(tripTicketRequestDTO.getCustomerAddress().getCommonName());
            }

            /* customer address phone no... */
            if (tripTicketRequestDTO.getCustomerAddress().getPhoneNumber() != null
                    && !Objects.equals(tripTicketRequestDTO.getCustomerAddress().getPhoneNumber(), "")) {
                customerAddressDTO.setPhoneNumber(tripTicketRequestDTO.getCustomerAddress().getPhoneNumber());
            }

            /* customer address state... */

            if (tripTicketRequestDTO.getCustomerAddress().getState() != null
                    && !Objects.equals(tripTicketRequestDTO.getCustomerAddress().getState(), "")) {
                customerAddressDTO.setState(tripTicketRequestDTO.getCustomerAddress().getState());
            } else {
                //messageList.add("CustomerAddress.State: Must not be null or empty");
                log.warn("CustomerAddress.State: Must not be null or empty for " + tripTicketRequestDTO.toString() + " trip ticket" );

            }

            /* customer address ZIP... */
            if (tripTicketRequestDTO.getCustomerAddress().getZipcode() != null
                    && !Objects.equals(tripTicketRequestDTO.getCustomerAddress().getZipcode(), "")) {
                customerAddressDTO.setZipcode(tripTicketRequestDTO.getCustomerAddress().getZipcode());

            } else {
                //messageList.add("CustomerAddress.ZipCode: Must not be null or empty");
                log.warn("CustomerAddress.ZipCode: Must not be null or empty for " + tripTicketRequestDTO.toString() + " trip ticket" );
            }

            /* customer address latitude... */
            if (tripTicketRequestDTO.getCustomerAddress().getLatitude() != 0) {
                customerAddressDTO.setLatitude(tripTicketRequestDTO.getCustomerAddress().getLatitude());
            }

            /* customer address longitude... */

            if (tripTicketRequestDTO.getCustomerAddress().getLongitude() != 0) {
                customerAddressDTO.setLongitude(tripTicketRequestDTO.getCustomerAddress().getLongitude());
            }

            /* customer address county... */
            if (tripTicketRequestDTO.getCustomerAddress().getCounty() != null
                    && tripTicketRequestDTO.getCustomerAddress().getCounty() != "") {

                customerAddressDTO.setCounty(tripTicketRequestDTO.getCustomerAddress().getCounty());
            }
            tripTicketDTO.setCustomerAddress(customerAddressDTO);
        }


        /* customer eligiblity factors... */
        if (tripTicketRequestDTO.getCustomerEligibilityFactors() != null
                && tripTicketRequestDTO.getCustomerEligibilityFactors() != "") {
            tripTicketDTO.setCustomerEligibilityFactors(tripTicketRequestDTO.getCustomerEligibilityFactors());
        } else {
            tripTicketDTO.setCustomerEligibilityFactors(null);
        }

        /* customer race... */
        if (tripTicketRequestDTO.getCustomerRace() != null && !Objects.equals(tripTicketRequestDTO.getCustomerRace(), "")) {
            tripTicketDTO.setCustomerRace(tripTicketRequestDTO.getCustomerRace());
        }

        /* customer DOB... */
        if (tripTicketRequestDTO.getCustomerDOB() != null && !Objects.equals(tripTicketRequestDTO.getCustomerDOB(), "")) {
            var parsedDate = parseRequestedDateTime(null, tripTicketRequestDTO.getCustomerDOB(), "customerDOB", messageList, false);
            if ( parsedDate.present ) {
                tripTicketDTO.setCustomerDob(parsedDate.date);
            }
        }

        /* customer emergency contact no... */
        if (tripTicketRequestDTO.getCustomerEmergencyPhone() != null
                && !Objects.equals(tripTicketRequestDTO.getCustomerEmergencyPhone(), "")) {
            tripTicketDTO.setCustomerEmergencyPhone(tripTicketRequestDTO.getCustomerEmergencyPhone());
        }

        /* customer first name... */
        if (tripTicketRequestDTO.getCustomerFirstName() != null && !Objects.equals(tripTicketRequestDTO.getCustomerFirstName(), "")) {
            tripTicketDTO.setCustomerFirstName(tripTicketRequestDTO.getCustomerFirstName());
        } else {
            messageList.add("CustomerFirstName: Must not be null or empty");
        }

        /* customer last name... */
        if (tripTicketRequestDTO.getCustomerLastName() != null && !Objects.equals(tripTicketRequestDTO.getCustomerLastName(), "")) {
            tripTicketDTO.setCustomerLastName(tripTicketRequestDTO.getCustomerLastName());
        } else {
            messageList.add("CustomerLastName: Must not be null or empty");
        }

        /* customer middle name... */
        if (tripTicketRequestDTO.getCustomerMiddleName() != null) {
            tripTicketDTO.setCustomerMiddleName(tripTicketRequestDTO.getCustomerMiddleName());
        }

        /* customer gender... */
        if (tripTicketRequestDTO.getCustomerGender() != null && tripTicketRequestDTO.getCustomerGender() != "") {
            tripTicketDTO.setCustomerGender(tripTicketRequestDTO.getCustomerGender());
        }

        tripTicketDTO.setCustomerPovertyLevel(tripTicketRequestDTO.getCustomerPovertyLevel());
        tripTicketDTO.setCustomerDisability(tripTicketRequestDTO.getCustomerDisability());
        tripTicketDTO.setCustomerRace(tripTicketRequestDTO.getCustomerRace());
        tripTicketDTO.setCustomerEthnicity(tripTicketRequestDTO.getCustomerEthnicity());
        tripTicketDTO.setCustomerEmail(tripTicketRequestDTO.getCustomerEmail());
        tripTicketDTO.setCustomerVeteran(tripTicketRequestDTO.getCustomerVeteran());
        tripTicketDTO.setCustomerHomePhone(tripTicketRequestDTO.getCustomerHomePhone());
        tripTicketDTO.setCustomerMobilePhone(tripTicketRequestDTO.getCustomerMobilePhone());
        tripTicketDTO.setCustomerMailingBillingAddress(tripTicketRequestDTO.getCustomerMailingBillingAddress());
        tripTicketDTO.setCustomerCaregiverName(tripTicketRequestDTO.getCustomerCaregiverName());
        tripTicketDTO.setCustomerCareInfo(tripTicketRequestDTO.getCustomerCareInfo());
        tripTicketDTO.setCustomerCaregiverContactInfo(tripTicketRequestDTO.getCustomerCaregiverContactInfo());
        tripTicketDTO.setCustomerEmergencyContactName(tripTicketRequestDTO.getCustomerEmergencyContactName());
        tripTicketDTO.setCustomerEmergencyContactRelationship(tripTicketRequestDTO.getCustomerEmergencyContactRelationship());
        tripTicketDTO.setCustomerEmergencyContactPhone(tripTicketRequestDTO.getCustomerEmergencyContactPhone());
        tripTicketDTO.setCustomerCareInfo(tripTicketRequestDTO.getCustomerCareInfo());
        tripTicketDTO.setCustomerFundingBillingInformation(tripTicketRequestDTO.getCustomerFundingBillingInformation());
        tripTicketDTO.setFundingType(tripTicketRequestDTO.getFundingType());
        tripTicketDTO.setTripNotes(tripTicketRequestDTO.getTripNotes());
        tripTicketDTO.setGuests(tripTicketRequestDTO.getNumGuests());
        tripTicketDTO.setAttendants(tripTicketRequestDTO.getNumAttendants());


        /* customer identifier... */
        if (tripTicketRequestDTO.getCustomerIdentifiers() != null
                && !Objects.equals(tripTicketRequestDTO.getCustomerIdentifiers(), "")) {
            tripTicketDTO.setCustomerIdentifiers(tripTicketRequestDTO.getCustomerIdentifiers());
        }

        /* customer informaion withheld... */
        boolean isCustomerInformationWithheld = tripTicketRequestDTO.isCustomerInformationWithheld();
        tripTicketDTO.setCustomerInformationWithheld(isCustomerInformationWithheld);


        /* customer mobility factors... */
        if (tripTicketRequestDTO.getCustomerMobilityFactors() != null
                || !Objects.equals(tripTicketRequestDTO.getCustomerMobilityFactors(), "")) {
            tripTicketDTO.setCustomerMobilityFactors(tripTicketRequestDTO.getCustomerMobilityFactors());
        }

        /* customer notes... */
        if (tripTicketRequestDTO.getCustomerNotes() != null && !Objects.equals(tripTicketRequestDTO.getCustomerNotes(), "")) {
            tripTicketDTO.setCustomerNotes(tripTicketRequestDTO.getCustomerNotes());
        }

        /* customer primary language... */
        if (tripTicketRequestDTO.getCustomerPrimaryLanguage() != null
                && !Objects.equals(tripTicketRequestDTO.getCustomerPrimaryLanguage(), "")) {
            tripTicketDTO.setPrimaryLanguage(tripTicketRequestDTO.getCustomerPrimaryLanguage());
        }

        /* customer primary phone... */
        if (tripTicketRequestDTO.getCustomerHomePhone() != null
                && !Objects.equals(tripTicketRequestDTO.getCustomerHomePhone(), "")) {
            tripTicketDTO.setCustomerHomePhone(tripTicketRequestDTO.getCustomerHomePhone());
        } else {
            if ( tripTicketDTO.getCustomerMobilePhone() != null ) {
                tripTicketDTO.setCustomerHomePhone(tripTicketDTO.getCustomerMobilePhone());
            } else {
                log.warn("CustomerHomePhone: Must not be null or empty for using default valid phone " + tripTicketRequestDTO.toString() + " trip ticket" );
                tripTicketDTO.setCustomerHomePhone("+18472568866");
            }
            //messageList.add("CustomerPrimaryPhone: Must not be null or empty");
        }

        /* customer seats required... */
        if (tripTicketRequestDTO.getCustomerSeatsRequired() != null) {
            tripTicketDTO.setCustomerSeatsRequired(tripTicketRequestDTO.getCustomerSeatsRequired());
        } else {
            tripTicketDTO.setCustomerSeatsRequired(0);
        }

        /* customer service level... */
        String serviceLevel = "AMB";
        if (tripTicketRequestDTO.getCustomerServiceLevel() != null
                && !Objects.equals(tripTicketRequestDTO.getCustomerServiceLevel(), "")) {
            if ( tripTicketRequestDTO.getCustomerServiceLevel().toLowerCase().contains("accessible") ||
                 tripTicketRequestDTO.getCustomerServiceLevel().toLowerCase().contains("wheelchair") ||
                 tripTicketRequestDTO.getCustomerServiceLevel().toLowerCase().contains("wc") ||
                 tripTicketRequestDTO.getCustomerServiceLevel().toLowerCase().contains("wav")
            ) {
                serviceLevel = "WAV";
            }
        }
        tripTicketDTO.setServiceLevel(serviceLevel);

        /* drop off address street 1... */
        if (tripTicketRequestDTO.getDropOffAddress().getStreet1() != null
                && !Objects.equals(tripTicketRequestDTO.getDropOffAddress().getStreet1(), "")) {
            dropOffAddressDTO.setStreet1(tripTicketRequestDTO.getDropOffAddress().getStreet1());
        } else {
            messageList.add("DropOffAddress.Street1: Must not be null or empty");
        }

        /* drop off address street 2... */
        if (tripTicketRequestDTO.getDropOffAddress().getStreet2() != null
        ) {
            dropOffAddressDTO.setStreet2(tripTicketRequestDTO.getDropOffAddress().getStreet2());
        }

        /* drop off address type... */
        if (tripTicketRequestDTO.getDropOffAddress().getAddressType() != null
                && !Objects.equals(tripTicketRequestDTO.getDropOffAddress().getAddressType(), "")) {
            dropOffAddressDTO.setAddressType(tripTicketRequestDTO.getDropOffAddress().getAddressType());
        }

        /* drop off address city... */
        if (tripTicketRequestDTO.getDropOffAddress().getCity() != null
                && !Objects.equals(tripTicketRequestDTO.getDropOffAddress().getCity(), "")) {
            dropOffAddressDTO.setCity(tripTicketRequestDTO.getDropOffAddress().getCity());
        } else {
            messageList.add("DropOffAddress.City: Must not be null or empty");
        }

        /* drop off address comman name... */
        if (tripTicketRequestDTO.getDropOffAddress().getCommonName() != null
                && !Objects.equals(tripTicketRequestDTO.getDropOffAddress().getCommonName(), "")) {
            dropOffAddressDTO.setCommonName(tripTicketRequestDTO.getDropOffAddress().getCommonName());
        }

        /* drop off address phone no... */
        if (tripTicketRequestDTO.getDropOffAddress().getPhoneNumber() != null
                && !Objects.equals(tripTicketRequestDTO.getDropOffAddress().getPhoneNumber(), "")) {
            dropOffAddressDTO.setPhoneNumber(tripTicketRequestDTO.getDropOffAddress().getPhoneNumber());
        }

        /* DOFF address state... */
        if (tripTicketRequestDTO.getDropOffAddress().getState() != null
                && !Objects.equals(tripTicketRequestDTO.getDropOffAddress().getState(), "")) {
            dropOffAddressDTO.setState(tripTicketRequestDTO.getDropOffAddress().getState());
        } else {
            log.warn("DropOffAddress.State: Must not be null or empty for using default valid state " + tripTicketRequestDTO.toString() + " trip ticket" );
            //messageList.add("DropOffAddress.State: Must not be null or empty");
        }

        /* DOFF zip... */
        if (tripTicketRequestDTO.getDropOffAddress().getZipcode() != null
                && !Objects.equals(tripTicketRequestDTO.getDropOffAddress().getZipcode(), "")) {
            dropOffAddressDTO.setZipcode(tripTicketRequestDTO.getDropOffAddress().getZipcode());
        } else {
            log.warn("DropOffAddress.ZipCode: Must not be null or empty for using default valid zip code " + tripTicketRequestDTO.toString() + " trip ticket" );
            //messageList.add("DropOffAddress.ZipCode: Must not be null or empty");
        }

        /* drop off county... */
        if (tripTicketRequestDTO.getDropOffAddress().getCounty() != null
                && !Objects.equals(tripTicketRequestDTO.getDropOffAddress().getCounty(), "")) {
            dropOffAddressDTO.setCounty(tripTicketRequestDTO.getDropOffAddress().getCounty());
        }

        /* drop off latitude... */
        if (tripTicketRequestDTO.getDropOffAddress().getLatitude() != 0) {
            dropOffAddressDTO.setLatitude(tripTicketRequestDTO.getDropOffAddress().getLatitude());
        } else {
            messageList.add("DropOffAddress.Latitude: Must not be null or empty");
        }

        /* drop off longitude... */
        if (tripTicketRequestDTO.getDropOffAddress().getLongitude() != 0) {
            dropOffAddressDTO.setLongitude(tripTicketRequestDTO.getDropOffAddress().getLongitude());
        } else {
            messageList.add("DropOffAddress.Longitude: Must not be null or empty");
        }

        /* estimated distance... */
        if (tripTicketRequestDTO.getEstimatedDistance() != null) {
            tripTicketDTO.setEstimatedTripDistance(tripTicketRequestDTO.getEstimatedDistance());
        }

        /* estimated_trip_travel_time... */
        if (tripTicketRequestDTO.getEstimatedTripTravelTime() != null) {
            /* convert string to time estimatedTravelTime */
            tripTicketDTO.setEstimatedTripTravelTime(tripTicketRequestDTO.getEstimatedTripTravelTime());
        }

        /* is outside core hours... */
        boolean isOutsideCoreHours = tripTicketRequestDTO.isOutsideCoreHours();

        tripTicketDTO.setOutsideCoreHours(isOutsideCoreHours);

        /* no of attendents... */
        if (tripTicketRequestDTO.getNumAttendants() != null) {
            tripTicketDTO.setAttendants(tripTicketRequestDTO.getNumAttendants());
        } else {
            tripTicketDTO.setAttendants(0);
        }

        /* no of guests... */
        if (tripTicketRequestDTO.getNumGuests() != null) {
            tripTicketDTO.setGuests(tripTicketRequestDTO.getNumGuests());
        } else {
            tripTicketDTO.setGuests(0);
        }

        tripTicketDTO.setCustomerNickName(tripTicketRequestDTO.getCustomerNickName());

        tripTicketDTO.setCustomerMiddleName(tripTicketRequestDTO.getCustomerMiddleName());

        /* origin customer id... */
        if (tripTicketRequestDTO.getOriginCustomerId() != null) {
            tripTicketDTO.setOriginCustomerId(tripTicketRequestDTO.getOriginCustomerId());
        } else {
            log.warn("OriginCustomerId: Must not be null or empty for using default valid customer id " + tripTicketRequestDTO.toString() + " trip ticket" );
            //messageList.add("OriginCustomerId: Must not be null or 0");
        }

        /* origin trip id... */
        if (tripTicketRequestDTO.getOriginTripId() != null) {
            tripTicketDTO.setRequesterTripId(tripTicketRequestDTO.getOriginTripId());
            tripTicketDTO.setCommonTripId(tripTicketRequestDTO.getOriginTripId());
        } else {
            messageList.add("OriginTripId: Must not be null or 0");
            //throw new InvalidInputException("OriginTripId:Must not be null or empty");
        }

        /* PU street 1... */
        if (tripTicketRequestDTO.getPickUpAddress().getStreet1() != null
                && !Objects.equals(tripTicketRequestDTO.getPickUpAddress().getStreet1(), "")) {
            pickUpAddressDTO.setStreet1(tripTicketRequestDTO.getPickUpAddress().getStreet1());
        } else {
            messageList.add("PickUpAddress.Street1: Must not be null or empty");
        }

        /* PU city... */
        if (tripTicketRequestDTO.getPickUpAddress().getCity() != null
                && !Objects.equals(tripTicketRequestDTO.getPickUpAddress().getCity(), "")) {
            pickUpAddressDTO.setCity(tripTicketRequestDTO.getPickUpAddress().getCity());
        } else {
            messageList.add("PickUpAddress.City: Must not be null or empty");
        }

        /* PU address comman name... */
        if (tripTicketRequestDTO.getPickUpAddress().getCommonName() != null) {
            pickUpAddressDTO.setCommonName(tripTicketRequestDTO.getPickUpAddress().getCommonName());
        }

        /* PU address phone no... */
        if (tripTicketRequestDTO.getPickUpAddress().getPhoneNumber() != null) {
            pickUpAddressDTO.setPhoneNumber(tripTicketRequestDTO.getPickUpAddress().getPhoneNumber());
        }

        /* PU address state... */
        if (tripTicketRequestDTO.getPickUpAddress().getState() != null
                && !Objects.equals(tripTicketRequestDTO.getPickUpAddress().getState(), "")) {
            pickUpAddressDTO.setState(tripTicketRequestDTO.getPickUpAddress().getState());
        } else {
            log.warn("PickUpAddress.State: Must not be null or empty for using default valid state " + tripTicketRequestDTO.toString() + " trip ticket" );
            //messageList.add("PickUpAddress.State: Must not be null or empty");
        }

        /* PU address zip... */
        if (tripTicketRequestDTO.getPickUpAddress().getZipcode() != null
                && !Objects.equals(tripTicketRequestDTO.getPickUpAddress().getZipcode(), "")) {
            pickUpAddressDTO.setZipcode(tripTicketRequestDTO.getPickUpAddress().getZipcode());
        } else {
            log.warn("PickUpAddress.ZipCode: Must not be null or empty for using default valid zip code " + tripTicketRequestDTO.toString() + " trip ticket" );
            //messageList.add("PickUpAddress.ZipCode: Must not be null or empty");
        }

        /* PU address type... */
        if (tripTicketRequestDTO.getPickUpAddress().getAddressType() != null
                && !Objects.equals(tripTicketRequestDTO.getPickUpAddress().getAddressType(), "")) {
            pickUpAddressDTO.setAddressType(tripTicketRequestDTO.getPickUpAddress().getAddressType());
        }

        /* pickup latitude... */
        if (tripTicketRequestDTO.getPickUpAddress().getLatitude() != 0) {
            pickUpAddressDTO.setLatitude(tripTicketRequestDTO.getPickUpAddress().getLatitude());
        } else {
            messageList.add("PickUpAddress.Latitude: Must not be null or empty");
        }

        /* pickup longitude... */
        if (tripTicketRequestDTO.getPickUpAddress().getLongitude() != 0) {
            pickUpAddressDTO.setLongitude(tripTicketRequestDTO.getPickUpAddress().getLongitude());
        } else {
            messageList.add("PickUpAddress.Longitude: Must not be null or empty");
        }

        /* pickup county... */
        if (tripTicketRequestDTO.getPickUpAddress().getCounty() != null
                && !Objects.equals(tripTicketRequestDTO.getPickUpAddress().getCounty(), "")) {
            pickUpAddressDTO.setCounty(tripTicketRequestDTO.getPickUpAddress().getCounty());
        }

        /* trip isolation... */
        boolean isTripIsolation = tripTicketRequestDTO.isTripIsolation();
        tripTicketDTO.setIsTripIsolation(isTripIsolation);

        /* Customer_Service_Animals... */
        boolean isTripServiceAnimal = tripTicketRequestDTO.isTripServiceAnimal();
        tripTicketDTO.setCustomerServiceAnimals(isTripServiceAnimal);

        boolean isPickupDatePresent = false;
        boolean isDropoffDatePresent = false;

        /* dropoff datetime... */
        if (tripTicketRequestDTO.getRequestedDropOffTime() != null
                && tripTicketRequestDTO.getRequestedDropOffTime() != "") {
            DateTimeParseResult pickRes = parseRequestedDateTime(tripTicketRequestDTO.getRequestedDropOffTime(), tripTicketRequestDTO.getTripDate(), "dropOffDateTime", messageList, true);
            if (pickRes.present) {
                log.info("[DTO-SET-DROPOFF] Before set: pickRes.date={}, class={}", pickRes.date, pickRes.date.getClass().getName());
                tripTicketDTO.setRequestedDropoffDate(pickRes.date);
                log.info("[DTO-SET-DROPOFF] After set: tripTicketDTO.getRequestedDropoffDate()={}, class={}",
                    tripTicketDTO.getRequestedDropoffDate(), tripTicketDTO.getRequestedDropoffDate().getClass().getName());
                tripTicketDTO.setRequestedDropOffTime(pickRes.time);
                isDropoffDatePresent = true;
            }
        }


        /* pickup date time... */
        if (tripTicketRequestDTO.getRequestedPickupTime() != null
                && tripTicketRequestDTO.getRequestedPickupTime() != "") {
            DateTimeParseResult pickRes = parseRequestedDateTime(tripTicketRequestDTO.getRequestedPickupTime(), tripTicketRequestDTO.getTripDate(), "pickUpDateTime", messageList, true);
            if (pickRes.present) {
                log.info("[DTO-SET-PICKUP] Before set: pickRes.date={}, class={}", pickRes.date, pickRes.date.getClass().getName());
                tripTicketDTO.setRequestedPickupDate(pickRes.date);
                log.info("[DTO-SET-PICKUP] After set: tripTicketDTO.getRequestedPickupDate()={}, class={}",
                    tripTicketDTO.getRequestedPickupDate(), tripTicketDTO.getRequestedPickupDate().getClass().getName());
                tripTicketDTO.setRequestedPickupTime(pickRes.time);
                isPickupDatePresent = true;
            }
        }

        /* check either one of the pickup date or drop off date will be present */
        if ((!isDropoffDatePresent) && (!isPickupDatePresent)) {
            messageList.add("Pick up date time or Drop off date time should be present");
        }

        /* schedulign priority... */
        if (tripTicketRequestDTO.getSchedulingPriority() != null) {
            tripTicketDTO.setSchedulingPriority(tripTicketRequestDTO.getSchedulingPriority());
        }

        /* time window after... */
        if (tripTicketRequestDTO.getTimeWindowAfter() != null) {
            tripTicketDTO.setTimeWindowAfter(tripTicketRequestDTO.getTimeWindowAfter());
        } else {
            tripTicketDTO.setTimeWindowAfter(0);
        }

        /* time window before... */
        if (tripTicketRequestDTO.getTimeWindowBefore() != null) {
            tripTicketDTO.setTimeWindowBefore(tripTicketRequestDTO.getTimeWindowBefore());
        } else {
            tripTicketDTO.setTimeWindowBefore(0);
        }

        /* trip funders... */
        if (tripTicketRequestDTO.getTripFunders() != null && tripTicketRequestDTO.getTripFunders() != "") {
            tripTicketDTO.setTripFunders(tripTicketRequestDTO.getTripFunders());
        } else {
            tripTicketDTO.setTripFunders(null);
        }

        /* trip notes... */
        if (tripTicketRequestDTO.getTripNotes() != null && tripTicketRequestDTO.getTripNotes() != "") {
            tripTicketDTO.setTripNotes(tripTicketRequestDTO.getTripNotes());
        }

        /* trip purpose... */
        if (tripTicketRequestDTO.getTripPurpose() != null
                && tripTicketRequestDTO.getTripPurpose() != "") {
            tripTicketDTO.setTripPurpose(tripTicketRequestDTO.getTripPurpose());
        }

        /* tripticket status... */
        if (tripTicketRequestDTO.getStatus() != null && tripTicketRequestDTO.getStatus() != "") {
            StatusDTO status = new StatusDTO();
            if (tripTicketRequestDTO.getStatus().equalsIgnoreCase("cancel") || tripTicketRequestDTO.getStatus().equalsIgnoreCase("cancelled") || tripTicketRequestDTO.getStatus().equalsIgnoreCase("cancellation")) {
                status.setStatusId(TripTicketStatusConstants.cancelled.tripTicketStatusUpdate());
            }
            if (tripTicketRequestDTO.getStatus().equalsIgnoreCase("end") || tripTicketRequestDTO.getStatus().equalsIgnoreCase("complete") || tripTicketRequestDTO.getStatus().equalsIgnoreCase("finished")) {
                status.setStatusId(TripTicketStatusConstants.completed.tripTicketStatusUpdate());
            }
            if (tripTicketRequestDTO.getStatus().equalsIgnoreCase("no show") || tripTicketRequestDTO.getStatus().equalsIgnoreCase("no-show") || tripTicketRequestDTO.getStatus().equalsIgnoreCase("noshow")) {
                status.setStatusId(TripTicketStatusConstants.noShow.tripTicketStatusUpdate());
            }
            if ( status.getStatusId() == 0 ) {
                status.setStatusId(TripTicketStatusConstants.available.tripTicketStatusUpdate());
            }
            tripTicketDTO.setStatus(status);
        }

        //Handling Exception
        if (!messageList.isEmpty()) {
            //tripTicketService.sendMailToOriginatorForInvalidInput(messageList, tripTicketDTO.getOriginProviderId());
            throw new InvalidInputCheckException(messageList, tripTicketDTO.getOriginProviderId());
        }

        //check latlong is valid or not before saved address
        tripTicketDTO.setPickupAddress(pickUpAddressDTO);
        tripTicketDTO.setDropOffAddress(dropOffAddressDTO);
        log.error("set tripTicketDTO " + tripTicketDTO + " for tripId " + tripTicketDTO.getCommonTripId() + " to pickupAddressDTO " + pickUpAddressDTO + " and dropOffAddressDTO " + dropOffAddressDTO);
        TripTicketDistanceDTO timeAndDistanceDTO = tripTicketDistanceService.checkDistanceTime(tripTicketDTO);
        if (timeAndDistanceDTO == null) {
            log.error("timeAndDistanceDTO is null for tripId " + tripTicketDTO.getCommonTripId());
            throw new InvalidInputException(
                    "The Pick up/Drop off Lat-Long is invalid, Ticket is not created. Please try again.");
        }

        // *********************new added by shankar I*******avoid duplication for
        // customer and addrs
        // *******************check for PickUp address********************##########
        Address pickUpAddress = null;
        if (pickUpAddressDTO.getStreet1() != null && pickUpAddressDTO.getCity() != null
                && pickUpAddressDTO.getState() != null && pickUpAddressDTO.getZipcode() != null) {

            boolean duplicationFlag = addressDAO.checkForAddrsDuplication(pickUpAddressDTO);

            if (duplicationFlag) {
                Address pickAddrs = null;
                pickAddrs = addressDAO.findExistingAddress(pickUpAddressDTO);
                pickUpAddress = pickAddrs;
                // update street2
                if ((pickAddrs.getStreet2() == null || pickAddrs.getStreet2().isEmpty())
                        && pickUpAddressDTO.getStreet2() != null) {
                    pickAddrs.setStreet2(pickUpAddressDTO.getStreet2());
                    pickUpAddress = addressDAO.updateAddress(pickAddrs);
                }

                AddressDTO pickUpAddrsDTO = addressService.convertIntoDTo(pickUpAddress);
                tripTicketDTO.setPickupAddress(pickUpAddrsDTO);

            } else {

                Address pickUpAddressBO = addressService.convertIntoBO(pickUpAddressDTO);
                pickUpAddress = addressService.createAddress(pickUpAddressBO);
                AddressDTO pickUpAddrsDTO = addressService.convertIntoDTo(pickUpAddress);
                tripTicketDTO.setPickupAddress(pickUpAddrsDTO);
            }
        } else {
            Address pickUpAddressBO = addressService.convertIntoBO(pickUpAddressDTO);
            pickUpAddress = addressService.createAddress(pickUpAddressBO);
            AddressDTO pickUpAddrsDTO = addressService.convertIntoDTo(pickUpAddress);
            tripTicketDTO.setPickupAddress(pickUpAddrsDTO);
        }

        // *******************check for DropOff address********************##########

        Address dropOffAddress = null;

        if (dropOffAddressDTO.getStreet1() != null && dropOffAddressDTO.getCity() != null
                && dropOffAddressDTO.getState() != null && dropOffAddressDTO.getZipcode() != null) {

            boolean duplicationFlag = addressDAO.checkForAddrsDuplication(dropOffAddressDTO);

            if (duplicationFlag) {
                Address dropAddrs = null;
                dropAddrs = addressDAO.findExistingAddress(dropOffAddressDTO);
                dropOffAddress = dropAddrs;
                // update street2
                if ((dropAddrs.getStreet2() == null || dropAddrs.getStreet2().isEmpty())
                        && dropOffAddressDTO.getStreet2() != null) {
                    dropAddrs.setStreet2(dropOffAddressDTO.getStreet2());
                    dropOffAddress = addressDAO.updateAddress(dropAddrs);
                }

                AddressDTO dropOffAddrsDTO = addressService.convertIntoDTo(dropOffAddress);
                tripTicketDTO.setDropOffAddress(dropOffAddrsDTO);

            } else {
                Address dropOffAddressBO = addressService.convertIntoBO(dropOffAddressDTO);
                dropOffAddress = addressService.createAddress(dropOffAddressBO);
                AddressDTO dropOffAddrsDTO = addressService.convertIntoDTo(dropOffAddress);
                tripTicketDTO.setDropOffAddress(dropOffAddrsDTO);

            }
        } else {
            Address dropOffAddressBO = addressService.convertIntoBO(dropOffAddressDTO);
            dropOffAddress = addressService.createAddress(dropOffAddressBO);
            AddressDTO dropOffAddrsDTO = addressService.convertIntoDTo(dropOffAddress);
            tripTicketDTO.setDropOffAddress(dropOffAddrsDTO);
        }

        // **************** new added for Check CustomerAddress ***********

        Address customerAddress = null;

        if (customerAddressDTO.getStreet1() != null && customerAddressDTO.getCity() != null
                && customerAddressDTO.getState() != null && customerAddressDTO.getZipcode() != null) {
            // AddressDTO tempAddrsDto = null;
            boolean duplicationFlag = addressDAO.checkForAddrsDuplication(customerAddressDTO);

            if (duplicationFlag) {
                Address custAddress = null;
                custAddress = addressDAO.findExistingAddress(customerAddressDTO);
                customerAddress = custAddress;

                // update street2
                if ((custAddress.getStreet2() == null || custAddress.getStreet2().isEmpty())
                        && customerAddressDTO.getStreet2() != null) {
                    custAddress.setStreet2(customerAddressDTO.getStreet2());
                    customerAddress = addressDAO.updateAddress(custAddress);
                }

                AddressDTO custAddrsDTO = addressService.convertIntoDTo(customerAddress);
                tripTicketDTO.setCustomerAddress(custAddrsDTO);

            } else {
                Address custAddrsBO = addressService.convertIntoBO(customerAddressDTO);
                customerAddress = addressService.createAddress(custAddrsBO);
                AddressDTO custAddrsDTO = addressService.convertIntoDTo(customerAddress);
                tripTicketDTO.setCustomerAddress(custAddrsDTO);
            }
        } else {
            Address custAddrsBO = addressService.convertIntoBO(customerAddressDTO);
            customerAddress = addressService.createAddress(custAddrsBO);
            AddressDTO custAddrsDTO = addressService.convertIntoDTo(customerAddress);
            tripTicketDTO.setCustomerAddress(custAddrsDTO);
        }

        // ****************************new added for check Duplicate
        // customer*************

        Customer customerObj = new Customer();
        String msg = null;

        try {
            customerObj.setCustomerFirstName(tripTicketDTO.getCustomerFirstName());
            customerObj.setCustomerLastName(tripTicketDTO.getCustomerLastName());
            if (tripTicketDTO.getCustomerHomePhone() != null) {
                customerObj.setCustomerPrimaryPhone(tripTicketDTO.getCustomerHomePhone());
            }
            if (tripTicketDTO.getCustomerDob() != null) {
                customerObj.setCustomerDob(java.sql.Date.valueOf(tripTicketDTO.getCustomerDob()));
            }
            if (customerAddress != null) {
                customerObj.setCustomerAddress(customerAddress);
            }
        } catch (Exception e) {
            log.error("Error setting customer fields in convertRequestJsonToTicketDTO: {}", e.getMessage(), e);
        }
        if (tripTicketDTO.getCustomerAddress() != null && tripTicketDTO.getCustomerFirstName() != null
                && tripTicketDTO.getCustomerLastName() != null && tripTicketDTO.getCustomerHomePhone() != null) {

            // AddressDTO tempAddrsDto = null;
            boolean duplicationFlag = customerDAO.checkForCustomerDuplication(tripTicketDTO);

            if (duplicationFlag) {
                msg = customerDAO
                        .getCustomerStatusByMsgId(CustomerStatusConstants.existingCustomer.getCustomerStatus());
                tripTicketDTO.setCustomerStatusForDuplication(msg);
                log.debug(
                        "***************************############ This client is already Registered in the Hub.************** Trip ticket is created.*************************************************");
            } else {
                /*
                 * customerObj.setCustomerFirstName(tripTicketDTO.getCustomerFirstName());
                 * customerObj.setCustomerLastName(tripTicketDTO.getCustomerLastName());
                 * customerObj.setCustomer_primary_phone(tripTicketDTO.getCustomer_primary_phone
                 * ()); customerObj.setCustomerDob(tripTicketDTO.getCustomerDob());
                 * customerObj.setCustomer_address(customerAddress);
                 */
                msg = customerDAO.getCustomerStatusByMsgId(CustomerStatusConstants.newCustomer.getCustomerStatus());
                tripTicketDTO.setCustomerStatusForDuplication(msg);
                customerDAO.createCustomer(customerObj);
                log.debug(
                        "***************************############  New Customer is Registered. *********************** Trip ticket is created. *************************************************");
            }
        } /*else {
			msg = customerDAO.getCustomerStatusByMsgId(CustomerStatusConstants.newCustomer.getCustomerStatus());
			tripTicketDTO.setCustomerStatusForDuplication(msg);
			customerDAO.createCustomer(customerObj);
			log.debug(
					"***************************############  New Customer is Registered. *********************** Trip ticket is created. *************************************************");

		}*/

        try {
            tripTicketDTO.setProvisionalProviderId(originProviderId);
        } catch (Exception e) {
            log.error("Error setting provisionalProviderId in convertRequestJsonToTicketDTO: {}", e.getMessage(), e);
            /* keeping empty cause this exception already handled */
        }
        tripTicketDTO.setVersion("1");

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("bindingResult", bindingResult);
        resultMap.put("tripTicketDTO", tripTicketDTO);
        resultMap.put("bindingResultList", messageList);
        return resultMap;
    }
}
