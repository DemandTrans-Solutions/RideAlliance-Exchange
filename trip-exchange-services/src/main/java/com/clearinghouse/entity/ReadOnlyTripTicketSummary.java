package com.clearinghouse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Mapping for DB view
 */
@Getter
@Entity
@Immutable
@Table(name = "v_tripticket_summary")
public class ReadOnlyTripTicketSummary {
    @Id
    @NotNull
    @Column(name = "TripTicketID", nullable = false)
    private Integer tripTicketID;

    @NotNull
    @Column(name = "RequesterProviderID", nullable = false)
    private Integer requesterProviderID;

    @Size(max = 50)
    @NotNull
    @Column(name = "RequesterCustomerID", nullable = false, length = 50)
    private String requesterCustomerID;

    @NotNull
    @Column(name = "StatusID", nullable = false)
    private Integer statusID;

    @Size(max = 50)
    @NotNull
    @Column(name = "RequesterTripID", nullable = false, length = 50)
    private String requesterTripID;

    @Size(max = 64)
    @NotNull
    @Column(name = "CommonTripID", nullable = false, length = 64)
    private String commonTripID;

    @Column(name = "ApprovedTripClaimID")
    private Integer approvedTripClaimID;

    @NotNull
    @Column(name = "CustomerAddressID", nullable = false)
    private Integer customerAddressID;

    @Column(name = "CustomerInternalID")
    private Integer customerInternalID;

    @NotNull
    @Column(name = "PickupAddressID", nullable = false)
    private Integer pickupAddressID;

    @NotNull
    @Column(name = "DropOffAddressID", nullable = false)
    private Integer dropOffAddressID;

    @Size(max = 150)
    @NotNull
    @Column(name = "CustomerFirstName", nullable = false, length = 150)
    private String customerFirstName;

    @Size(max = 150)
    @Column(name = "CustomerMiddleName", length = 150)
    private String customerMiddleName;

    @Size(max = 150)
    @NotNull
    @Column(name = "CustomerLastName", nullable = false, length = 150)
    private String customerLastName;

    @Size(max = 100)
    @Column(name = "CustomerEmail", length = 100)
    private String customerEmail;

    @Size(max = 20)
    @Column(name = "CustomerHomePhone", length = 20)
    private String customerHomePhone;

    @Size(max = 20)
    @Column(name = "CustomerEmergencyPhone", length = 20)
    private String customerEmergencyPhone;

    @Column(name = "CustomerDateOfBirth")
    private Instant customerDateOfBirth;

    @Size(max = 10)
    @Column(name = "CustomerGender", length = 10)
    private String gender;

    @Size(max = 100)
    @Column(name = "CustomerRace", length = 100)
    private String customerRace;

    @Size(max = 200)
    @Column(name = "ImpairmentDescription", length = 200)
    private String impairmentDescription;

    @Column(name = "IsInformationWithheld")
    private Boolean isInformationWithheld;

    @Size(max = 50)
    @Column(name = "PrimaryLanguage", length = 50)
    private String primaryLanguage;

    @Lob
    @Column(name = "CustomerNotes")
    private String customerNotes;

    @Column(name = "BoardingTime")
    private Integer boardingTime;

    @Column(name = "DeboardingTime")
    private Integer deboardingTime;

    @Column(name = "SeatsRequired")
    private Integer seatsRequired;

    @Size(max = 10)
    @Column(name = "SchedulingPriority", length = 10)
    private String schedulingPriority;

    @Column(name = "Attendants")
    private Integer attendants;

    @Column(name = "Guests")
    private Integer guests;

    @Size(max = 200)
    @Column(name = "Purpose", length = 200)
    private String purpose;

    @Lob
    @Column(name = "TripNotes")
    private String tripNotes;

    @Size(max = 200)
    @Column(name = "CustomerIdentifiers", length = 200)
    private String customerIdentifiers;

    @Size(max = 200)
    @Column(name = "CustomerEligibilityFactors", length = 200)
    private String customerEligibilityFactors;

    @Size(max = 200)
    @Column(name = "CustomerMobilityFactors", length = 200)
    private String customerMobilityFactors;

    @Column(name = "IsServiceAnimals")
    private Boolean isServiceAnimals;

    @Size(max = 200)
    @Column(name = "TripFunders", length = 200)
    private String tripFunders;

    @Size(max = 200)
    @Column(name = "CustomerAssistanceNeeds", length = 200)
    private String customerAssistanceNeeds;

