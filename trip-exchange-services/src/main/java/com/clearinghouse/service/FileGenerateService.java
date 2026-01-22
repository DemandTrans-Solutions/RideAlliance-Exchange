package com.clearinghouse.service;

import com.clearinghouse.dao.TripTicketDAO;
import com.clearinghouse.dto.ProvidersWeeklyReportDTO;
import com.clearinghouse.dto.TripResultDTO;
import com.clearinghouse.dto.UploadFile;
import com.clearinghouse.entity.Notification;
import com.clearinghouse.entity.TripClaim;
import com.clearinghouse.entity.TripTicket;
import com.clearinghouse.enumentity.NotificationTemplateCodeValue;
import com.opencsv.CSVWriter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author Shankar I.
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class FileGenerateService {

    //@Value("${CSV-filepath}")
    private final String filePath;


    private final Environment environment;

//to get tripTicketStatus By Id

    private final TripTicketDAO tripTicketDAO;


    public Boolean createFolderByFolderPath(String path) {
        var status = true;
        File theDir = new File(path);
        if (!theDir.exists()) {
            try {
                theDir.mkdirs();
                status = true;
            } catch (Exception ex) {
                status = false;
                log.error("Error creating directory: {}", path, ex);
            }
        }
        return Boolean.valueOf(status);
    }

    public String createCSVForClaimApprovedDeclineRescind(TripTicket tripticket, Notification emailNotification) {
        // first create file object for file placed at location
        // specified by filepath
        File file = null;

        if ((emailNotification.getNotificationTemplate()
                .getNotificationTemplateId()) == (NotificationTemplateCodeValue.claimDeclinedTemplateCode
                .templateCodeValue())
                || (emailNotification.getNotificationTemplate()
                .getNotificationTemplateId()) == (NotificationTemplateCodeValue.claimRescindedTemplateCode
                .templateCodeValue())) {

            if ((emailNotification.getNotificationTemplate()
                    .getNotificationTemplateId()) == (NotificationTemplateCodeValue.claimDeclinedTemplateCode
                    .templateCodeValue())) {
                file = new File(filePath, tripticket.getId() + "_DeclinedClaimTripTicket.csv");
            } else if ((emailNotification.getNotificationTemplate()
                    .getNotificationTemplateId()) == (NotificationTemplateCodeValue.claimRescindedTemplateCode
                    .templateCodeValue())) {
                file = new File(filePath, tripticket.getId() + "_RescindedClaimTripTicket.csv");
            }

            try {
                // create FileWriter object with file as parameter
                FileWriter outputfile = new FileWriter(file);

                // create CSVWriter object filewriter object as parameter
                CSVWriter writer = new CSVWriter(outputfile);

                // adding header to csv
                String[] header = {"Trip_Ticket_Id", "common_trip_id", "origin_provider", "Requester Cost",
                        "Calculated Proposed Cost", "Final Proposed Cost", "Customer_Name", "Seat_Required",
                        "requested_pickup_date", "requested_pickup_time", "Pickup_Address", "DropOff_Address",};

                writer.writeNext(header);

                // just for
                // Out_of_Hours_Acknowledgment,reqProviderCost,claimantCost,updatedClaimantCost
                // value
                Set<TripClaim> tripClaims = tripticket.getTripClaims();
                float reqProviderCost = 0;
                float claimantCost = 0;
                float calculatedClaimantCost = 0;
                for (TripClaim tripClaim : tripClaims) {
                    reqProviderCost = tripClaim.getRequesterProviderFare();
                    calculatedClaimantCost = tripClaim.getCalculatedProposedFare();
                    claimantCost = tripClaim.getProposedFare();

                }
                // for changing localDateTime format to mm-dd-yyyy HH:mm:ss and localDate to
                // mm-dd-yyyy format
                String pickupDate = "";

                if (tripticket.getRequestedPickupDate() != null) {
                    LocalDate pickupDt = tripticket.getRequestedPickupDate();
                    pickupDate = DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.ENGLISH).format(pickupDt);
                }

                // add data to csv
                String[] data1 = {String.valueOf(tripticket.getId()), tripticket.getCommonTripId(),
                        tripticket.getOriginProvider().getProviderName(), String.valueOf(reqProviderCost),
                        String.valueOf(calculatedClaimantCost), String.valueOf(claimantCost),
                        tripticket.getCustomerFirstName() + " " + tripticket.getCustomerLastName(),
                        String.valueOf(tripticket.getCustomerSeatsRequired()), pickupDate,
                        tripticket.getRequestedPickupTime().toString(),
                        (tripticket.getPickupAddress().getStreet1() == null ? ""
                                : tripticket.getPickupAddress().getStreet1())
                                + (tripticket.getPickupAddress().getStreet2() == null ? ""
                                : tripticket.getPickupAddress().getStreet2())
                                + (tripticket.getPickupAddress().getCity() == null ? ""
                                : tripticket.getPickupAddress().getCity())
                                + (tripticket.getPickupAddress().getState() == null ? ""
                                : tripticket.getPickupAddress().getState())
                                + (tripticket.getPickupAddress().getCounty() == null ? ""
                                : tripticket.getPickupAddress().getCounty())
                                + (tripticket.getPickupAddress().getZipcode() == null ? ""
                                : tripticket.getPickupAddress().getZipcode()),
                        (tripticket.getDropOffAddress().getStreet1() == null ? ""
                                : tripticket.getDropOffAddress().getStreet1())
                                + (tripticket.getDropOffAddress().getStreet2() == null ? ""
                                : tripticket.getDropOffAddress().getStreet2())
                                + (tripticket.getDropOffAddress().getCity() == null ? ""
                                : tripticket.getDropOffAddress().getCity())
                                + (tripticket.getDropOffAddress().getState() == null ? ""
                                : tripticket.getDropOffAddress().getState())
                                + (tripticket.getDropOffAddress().getCounty() == null ? ""
                                : tripticket.getDropOffAddress().getCounty())
                                + (tripticket.getDropOffAddress().getZipcode() == null ? ""
                                : tripticket.getDropOffAddress().getZipcode()),};
                writer.writeNext(data1);

                // closing writer connection
                writer.close();
            } catch (Exception e) {
                log.error("Error writing Declined/Rescinded Claim CSV file", e);
            }

        } else {

            if ((emailNotification.getNotificationTemplate()
                    .getNotificationTemplateId()) == (NotificationTemplateCodeValue.claimApprovedTemplateCode
                    .templateCodeValue())) {
                file = new File(filePath, tripticket.getId() + "_ApprovedClaimTripTicket.csv");
            } else if ((emailNotification.getNotificationTemplate()
                    .getNotificationTemplateId()) == (NotificationTemplateCodeValue.claimUpdated.templateCodeValue())) {
                file = new File(filePath, tripticket.getId() + "_UpdatedClaimTripTicket.csv");
            }
            try {
                // create FileWriter object with file as parameter
                FileWriter outputfile = new FileWriter(file);

                // create CSVWriter object filewriter object as parameter
                CSVWriter writer = new CSVWriter(outputfile);

                // adding header to csv
                String[] header = {"Trip_Ticket_Id", "common_trip_id", "origin_provider", "Requester Cost",
                        "Calculated Proposed Cost", "Final Proposed Cost", "Customer_Name", "Customer_Email",
                        "customer_primary_phone", "customer_emergency_phone", "impairment_description", "Seat_Required",
                        "Boarding_Time", "Deboarding_Time", "Customer_Notes", "requested_pickup_date",
                        "requested_pickup_time", "earliest_pickup_time", "appointment_time", "Pickup_Address",
                        "Pickup_phone_number", "requested_dropoff_date", "requested_dropOff_time", "DropOff_Address",
                        "DropOff_phone_number", "Purpose", "attendants", "guests", "trip_notes",
                        "customer_mobility_factors", "customer_service_animals", "trip_funders",
                        "customer_assistance_needs", "attendant_mobility_factors", "guest_mobility_factors",
                        "service_level", "estimated_trip_travel_time", "estimated_trip_distance", "is_trip_isolation",
                        "is_outside_coreHours", "time_window_before", "time_window_after", "provider_white_list",
                        "Status", "provisional_provider"};

                writer.writeNext(header);

                // just for
                // Out_of_Hours_Acknowledgment,reqProviderCost,claimantCost,updatedClaimantCost
                // value
                Set<TripClaim> tripClaims = tripticket.getTripClaims();

                float reqProviderCost = 0;
                float claimantCost = 0;
                float calculatedClaimantCost = 0;
                for (TripClaim tripClaim : tripClaims) {
                    reqProviderCost = tripClaim.getRequesterProviderFare();
                    calculatedClaimantCost = tripClaim.getCalculatedProposedFare();
                    claimantCost = tripClaim.getProposedFare();

                }
                // for changing localDateTime format to mm-dd-yyyy HH:mm:ss and localDate to
                // mm-dd-yyyy format
                String pickupDate = "";
                String dropOffDate = "";
                // for trip status bcz not get from tripTicket.getStatus() object
                String tripTicketStatus = "";

                if (tripticket.getRequestedPickupDate() != null) {
                    LocalDate pickupDt = tripticket.getRequestedPickupDate();
                    pickupDate = DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.ENGLISH).format(pickupDt);
                }
                if (tripticket.getRequestedDropoffDate() != null) {
                    LocalDate dropOffDt = tripticket.getRequestedDropoffDate();
                    dropOffDate = DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.ENGLISH).format(dropOffDt);
                }
                if (tripticket.getStatus().getStatusId() != 0) {
                    tripTicketStatus = tripTicketDAO.getTripTicketStatusById(tripticket.getStatus().getStatusId());
                }
                // add data to csv
                String[] data1 = {String.valueOf(tripticket.getId()), tripticket.getCommonTripId(),
                        tripticket.getOriginProvider().getProviderName(), String.valueOf(reqProviderCost),
                        String.valueOf(calculatedClaimantCost), String.valueOf(claimantCost),
                        tripticket.getCustomerFirstName() + " " + tripticket.getCustomerLastName(),
                        tripticket.getCustomerEmail(), tripticket.getCustomerHomePhone(),
                        tripticket.getCustomerEmergencyPhone(), tripticket.getImpairmentDescription(),
                        String.valueOf(tripticket.getCustomerSeatsRequired()),
                        (tripticket.getBoardingTime() == 0 ? "" : String.valueOf(tripticket.getBoardingTime())),
                        (tripticket.getDeboardingTime() == 0 ? "" : String.valueOf(tripticket.getDeboardingTime())),
                        tripticket.getCustomerNotes(), pickupDate, tripticket.getRequestedPickupTime().toString(),
                        tripticket.getEarliestPickupTime() == null ? "" : String.valueOf(tripticket.getEarliestPickupTime()),
                        tripticket.getAppointmentTime().toString(),
                        (tripticket.getPickupAddress().getStreet1() == null ? ""
                                : tripticket.getPickupAddress().getStreet1())
                                + (tripticket.getPickupAddress().getStreet2() == null ? ""
                                : tripticket.getPickupAddress().getStreet2())
                                + (tripticket.getPickupAddress().getCity() == null ? ""
                                : tripticket.getPickupAddress().getCity())
                                + (tripticket.getPickupAddress().getState() == null ? ""
                                : tripticket.getPickupAddress().getState())
                                + (tripticket.getPickupAddress().getCounty() == null ? ""
                                : tripticket.getPickupAddress().getCounty())
                                + (tripticket.getPickupAddress().getZipcode() == null ? ""
                                : tripticket.getPickupAddress().getZipcode()),
                        tripticket.getPickupAddress().getPhoneNumber(), dropOffDate,
                        (tripticket.getDropOffAddress().getStreet1() == null ? ""
                                : tripticket.getDropOffAddress().getStreet1())
                                + (tripticket.getDropOffAddress().getStreet2() == null ? ""
                                : tripticket.getDropOffAddress().getStreet2())
                                + (tripticket.getDropOffAddress().getCity() == null ? ""
                                : tripticket.getDropOffAddress().getCity())
                                + (tripticket.getDropOffAddress().getState() == null ? ""
                                : tripticket.getDropOffAddress().getState())
                                + (tripticket.getDropOffAddress().getCounty() == null ? ""
                                : tripticket.getDropOffAddress().getCounty())
                                + (tripticket.getDropOffAddress().getZipcode() == null ? ""
                                : tripticket.getDropOffAddress().getZipcode()),
                        tripticket.getDropOffAddress().getPhoneNumber(), tripticket.getTripPurpose(),
                        tripticket.getTripNotes(), tripticket.getCustomerMobilityFactors(),
                        String.valueOf(tripticket.isCustomerServiceAnimals()), tripticket.getTripFunders(),
                        tripticket.getCustomerAssistanceNeeds(), tripticket.getAttendantMobilityFactors(),
                        tripticket.getGuestMobilityFactors(), tripticket.getServiceLevel(),
                        String.valueOf(tripticket.getEstimatedTripTravelTime()),
                        String.valueOf(tripticket.getEstimatedTripDistance()),
                        String.valueOf(tripticket.isTripIsolation()),
                        String.valueOf(tripticket.isOutsideCoreHours()),
                        String.valueOf(tripticket.getTimeWindowBefore()),
                        String.valueOf(tripticket.getTimeWindowAfter()), tripticket.getProviderWhiteList(),
                        tripTicketStatus, tripticket.getProvisionalProvider().getProviderName()};
                writer.writeNext(data1);

                // closing writer connection
                writer.close();
            } catch (Exception e) {
                log.error("Error writing Approved/Updated Claim CSV file", e);
            }
        }
        return file.getPath();
    }


    public String createCSVtoGenerateWeeklyReport(Integer providerId, String providerAs,
                                                  List<ProvidersWeeklyReportDTO> providersWeeklyReportDTOList) {
        // specified by filepath
        File file = null;
        String reportFilepath = environment.getRequiredProperty("CSV-reportfilepath");

        file = new File(reportFilepath,
                java.time.LocalDate.now() + "_" + providerAs + "_" + providerId + "_WeeklyTripTicketReport.csv");

        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(file);

            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);

            // adding header to csv
            String[] header = {"Ticket Created Date/Time", "TripTicket Status", "Requested PickUp DateTime",
                    "Requested Dropoff DateTime", "PickUp Address", "DropOff Address", "Distance In Mile",
                    "Time In Hours", " Final Proposed Cost", "Funding Source"};

            writer.writeNext(header);

            for (ProvidersWeeklyReportDTO providersWeeklyReportDTO : providersWeeklyReportDTOList) {
                // add data to csv
                String[] data1 = {providersWeeklyReportDTO.getDate(), providersWeeklyReportDTO.getStatus(),
                        providersWeeklyReportDTO.getPickupDate() + " "
                                + providersWeeklyReportDTO.getPickupTime(),
                        providersWeeklyReportDTO.getDropOffDate() + " "
                                + providersWeeklyReportDTO.getDropOffTime(),
                        providersWeeklyReportDTO.getPickupAddress(), providersWeeklyReportDTO.getDropOffAddress(),
                        String.valueOf(providersWeeklyReportDTO.getDistance()),
                        String.valueOf(providersWeeklyReportDTO.getTime()),
                        providersWeeklyReportDTO.getFinalProposedCost() == null ? " "
                                : String.valueOf(providersWeeklyReportDTO.getFinalProposedCost()),
                        providersWeeklyReportDTO.getFundingSource()};
                writer.writeNext(data1);

            } // end for
            // closing writer connection
            writer.close();
        } catch (Exception e) {
            log.error("Error writing Weekly Report CSV file", e);
        }
        return file.getPath();
    }

    // Prasad .J

    public Boolean createFileByFilePath(UploadFile listFile) {
        var status = true;
        byte[] imageByte = null;
        if (listFile.getDocumentValue() != null) {
            imageByte = Base64.decodeBase64(listFile.getDocumentValue());
        }
        FileOutputStream fileOutputStream = null;
        try {
            if (imageByte != null) {
                fileOutputStream = new FileOutputStream(listFile.getDocumentPath());
                fileOutputStream.write(imageByte);
                status = true;
            }
        } catch (FileNotFoundException ex) {
            status = false;
            log.error("File not found: {}", listFile.getDocumentPath(), ex);
        } catch (IOException ex) {
            status = false;
            log.error("IO error writing file: {}", listFile.getDocumentPath(), ex);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    status = false;
                    log.error("IO error closing file: {}", listFile.getDocumentPath(), e);
                }
            }
        }

        return Boolean.valueOf(status);
    }

    // Prasad .J

    public boolean deleteFileByFilePath(String path) {
        var status = true;
        try {
            if (org.codehaus.plexus.util.FileUtils.fileExists(path)) {
                org.codehaus.plexus.util.FileUtils.forceDelete(new File(path));
            }
            status = true;
        } catch (FileNotFoundException ex) {
            log.error("File not found: {}", path, ex);
            status = false;
        } catch (Exception ex) {
            log.error("Error deleting file: {}", path, ex);
            status = false;
        }
        return Boolean.valueOf(status);
    }


    public String createCSVForTripCompletedResultData(List<TripResultDTO> tripResultDTOList, Notification emailNotification) {
        // first create file object for file placed at location
        // specified by filepath
        File file = null;


        if ((emailNotification.getNotificationTemplate()
                .getNotificationTemplateId()) == (NotificationTemplateCodeValue.tripResultCompleted
                .templateCodeValue())) {
            String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss.SSSSSS").format(new Date());
            file = new File(filePath, fileName + "_CompletedTripTickets.csv");
        }

        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(file);

            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);

            // adding header to csv
            String[] header = {"TripTicketID", "Claimant Provider Name", "PickUp Address", "PickUpLat/Long",
                    "Dropoff Address", "DropOffLat/Long", "TripDate", "Scheduled Pickup Time", "Scheduled DropOff Time",
                    "NoShowFlag", "ActualPickUpArriveTime", "ActualPickUpDepartTime", "ActualDropOffArriveTime", "ActualDropOffDepartTime",
                    "Number Of Passengers", "Number of Guests", "Number of Attendants", "FareCollected", "VehicleID", "DriverID"};

            writer.writeNext(header);

            for (TripResultDTO tripResultDTO : tripResultDTOList) {

                //for change date format
                DateFormat originalFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
                DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                Date date2 = null;
                try {
                    date2 = originalFormat.parse(tripResultDTO.getTripDate().toString());
                } catch (ParseException e) {
                    log.error("Error parsing trip date for TripResultDTO: {}", tripResultDTO.getTripDate(), e);
                }
                String formattedTripDate = targetFormat.format(date2);

                // add data to csv
                String[] data1 = {String.valueOf(tripResultDTO.getTripTicketId()), tripResultDTO.getClaimantProvider(),
                        tripResultDTO.getPickUpAddress(), tripResultDTO.getPickUpLatitude() + ", " + tripResultDTO.getPickupLongitude(),
                        tripResultDTO.getDropOffAddress(), tripResultDTO.getDropOffLatitude() + ", " + tripResultDTO.getDropOffLongitude(),
                        formattedTripDate, tripResultDTO.getScheduledPickupTime(), tripResultDTO.getScheduledDropOffTime(),
                        // Safely handle nullable Boolean isNoShowFlag: show "TRUE"/"FALSE" or default to "FALSE"
                        (tripResultDTO.getIsNoShowFlag() != null ? String.valueOf(tripResultDTO.getIsNoShowFlag()) : "FALSE"),
                        tripResultDTO.getActualPickupArriveTime() != null ? String.valueOf(tripResultDTO.getActualPickupArriveTime()) : "",
                        tripResultDTO.getActualPickupDepartTime() != null ? String.valueOf(tripResultDTO.getActualPickupDepartTime()) : "",
                        tripResultDTO.getActualDropOffArriveTime() != null ? String.valueOf(tripResultDTO.getActualDropOffArriveTime()) : "",
                        tripResultDTO.getActualDropOffDepartTime() != null ? String.valueOf(tripResultDTO.getActualDropOffDepartTime()) : "",
                        tripResultDTO.getNumberOfPassengers() != 0 ? String.valueOf(tripResultDTO.getNumberOfPassengers()) : "",
                        tripResultDTO.getNumberOfGuests() != 0 ? String.valueOf(tripResultDTO.getNumberOfGuests()) : "",
                        tripResultDTO.getNumberOfAttendants() != 0 ? String.valueOf(tripResultDTO.getNumberOfAttendants()) : "",
                        tripResultDTO.getFareCollected() != 0.0 ? String.valueOf(tripResultDTO.getFareCollected()) : "",
                        tripResultDTO.getVehicleId() != null ? String.valueOf(tripResultDTO.getVehicleId()) : "",
                        tripResultDTO.getDriverId() != null ? String.valueOf(tripResultDTO.getDriverId()) : ""
                };
                writer.writeNext(data1);
            }
            // closing writer connection
            writer.close();
        } catch (Exception e) {
            log.error("Error writing Trip Completed Result CSV file", e);
        }
        return file.getPath();
    }
}
