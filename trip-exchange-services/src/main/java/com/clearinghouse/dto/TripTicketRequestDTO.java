package com.clearinghouse.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.sql.Time;


@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TripTicketRequestDTO {


    private Integer tripTicketId;
    @NotNull
    private String originTripId;
    @NotNull
    private Integer originProviderId;
    @NotNull
    private String tripDate;
    private String appointmentTime;
    @NotNull
    private String requestedDropOffTime;
    @NotNull
    private String requestedPickupTime;
    private Integer timeWindowBefore;
    private Integer timeWindowAfter;

    private String vehicleType;
    private String tripPurpose;


    @Valid
    private AddressDTO customerAddress;

    @NotNull
    @Valid
    private AddressDTO pickUpAddress;

    @NotNull
    @Valid
    private AddressDTO dropOffAddress;


    @NotNull
    private String customerFirstName;
    private String customerMiddleName;
    @NotNull
    private String customerLastName;
    private String customerGender;
    private String customerRace;
    private String customerDOB;
    private String customerEmergencyPhone;
    private String customerIdentifiers;
    private String customerEligibilityFactors;
    private String customerMobilityFactors;
    private String customerNotes;
    private String customerPrimaryLanguage;
    private Integer customerSeatsRequired;
    private String customerServiceLevel;
    private String customerNickName;
    private String customerMobilePhone;
    private String customerMailingBillingAddress;
    private String customerFundingBillingInformation;
    private String customerFundingEntity;
    private String fundingType;
    private Boolean customerLowIncome;
    private String customerDisability;
    private Boolean customerVeteran;
    private String customerEthnicity;
    private String customerEmail;
    private String customerCaregiverName;
    private String customerCaregiverContactInfo;
    private String customerEmergencyContactName;
    private String customerEmergencyContactRelationship;
    private String customerEmergencyContactPhone;
    private String customerPovertyLevel;
    private String customerHomePhone;
    private String customerCareInfo;
    private Float estimatedDistance;
    private Integer estimatedTripTravelTime;
    private Integer numAttendants;
    private Integer numGuests;
    private boolean outsideCoreHours;
    private boolean customerInformationWithheld;
    private boolean tripIsolation;
    private boolean tripServiceAnimal;
    private String schedulingPriority;
    private String tripFunders;
    private String tripNotes;

    private String originCustomerId;
    private String status;


    // Completed Trip Data (Upload can be either complete or new records)
    private String noShowReason;
    private String cancelReason;
    private float fareCollected;
    private String vehicleId;
    private String driverId;

    private String actualPickupArriveTime;
    private String actualPickupDepartTime;
    private String actualDropOffArriveTime;
    private String actualDropOffDepartTime;



    public boolean isComplete() {
        return  ( getStatus() != null &&
                ( getStatus().toLowerCase().contains("complete") ||
                  getStatus().toLowerCase().contains("cancel") ||
                  getStatus().toLowerCase().contains("no show") ||
                  getStatus().toLowerCase().contains("no_show") ||
                  getStatus().toLowerCase().contains("no-show")) );
    }

    @Override
    public String toString() {
        return "TripTicketRequestDTO [tripTicketId=" + tripTicketId + ", appointmentTime=" + appointmentTime + ", customerAddress="
                + customerAddress + ", pickUpAddress=" + pickUpAddress + ", dropOffAddress=" + dropOffAddress
                + ", originTripId=" + originTripId + ", requestedDropOffTime=" + requestedDropOffTime
                + ", requestedPickupTime=" + requestedPickupTime + ", customerFirstName=" + customerFirstName
                + ", customerMiddleName=" + customerMiddleName + ", customerLastName=" + customerLastName
                + ", customerGender=" + customerGender + ", customerRace=" + customerRace + ", customerDOB="
                + customerDOB + ", customerEmergencyPhone=" + customerEmergencyPhone + ", customerIdentifiers="
                + customerIdentifiers + ", customerEligibilityFactors=" + customerEligibilityFactors
                + ", customerMobilityFactors=" + customerMobilityFactors + ", customerNotes=" + customerNotes
                + ", customerPrimaryLanguage=" + customerPrimaryLanguage + ", customerHomePhone="
                + customerHomePhone + ", customerSeatsRequired=" + customerSeatsRequired + ", customerServiceLevel="
                + customerServiceLevel + ", estimatedDistance=" + estimatedDistance + ", estimatedTripTravelTime="
                + estimatedTripTravelTime + ", numAttendants=" + numAttendants + ", numGuests=" + numGuests
                + ", outsideCoreHours=" + outsideCoreHours + ", customerInformationWithheld="
                + customerInformationWithheld + ", tripIsolation=" + tripIsolation + ", tripServiceAnimal="
                + tripServiceAnimal + ", schedulingPriority=" + schedulingPriority + ", timeWindowBefore="
                + timeWindowBefore + ", timeWindowAfter=" + timeWindowAfter + ", tripFunders=" + tripFunders
                + ", tripNotes=" + tripNotes + ", tripPurpose=" + tripPurpose
                + ", originCustomerId=" + originCustomerId + ", status=" + status + "]";
    }

}
