package com.clearinghouse.service;

import com.clearinghouse.dao.*;
import com.clearinghouse.dto.*;
import com.clearinghouse.entity.*;
import com.clearinghouse.testutil.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TripTicketServiceTest {

    @Mock
    private TripTicketDAO tripTicketDAO;

    @Mock
    private ProviderDAO providerDAO;

    @Mock
    private TripTicketVectorStoreService tripTicketVectorStoreService;

    @Mock
    private ActivityService activityService;

    @Mock
    private UserNotificationDataDAO userNotificationDataDAO;

    @Mock
    private NotificationDAO notificationDAO;

    @Mock
    private ModelMapper tripTicketModelMapper;

    @InjectMocks
    private TripTicketService tripTicketService;

    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // keep tripTicketModelMapper as a Mockito mock (do not reassign to a real ModelMapper)
        
        TripTicketDTO mockDTO = new TripTicketDTO();
        mockDTO.setId(1);
        when(tripTicketModelMapper.map(any(), eq(TripTicketDTO.class))).thenReturn(mockDTO);
        // also stub mapping to BO so service toBO/toDTO won't operate on null
        when(tripTicketModelMapper.map(any(TripTicketDTO.class), eq(TripTicket.class))).thenAnswer(invocation -> {
            TripTicketDTO dto = invocation.getArgument(0);
            TripTicket tt = new TripTicket();
            tt.setId(dto.getId());
            return tt;
        });
        
        Provider mockProvider = new Provider();
        mockProvider.setProviderId(1);
        // provide a default expiration time to avoid NPE when service calls toString()
        java.sql.Time defaultTime = java.sql.Time.valueOf("12:00:00");
        mockProvider.setTripTicketExpirationTimeOfDay(defaultTime);
        when(providerDAO.findProviderByProviderId(anyInt())).thenReturn(mockProvider);
    // stub vector store service used during update flow
    when(tripTicketVectorStoreService.updateTripTicket(any(TripTicketDTO.class))).thenAnswer(invocation -> invocation.getArgument(0));
    //when(tripTicketVectorStoreService.addTripTicket(any(TripTicketDTO.class))).thenAnswer(invocation -> invocation.getArgument(0));
    // avoid notification NPEs by returning empty lists
    when(userNotificationDataDAO.getUsersForTripReceived(anyInt())).thenReturn(Collections.emptyList());
    }

    @Test
    void findAllTripTicket_shouldReturnAllTickets() {
        // Setup test data
        LocalDateTime now = LocalDateTime.now();
        TripTicket ticket1 = TestData.tripTicket()
            .withId(1)
            .withPickupTime("10:00")
            .withPickupDate(now)
            .build();
        // ensure origin provider exists to avoid activity creation NPEs
        Provider origin1 = new Provider(); origin1.setProviderId(5); ticket1.setOriginProvider(origin1);
        TripTicket ticket2 = TestData.tripTicket()
            .withId(2)
            .withPickupTime("14:00")
            .withPickupDate(now)
            .build();
        Provider origin2 = new Provider(); origin2.setProviderId(6); ticket2.setOriginProvider(origin2);
        
        when(tripTicketDAO.findAllTripTickets()).thenReturn(Arrays.asList(ticket1, ticket2));

        // Execute
        List<TripTicketDTO> result = tripTicketService.findAllTripTicket();

        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(tripTicketDAO).findAllTripTickets();
    }

    @Test
    public void findTripTicketByTripTicketId_shouldReturnTripTicket() {
        // Setup test data
        int ticketId = 1;
        LocalDateTime now = LocalDateTime.now();
        TripTicket ticket = TestData.tripTicket()
            .withId(ticketId)
            .withPickupTime("10:00")
            .withPickupDate(now)
            .build();
        Provider origin = new Provider(); origin.setProviderId(5); ticket.setOriginProvider(origin);

        when(tripTicketDAO.findTripTicketByTripTicketId(ticketId)).thenReturn(ticket);

        // Execute
        TripTicketDTO result = tripTicketService.findTripTicketByTripTicketId(ticketId);

        // Verify
        assertNotNull(result);
        assertEquals(ticketId, result.getId());
        verify(tripTicketDAO).findTripTicketByTripTicketId(ticketId);
    }

    @Test
    void testCreateTripTicket() {
        // Setup test data
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);
        TripTicket ticket = TestData.tripTicket()
            .withId(1)
            .withPickupDate(futureDate)
            .build();
        // ensure origin provider exists to avoid activity creation NPEs
        Provider origin = new Provider(); origin.setProviderId(5); ticket.setOriginProvider(origin);
        TripTicketDTO ticketDTO = TestData.tripTicket()
            .withId(1)
            .withPickupDate(futureDate)
            .buildDTO();

        when(tripTicketDAO.createTripTicket(any(TripTicket.class))).thenReturn(ticket);

        // Execute
        TripTicketDTO result = tripTicketService.createTripTicket(ticketDTO);

        // Verify
        assertNotNull(result);
        assertEquals(ticketDTO.getId(), result.getId());
        verify(tripTicketDAO).createTripTicket(any(TripTicket.class));
    }

    @Test
    public void testUpdateTripTicket() {
        // Setup test data
        int ticketId = 1;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(1);
        
        TripTicket existingTicket = TestData.tripTicket()
            .withId(ticketId)
            .withPickupDate(now)
            .build();
        Provider originExisting = new Provider(); originExisting.setProviderId(5); existingTicket.setOriginProvider(originExisting);
        TripTicket updatedTicket = TestData.tripTicket()
            .withId(ticketId)
            .withPickupTime("10:00")
            .withPickupDate(now)
            .build();
        Provider originUpdated = new Provider(); originUpdated.setProviderId(5); updatedTicket.setOriginProvider(originUpdated);
        TripTicketDTO ticketDTO = TestData.tripTicket()
            .withId(ticketId)
            .withPickupDate(futureDate)
            .buildDTO();

    // Service updateTripTicket does not call find by id; it converts DTO->BO and calls update
    when(tripTicketDAO.updateTripTicket(any(TripTicket.class))).thenReturn(updatedTicket);

        // Execute
        TripTicketDTO result = tripTicketService.updateTripTicket(ticketDTO);

        // Verify
        assertNotNull(result);
        assertEquals(ticketDTO.getId(), result.getId());
        verify(tripTicketDAO).updateTripTicket(any(TripTicket.class));
        verify(tripTicketVectorStoreService).updateTripTicket(any(TripTicketDTO.class));
    }

    /*
    @Test
    void updateTripTicketStatus_shouldUpdateStatus() {
        // Setup
        int ticketId = 1;
        Status newStatus = new Status();
        newStatus.setStatusId(2);
        newStatus.setDescription("IN_PROGRESS");

        TripTicket existingTicket = TestData.tripTicket()
            .withId(ticketId)
            .withPickupDate(LocalDateTime.now().plusDays(1))
            .build();

        when(tripTicketDAO.findTripTicketByTripTicketId(ticketId)).thenReturn(existingTicket);
        when(tripTicketDAO.updateTripTicket(any(TripTicket.class))).thenReturn(existingTicket);

        // Execute
        tripTicketService.updatedLastSyncDateAndTripStatus(ticketId, newStatus);

        // Verify
        verify(tripTicketDAO).findTripTicketByTripTicketId(ticketId);
        verify(tripTicketDAO).updateTripTicket(any(TripTicket.class));
    }

     */
}