-- H2 compatible schema for testing

-- Table structure for table address
DROP TABLE IF EXISTS "address";
CREATE TABLE "address" (
  "AddressID" int NOT NULL AUTO_INCREMENT,
  "Street1" varchar(1000) DEFAULT NULL,
  "Street2" varchar(255) DEFAULT NULL,
  "City" varchar(100) DEFAULT NULL,
  "County" varchar(100) DEFAULT NULL,
  "State" varchar(100) DEFAULT NULL,
  "ZipCode" varchar(10) DEFAULT NULL,
  "Latitude" varchar(50) DEFAULT NULL,
  "Longitude" varchar(50) DEFAULT NULL,
  "CommonName" varchar(255) DEFAULT NULL,
  "PhoneNumber" varchar(20) DEFAULT NULL,
  "PhoneExtension" varchar(10) DEFAULT NULL,
  "AddressType" varchar(20) DEFAULT NULL,
  "AddedBy" int DEFAULT NULL,
  "AddedOn" datetime(6) DEFAULT NULL,
  "UpdatedBy" int DEFAULT NULL,
  "UpdatedOn" datetime(6) DEFAULT NULL,
  PRIMARY KEY ("AddressID")
);

-- Minimal table structure for table tripclaim (used by some DAO report queries)
DROP TABLE IF EXISTS "tripclaim";
CREATE TABLE "tripclaim" (
  "TripClaimID" int NOT NULL AUTO_INCREMENT,
  "ClaimantProviderID" int DEFAULT NULL,
  "ClaimantTripID" int DEFAULT NULL,
  "TripTicketID" int DEFAULT NULL,
  "AcknowledgementStatus" varchar(50) DEFAULT NULL,
  "CalculatedProposedFare" decimal(10,2) DEFAULT NULL,
  "ProposedFare" decimal(10,2) DEFAULT NULL,
  "ProposedPickupTime" datetime(6) DEFAULT NULL,
  "RequesterProviderFare" decimal(10,2) DEFAULT NULL,
  "ServiceID" int DEFAULT NULL,
  "StatusID" int DEFAULT NULL,
  "ExpirationDate" datetime(6) DEFAULT NULL,
  "IsExpired" boolean DEFAULT NULL,
  "NewRecord" boolean DEFAULT NULL,
  "Notes" clob DEFAULT NULL,
  "AddedBy" int DEFAULT NULL,
  "AddedOn" datetime(6) DEFAULT NULL,
  "UpdatedBy" int DEFAULT NULL,
  "UpdatedOn" datetime(6) DEFAULT NULL,
  "Version" int DEFAULT NULL,
  PRIMARY KEY ("TripClaimID")
);

-- Table structure for table provider
DROP TABLE IF EXISTS "provider";
CREATE TABLE "provider" (
  "ProviderID" int NOT NULL AUTO_INCREMENT,
  "AddressID" int NOT NULL,
  "ProviderTypeId" int NOT NULL,
  "IsActive" boolean DEFAULT NULL,
  "ProviderName" varchar(255) DEFAULT NULL,
  "contactEmail" varchar(255) DEFAULT NULL,
  "APIkey" varchar(150) DEFAULT NULL,
  "PrivateKey" varchar(150) DEFAULT NULL,
  "TripTicketExpirationDaysBefore" int DEFAULT 0,
  "TripTicketExpirationTime" time(6) DEFAULT NULL,
  "TripTicketProvisionalTime" time(6) DEFAULT NULL,
  "LastSyncDateTime" varchar(150) DEFAULT NULL,
  "AddedBy" int DEFAULT NULL,
  "AddedOn" datetime(6) DEFAULT NULL,
  "UpdatedBy" int DEFAULT NULL,
  "UpdatedOn" datetime(6) DEFAULT NULL,
  PRIMARY KEY ("ProviderID")
);

