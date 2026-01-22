package com.clearinghouse.service;

import com.clearinghouse.dao.AddressDAO;
import com.clearinghouse.dao.ServiceDAO;
import com.clearinghouse.dao.TripTicketDAO;
import com.clearinghouse.dto.*;
import com.clearinghouse.entity.Address;
import com.clearinghouse.entity.Service;
import com.clearinghouse.entity.Status;
import com.clearinghouse.entity.TripTicket;
import com.clearinghouse.enumentity.TripTicketStatusConstants;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@org.springframework.stereotype.Service
@Transactional
@AllArgsConstructor
@Slf4j
public class TripTicketDataService {

    private final ServiceDAO serviceDAO;
    private final AddressDAO addressDAO;
    private final TripTicketDAO tripTicketDAO;
    private final TripTicketService tripTicketService;


    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Generate test trip tickets for all services with valid providers.
     * Each service will get 5 trips with pickup and dropoff coordinates inside the service geometry.
     *
     * @return The number of trip tickets generated
     */
    public int generateTestTripTickets() {
        log.info("Starting test trip ticket generation");
        List<Service> services = serviceDAO.findAllSerivearea();
        int generatedCount = 0;

        // Filter services with valid providers
        List<Service> validServices = new ArrayList<>();
        for (Service service : services) {
            if (service.getProvider() != null && service.isActive() && service.getProvider().getProviderId() < 38) { // Uber and FlexRide do not create tickets
                validServices.add(service);
            }
        }

        log.info("Found {} valid services with providers", validServices.size());

        // Generate 5 trip tickets for each valid service
        for (Service service : validServices) {
            for (int i = 0; i < 10; i++) {
                try {
                    TripTicket tripTicket = generateTripTicketForService(service);
                    if (tripTicket != null) {
                        generatedCount++;
                    } else {
                        log.warn("Failed to generate trip ticket for service {}", service.getServiceId());
                    }
                } catch (Exception e) {
                    log.error("Error generating trip ticket for service {}: {}", service.getServiceId(), e.getMessage());
                }
            }
        }

        log.info("Successfully generated {} test trip tickets", generatedCount);
        return generatedCount;
    }

