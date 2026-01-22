/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author chaitanyaP
 */

@Getter
@Setter
@NoArgsConstructor
public class TripTicketDTO {
    private int id;
    //previously it was requester_provider_id
    @NotNull
    @Min(1)
    @JsonProperty("origin_provider_id")
    private int originProviderId;

    @NotNull
    @JsonProperty("origin_customer_id")
    private String originCustomerId;

    @NotNull
    @JsonProperty("requester_trip_id")
    private String requesterTripId;
    @JsonProperty("common_trip_id")
    private String commonTripId;
    @JsonProperty("approved_trip_claim_id")
    private Integer approvedTripClaimId;

    @JsonProperty("customer_address")
    private AddressDTO customerAddress;
    //private Integer customer_addressId;
    @JsonProperty("customer_internal_id")
    private Integer customerInternalId;

    @NotBlank
    @Size(min = 1, max = 150)
    @JsonProperty("customer_first_name")
    private String customerFirstName;
    @JsonProperty("customer_middle_name")
    private String customerMiddleName;

    @JsonProperty("customer_nick_name")
    private String customerNickName;

    @NotBlank
    @Size(min = 1, max = 150)
    @JsonProperty("customer_last_name")
    private String customerLastName;
    @JsonProperty("customer_email")
    private String customerEmail;
    @JsonProperty("customer_home_phone")
    private String customerHomePhone;
    @JsonProperty("customer_emergency_phone")
    private String customerEmergencyPhone;

    @JsonProperty("customer_emergency_contact_name")
    private String customerEmergencyContactName;
    @JsonProperty("customer_emergency_contact_phone")
    private String customerEmergencyContactPhone;

    @JsonProperty("customer_mobile_phone")
    private String customerMobilePhone;

    //    @JsonDeserialize(using =JsonDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("customer_dob")
    private LocalDate customerDob;

    @JsonProperty("customer_gender")
    private String customerGender;
    @JsonProperty("customer_race")
    private String customerRace;
    @JsonProperty("customer_ethnicity")
    private String customerEthnicity;
    @JsonProperty("impairment_description")
    private String impairmentDescription;
    @JsonProperty("customer_information_withheld")
    private boolean customerInformationWithheld;
    @JsonProperty("primary_language")
    private String primaryLanguage;

    @JsonProperty("customer_mailing_billing_address")
    private String customerMailingBillingAddress;

    @JsonProperty("customer_caregiver_name")
    private String customerCaregiverName;

    @JsonProperty("customer_caregiver_contact_info")
    private String customerCaregiverContactInfo;

    @JsonProperty("customer_emergency_contact_relationship")
    private String customerEmergencyContactRelationship;

    @JsonProperty("customer_care_info")
    private String customerCareInfo;

    @JsonProperty("customer_funding_billing_information")
    private String customerFundingBillingInformation;

    @JsonProperty("funding_type")
    private String fundingType;

    @JsonProperty("customer_notes")
    private String customerNotes;

    @JsonProperty("boarding_time")
    private Integer boardingTime;

    @JsonProperty("deboarding_time")
    private Integer deboardingTime;
    @JsonProperty("customer_seats_required")
    private Integer customerSeatsRequired;
    @NotNull
    @Valid
    @JsonProperty("pickup_address")
    private AddressDTO pickupAddress;

    @NotNull
    @Valid
    @JsonProperty("drop_off_address")
    private AddressDTO dropOffAddress;
    @JsonProperty("scheduling_priority")
    private String schedulingPriority;
    private Integer attendants;
    private Integer guests;
    private String purpose;

    @JsonProperty("trip_notes")
    private String tripNotes;
    @JsonProperty("customer_identifiers")
    private String customerIdentifiers;
    @JsonProperty("customer_eligibility_factors")
    private String customerEligibilityFactors;
    @JsonProperty("customer_mobility_factors")
    private String customerMobilityFactors;
    @JsonProperty("customer_service_animals")
    private boolean customerServiceAnimals;
    @JsonProperty("trip_funders")
    private String tripFunders;
    @JsonProperty("customer_assistance_needs")
    private String customerAssistanceNeeds;
    @JsonProperty("attendant_mobility_factors")
    private String attendantMobilityFactors;

    @JsonProperty("customer_disability")
    private String customerDisability;

    @JsonProperty("customer_veteran")
    private Boolean customerVeteran;

    @JsonProperty("customer_poverty_level")
    private String customerPovertyLevel;
    @JsonProperty("guest_mobility_factors")
    private String guestMobilityFactors;

    private String serviceLevel;

    //    @DateTimeFormat(pattern = "yyyy-MM-dd",iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("requested_pickup_date")
    private LocalDate requestedPickupDate;
//   @JsonFormat(pattern = "yyyy-MM-dd",timezone = "UTC")

    @JsonProperty("requested_pickup_time")
    private Time requestedPickupTime;

    @JsonFormat(pattern = "yyyy-MM-dd") //yyyy-MM-dd@HH:mm:ss.SSSZ
    @JsonProperty("requested_dropoff_date")
    private LocalDate requestedDropoffDate;
    @JsonProperty("requested_dropoff_time")
    private Time requestedDropOffTime;
    @JsonProperty("earliest_pickup_time")
    private Time earliestPickupTime;
    @JsonProperty("appointment_time")
    private Time appointmentTime;
    @JsonProperty("customerLoadTime")
    private Time customerLoadTime;
    @JsonProperty("customerUnloadTime")
    private Time customerUnloadTime;
    @JsonProperty("estimated_trip_travel_time")
    private Integer estimatedTripTravelTime;
    @JsonProperty("estimated_trip_distance")
    private Float estimatedTripDistance;