    @Size(max = 200)
    @Column(name = "AttendantMobilityFactors", length = 200)
    private String attendantMobilityFactors;

    @Size(max = 200)
    @Column(name = "GuestMobilityFactors", length = 200)
    private String guestMobilityFactors;

    @Size(max = 200)
    @Column(name = "ServiceLevel", length = 200)
    private String serviceLevel;

    @Column(name = "RequestedPickupDate")
    private LocalDate requestedPickupDate;

    @Column(name = "RequestedPickupTime")
    private LocalTime requestedPickupTime;

    @Column(name = "RequestedDropOffDate")
    private LocalDate requestedDropOffDate;

    @Column(name = "RequestedDropOffTime")
    private LocalTime requestedDropOffTime;

    @Column(name = "EarliestPickupTime")
    private LocalTime earliestPickupTime;

    @Column(name = "AppointmentTime")
    private LocalTime appointmentTime;

    @Column(name = "CustomerLoadTime")
    private LocalTime customerLoadTime;

    @Column(name = "CustomerUnloadTime")
    private LocalTime customerUnloadTime;

    @Column(name = "EstimatedTripDistance", precision = 10)
    private BigDecimal estimatedTripDistance;

    @Column(name = "EstimatedTripTravelTime")
    private Integer estimatedTripTravelTime;

    @Column(name = "IsTripIsolation")
    private Boolean isTripIsolation;

    @Column(name = "IsOutsideCoreHours")
    private Boolean isOutsideCoreHours;

    @Column(name = "TimeWindowBefore")
    private Integer timeWindowBefore;

    @Column(name = "TimeWindowAfter")
    private Integer timeWindowAfter;

    @Size(max = 200)
    @Column(name = "ProviderWhiteList", length = 200)
    private String providerWhiteList;

    @Size(max = 200)
    @Column(name = "ProviderBlackList", length = 200)
    private String providerBlackList;

    @Column(name = "LastStatusChangedByProviderID")
    private Integer lastStatusChangedByProviderID;

    @Column(name = "TripTicketProvisionalTime")
    private Instant tripTicketProvisionalTime;

    @Column(name = "ProvisionalProviderID")
    private Integer provisionalProviderID;

    @Column(name = "ExpirationDate")
    private Instant expirationDate;

    @Column(name = "IsExpired")
    private Boolean isExpired;

    @Lob
    @Column(name = "CustomerCustomFields")
    private String customerCustomFields;

    @Lob
    @Column(name = "TripCustomFields")
    private String tripCustomFields;

    @NotNull
    @Column(name = "Version", nullable = false)
    private Integer version;

    @Column(name = "IsRejected")
    private Boolean isRejected;

    @Size(max = 200)
    @Column(name = "RejectedReason", length = 200)
    private String rejectedReason;

    @NotNull
    @Column(name = "IsInvisible", nullable = false)
    private Boolean isInvisible = false;

    @Column(name = "AddedBy")
    private Integer addedBy;

    @Column(name = "AddedOn")
    private Instant addedOn;

    @Column(name = "UpdatedBy")
    private Integer updatedBy;

    @Column(name = "UpdatedOn")
    private Instant updatedOn;

    @Column(name = "RequesterProviderFare", precision = 10, scale = 2)
    private BigDecimal requesterProviderFare;

    @Column(name = "OriginProviderID")
    private Integer originProviderID;

    @Size(max = 255)
    @Column(name = "OriginProviderName")
    private String originProviderName;

    @Size(max = 255)
    @Column(name = "OriginProviderEmail")
    private String originProviderEmail;

    @Column(name = "OriginProviderTypeId")
    private Integer originProviderTypeId;

    @Size(max = 255)
    @Column(name = "LastStatusChangedByProviderName")
    private String lastStatusChangedByProviderName;

    @Size(max = 255)
    @Column(name = "LastStatusChangedByProviderEmail")
    private String lastStatusChangedByProviderEmail;

    @Size(max = 255)
    @Column(name = "ProvisionalProviderName")
    private String provisionalProviderName;

    @Size(max = 255)
    @Column(name = "ProvisionalProviderEmail")
    private String provisionalProviderEmail;

    @Size(max = 1000)
    @Column(name = "CustomerAddressStreet1", length = 1000)
    private String customerAddressStreet1;

    @Size(max = 255)
    @Column(name = "CustomerAddressStreet2")
    private String customerAddressStreet2;