    /**
     * Generate a single trip ticket for a given service with valid coordinates
     *
     * @param service The service to generate a trip ticket for
     * @return The created TripTicket
     */
    @Transactional
    public TripTicket generateTripTicketForService(Service service) {
        try {
            // Generate valid pickup and dropoff addresses within the service area geometry
            Address pickupAddress = generateAddressInServiceArea(service);
            Address dropoffAddress = generateAddressInServiceArea(service);
            Address custAddress = generateAddressInServiceArea(service);

            if (pickupAddress == null || dropoffAddress == null) {
                log.warn("Could not generate valid addresses for service {}", service.getServiceId());
                return null;
            }

            // Save addresses to the database
            addressDAO.createNewAddress(pickupAddress);
            addressDAO.createNewAddress(dropoffAddress);
            addressDAO.createNewAddress(custAddress);


            // Create trip ticket with random customer data
            TripTicket tripTicket = new TripTicket();

            tripTicket.setTripTicketInvisible(false);
            tripTicket.setVersion("1.0");
            tripTicket.setExpired(false);
            tripTicket.setCustomerInformationWithheld(false);
            tripTicket.setCustomerServiceAnimals(new Random().nextBoolean());
            tripTicket.setOutsideCoreHours(false);

            tripTicket.setCustomerAddress(custAddress);

            // Set provider information
            tripTicket.setOriginProvider(service.getProvider());

            // Set customer information
            setRandomCustomerData(tripTicket);

            // Set trip information
            tripTicket.setPickupAddress(pickupAddress);
            tripTicket.setDropOffAddress(dropoffAddress);

            // Set dates and times
            setRandomDateTimeData(tripTicket);

            // Set common fields
            tripTicket.setServiceLevel(getRandomServiceLevel());
            tripTicket.setRequesterTripId(Integer.toString(new Random().nextInt(100000)));
            tripTicket.setCommonTripId(Integer.toString(new Random().nextInt(100000)));

            tripTicket.setEstimatedTripTravelTime(5 + new Random().nextInt(40));
            tripTicket.setEstimatedTripDistance(1 + new Random().nextFloat(15));
            tripTicket.setCustomerSeatsRequired(1 + new Random().nextInt(3));
            tripTicket.setRequesterProviderFare(10 + new Random().nextFloat(50));

            // Set trip status
            Status status = new Status();
            status.setStatusId(TripTicketStatusConstants.available.tripTicketStatusUpdate());
            tripTicket.setStatus(status);

            // Save the trip ticket
            var ticket = tripTicketDAO.createTripTicket(tripTicket);
            getEntityManager().flush();
            tripTicketService.createActivityForTripTicketForCreateTripTicket(ticket);
            return ticket;
        } catch (Exception e) {
            log.error("Failed to persist trip ticket for service {}: {}", service.getServiceId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Generate an address with coordinates inside the service area
     *
     * @param service The service to generate address for
     * @return Address with valid coordinates
     */
    private Address generateAddressInServiceArea(Service service) {
        final int MAX_ATTEMPTS = 100;
        Random random = new Random();

        // Extract bounds from the service area geometry
        Geometry geometry = service.getServiceAreaGeometry();
        if (geometry == null) {
            log.warn("Service {} has null geometry", service.getServiceId());
            return null;
        }

        // Get the envelope (bounding box) of the geometry
        org.locationtech.jts.geom.Envelope envelope = geometry.getEnvelopeInternal();

        // Extract min/max lat/long from the envelope
        double minLat = envelope.getMinY();
        double maxLat = envelope.getMaxY();
        double minLon = envelope.getMinX();
        double maxLon = envelope.getMaxX();

        log.info("Service {} geometry bounds: lat [{}, {}], lon [{}, {}]",
                service.getServiceId(), minLat, maxLat, minLon, maxLon);

        // Attempt to find valid lat/long within the service area
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            float lat = (float) (minLat + (maxLat - minLat) * random.nextFloat());
            float lon = (float) (minLon + (maxLon - minLon) * random.nextFloat());

            // Check if the coordinates are within the service area
            if (serviceDAO.checkAddressInService(service, lat, lon)) {
                Address address = new Address();
                address.setLatitude(lat);
                address.setLongitude(lon);
                address.setStreet1(generateRandomStreetAddress());
                address.setCity(generateRandomCity());
                address.setState(generateRandomState());
                address.setZipcode(generateRandomZipCode());
                return address;
            }
        }

        log.warn("Failed to find valid coordinates for service {} after {} attempts",
                service.getServiceId(), MAX_ATTEMPTS);
        return null;
    }

    private void setRandomCustomerData(TripTicket tripTicket) {
        tripTicket.setCustomerFirstName(getRandomFirstName());
        tripTicket.setOriginCustomerId(Integer.toString(new Random().nextInt(100000)));
        tripTicket.setRequesterTripId(Integer.toString(new Random().nextInt(100000)));
        tripTicket.setCustomerLastName(getRandomLastName());
        tripTicket.setCustomerEmail(tripTicket.getCustomerFirstName().toLowerCase() +
                "." + tripTicket.getCustomerLastName().toLowerCase() + "@example.com");
        tripTicket.setCustomerHomePhone(generateRandomPhoneNumber());
        tripTicket.setCustomerGender(getRandomGender());
        tripTicket.setCustomerDob(getRandomDob());
        tripTicket.setPrimaryLanguage("English");
        tripTicket.setCustomerInformationWithheld(false);
        tripTicket.setAttendants(new Random().nextInt(2));
        tripTicket.setCustomerSeatsRequired(1 + new Random().nextInt(3));
        tripTicket.setGuests(new Random().nextInt(3));
        tripTicket.setCustomerEligibilityFactors("Test Eligibility");
        tripTicket.setCustomerMobilityFactors(getRandomMobilityFactor());
        tripTicket.setBoardingTime(5 + new Random().nextInt(10));
        tripTicket.setDeboardingTime(5 + new Random().nextInt(10));

    }

    private void setRandomDateTimeData(TripTicket tripTicket) {
        // Set pickup date (between today and next 30 days)
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(new Random().nextInt(30));

        int selector = new Random().nextInt(10);


        // 70% chance to set pickup date 30% chance of dropoff date
        if (selector <= 7) {
            tripTicket.setRequestedPickupDate(futureDate);
            // Set pickup time (between 8 AM and 6 PM)
            int hour = 6 + new Random().nextInt(12);
            int minute = new Random().nextInt(60);
            LocalTime pickupTime = LocalTime.of(hour, minute);
            tripTicket.setRequestedPickupTime(Time.valueOf(pickupTime));
        } else {
            tripTicket.setRequestedDropoffDate(futureDate);
            // Set pickup time (between 8 AM and 6 PM)
            int hour = 6 + new Random().nextInt(12);
            int minute = new Random().nextInt(60);
            LocalTime doTime = LocalTime.of(hour, minute);
            tripTicket.setRequestedDropOffTime(Time.valueOf(doTime));
        }


    }

    // Random data generation helper methods
    private String getRandomFirstName() {
        String[] firstNames = {"John", "Jane", "Michael", "Mary", "Robert", "Susan", "James",
                "Patricia", "David", "Jennifer", "Richard", "Elizabeth"};
        return firstNames[new Random().nextInt(firstNames.length)];
    }

    private String getRandomLastName() {
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis",
                "Garcia", "Rodriguez", "Wilson", "Martinez", "Anderson"};
        return lastNames[new Random().nextInt(lastNames.length)];
    }

    private String getRandomGender() {
        String[] genders = {"Male", "Female", "Other"};
        return genders[new Random().nextInt(genders.length)];
    }

    private LocalDate getRandomDob() {
        int yearsAgo = 21 + new Random().nextInt(60);
        int extraDays = new Random().nextInt(365);
        return LocalDate.now().minusYears(yearsAgo).minusDays(extraDays);
    }

    private String getRandomServiceLevel() {
        String[] serviceLevels = {"Ambulatory", "Wheelchair", "Stretcher"};
        return serviceLevels[new Random().nextInt(serviceLevels.length)];
    }

    private String getRandomMobilityFactor() {
        String[] mobilityFactors = {"None", "Cane", "Walker", "Wheelchair", "Service Animal"};
        return mobilityFactors[new Random().nextInt(mobilityFactors.length)];
    }

    private String generateRandomStreetAddress() {
        String[] streetNames = {"Main St", "Oak Ave", "Maple Rd", "Washington Blvd", "Park Ave",
                "Pine St", "Cedar Ln", "Elm St", "River Rd", "Lake Dr"};
        return (100 + new Random().nextInt(9900)) + " " + streetNames[new Random().nextInt(streetNames.length)];
    }

    private String generateRandomCity() {
        String[] cities = {"Springfield", "Franklin", "Greenville", "Bristol", "Clinton",
                "Madison", "Georgetown", "Salem", "Oxford", "Arlington"};
        return cities[new Random().nextInt(cities.length)];
    }

    private String generateRandomState() {
        String[] states = {"CO",};
        return states[new Random().nextInt(states.length)];
    }

    private String generateRandomZipCode() {
        return String.format("%05d", 80001 + new Random().nextInt(81658));
    }

    private String generateRandomPhoneNumber() {
        return String.format("(%03d) %03d-%04d",
                100 + new Random().nextInt(900),
                100 + new Random().nextInt(900),
                1000 + new Random().nextInt(9000));
    }




    public TripSummary generateTestTripSummary() {
        Random random = new Random();
        var coords = generateTestCoordinates(true);

        // Generate vehicle location with the provided coordinates
        VehicleLocation vehicleLocation = generateTestVehicleLocation(coords.latitude(), coords.longitude());

        return TripSummary.builder()
                // Base trip properties
                .guest(generateTestUberGuest())
                .pickup(generateTestCoordinates(true))
                .dropoff(generateTestCoordinates(false))
                .noteForDriver("Please call upon arrival")
                .additionalGuests(generateTestAdditionalGuests())
                .communicationChannel("SMS")
                .productId("product-" + random.nextInt(1000))
                .fareId("fare-" + random.nextInt(1000))
                .policyUuid(UUID.randomUUID().toString())
                .expenseCode("EXP-" + random.nextInt(10000))
                .expenseMemo("Business trip")
                .senderDisplayName(getRandomFirstName() + " " + getRandomLastName())
                .callEnabled(random.nextBoolean())


                // Trip status and identification
                .requestId("req-" + random.nextInt(100000))
                .status(getRandomTripStatus())
                .requestTime(System.currentTimeMillis() - random.nextInt(3600000))
                .riderTrackingUrl("https://track.example.com/" + UUID.randomUUID())
                .surgeMultiplier(1.0 + random.nextDouble())
                .requesterUuid(UUID.randomUUID().toString())
                .locationUuid(UUID.randomUUID().toString())

                // Fare and cost information
                .currencyCode("USD")
                .clientFare("$" + (10 + random.nextInt(90)) + "." + random.nextInt(100))
                .clientFareNumeric(10.0 + random.nextDouble() * 90.0)
                .clientFareWithoutTip("$" + (10 + random.nextInt(80)) + "." + random.nextInt(100))
                .canTip(true)

                // Trip metrics
                .tripDistanceMiles(1.0 + random.nextDouble() * 20.0)
                .tripDurationSeconds(600L + random.nextInt(3000))
                .tripLegNumber(1)
                .totalTripLegs(1 + random.nextInt(3))

                // Optional trip details
                .finalDestination(generateTestCoordinates(false))
                .statusDetail("On schedule")

                .vehicleLocation(vehicleLocation)
                .driver(generateTestDriver())
                .vehicle(generateTestVehicle())
                .beginTripTime(System.currentTimeMillis() - random.nextInt(1800000))
                .dropoffTime(System.currentTimeMillis() + random.nextInt(1800000))

                .schedulingDetails(generateTestSchedulingDetails())


                .build();
    }

    // Helper methods to generate nested objects
    private UberGuest generateTestUberGuest() {
        String firstName = getRandomFirstName();
        String lastName = getRandomLastName();
        return UberGuest.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@example.com")
                .phoneNumber(generateRandomPhoneNumber())
                .build();
    }

    private CoordinatesWithPlace generateTestCoordinates(boolean isPickup) {
        Random random = new Random();
        double baseLat = 39.7392 + (random.nextDouble() - 0.5) * 0.1; // Denver area
        double baseLon = -104.9903 + (random.nextDouble() - 0.5) * 0.1;

        return CoordinatesWithPlace.builder()
                .latitude(baseLat)
                .longitude(baseLon)
                .build();
    }

    private List<UberGuest> generateTestAdditionalGuests() {
        Random random = new Random();
        int count = random.nextInt(3); // 0-2 additional guests
        List<UberGuest> guests = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            guests.add(generateTestUberGuest());
        }

        return guests;
    }


