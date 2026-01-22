/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Formula;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

import static jakarta.persistence.FetchType.LAZY;

/**
 *
 * @author chaitanyaP
 */
@Entity
@Table(name = "tripticket" )
public class TripTicket extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TripTicketID")
    private int id;

    // Default constructor
    public TripTicket() {
    }

    // Constructor with ID parameter
    public TripTicket(int id) {
        this.id = id;
    }

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "RequesterProviderID")
    private Provider originProvider;

    @Column(name = "RequesterCustomerID")
    private String originCustomerId;

    @Column(name = "RequesterTripID")
    private String requesterTripId;

    // unique constraints..
    @Column(name = "CommonTripID")
    private String commonTripId;

    // mappingwith tripClaimTable
    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ApprovedTripClaimID")
    private TripClaim approvedTripClaim;

    // mapping with address table..
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CustomerAddressID")
    private Address customerAddress;

    @Column(name = "CustomerInternalID")
    private Integer customerInternalId;

    @Column(name = "CustomerFirstName")
    private String customerFirstName;

    @Column(name = "CustomerMiddleName")
    private String customerMiddleName;

    @Column(name = "CustomerNickName")
    private String customerNickName;

    @Column(name = "CustomerLastName")
    private String customerLastName;

    @Column(name = "CustomerEmail")
    private String customerEmail;

    @Column(name = "CustomerCaregiverContactInfo")
    private String customerCaregiverContactInfo;

    @Column(name = "CustomerHomePhone")
    private String customerHomePhone;

    @Column(name = "CustomerMobilePhone")
    private String customerMobilePhone;

    @Column(name = "CustomerEmergencyPhone")
    private String customerEmergencyPhone;

    @Column(name = "CustomerEmergencyContactName")
    private String customerEmergencyContactName;

    @Column(name = "CustomerEmergencyContactPhone")
    private String customerEmergencyContactPhone;

    @Column(name = "CustomerEmergencyContactRelationship")
    private String customerEmergencyContactRelationship;

    @Column(name = "CustomerMailingBillingAddress")
    private String customerMailingBillingAddress;

    @Column(name = "CustomerCaregiverName")
    private String customerCaregiverName;

    @Column(name = "CustomerCareInfo")
    private String customerCareInfo;

    @Column(name = "CustomerFundingBillingInformation")
    private String customerFundingBillingInformation;

    @Column(name = "FundingType")
    private String fundingType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "CustomerDateOfBirth")
    private LocalDate customerDob;

    @Column(name = "CustomerGender")
    private String customerGender;

    @Column(name = "CustomerRace")
    private String customerRace;

    @Column(name = "CustomerEthnicity")
    private String customerEthnicity;

    @Column(name = "ImpairmentDescription")
    private String impairmentDescription;

    @Column(name = "IsInformationWithheld")
    private Boolean customerInformationWithheld;

    @Column(name = "PrimaryLanguage")
    private String primaryLanguage;

    @Column(name = "CustomerNotes")
    private String customerNotes;

    @Column(name = "BoardingTime")
    private Integer boardingTime;

    @Column(name = "DeboardingTime")
    private Integer deboardingTime;

    @Column(name = "SeatsRequired")
    private Integer customerSeatsRequired;

    // mapping with address table..
    @OneToOne//(cascade = { CascadeType.MERGE })
    @JoinColumn(name = "PickupAddressID")
    private Address pickupAddress;

    // mapping with address table..
    @OneToOne//(cascade = { CascadeType.MERGE })
    @JoinColumn(name = "DropOffAddressID")
    private Address dropOffAddress;

    @Column(name = "SchedulingPriority")
    private String schedulingPriority;

    @Column(name = "Attendants")
    private Integer attendants;


    @Column(name = "RequesterProviderFare")
    private Float requesterProviderFare;

    @Column(name = "Guests")
    private Integer guests;

    @Column(name = "Purpose")
    private String tripPurpose;

    @Column(name = "TripNotes")
    private String tripNotes;

    @Column(name = "CustomerIdentifiers")
    private String customerIdentifiers;

    @Column(name = "CustomerEligibilityFactors")
    private String customerEligibilityFactors;

    @Column(name = "CustomerMobilityFactors")
    private String customerMobilityFactors;

    @Column(name = "IsServiceAnimals")
    private Boolean customerServiceAnimals;

    @Column(name = "TripFunders")
    private String tripFunders;

    @Column(name = "CustomerAssistanceNeeds")
    private String customerAssistanceNeeds;

    @Column(name = "AttendantMobilityFactors")
    private String attendantMobilityFactors;

    @Column(name = "GuestMobilityFactors")
    private String guestMobilityFactors;

    @Column(name = "ServiceLevel")
    private String serviceLevel;


    @Temporal(TemporalType.DATE)
    @Column(name = "RequestedPickupDate")
    private LocalDate requestedPickupDate;

    @Column(name = "RequestedPickupTime")
    private Time requestedPickupTime;


    @Temporal(TemporalType.DATE)
    @Column(name = "RequestedDropOffDate")
    private LocalDate requestedDropoffDate;

    @Column(name = "RequestedDropOffTime")
    private Time requestedDropOffTime;

    @Column(name = "EarliestPickupTime")
    private Time earliestPickupTime;

    @Column(name = "AppointmentTime")
    private Time appointmentTime;

    @Column(name = "CustomerLoadTime")
    private Time customerLoadTime;

    @Column(name = "CustomerUnloadTime")
    private Time customerUnloadTime;

    @Column(name = "IsCustomerVeteran")
    private Boolean customerVeteran;

    @Column(name = "EstimatedTripDistance")
    private Float estimatedTripDistance;

    @Column(name = "EstimatedTripTravelTime")
    private Integer estimatedTripTravelTime;

    @Column(name = "IsTripIsolation")
    private Boolean tripIsolation;

    @Column(name = "IsOutsideCoreHours")
    private Boolean outsideCoreHours;

    @Column(name = "TimeWindowBefore")
    private Integer timeWindowBefore;

    @Column(name = "TimeWindowAfter")
    private Integer timeWindowAfter;

    @Column(name = "ProviderWhiteList")
    private String providerWhiteList;

    @Column(name = "ProviderBlackList")
    private String providerBlackList;

    @Column(name = "VehicleType")
    private String vehicleType;

    @Column(name="TripPurpose")
    private String tripPurposeDescription;

    @Column(name="CustomerPovertyLevel")
    private String customerPovertyLevel;

    @Column(name="CustomerDisability")
    private String customerDisability;

    // mapping with status table..
    @OneToOne
    @JoinColumn(name = "StatusID")
    private Status status;

    // mapping with provider..
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LastStatusChangedByProviderID")
    private Provider lastStatusChangedByProvider;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSZ")