    @Size(max = 100)
    @Column(name = "CustomerAddressCity", length = 100)
    private String customerAddressCity;

    @Size(max = 100)
    @Column(name = "CustomerAddressCounty", length = 100)
    private String customerAddressCounty;

    @Size(max = 100)
    @Column(name = "CustomerAddressState", length = 100)
    private String customerAddressState;

    @Size(max = 10)
    @Column(name = "CustomerAddressZipCode", length = 10)
    private String customerAddressZipCode;

    @Size(max = 255)
    @Column(name = "CustomerAddressCommonName")
    private String customerAddressCommonName;

    @Size(max = 20)
    @Column(name = "CustomerAddressPhoneNumber", length = 20)
    private String customerAddressPhoneNumber;

    @Column(name = "CustomerAddressLatitude")
    private Float customerAddressLatitude;

    @Column(name = "CustomerAddressLongitude")
    private Float customerAddressLongitude;

    @Size(max = 1000)
    @Column(name = "PickupAddressStreet1", length = 1000)
    private String pickupAddressStreet1;

    @Size(max = 255)
    @Column(name = "PickupAddressStreet2")
    private String pickupAddressStreet2;

    @Size(max = 100)
    @Column(name = "PickupAddressCity", length = 100)
    private String pickupAddressCity;

    @Size(max = 100)
    @Column(name = "PickupAddressCounty", length = 100)
    private String pickupAddressCounty;

    @Size(max = 100)
    @Column(name = "PickupAddressState", length = 100)
    private String pickupAddressState;

    @Size(max = 10)
    @Column(name = "PickupAddressZipCode", length = 10)
    private String pickupAddressZipCode;

    @Size(max = 255)
    @Column(name = "PickupAddressCommonName")
    private String pickupAddressCommonName;

    @Size(max = 20)
    @Column(name = "PickupAddressPhoneNumber", length = 20)
    private String pickupAddressPhoneNumber;

    @Column(name = "PickupAddressLatitude")
    private Float pickupAddressLatitude;

    @Column(name = "PickupAddressLongitude")
    private Float pickupAddressLongitude;

    @Size(max = 1000)
    @Column(name = "DropOffAddressStreet1", length = 1000)
    private String dropOffAddressStreet1;

    @Size(max = 255)
    @Column(name = "DropOffAddressStreet2")
    private String dropOffAddressStreet2;

    @Size(max = 100)
    @Column(name = "DropOffAddressCity", length = 100)
    private String dropOffAddressCity;

    @Size(max = 100)
    @Column(name = "DropOffAddressCounty", length = 100)
    private String dropOffAddressCounty;

    @Size(max = 100)
    @Column(name = "DropOffAddressState", length = 100)
    private String dropOffAddressState;

    @Size(max = 10)
    @Column(name = "DropOffAddressZipCode", length = 10)
    private String dropOffAddressZipCode;

    @Size(max = 255)
    @Column(name = "DropOffAddressCommonName")
    private String dropOffAddressCommonName;

    @Column(name = "DropOffAddressLatitude")
    private Float dropOffAddressLatitude;

    @Column(name = "DropOffAddressLongitude")
    private Float dropOffAddressLongitude;

    @Size(max = 50)
    @Column(name = "Status_Type", length = 50)
    private String statusType;

    @Size(max = 255)
    @Column(name = "StatusDescription")
    private String statusDescription;

    @Column(name = "ApprovedTripClaimTripClaimID")
    private Integer approvedTripClaimTripClaimID;

    @Column(name = "ApprovedTripClaimClaimantProviderID")
    private Integer approvedTripClaimClaimantProviderID;

    @Column(name = "approvedTripClaim_StatusID")
    private Integer approvedtripclaimStatusid;

    @Column(name = "ApprovedTripClaimAcknowledgementStatus")
    private Boolean approvedTripClaimAcknowledgementStatus;

    @Column(name = "ApprovedTripClaimProposedPickupTime")
    private Instant approvedTripClaimProposedPickupTime;

    @Column(name = "ApprovedTripClaimRequesterProviderFare", precision = 10, scale = 2)
    private BigDecimal approvedTripClaimRequesterProviderFare;

    @Column(name = "ApprovedTripClaimCalculatedProposedFare", precision = 10, scale = 2)
    private BigDecimal approvedTripClaimCalculatedProposedFare;