    private VehicleLocation generateTestVehicleLocation(Double latitude, Double longitude) {
        Random random = new Random();
        return VehicleLocation.builder()
                .latitude(latitude)
                .longitude(longitude)
                .bearing(random.nextDouble(360))
                .build();
    }

    private Driver generateTestDriver() {
        String firstName = getRandomFirstName();
        String lastName = getRandomLastName();
        Random random = new Random();

        return Driver.builder()


                .name(firstName + " " + lastName)
                .phoneNumber(generateRandomPhoneNumber())
                .rating(5.0f + random.nextDouble())
                .pictureUrl("https://example.com/driver/pic/" + UUID.randomUUID())
                .build();
    }

    private Vehicle generateTestVehicle() {
        String[] makes = {"Toyota", "Honda", "Ford", "Chevrolet", "Hyundai"};
        String[] models = {"Camry", "Accord", "Fusion", "Malibu", "Sonata"};
        String[] colors = {"Black", "White", "Silver", "Blue", "Red"};

        Random random = new Random();

        return Vehicle.builder()
                .make(makes[random.nextInt(makes.length)])
                .model(models[random.nextInt(models.length)])
                .vehicleColorName(colors[random.nextInt(colors.length)])
                .licensePlate(generateRandomLicensePlate())
                .build();
    }

    private SchedulingDetails generateTestSchedulingDetails() {
        Random random = new Random();
        return SchedulingDetails.builder()


                .pickupTime(System.currentTimeMillis() + random.nextInt(604800000))
                .build();
    }


    private String getRandomTripStatus() {
        String[] statuses = {"REQUESTED", "ACCEPTED", "ARRIVING", "IN_PROGRESS", "COMPLETED", "CANCELED"};
        return statuses[new Random().nextInt(statuses.length)];
    }

    private String generateRandomLicensePlate() {
        Random random = new Random();
        String letters = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        String numbers = "0123456789";

        StringBuilder plate = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            plate.append(letters.charAt(random.nextInt(letters.length())));
        }
        plate.append("-");
        for (int i = 0; i < 3; i++) {
            plate.append(numbers.charAt(random.nextInt(numbers.length())));
        }

        return plate.toString();
    }







    public EntityManager getEntityManager() {
        return entityManager;
    }
}