-- Table structure for table providertype
DROP TABLE IF EXISTS "providertype";
CREATE TABLE "providertype" (
  "ProviderTypeId" int NOT NULL AUTO_INCREMENT,
  "ProviderType" varchar(50) NOT NULL,
  PRIMARY KEY ("ProviderTypeId")
);

-- Table structure for table status
DROP TABLE IF EXISTS "status";
CREATE TABLE "status" (
  "StatusID" int NOT NULL AUTO_INCREMENT,
  "Type" varchar(50) DEFAULT NULL,
  "Description" varchar(255) DEFAULT NULL,
  "AddedBy" int DEFAULT NULL,
  "AddedOn" datetime(6) DEFAULT NULL,
  "UpdatedBy" int DEFAULT NULL,
  "UpdatedOn" datetime(6) DEFAULT NULL,
  PRIMARY KEY ("StatusID")
);

-- Table structure for table tripticket
DROP TABLE IF EXISTS "tripticket";
CREATE TABLE "tripticket" (
  "TripTicketID" int NOT NULL AUTO_INCREMENT,
  "RequesterProviderID" int NOT NULL,
  "RequesterCustomerID" int NOT NULL,
  "StatusID" int NOT NULL,
  "RequesterTripID" int NOT NULL,
  "CommonTripID" varchar(64) NOT NULL,
  "ApprovedTripClaimID" int DEFAULT NULL,
  "CustomerAddressID" int NOT NULL,
  "CustomerInternalID" int DEFAULT NULL,
  "PickupAddressID" int NOT NULL,
  "DropOffAddressID" int NOT NULL,
  "CustomerFirstName" varchar(150) NOT NULL,
  "CustomerMiddleName" varchar(150) DEFAULT NULL,
  "CustomerLastName" varchar(150) NOT NULL,
  "CustomerEmail" varchar(100) DEFAULT NULL,
  "CustomerHomePhone" varchar(20) DEFAULT NULL,
  "CustomerEmergencyPhone" varchar(20) DEFAULT NULL,
  "CustomerDateOfBirth" datetime(6) DEFAULT NULL,
  "CustomerGender" varchar(10) DEFAULT NULL,
  "CustomerRace" varchar(100) DEFAULT NULL,
  "ImpairmentDescription" varchar(200) DEFAULT NULL,
  "IsInformationWithheld" boolean DEFAULT NULL,
  "PrimaryLanguage" varchar(50) DEFAULT NULL,
  "CustomerNotes" clob DEFAULT NULL,
  "CustomerCareInfo" varchar(45) DEFAULT NULL,
  "CustomerCaregiverContactInfo" varchar(45) DEFAULT NULL,
  "CustomerCaregiverName" varchar(200) DEFAULT NULL,
  "CustomerNickName" varchar(45) DEFAULT NULL,
  "CustomerPovertyLevel" varchar(45) DEFAULT NULL,
  "CustomerDisability" varchar(45) DEFAULT NULL,
  "CustomerEthnicity" varchar(45) DEFAULT NULL,
  "IsCustomerVeteran" boolean DEFAULT NULL,
  "CustomerMobilePhone" varchar(20) DEFAULT NULL,
  "CustomerMailingBillingAddress" varchar(100) DEFAULT NULL,
  "CustomerEmergencyContactRelationship" varchar(45) DEFAULT NULL,
  "CustomerEmergencyContactPhone" varchar(20) DEFAULT NULL,
  "CustomerEmergencyContactName" varchar(100) DEFAULT NULL,
  "CustomerFundingBillingInformation" varchar(45) DEFAULT NULL,
  "FundingType" varchar(45) DEFAULT NULL,
  "BoardingTime" int DEFAULT NULL,
  "DeboardingTime" int DEFAULT NULL,
  "SeatsRequired" int DEFAULT NULL,
  "SchedulingPriority" varchar(10) DEFAULT NULL,
  "Attendants" int DEFAULT NULL,
  "Guests" int DEFAULT NULL,
  "Purpose" varchar(200) DEFAULT NULL,
  "TripNotes" clob DEFAULT NULL,
  "CustomerIdentifiers" varchar(200) DEFAULT NULL,
  "CustomerEligibilityFactors" varchar(200) DEFAULT NULL,
  "CustomerMobilityFactors" varchar(200) DEFAULT NULL,
  "IsServiceAnimals" boolean DEFAULT NULL,
  "TripFunders" varchar(200) DEFAULT NULL,
  "CustomerAssistanceNeeds" varchar(200) DEFAULT NULL,
  "AttendantMobilityFactors" varchar(200) DEFAULT NULL,
  "GuestMobilityFactors" varchar(200) DEFAULT NULL,
  "ServiceLevel" varchar(200) DEFAULT NULL,
  "RequestedPickupDate" date DEFAULT NULL,
  "RequestedPickupTime" time(6) DEFAULT NULL,
  "RequestedDropOffDate" date DEFAULT NULL,
  "RequestedDropOffTime" time(6) DEFAULT NULL,
  "EarliestPickupTime" time(6) DEFAULT NULL,
  "AppointmentTime" time(6) DEFAULT NULL,
  "CustomerLoadTime" time(6) DEFAULT NULL,
  "CustomerUnloadTime" time(6) DEFAULT NULL,
  "EstimatedTripDistance" decimal(10,0) DEFAULT 0,
  "EstimatedTripTravelTime" int DEFAULT NULL,
  "IsTripIsolation" boolean DEFAULT NULL,
  "IsOutsideCoreHours" boolean DEFAULT NULL,
  "TimeWindowBefore" int DEFAULT NULL,
  "TimeWindowAfter" int DEFAULT NULL,
  "ProviderWhiteList" varchar(200) DEFAULT NULL,
  "ProviderBlackList" varchar(200) DEFAULT NULL,
  "LastStatusChangedByProviderID" int DEFAULT NULL,
  "RequesterProviderFare" decimal(10,2) DEFAULT NULL,
  "TripTicketProvisionalTime" datetime(6) DEFAULT NULL,
  "ProvisionalProviderID" int DEFAULT NULL,
  "ExpirationDate" datetime(6) DEFAULT NULL,
  "IsExpired" boolean DEFAULT NULL,
  "CustomerCustomFields" clob DEFAULT NULL,
  "TripCustomFields" clob DEFAULT NULL,
  "Version" int NOT NULL,
  "IsRejected" boolean DEFAULT NULL,
  "RejectedReason" varchar(200) DEFAULT NULL,
  "IsInvisible" boolean NOT NULL,
  "AddedBy" int DEFAULT NULL,
  "AddedOn" datetime(6) DEFAULT NULL,
  "UpdatedBy" int DEFAULT NULL,
  "UpdatedOn" datetime(6) DEFAULT NULL,
  "OriginProviderProviderID" int DEFAULT NULL,
  "Timestamp" datetime DEFAULT NULL,
  "VectorID" varchar(255) DEFAULT NULL,
  "VehicleType" varchar(45) DEFAULT NULL,
  "TripPurpose" varchar(45) DEFAULT NULL,
  PRIMARY KEY ("TripTicketID")
);


