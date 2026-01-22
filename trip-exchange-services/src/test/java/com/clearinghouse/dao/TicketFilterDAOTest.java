package com.clearinghouse.dao;

import com.clearinghouse.entity.TicketFilter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketFilterDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<TicketFilter> typedQuery;

    // Mock dependencies that TicketFilterDAO expects via constructor
    @Mock
    private TripTicketDAO tripTicketDAO;
    @Mock
    private UserDAO userDAO;
    @Mock
    private ListDAO listDAO;
    @Mock
    private ServiceAreaDAO serviceareaDAO;
    @Mock
    private ServiceDAO serviceDAO;
    @Mock
    private com.clearinghouse.service.ServiceService serviceService;

    @InjectMocks
    private TicketFilterDAO ticketFilterDAO;

    private TicketFilterDAO ticketFilterDAOSpy;

    @BeforeEach
    void setUp() {
        // Create a spy so we can stub protected getEntityManager() to return our mock
        ticketFilterDAOSpy = org.mockito.Mockito.spy(ticketFilterDAO);
        org.mockito.Mockito.doReturn(entityManager).when(ticketFilterDAOSpy).getEntityManager();
    }

    @Test
    void findAllFilters_ShouldReturnFilterList() {
        // Arrange
        TicketFilter tf = new TicketFilter();
        List<TicketFilter> expectedFilters = Arrays.asList(tf);
    when(entityManager.createQuery(anyString())).thenReturn((TypedQuery<TicketFilter>) typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedFilters);

        // Act
    List<TicketFilter> result = ticketFilterDAOSpy.findAllFilters();

        // Assert
        assertNotNull(result);
        assertEquals(expectedFilters.size(), result.size());
        assertEquals(expectedFilters.get(0), result.get(0));
        verify(entityManager).createQuery("SELECT f FROM TicketFilter f");
        verify(typedQuery).getResultList();
    }

}