    @Column(name = "ApprovedTripClaimProposedFare", precision = 10, scale = 2)
    private BigDecimal approvedTripClaimProposedFare;

    @Size(max = 500)
    @Column(name = "ApprovedTripClaimNotes", length = 500)
    private String approvedTripClaimNotes;

    @Column(name = "ApprovedTripClaimExpirationDate")
    private Instant approvedTripClaimExpirationDate;

    @Column(name = "ApprovedTripClaimIsExpired")
    private Boolean approvedTripClaimIsExpired;

    @Column(name = "ApprovedTripClaimAddedBy")
    private Integer approvedTripClaimAddedBy;

    @Column(name = "ApprovedTripClaimAddedOn")
    private Instant approvedTripClaimAddedOn;

    @Column(name = "ApprovedTripClaimUpdatedBy")
    private Integer approvedTripClaimUpdatedBy;

    @Column(name = "ApprovedTripClaimUpdatedOn")
    private Instant approvedTripClaimUpdatedOn;

    @Column(name = "TripResultNoShowFlag")
    private Boolean tripResultNoShowFlag;

    @Column(name = "TripResultTripDate")
    private LocalDate tripResultTripDate;

    @Column(name = "TripResultActualPickupArriveTime")
    private LocalTime tripResultActualPickupArriveTime;

    @Column(name = "TripResultActualPickupDepartTime")
    private LocalTime tripResultActualPickupDepartTime;

    @Column(name = "TripResultActualDropOffArriveTime")
    private LocalTime tripResultActualDropOffArriveTime;

    @Column(name = "TripResultActualDropOffDepartTime")
    private LocalTime tripResultActualDropOffDepartTime;

    @Size(max = 150)
    @Column(name = "TripResultPickupLatitude", length = 150)
    private String tripResultPickupLatitude;

    @Size(max = 150)
    @Column(name = "TripResult_PickupLongitude", length = 150)
    private String tripresultPickuplongitude;

    @Size(max = 150)
    @Column(name = "TripResultDropOffLatitude", length = 150)
    private String tripResultDropOffLatitude;

    @Size(max = 150)
    @Column(name = "TripResultDropOffLongitude", length = 150)
    private String tripResultDropOffLongitude;

    @Column(name = "TripResultFareCollected", precision = 5, scale = 2)
    private BigDecimal tripResultFareCollected;

    @Column(name = "TripResultNumberOfPassengers")
    private Integer tripResultNumberOfPassengers;

    @Column(name = "TripResultNumberOfGuests")
    private Integer tripResultNumberOfGuests;

    @Column(name = "TripResultNumberOfAttendants")
    private Integer tripResultNumberOfAttendants;

    @Size(max = 50)
    @Column(name = "TripResultRateType", length = 50)
    private String tripResultRateType;

    @Column(name = "TripResultRate", precision = 10)
    private BigDecimal tripResultRate;

    @Size(max = 150)
    @Column(name = "TripResultDriverName", length = 150)
    private String tripResultDriverName;

    @Size(max = 50)
    @Column(name = "TripResultVehicleType", length = 50)
    private String tripResultVehicleType;

    @Size(max = 50)
    @Column(name = "TripResultVehicleName", length = 50)
    private String tripResultVehicleName;

    @Size(max = 50)
    @Column(name = "TripResultFareType", length = 50)
    private String tripResultFareType;

    @Column(name = "TripResultBaseFare", precision = 18, scale = 4)
    private BigDecimal tripResultBaseFare;

    @Column(name = "TripResultFare", precision = 18, scale = 4)
    private BigDecimal tripResultFare;

    @Column(name = "TripResultMilesTraveled", precision = 18, scale = 4)
    private BigDecimal tripResultMilesTraveled;

    @Column(name = "TripResultBillableMileage", precision = 18, scale = 4)
    private BigDecimal tripResultBillableMileage;

    @Size(max = 4000)
    @Column(name = "TripResultNotes", length = 4000)
    private String tripResultNotes;

    @Size(max = 1000)
    @Column(name = "TripResultOutcome", length = 1000)
    private String tripResultOutcome;

    @Column(name = "TripResultAddedBy")
    private Integer tripResultAddedBy;

    @Column(name = "TripResultAddedOn")
    private Instant tripResultAddedOn;

    @Column(name = "TripResultUpdatedBy")
    private Integer tripResultUpdatedBy;

    @Column(name = "TripResultUpdatedOn")
    private Instant tripResultUpdatedOn;

}