-- Minimal table structure for table tripresult (used by DAO fetches)
DROP TABLE IF EXISTS "tripresult";
CREATE TABLE "tripresult" (
  "TripResultID" int NOT NULL AUTO_INCREMENT,
  "ActualDropOffArriveTime" datetime(6) DEFAULT NULL,
  "ActualDropOffDepartTime" datetime(6) DEFAULT NULL,
  "ActualPickupArriveTime" datetime(6) DEFAULT NULL,
  "ActualPickupDepartTime" datetime(6) DEFAULT NULL,
  "BaseFare" decimal(10,2) DEFAULT NULL,
  "BillableMileage" decimal(10,2) DEFAULT NULL,
  "AddedOn" datetime(6) DEFAULT NULL,
  "AddedBy" int DEFAULT NULL,
  "DriverID" int DEFAULT NULL,
  "DriverName" varchar(255) DEFAULT NULL,
  "DropOffLatitude" decimal(18,8) DEFAULT NULL,
  "DropOffLongitude" decimal(18,8) DEFAULT NULL,
  "ExtraSecurementCount" int DEFAULT NULL,
  "Fare" decimal(10,2) DEFAULT NULL,
  "FareCollected" decimal(10,2) DEFAULT NULL,
  "FareType" varchar(50) DEFAULT NULL,
  "NoShowFlag" boolean DEFAULT NULL,
  "MilesTraveled" decimal(10,2) DEFAULT NULL,
  "Notes" clob DEFAULT NULL,
  "NumberOfAttendants" int DEFAULT NULL,
  "NumberOfGuests" int DEFAULT NULL,
  "NumberOfPassengers" int DEFAULT NULL,
  "OdometerEnd" int DEFAULT NULL,
  "OdometerStart" int DEFAULT NULL,
  "Outcome" varchar(100) DEFAULT NULL,
  "PickupLatitude" decimal(18,8) DEFAULT NULL,
  "PickupLongitude" decimal(18,8) DEFAULT NULL,
  "Rate" decimal(10,2) DEFAULT NULL,
  "RateType" varchar(50) DEFAULT NULL,
  "TripClaimID" int DEFAULT NULL,
  "TripDate" datetime(6) DEFAULT NULL,
  "TripTicketID" int DEFAULT NULL,
  "UpdatedOn" datetime(6) DEFAULT NULL,
  "UpdatedBy" int DEFAULT NULL,
  "VehicleID" int DEFAULT NULL,
  "VehicleName" varchar(255) DEFAULT NULL,
  "VehicleType" varchar(100) DEFAULT NULL,
  "Version" int DEFAULT NULL,
  "CancellationReason" varchar(500) DEFAULT NULL,
  "NoShowReason" varchar(500) DEFAULT NULL,
  PRIMARY KEY ("TripResultID")
);

