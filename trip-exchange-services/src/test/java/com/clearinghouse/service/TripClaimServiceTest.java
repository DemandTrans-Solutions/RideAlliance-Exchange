package com.clearinghouse.service;

import com.clearinghouse.dao.*;
import com.clearinghouse.dto.TripClaimDTO;
import com.clearinghouse.entity.*;
import com.clearinghouse.enumentity.TripClaimStatusConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.List;
import com.clearinghouse.dto.StatusDTO;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TripClaimServiceTest {

    @Mock
    private TripClaimDAO tripClaimDAO;

    @Mock
    private TripTicketDAO tripTicketDAO;

    @Mock
    private UserNotificationDataDAO userNotificationDataDAO;

    @Mock
    private ModelMapper tripClaimModelMapper;

    @Mock
    private NotificationDAO notificationDAO;

    @Mock
    private ActivityService activityService;

    @Mock
    private ProviderDAO providerDAO;

    @Mock
    private UserService userService;

    @InjectMocks
    private TripClaimService tripClaimService;

    // shared default DTO returned by ModelMapper in tests
    private TripClaimDTO defaultClaimDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        Provider mockProvider = new Provider();
        mockProvider.setProviderId(1);
        mockProvider.setContactEmail("test@example.com");
        
    TripTicket mockTicket = new TripTicket();
    mockTicket.setId(1);
    // provide origin and lastStatusChangedByProvider to avoid NPEs in service methods
    Provider origin = new Provider();
    origin.setProviderId(5);
    mockTicket.setOriginProvider(origin);
    Provider lastChanged = new Provider();
    lastChanged.setProviderId(6);
    mockTicket.setLastStatusChangedByProvider(lastChanged);
    // ensure tripClaims set exists to allow approve flow iterations
    mockTicket.setTripClaims(new HashSet<>());
        
        TripClaim mockClaim = new TripClaim();
        mockClaim.setClaimantProvider(mockProvider);
        mockClaim.setTripTicket(mockTicket);
        mockClaim.setNotes("Test Notes");
        // give mockClaim a default status to avoid NPEs when service inspects status
        Status defaultStatus = new Status();
        defaultStatus.setStatusId(1);
        mockClaim.setStatus(defaultStatus);

    // Default stubs used by multiple tests to provide non-null nested objects
        when(tripClaimDAO.findTripClaimByTripClaimId(anyInt())).thenReturn(mockClaim);
        when(tripTicketDAO.findTripTicketByTripTicketId(anyInt())).thenReturn(mockTicket);
        when(providerDAO.findProviderByProviderId(anyInt())).thenReturn(mockProvider);
    // Mock security context to provide username for toDTO logic
    org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
    when(auth.getName()).thenReturn("testUser");
    when(auth.getPrincipal()).thenReturn("testUser");
    // set details to a User with id to satisfy updateTickcetForClaimAction
    User authUser = new User();
    authUser.setId(1);
    when(auth.getDetails()).thenReturn(authUser);
    org.springframework.security.core.context.SecurityContext secCtx = mock(org.springframework.security.core.context.SecurityContext.class);
    when(secCtx.getAuthentication()).thenReturn(auth);
    org.springframework.security.core.context.SecurityContextHolder.setContext(secCtx);
    when(userService.findProviderIdByUsername("testUser")).thenReturn(1);
    // default: no provider partners to avoid automatic approve branch
    when(userNotificationDataDAO.getProviderPartners(anyInt())).thenReturn(List.of());
    defaultClaimDTO = new TripClaimDTO();
    StatusDTO statusDTO = new StatusDTO();
    statusDTO.setStatusId(TripClaimStatusConstants.pending.tripClaimStatusUpdate());
    defaultClaimDTO.setStatus(statusDTO);
    defaultClaimDTO.setRequesterProviderFare(0f);
    // ensure proposedPickupTime is parseable by service (ISO_LOCAL_DATE_TIME)
    defaultClaimDTO.setProposedPickupTime(java.time.LocalDateTime.now().toString());
    // provide identifying fields to the DTO returned by the mapper
    defaultClaimDTO.setId(1);
    defaultClaimDTO.setNotes("Default claim notes");
    when(tripClaimModelMapper.map(any(TripClaim.class), eq(TripClaimDTO.class))).thenReturn(defaultClaimDTO);
        

    }

    @Test
    void findAllTripClaims_ReturnsAllTripClaims() {
        int tripTicketId = 1;
        Provider p = new Provider();
        p.setProviderId(1);
        TripTicket tt = new TripTicket();
        tt.setId(tripTicketId);
        TripClaim c1 = new TripClaim();
        c1.setClaimantProvider(p);
        c1.setTripTicket(tt);
        TripClaim c2 = new TripClaim();
        c2.setClaimantProvider(p);
        c2.setTripTicket(tt);
        List<TripClaim> tripClaims = List.of(c1, c2);
    when(tripClaimDAO.findAllTripClaims(tripTicketId)).thenReturn(tripClaims);
    when(tripClaimModelMapper.map(any(TripClaim.class), eq(TripClaimDTO.class))).thenReturn(defaultClaimDTO);

        List<TripClaimDTO> result = tripClaimService.findAllTripClaims(tripTicketId);

        assertEquals(tripClaims.size(), result.size());
        verify(tripClaimDAO).findAllTripClaims(tripTicketId);
    }

    @Test
    void findTripClaimByTripClaimId_ReturnsTripClaim() {
        int tripTicketId = 1;
        int tripClaimId = 1;
        TripClaim tripClaim = new TripClaim();
        Provider p = new Provider();
        p.setProviderId(1);
        tripClaim.setClaimantProvider(p);
        TripTicket tt = new TripTicket();
        tt.setId(tripTicketId);
        tripClaim.setTripTicket(tt);
    when(tripClaimDAO.findTripClaimByTripClaimId( tripClaimId)).thenReturn(tripClaim);
    when(tripClaimModelMapper.map(tripClaim, TripClaimDTO.class)).thenReturn(defaultClaimDTO);

        TripClaimDTO result = tripClaimService.findTripClaimByTripClaimId( tripClaimId);

        assertNotNull(result);
        verify(tripClaimDAO).findTripClaimByTripClaimId( tripClaimId);
    }

    @Test
    void createTripClaim_CreatesNewTripClaim() {
        int tripTicketId = 1;
        TripClaimDTO tripClaimDTO = new TripClaimDTO();
        StatusDTO s = new StatusDTO();
        s.setStatusId(TripClaimStatusConstants.pending.tripClaimStatusUpdate());
        tripClaimDTO.setStatus(s);
        // ensure toBO keeps claimantProvider by setting claimantProviderId
        tripClaimDTO.setClaimantProviderId(1);
        TripClaim tripClaim = new TripClaim();
        Provider p = new Provider();
        p.setProviderId(1);
        tripClaim.setClaimantProvider(p);
        TripTicket tt = new TripTicket();
        tt.setId(tripTicketId);
        tripClaim.setTripTicket(tt);
    TripTicket tripTicket = new TripTicket();
    Provider origin = new Provider();
    origin.setProviderId(5);
    tripTicket.setOriginProvider(origin);
    when(tripClaimModelMapper.map(tripClaimDTO, TripClaim.class)).thenReturn(tripClaim);
        when(tripTicketDAO.findTripTicketByTripTicketId(tripTicketId)).thenReturn(tripTicket);
        when(tripClaimDAO.createTripTripClaim(tripClaim)).thenReturn(tripClaim);
        when(tripClaimModelMapper.map(tripClaim, TripClaimDTO.class)).thenReturn(tripClaimDTO);

        TripClaimDTO result = tripClaimService.createTripClaim(tripTicketId, tripClaimDTO);

        assertNotNull(result);
        verify(tripClaimDAO).createTripTripClaim(tripClaim);
    }

    @Test
    void updateTripClaim_UpdatesTripClaim() {
        int tripTicketId = 1;
        TripClaimDTO tripClaimDTO = new TripClaimDTO();
    tripClaimDTO.setRequesterProviderFare(0f);
    StatusDTO s = new StatusDTO();
    s.setStatusId(TripClaimStatusConstants.pending.tripClaimStatusUpdate());
    tripClaimDTO.setStatus(s);
    tripClaimDTO.setProposedPickupTime(java.time.LocalDateTime.now().toString());
    tripClaimDTO.setNotes("new notes");
        TripClaim tripClaim = new TripClaim();
        Provider p = new Provider();
        p.setProviderId(1);
        tripClaim.setClaimantProvider(p);
        tripClaim.setNotes("existing notes");
        TripTicket tt = new TripTicket();
        tt.setId(tripTicketId);
        tripClaim.setTripTicket(tt);
        when(tripClaimModelMapper.map(tripClaimDTO, TripClaim.class)).thenReturn(tripClaim);
    when(tripClaimDAO.findTripClaimByTripClaimId( tripClaimDTO.getId())).thenReturn(tripClaim);
        when(tripClaimDAO.updateTripClaim(tripClaim)).thenReturn(tripClaim);
        when(tripClaimModelMapper.map(tripClaim, TripClaimDTO.class)).thenReturn(tripClaimDTO);

        TripClaimDTO result = tripClaimService.updateTripClaim(tripTicketId, tripClaimDTO);

    assertNotNull(result);
    // updateTripClaim is invoked twice in the service flow (initial update + final update)
    verify(tripClaimDAO, times(2)).updateTripClaim(tripClaim);
    }

    @Test
    void rescindTripClaim_RescindsTripClaim() {
        int tripTicketId = 1;
        int tripClaimId = 1;
        TripClaim tripClaim = new TripClaim();
        var status = new Status();
        status.setStatusId(TripClaimStatusConstants.pending.tripClaimStatusUpdate());;
        tripClaim.setStatus(status);
        Provider p = new Provider();
        p.setProviderId(1);
        tripClaim.setClaimantProvider(p);
        TripTicket tt = new TripTicket();
        tt.setId(tripTicketId);
        tripClaim.setTripTicket(tt);
    when(tripClaimDAO.findTripClaimByTripClaimId( tripClaimId)).thenReturn(tripClaim);
    when(tripClaimDAO.updateTripClaim(tripClaim)).thenReturn(tripClaim);
    when(tripClaimModelMapper.map(tripClaim, TripClaimDTO.class)).thenReturn(defaultClaimDTO);

        TripClaimDTO result = tripClaimService.rescindTripClaim(tripTicketId, tripClaimId);

        assertNotNull(result);
        verify(tripClaimDAO).updateTripClaim(tripClaim);
    }

    @Test
    void declineTripClaim_DeclinesTripClaim() {
        int tripTicketId = 1;
        int tripClaimId = 1;
        TripClaim tripClaim = new TripClaim();
        // ensure tripClaim returned from DAO has a tripTicket and origin provider to avoid NPE
    TripTicket tt = new TripTicket();
        tt.setId(tripTicketId);
        Provider origin = new Provider();
        origin.setProviderId(5);
        tt.setOriginProvider(origin);
    // ensure claimant provider exists to avoid NPE in activity creation
    Provider claimant = new Provider();
    claimant.setProviderId(2);
    tripClaim.setClaimantProvider(claimant);
    tripClaim.setTripTicket(tt);
    when(tripClaimDAO.findTripClaimByTripClaimId( tripClaimId)).thenReturn(tripClaim);
    when(tripClaimDAO.updateTripClaim(tripClaim)).thenReturn(tripClaim);
    when(tripClaimModelMapper.map(tripClaim, TripClaimDTO.class)).thenReturn(defaultClaimDTO);

        TripClaimDTO result = tripClaimService.declineTripClaim(tripTicketId, tripClaimId);

        assertNotNull(result);
        verify(tripClaimDAO).updateTripClaim(tripClaim);
    }

    @Test
    void approveTripClaim_ApprovesTripClaim() {
        int tripTicketId = 1;
        int tripClaimId = 1;
        TripClaim tripClaim = new TripClaim();
        // ensure the returned trip claim has the expected nested objects
    TripTicket tripTicket = new TripTicket();
        tripTicket.setId(tripTicketId);
    // provide origin and lastStatusChangedByProvider to avoid NPEs in activity creation
    Provider origin = new Provider();
    origin.setProviderId(3);
    tripTicket.setOriginProvider(origin);
    tripTicket.setLastStatusChangedByProvider(origin);
        Provider claimant = new Provider();
        claimant.setProviderId(2);
        tripClaim.setTripTicket(tripTicket);
        tripClaim.setClaimantProvider(claimant);
        // give the tripClaim a status and an id to simulate realistic DAO object
        tripClaim.setId(tripClaimId);
        Status status = new Status();
        status.setStatusId(TripClaimStatusConstants.pending.tripClaimStatusUpdate());
        tripClaim.setStatus(status);

    // ensure tripTicket has claims set so service can iterate without NPE
    Set<TripClaim> claimsSet = new HashSet<>();
    claimsSet.add(tripClaim);
    tripTicket.setTripClaims(claimsSet);

        when(tripClaimDAO.findTripClaimByTripClaimId(tripClaimId)).thenReturn(tripClaim);
    when(tripTicketDAO.findTripTicketByTripTicketId(tripTicketId)).thenReturn(tripTicket);
        when(tripTicketDAO.updateTripTicket(tripTicket)).thenReturn(tripTicket);
        when(tripClaimModelMapper.map(tripClaim, TripClaimDTO.class)).thenReturn(defaultClaimDTO);

        TripClaimDTO result = tripClaimService.approveTripClaim(tripTicketId, tripClaimId);

        assertNotNull(result);
    verify(tripTicketDAO).updateTripTicket(tripTicket);
    }
}