    @JsonProperty("is_trip_isolation")
    private boolean isTripIsolation;
    @JsonProperty("is_outside_coreHours")
    private boolean isOutsideCoreHours;
    @JsonProperty("time_window_before")
    private Integer timeWindowBefore;
    @JsonProperty("time_window_after")
    private Integer timeWindowAfter;
    @JsonProperty("provider_white_list")
    private String providerWhiteList;
    @JsonProperty("provider_black_list")
    private String providerBlackList;
    private StatusDTO status;
    @JsonProperty("last_status_changed_by_providerId")
    private Integer lastStatusChangedByProviderId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonProperty("trip_ticket_provisional_time")
    private String tripTicketProvisionalTime;
    @JsonProperty("provisional_provider_id")
    private Integer provisionalProviderId;

    @JsonProperty("expiration_date")
    private String expirationDate;
    @JsonProperty("is_expired")
    private boolean isExpired;
    @JsonProperty("customer_custom_fields")
    private String customerCustomFields;
    @JsonProperty("trip_custom_fields")
    private String tripCustomFields;

    @JsonProperty("trip_purpose")
    private String tripPurpose;

    @JsonProperty("vehicle_type")
    private String vehicleType;

    private String version;


    //new added by shankar
    @JsonProperty("customerStatusForDuplication")
    private String customerStatusForDuplication;
    private boolean isEligibleForClaim;
    private List<String> tripFundersList;
    private boolean isTripCancel;
    @JsonProperty("isTripTicketInvisible")
    private boolean isTripTicketInvisible;

    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;


    public void setIsTripIsolation(boolean isTripIsolation) {
        this.isTripIsolation = isTripIsolation;
    }

    public boolean getIsTripIsolation() {
        return isTripIsolation;
    }

    public void setIsExpired(boolean isExpired) {
        this.isExpired = isExpired;
    }


    public void setIsEligibleForClaim(boolean isEligibleForClaim) {
        this.isEligibleForClaim = isEligibleForClaim;
    }


    public void setIsTripCancel(boolean isTripCancel) {
        this.isTripCancel = isTripCancel;
    }


    @Override
    public String toString() {
        return "TripTicketDTO{" + "id=" + id + ", originProviderId=" + originProviderId + ", originCustomerId=" + originCustomerId + ", requesterTripId=" + requesterTripId + ", commonTripId=" + commonTripId + ", approvedTripClaimId=" + approvedTripClaimId + ", customerAddress=" + customerAddress + ", customerInternalId=" + customerInternalId + ", customerFirstName=" + customerFirstName + ", customerMiddleName=" + customerMiddleName + ", customerLastName=" + customerLastName + ", customerEmail=" + customerEmail + ", customerHomePhone=" + customerHomePhone + ", customerEmergencyPhone=" + customerEmergencyPhone + ", customerDob=" + customerDob + ", gender=" + customerGender + ", customerRace=" + customerRace + ", impairmentDescription=" + impairmentDescription + ", customerInformationWithheld=" + customerInformationWithheld + ", primaryLanguage=" + primaryLanguage + ", customerNotes=" + customerNotes + ", boardingTime=" + boardingTime + ", deboardingTime=" + deboardingTime + ", customerSeatsRequired=" + customerSeatsRequired + ", pickupAddress=" + pickupAddress + ", dropOffAddress=" + dropOffAddress + ", schedulingPriority=" + schedulingPriority + ", attendants=" + attendants + ", guests=" + guests + ", purpose=" + purpose + ", tripNotes=" + tripNotes + ", customerIdentifiers=" + customerIdentifiers + ", customerEligibilityFactors=" + customerEligibilityFactors + ", customerMobilityFactors=" + customerMobilityFactors + ", customerServiceAnimals=" + customerServiceAnimals + ", tripFunders=" + tripFunders + ", customerAssistanceNeeds=" + customerAssistanceNeeds + ", attendantMobilityFactors=" + attendantMobilityFactors + ", guestMobilityFactors=" + guestMobilityFactors + ", serviceLevel=" + serviceLevel + ", requestedPickupDate=" + requestedPickupDate + ", requestedPickupTime=" + requestedPickupTime + ", requestedDropoffDate=" + requestedDropoffDate + ", requestedDropOffTime=" + requestedDropOffTime + ", earliestPickupTime=" + earliestPickupTime + ", appointmentTime=" + appointmentTime + ", customerLoadTime=" + customerLoadTime + ", customerUnloadTime=" + customerUnloadTime + ", estimatedTripTravelTime=" + estimatedTripTravelTime + ", estimatedTripDistance=" + estimatedTripDistance + ", isTripIsolation=" + isTripIsolation + ", isOutsideCoreHours=" + isOutsideCoreHours + ", timeWindowBefore=" + timeWindowBefore + ", timeWindowAfter=" + timeWindowAfter + ", providerWhiteList=" + providerWhiteList + ", providerBlackList=" + providerBlackList + ", status=" + status + ", lastStatusChangedByProviderId=" + lastStatusChangedByProviderId + ", tripTicketProvisionalTime=" + tripTicketProvisionalTime + ", provisionalProviderId=" + provisionalProviderId + ", expirationDate=" + expirationDate + ", isExpired=" + isExpired + ", customerCustomFields=" + customerCustomFields + ", tripCustomFields=" + tripCustomFields + ", version=" + version + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + '}';
    }

}