-- Table structure for table activity
DROP TABLE IF EXISTS "activity";
CREATE TABLE "activity" (
  "ActivityID" int NOT NULL AUTO_INCREMENT,
  "Action" varchar(255) DEFAULT NULL,
  "ActionDetails" clob DEFAULT NULL,
  "ActionTakenBy" varchar(255) DEFAULT NULL,
  "TripTicketID" int DEFAULT NULL,
  "AddedOn" datetime(6) DEFAULT NULL,
  "AddedBy" int DEFAULT NULL,
  "UpdatedOn" datetime(6) DEFAULT NULL,
  "UpdatedBy" int DEFAULT NULL,
  PRIMARY KEY ("ActivityID")
);


-- Table structure for table claimanttripticket (minimal for DAO queries)
DROP TABLE IF EXISTS "claimanttripticket";
CREATE TABLE "claimanttripticket" (
  "ClaimantTripTicketID" int NOT NULL AUTO_INCREMENT,
  "ClaimantProviderID" int DEFAULT NULL,
  "ClaimantTripID" int DEFAULT NULL,
  "TripTicketID" int DEFAULT NULL,
  "AddedOn" datetime(6) DEFAULT NULL,
  "AddedBy" int DEFAULT NULL,
  "UpdatedOn" datetime(6) DEFAULT NULL,
  "UpdatedBy" int DEFAULT NULL,
  PRIMARY KEY ("ClaimantTripTicketID")
);