//    @Convert(converter = LocalDateTimeAttributeConverter.class)
    @Column(name = "TripTicketProvisionalTime")
    private LocalDateTime tripTicketProvisionalTime;

    // mapping with provider table..
    @OneToOne
    @JoinColumn(name = "ProvisionalProviderID")
    private Provider provisionalProvider;

    @Column(name = "ExpirationDate")
    private LocalDateTime expirationDate;

    @Column(name = "IsExpired")
    private Boolean expired;

    @Column(name = "CustomerCustomFields")
    private String customerCustomFields;

    @Column(name = "TripCustomFields")
    private String tripCustomFields;

    @Column(name = "Version")
    private String version;

    @OneToMany(cascade = CascadeType.ALL, fetch = LAZY)
    @JoinColumn(name = "TripTicketID", referencedColumnName = "TripTicketID", updatable = false)
    private Set<ClaimantTripTicket> claimantTripTicket;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "TripTicketID", referencedColumnName = "TripTicketID", updatable = false)
    private Set<TripClaim> tripClaims;

    @OneToOne(mappedBy = "tripTicket", fetch = FetchType.LAZY)
//    @JoinColumn(name = "TripTicketID")
    private TripResult tripResult;

    @OneToMany(cascade = CascadeType.ALL, fetch = LAZY)
    @JoinColumn(name = "TripTicketID", referencedColumnName = "TripTicketID", updatable = false)
    private Set<TripTicketComment> tripTicketComments;

    // Use quoted column names in formula to match the test schema's mixed-case column identifiers
    @Formula(value = "concat(\"RequestedPickupDate\",' ',\"RequestedPickupTime\")")
    String pickupDateTime;

    @Column(name = "IsInvisible")
    private Boolean tripTicketInvisible;

    @Column(name = "VectorID")
    private String vectorStoreId;


    public void setCustomerInternalId(Integer customerInternalId) {
        this.customerInternalId = customerInternalId;
    }


    public Boolean getTripTicketInvisible() {
        return tripTicketInvisible;
    }

    public void setTripTicketInvisible(Boolean tripTicketInvisible) {
        this.tripTicketInvisible = tripTicketInvisible;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public String getCustomerDisability() {
        return customerDisability;
    }

    public void setCustomerDisability(String customerDisability) {
        this.customerDisability = customerDisability;
    }

    public String getCustomerPovertyLevel() {
        return customerPovertyLevel;
    }

    public void setCustomerPovertyLevel(String customerPovertyLevel) {
        this.customerPovertyLevel = customerPovertyLevel;
    }

    public String getTripPurposeDescription() {
        return tripPurposeDescription;
    }

    public void setTripPurposeDescription(String tripPurposeDescription) {
        this.tripPurposeDescription = tripPurposeDescription;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public void setTimeWindowAfter(Integer timeWindowAfter) {
        this.timeWindowAfter = timeWindowAfter;
    }

    public void setTimeWindowBefore(Integer timeWindowBefore) {
        this.timeWindowBefore = timeWindowBefore;
    }

    public Boolean getOutsideCoreHours() {
        return outsideCoreHours;
    }

    public void setOutsideCoreHours(Boolean outsideCoreHours) {
        this.outsideCoreHours = outsideCoreHours;
    }

    public Boolean getTripIsolation() {
        return tripIsolation;
    }

    public void setTripIsolation(Boolean tripIsolation) {
        this.tripIsolation = tripIsolation;
    }

    public void setEstimatedTripTravelTime(Integer estimatedTripTravelTime) {
        this.estimatedTripTravelTime = estimatedTripTravelTime;
    }

    public void setEstimatedTripDistance(Float estimatedTripDistance) {
        this.estimatedTripDistance = estimatedTripDistance;
    }

    public Boolean getCustomerVeteran() {
        return customerVeteran;
    }

    public void setCustomerVeteran(Boolean customerVeteran) {
        this.customerVeteran = customerVeteran;
    }

    public Boolean getCustomerServiceAnimals() {
        return customerServiceAnimals;
    }

    public void setCustomerServiceAnimals(Boolean customerServiceAnimals) {
        this.customerServiceAnimals = customerServiceAnimals;
    }

    public String getTripPurpose() {
        return tripPurpose;
    }

    public void setTripPurpose(String tripPurpose) {
        this.tripPurpose = tripPurpose;
    }

    public void setGuests(Integer guests) {
        this.guests = guests;
    }

    public void setAttendants(Integer attendants) {
        this.attendants = attendants;
    }

    public void setCustomerSeatsRequired(Integer customerSeatsRequired) {
        this.customerSeatsRequired = customerSeatsRequired;
    }

    public void setDeboardingTime(Integer deboardingTime) {
        this.deboardingTime = deboardingTime;
    }

    public void setBoardingTime(Integer boardingTime) {
        this.boardingTime = boardingTime;
    }

    public Boolean getCustomerInformationWithheld() {
        return customerInformationWithheld;
    }

    public void setCustomerInformationWithheld(Boolean customerInformationWithheld) {
        this.customerInformationWithheld = customerInformationWithheld;
    }

    public String getCustomerEthnicity() {
        return customerEthnicity;
    }

    public void setCustomerEthnicity(String customerEthnicity) {
        this.customerEthnicity = customerEthnicity;
    }

    public String getCustomerGender() {
        return customerGender;
    }

    public void setCustomerGender(String customerGender) {
        this.customerGender = customerGender;
    }

    public String getFundingType() {
        return fundingType;
    }

    public void setFundingType(String fundingType) {
        this.fundingType = fundingType;
    }

    public String getCustomerFundingBillingInformation() {
        return customerFundingBillingInformation;
    }

    public void setCustomerFundingBillingInformation(String customerFundingBillingInformation) {
        this.customerFundingBillingInformation = customerFundingBillingInformation;
    }

    public String getCustomerCareInfo() {
        return customerCareInfo;
    }

    public void setCustomerCareInfo(String customerCareInfo) {
        this.customerCareInfo = customerCareInfo;
    }

    public String getCustomerCaregiverName() {
        return customerCaregiverName;
    }

    public void setCustomerCaregiverName(String customerCaregiverName) {
        this.customerCaregiverName = customerCaregiverName;
    }

    public String getCustomerMailingBillingAddress() {
        return customerMailingBillingAddress;
    }

    public void setCustomerMailingBillingAddress(String customerMailingBillingAddress) {
        this.customerMailingBillingAddress = customerMailingBillingAddress;
    }

    public String getCustomerEmergencyContactRelationship() {
        return customerEmergencyContactRelationship;
    }

    public void setCustomerEmergencyContactRelationship(String customerEmergencyContactRelationship) {
        this.customerEmergencyContactRelationship = customerEmergencyContactRelationship;
    }

    public String getCustomerEmergencyContactPhone() {
        return customerEmergencyContactPhone;
    }

    public void setCustomerEmergencyContactPhone(String customerEmergencyContactPhone) {
        this.customerEmergencyContactPhone = customerEmergencyContactPhone;
    }

    public String getCustomerMobilePhone() {
        return customerMobilePhone;
    }

    public void setCustomerMobilePhone(String customerMobilePhone) {
        this.customerMobilePhone = customerMobilePhone;
    }

    public String getCustomerHomePhone() {
        return customerHomePhone;
    }

    public void setCustomerHomePhone(String customerHomePhone) {
        this.customerHomePhone = customerHomePhone;
    }

    public String getCustomerCaregiverContactInfo() {
        return customerCaregiverContactInfo;
    }

    public void setCustomerCaregiverContactInfo(String customerCaregiverContactInfo) {
        this.customerCaregiverContactInfo = customerCaregiverContactInfo;
    }

    public String getCustomerNickName() {
        return customerNickName;
    }

    public void setCustomerNickName(String customerNickName) {
        this.customerNickName = customerNickName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Provider getOriginProvider() {
        return originProvider;
    }

    public void setOriginProvider(Provider originProvider) {
        this.originProvider = originProvider;
    }

    public String getOriginCustomerId() {
        return originCustomerId;
    }

    public void setOriginCustomerId(String originCustomerId) {
        this.originCustomerId = originCustomerId;
    }

    public String getRequesterTripId() {
        return requesterTripId;
    }

    public void setRequesterTripId(String requesterTripId) {
        this.requesterTripId = requesterTripId;
    }

    public String getCommonTripId() {
        return commonTripId;
    }

    public void setCommonTripId(String commonTripId) {
        this.commonTripId = commonTripId;
    }

    public TripClaim getApprovedTripClaim() {
        return approvedTripClaim;
    }

    public void setApprovedTripClaim(TripClaim approvedTripClaim) {
        this.approvedTripClaim = approvedTripClaim;
    }

    public Address getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(Address customerAddress) {
        this.customerAddress = customerAddress;
    }

    public Integer getCustomerInternalId() {
        return customerInternalId;
    }



    public String getCustomerFirstName() {
        return customerFirstName;
    }

    public void setCustomerFirstName(String customerFirstName) {
        this.customerFirstName = customerFirstName;
    }

    public String getCustomerMiddleName() {
        return customerMiddleName;
    }

    public void setCustomerMiddleName(String customerMiddleName) {
        this.customerMiddleName = customerMiddleName;
    }

    public String getCustomerLastName() {
        return customerLastName;
    }

    public void setCustomerLastName(String customerLastName) {
        this.customerLastName = customerLastName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }


    public String getCustomerEmergencyPhone() {
        return customerEmergencyPhone;
    }

    public void setCustomerEmergencyPhone(String customerEmergencyPhone) {
        this.customerEmergencyPhone = customerEmergencyPhone;
    }

    public Float getRequesterProviderFare() {
        return requesterProviderFare;
    }

    public void setRequesterProviderFare(Float requesterProviderFare) {
        this.requesterProviderFare = requesterProviderFare;
    }

    public LocalDate getCustomerDob() {
        return customerDob;
    }

    public void setCustomerDob(LocalDate customerDob) {
        this.customerDob = customerDob;
    }



    public String getCustomerRace() {
        return customerRace;
    }

    public void setCustomerRace(String customerRace) {
        this.customerRace = customerRace;
    }

    public String getImpairmentDescription() {
        return impairmentDescription;
    }

    public void setImpairmentDescription(String impairmentDescription) {
        this.impairmentDescription = impairmentDescription;
    }

    public boolean isCustomerInformationWithheld() {
        return customerInformationWithheld;
    }

    public void setCustomerInformationWithheld(boolean customerInformationWithheld) {
        this.customerInformationWithheld = customerInformationWithheld;
    }

    public String getPrimaryLanguage() {
        return primaryLanguage;
    }

    public void setPrimaryLanguage(String primaryLanguage) {
        this.primaryLanguage = primaryLanguage;
    }

    public String getCustomerNotes() {
        return customerNotes;
    }

    public void setCustomerNotes(String customerNotes) {
        this.customerNotes = customerNotes;
    }

    public Integer getBoardingTime() {
        return boardingTime;
    }



    public Integer getDeboardingTime() {
        return deboardingTime;
    }



    public Integer getCustomerSeatsRequired() {
        return customerSeatsRequired;
    }



    public Address getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(Address pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public Address getDropOffAddress() {
        return dropOffAddress;
    }

    public void setDropOffAddress(Address dropOffAddress) {
        this.dropOffAddress = dropOffAddress;
    }

    public String getSchedulingPriority() {
        return schedulingPriority;
    }

    public void setSchedulingPriority(String schedulingPriority) {
        this.schedulingPriority = schedulingPriority;
    }

    public Integer getAttendants() {
        return attendants;
    }



    public Integer getGuests() {
        return guests;
    }





    public String getTripNotes() {
        return tripNotes;
    }

    public void setTripNotes(String tripNotes) {
        this.tripNotes = tripNotes;
    }

    public String getCustomerIdentifiers() {
        return customerIdentifiers;
    }

    public void setCustomerIdentifiers(String customerIdentifiers) {
        this.customerIdentifiers = customerIdentifiers;
    }

    public String getCustomerEligibilityFactors() {
        return customerEligibilityFactors;
    }

    public void setCustomerEligibilityFactors(String customerEligibilityFactors) {
        this.customerEligibilityFactors = customerEligibilityFactors;
    }

    public String getCustomerMobilityFactors() {
        return customerMobilityFactors;
    }

    public void setCustomerMobilityFactors(String customerMobilityFactors) {
        this.customerMobilityFactors = customerMobilityFactors;
    }

    public boolean isCustomerServiceAnimals() {
        return customerServiceAnimals;
    }

    public void setCustomerServiceAnimals(boolean customerServiceAnimals) {
        this.customerServiceAnimals = customerServiceAnimals;
    }

    public String getTripFunders() {
        return tripFunders;
    }

    public void setTripFunders(String tripFunders) {
        this.tripFunders = tripFunders;
    }

    public String getCustomerAssistanceNeeds() {
        return customerAssistanceNeeds;
    }

    public void setCustomerAssistanceNeeds(String customerAssistanceNeeds) {
        this.customerAssistanceNeeds = customerAssistanceNeeds;
    }

    public String getAttendantMobilityFactors() {
        return attendantMobilityFactors;
    }

    public void setAttendantMobilityFactors(String attendantMobilityFactors) {
        this.attendantMobilityFactors = attendantMobilityFactors;
    }

    public String getGuestMobilityFactors() {
        return guestMobilityFactors;
    }

    public void setGuestMobilityFactors(String guestMobilityFactors) {
        this.guestMobilityFactors = guestMobilityFactors;
    }

    public String getServiceLevel() {
        return serviceLevel;
    }

    public void setServiceLevel(String serviceLevel) {
        this.serviceLevel = serviceLevel;
    }

    public LocalDate getRequestedPickupDate() {
        return requestedPickupDate;
    }

    public void setRequestedPickupDate(LocalDate requestedPickupDate) {
        this.requestedPickupDate = requestedPickupDate;
    }

    public Time getRequestedPickupTime() {
        return requestedPickupTime;
    }

    public void setRequestedPickupTime(Time requestedPickupTime) {
        this.requestedPickupTime = requestedPickupTime;
    }

    public LocalDate getRequestedDropoffDate() {
        return requestedDropoffDate;
    }

    public void setRequestedDropoffDate(LocalDate requestedDropoffDate) {
        this.requestedDropoffDate = requestedDropoffDate;
    }

    public Time getRequestedDropOffTime() {
        return requestedDropOffTime;
    }

    public void setRequestedDropOffTime(Time requestedDropOffTime) {
        this.requestedDropOffTime = requestedDropOffTime;
    }

    public Time getEarliestPickupTime() {
        return earliestPickupTime;
    }

    public void setEarliestPickupTime(Time earliestPickupTime) {
        this.earliestPickupTime = earliestPickupTime;
    }

    public Time getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(Time appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public Time getCustomerLoadTime() {
        return customerLoadTime;
    }

    public void setCustomerLoadTime(Time customerLoadTime) {
        this.customerLoadTime = customerLoadTime;
    }

    public Time getCustomerUnloadTime() {
        return customerUnloadTime;
    }

    public void setCustomerUnloadTime(Time customerUnloadTime) {
        this.customerUnloadTime = customerUnloadTime;
    }

    public Float getEstimatedTripDistance() {
        return estimatedTripDistance;
    }

    public void setEstimatedTripDistance(float estimatedTripDistance) {
        this.estimatedTripDistance = estimatedTripDistance;
    }

    public Integer getEstimatedTripTravelTime() {
        return estimatedTripTravelTime;
    }



    public boolean isTripIsolation() {
        return tripIsolation;
    }

    public void setTripIsolation(boolean tripIsolation) {
        this.tripIsolation = tripIsolation;
    }

    public boolean isOutsideCoreHours() {
        return outsideCoreHours;
    }

    public void setOutsideCoreHours(boolean outsideCoreHours) {
        this.outsideCoreHours = outsideCoreHours;
    }

    public Integer getTimeWindowBefore() {
        return timeWindowBefore;
    }



    public Integer getTimeWindowAfter() {
        return timeWindowAfter;
    }



    public String getProviderWhiteList() {
        return providerWhiteList;
    }

    public void setProviderWhiteList(String providerWhiteList) {
        this.providerWhiteList = providerWhiteList;
    }

    public String getProviderBlackList() {
        return providerBlackList;
    }

    public void setProviderBlackList(String providerBlackList) {
        this.providerBlackList = providerBlackList;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Provider getLastStatusChangedByProvider() {
        return lastStatusChangedByProvider;
    }

    public void setLastStatusChangedByProvider(Provider lastStatusChangedByProvider) {
        this.lastStatusChangedByProvider = lastStatusChangedByProvider;
    }

    public LocalDateTime getTripTicketProvisionalTime() {
        return tripTicketProvisionalTime;
    }

    public void setTripTicketProvisionalTime(LocalDateTime tripTicketProvisionalTime) {
        this.tripTicketProvisionalTime = tripTicketProvisionalTime;
    }

    public Provider getProvisionalProvider() {
        return provisionalProvider;
    }

    public void setProvisionalProvider(Provider provisionalProvider) {
        this.provisionalProvider = provisionalProvider;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public String getCustomerCustomFields() {
        return customerCustomFields;
    }

    public void setCustomerCustomFields(String customerCustomFields) {
        this.customerCustomFields = customerCustomFields;
    }

    public String getTripCustomFields() {
        return tripCustomFields;
    }

    public void setTripCustomFields(String tripCustomFields) {
        this.tripCustomFields = tripCustomFields;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Set<ClaimantTripTicket> getClaimantTripTicket() {
        return claimantTripTicket;
    }

    public void setClaimantTripTicket(Set<ClaimantTripTicket> claimantTripTicket) {
        this.claimantTripTicket = claimantTripTicket;
    }

    public Set<TripClaim> getTripClaims() {
        return tripClaims;
    }

    public void setTripClaims(Set<TripClaim> tripClaims) {
        this.tripClaims = tripClaims;
    }

    public TripResult getTripResult() {
        return tripResult;
    }

    public void setTripResult(TripResult tripResult) {
        this.tripResult = tripResult;
    }

    public Set<TripTicketComment> getTripTicketComments() {
        return tripTicketComments;
    }

    public void setTripTicketComments(Set<TripTicketComment> tripTicketComments) {
        this.tripTicketComments = tripTicketComments;
    }

    public String getPickupDateTime() {
        return pickupDateTime;
    }

    public void setPickupDateTime(String pickupDateTime) {
        this.pickupDateTime = pickupDateTime;
    }

    public boolean isTripTicketInvisible() {
        return tripTicketInvisible;
    }

    public void setTripTicketInvisible(boolean tripTicketInvisible) {
        this.tripTicketInvisible = tripTicketInvisible;
    }

    public String getVectorStoreId() {
        return vectorStoreId;
    }

    public void setVectorStoreId(String vectorStoreId) {
        this.vectorStoreId = vectorStoreId;
    }


    @PostLoad
    private void initializeNullFields() {
        if (expired == null) {
            expired = false;
        }
        if (tripTicketInvisible == null) {
            tripTicketInvisible = false;
        }
        if (originCustomerId == null) {
            originCustomerId = "0";
        }
        if (requesterTripId == null) {
            requesterTripId = "0";
        }
        if (boardingTime == null) {
            boardingTime = 0;
        }
        if (deboardingTime == null) {
            deboardingTime = 0;
        }
        if (customerSeatsRequired == null) {
            customerSeatsRequired = 0;
        }
        if (attendants == null) {
            attendants = 0;
        }
        if (guests == null) {
            guests = 0;
        }
        if (timeWindowBefore == null) {
            timeWindowBefore = 0;
        }
        if (timeWindowAfter == null) {
            timeWindowAfter = 0;
        }
        if (estimatedTripTravelTime == null) {
            estimatedTripTravelTime = 0;
        }
    }


    public void loadLazyFields(){
        getTripClaims();
        getClaimantTripTicket();
        getTripTicketComments();
        getTripResult();
        getApprovedTripClaim();
        getTripFunders();
        getTripCustomFields();
        getCustomerCustomFields();
        getTripTicketProvisionalTime();
        getExpirationDate();
        getProvisionalProvider();

        // Ensure nested lazy associations inside TripClaim are initialized so mappers can access their fields
        if (tripClaims != null) {
            for (TripClaim claim : tripClaims) {
                if (claim != null) {
                    Provider claimant = claim.getClaimantProvider();
                    if (claimant != null) {
                        // touch identifier and name to force initialization of the proxy
                        claimant.getProviderId();
                        claimant.getProviderName();
                    }
                }
            }
        }

        // Also ensure the approvedTripClaim's claimant provider is initialized
        if (approvedTripClaim != null && approvedTripClaim.getClaimantProvider() != null) {
            Provider ap = approvedTripClaim.getClaimantProvider();
            ap.getProviderId();
            ap.getProviderName();
        }
    }

    @Override
    public String toString() {
        return "TripTicket [id=" + id + ", originProviderId=" + (originProvider != null ? originProvider.getProviderId() : null) + ", originCustomerId="
                + originCustomerId + ", requesterTripId=" + requesterTripId + ", commonTripId=" + commonTripId
                + ", approvedTripClaimId=" + (approvedTripClaim != null ? approvedTripClaim.getId() : null) + ", customerAddress=" + customerAddress
                + ", customerInternalId=" + customerInternalId + ", customerFirstName=" + customerFirstName
                + ", customerMiddleName=" + customerMiddleName + ", customerLastName=" + customerLastName
                + ", customerEmail=" + customerEmail + ", customerHomePhone=" + customerHomePhone
                + ", customerEmergencyPhone=" + customerEmergencyPhone + ", customerDob=" + customerDob
                + ", gender=" + customerGender + ", customerRace=" + customerRace + ", impairmentDescription="
                + impairmentDescription + ", customerInformationWithheld=" + customerInformationWithheld
                + ", primaryLanguage=" + primaryLanguage + ", customerNotes=" + customerNotes + ", boardingTime="
                + boardingTime + ", deboardingTime=" + deboardingTime + ", customerSeatsRequired="
                + customerSeatsRequired + ", pickupAddress=" + pickupAddress + ", dropOffAddress="
                + dropOffAddress + ", schedulingPriority=" + schedulingPriority + ", attendants=" + attendants
                + ", guests=" + guests + ", purpose=" + tripPurpose + ", tripNotes=" + tripNotes
                + ", customerIdentifiers=" + customerIdentifiers + ", customerEligibilityFactors="
                + customerEligibilityFactors + ", customerMobilityFactors=" + customerMobilityFactors
                + ", customerServiceAnimals=" + customerServiceAnimals + ", tripFunders=" + tripFunders
                + ", customerAssistanceNeeds=" + customerAssistanceNeeds + ", attendantMobilityFactors="
                + attendantMobilityFactors + ", guestMobilityFactors=" + guestMobilityFactors + ", serviceLevel="
                + serviceLevel + ", requestedPickupDate=" + requestedPickupDate + ", requestedPickupTime="
                + requestedPickupTime + ", requestedDropoffDate=" + requestedDropoffDate
                + ", requestedDropOffTime=" + requestedDropOffTime + ", earliestPickupTime="
                + earliestPickupTime + ", appointmentTime=" + appointmentTime + ", customerLoadTime="
                + customerLoadTime + ", customerUnloadTime=" + customerUnloadTime + ", estimatedTripDistance="
                + estimatedTripDistance + ", estimatedTripTravelTime=" + estimatedTripTravelTime
                + ", tripIsolation=" + tripIsolation + ", outsideCoreHours=" + outsideCoreHours
                + ", timeWindowBefore=" + timeWindowBefore + ", timeWindowAfter=" + timeWindowAfter
                + ", providerWhiteList=" + providerWhiteList + ", providerBlackList=" + providerBlackList
                + ", status=" + status + ", lastStatusChangedByProvider=" + (lastStatusChangedByProvider != null ? lastStatusChangedByProvider.getProviderId() : null)
                + ", tripTicketProvisionalTime=" + tripTicketProvisionalTime + ", provisionalProviderId="
                + (provisionalProvider != null ? provisionalProvider.getProviderId() : null) + ", expirationDate="
                + expirationDate + ", expired=" + expired
                + ", customerCustomFields=" + customerCustomFields + ", tripCustomFields=" + tripCustomFields
                + ", version=" + version + ", claimantTripTicket=" + claimantTripTicket + ", tripClaims=" + tripClaims
                + ", tripResultId=" + (tripResult != null ? tripResult.getId() : null)
                + ", pickupDateTime=" + pickupDateTime + ", tripTicketInvisible=" + tripTicketInvisible + "]";
    }

}
