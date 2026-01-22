package com.clearinghouse.dao;

import com.clearinghouse.entity.Activity;
import com.clearinghouse.entity.TripTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import com.clearinghouse.entity.AbstractEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(ActivityDAO.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Sql(scripts = {"classpath:test-schema.sql"})
class ActivityDAOTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ActivityDAO activityDAO;

    private Activity testActivity;
    private TripTicket testTripTicket;

    @BeforeEach
    void setUp() {
        // ensure timezone is set for lifecycle callbacks
        com.clearinghouse.entity.AbstractEntity.setTimezone("UTC");
        // Get or create a test trip ticket
        testTripTicket = entityManager.find(TripTicket.class, 1);
        if (testTripTicket == null) {
            testTripTicket = new TripTicket();
            // ensure required non-null relations exist (RequesterProviderID)
            com.clearinghouse.entity.Address addr = new com.clearinghouse.entity.Address();
            addr.setStreet1("123 Test St");
            addr.setCity("Testville");
            // latitude/longitude are primitive floats - must set non-null values
            addr.setLatitude(0.0f);
            addr.setLongitude(0.0f);
            entityManager.persist(addr);
            entityManager.flush();

            com.clearinghouse.entity.Provider provider = new com.clearinghouse.entity.Provider();
            provider.setProviderName("TestProvider");
            provider.setIsActive(true);
            provider.setAddress(addr);
            // create a ProviderType to satisfy NOT NULL provider_type_id
            com.clearinghouse.entity.ProviderType pt = new com.clearinghouse.entity.ProviderType();
            pt.setProviderTypes("TestType");
            entityManager.persist(pt);
            entityManager.flush();
            provider.setProviderType(pt);
            entityManager.persist(provider);
            entityManager.flush();

            testTripTicket.setOriginProvider(provider);
            // ensure RequesterCustomerID is not null (schema requires it)
            testTripTicket.setOriginCustomerId("0");
            testTripTicket.setRequesterTripId("0");
            testTripTicket.setCommonTripId("CT-TEST");
            // set customer names required by schema
            testTripTicket.setCustomerFirstName("John");
            testTripTicket.setCustomerLastName("Doe");
            // satisfy required address columns by reusing the created address
            testTripTicket.setCustomerAddress(addr);
            testTripTicket.setPickupAddress(addr);
            testTripTicket.setDropOffAddress(addr);
            // create and set a Status to satisfy NOT NULL statusid
            com.clearinghouse.entity.Status status = new com.clearinghouse.entity.Status();
            status.setType("NEW");
            status.setDescription("Test status");
            entityManager.persist(status);
            entityManager.flush();
            testTripTicket.setStatus(status);
            // set non-null flags
            testTripTicket.setPickupDateTime(null);
            testTripTicket.setVersion("0");
            testTripTicket.setTripTicketInvisible(false);
            // let JPA assign the id
            entityManager.persist(testTripTicket);
            // ensure entity is flushed and id assigned
            entityManager.flush();
        } else {
            // reattach managed instance in case it's detached
            testTripTicket = entityManager.merge(testTripTicket);
        }

        // Create a test activity
        testActivity = new Activity();
        testActivity.setTripTicket(testTripTicket);
        testActivity.setAction("TEST_ACTION");

        
        // Persist test data
        entityManager.persist(testActivity);
        entityManager.flush();
    }

    @Test
    void findAllActivities_ShouldReturnAllActivities() {
        // Act
        List<Activity> result = activityDAO.findAllActivities();
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(a -> a.getActivityId() == testActivity.getActivityId()));
    }

    @Test
    void findActivityById_ShouldReturnActivity() {
        // Act
        Activity result = activityDAO.findActivityByActivityId(testActivity.getActivityId());
        
        // Assert
        assertNotNull(result);
        assertEquals(testActivity.getAction(), result.getAction());
    }

    @Test
    void createActivity_ShouldPersistActivity() {
        // Arrange
        Activity newActivity = new Activity();
        newActivity.setTripTicket(testTripTicket);
        newActivity.setAction("NEW_TEST_ACTION");

        
        // Act
        Activity result = activityDAO.createActivity(newActivity);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getActivityId() > 0);
        assertEquals(newActivity.getAction(), result.getAction());
    }

    @Test
    void updateActivity_ShouldUpdateAndReturnActivity() {
        // Arrange
        Activity activity = entityManager.find(Activity.class, testActivity.getActivityId());
        activity.setAction("UPDATED_ACTION");
        
        // Act
        Activity result = activityDAO.update(activity);
        
        // Assert
        assertNotNull(result);
        assertEquals("UPDATED_ACTION", result.getAction());
    }

    @Test
    void deleteActivityById_ShouldDeleteActivity() {
        // Act
        activityDAO.delete(testActivity);
        
        // Assert
        Activity result = entityManager.find(Activity.class, testActivity.getActivityId());
        assertNull(result);
    }

    @Test
    void findActivitiesByTripTicketId_ShouldReturnActivitiesForTripTicket() {
        // Act
        List<Activity> result = activityDAO.findAllActivitesByTripTicketId(testTripTicket.getId());
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(testTripTicket.getId(), result.get(0).getTripTicket().getId());
    }
}