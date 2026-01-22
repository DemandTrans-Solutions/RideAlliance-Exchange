-- Sample data for testing

INSERT INTO "providertype" ("ProviderTypeId", "ProviderType") VALUES (1, 'TestProvider');
INSERT INTO "address" ("AddressID", "Street1", "City", "State", "ZipCode", "Latitude", "Longitude") VALUES (1, '123 Test St', 'TestCity', 'TS', '12345', '0.0', '0.0');
INSERT INTO "provider" ("ProviderID", "AddressID", "ProviderTypeId", "IsActive", "ProviderName", "APIkey", "contactEmail", "LastSyncDateTime", "TripTicketExpirationDaysBefore") VALUES (1, 1, 1, 1, 'Test Provider', 'apikey-123', 'test@example.com', '2025-01-01T00:00:00', 0);
INSERT INTO "status" ("StatusID", "Type", "Description") VALUES (2, 'AVAILABLE', 'Ticket available for claiming');
INSERT INTO "status" ("StatusID", "Type", "Description") VALUES (7, 'COMPLETED', 'Ticket completed');
-- Trip 1: available
INSERT INTO "tripticket" ("TripTicketID", "RequesterProviderID", "RequesterCustomerID", "StatusID", "RequesterTripID", "CommonTripID", "CustomerAddressID", "PickupAddressID", "DropOffAddressID", "CustomerFirstName", "CustomerLastName", "Version", "IsInvisible", "OriginProviderProviderID", "AddedOn", "UpdatedOn") 
VALUES (1, 1, 1, 2, 1, 'TRIP-001', 1, 1, 1, 'Test', 'User', 1, false, 1, '2025-01-15 00:00:00+00:00', '2025-01-15 00:00:00+00:00');

-- Completed trip for reporting
INSERT INTO "tripticket" ("TripTicketID", "RequesterProviderID", "RequesterCustomerID", "StatusID", "RequesterTripID", "CommonTripID", "CustomerAddressID", "PickupAddressID", "DropOffAddressID", "CustomerFirstName", "CustomerLastName", "Version", "IsInvisible", "OriginProviderProviderID", "AddedOn", "UpdatedOn") 
VALUES (2, 1, 2, 7, 2, 'TRIP-002', 1, 1, 1, 'Completed', 'User', 1, false, 1, '2025-06-01 00:00:00+00:00', '2025-06-02 00:00:00+00:00');

-- Seed a minimal user and role for UserDAO tests
-- Insert into quoted USER table to match DDL and avoid reserved word parsing issues
INSERT INTO "user" ("UserID", "UserName", "Email", "IsActive", "ProviderID", "AddedOn", "FailedAttempts", "LogInCount") VALUES (1, 'testuser', 'test@example.com', true, 1, '2025-01-01 00:00:00', 0, 0);
INSERT INTO "userroles" ("User_UserID", "Authority") VALUES (1, 'ROLE_USER');