DROP TABLE IF EXISTS "user";
CREATE TABLE "user" (
  "UserID" int NOT NULL AUTO_INCREMENT,
  "AccountLockedDate" datetime(6) DEFAULT NULL,
  "AccountDisabled" boolean DEFAULT NULL,
  "AccountExpired" boolean DEFAULT NULL,
  "AccountLocked" boolean DEFAULT NULL,
  "AuthanticationTypeIsAdapter" boolean DEFAULT NULL,
  "AddedOn" datetime(6) DEFAULT NULL,
  "AddedBy" int DEFAULT NULL,
  "CredentialsExpired" boolean DEFAULT NULL,
  "CurrentLogInDate" datetime(6) DEFAULT NULL,
  "CurrentLoginIp" varchar(100) DEFAULT NULL,
  "Email" varchar(255) DEFAULT NULL,
  "FailedAttempts" int NOT NULL DEFAULT 0,
  "IsActive" boolean DEFAULT NULL,
  "IsNotifyClaimedTicketExpired" boolean DEFAULT NULL,
  "IsNotifyClaimedTicketRescinded" boolean DEFAULT NULL,
  "IsNotifyNewTripClaimAutoApproved" boolean DEFAULT NULL,
  "IsNotifyNewTripClaimAwaitingApproval" boolean DEFAULT NULL,
  "IsNotifyPartnerCreatesTicket" boolean DEFAULT NULL,
  "IsNotifyPartnerUpdateTicket" boolean DEFAULT NULL,
  "IsNotifyTripCancelled" boolean DEFAULT NULL,
  "IsNotifyTripClaimApproved" boolean DEFAULT NULL,
  "IsNotifyTripClaimCancelled" boolean DEFAULT NULL,
  "IsNotifyTripClaimDeclined" boolean DEFAULT NULL,
  "IsNotifyTripClaimRescinded" boolean DEFAULT NULL,
  "IsNotifyTripCommentAdded" boolean DEFAULT NULL,
  "IsNotifyTripExpired" boolean DEFAULT NULL,
  "IsNotifyTripPriceMismatched" boolean DEFAULT NULL,
  "IsNotifyTripReceived" boolean DEFAULT NULL,
  "IsNotifyTripResultSubmitted" boolean DEFAULT NULL,
  "IsNotifyTripWeeklyReport" boolean DEFAULT NULL,
  "IsPasswordExpired" boolean DEFAULT NULL,
  "JobTitle" varchar(255) DEFAULT NULL,
  "LastFailedAttemptDate" datetime(6) DEFAULT NULL,
  "LastLogInDate" datetime(6) DEFAULT NULL,
  "LastLogInIP" varchar(100) DEFAULT NULL,
  "LogInConfirmationDate" datetime(6) DEFAULT NULL,
  "LogInConfirmationSentDate" datetime(6) DEFAULT NULL,
  "LogInCount" int DEFAULT 0,
  "Name" varchar(255) DEFAULT NULL,
  "PrePassword1" varchar(255) DEFAULT NULL,
  "PrePassword2" varchar(255) DEFAULT NULL,
  "PrePassword3" varchar(255) DEFAULT NULL,
  "PrePassword4" varchar(255) DEFAULT NULL,
  "Password" varchar(255) DEFAULT NULL,
  "PhoneNumber" varchar(50) DEFAULT NULL,
  "ProviderID" int DEFAULT NULL,
  "ResetPasswordDate" datetime(6) DEFAULT NULL,
  "ResetPasswordRequestDate" datetime(6) DEFAULT NULL,
  "ResetPasswordToken" varchar(255) DEFAULT NULL,
  "ResponseDataForUI" clob DEFAULT NULL,
  "TemporaryPassword" varchar(255) DEFAULT NULL,
  "UnconfirmedEmail" varchar(255) DEFAULT NULL,
  "UpdatedOn" datetime(6) DEFAULT NULL,
  "UpdatedBy" int DEFAULT NULL,
  "UserName" varchar(255) DEFAULT NULL,
  PRIMARY KEY ("UserID")
);

-- Table structure for userroles
DROP TABLE IF EXISTS "userroles";
CREATE TABLE "userroles" (
  "User_UserID" int NOT NULL,
  "Authority" varchar(255) NOT NULL,
  PRIMARY KEY ("User_UserID", "Authority")
);


