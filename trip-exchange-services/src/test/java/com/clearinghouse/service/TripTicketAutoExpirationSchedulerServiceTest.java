package com.clearinghouse.service;

import com.clearinghouse.dao.*;
import com.clearinghouse.dto.ActivityDTO;
import com.clearinghouse.entity.*;
import freemarker.template.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TripTicketAutoExpirationSchedulerServiceTest {

    @Mock
    private UserNotificationDataDAO userNotificationDataDAO;

    @Mock
    private ProviderDAO providerDAO;

    @Mock
    private ActivityService activityService;

    @Mock
    private NotificationDAO notificationDAO;

    @Mock
    private TripTicketDAO tripTicketDAO;

    @Mock
    private Configuration freemarkerConfiguration;

    @InjectMocks
    private TripTicketAutoExpirationSchedulerService tripTicketAutoExpirationSchedulerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        Provider provider = new Provider();
        provider.setProviderName("Test Provider");
        
        TripTicket ticket = new TripTicket();
        ticket.setOriginProvider(provider);
        
        
    }

    @Test
    void getAvailableTripTicketsWithNoClaims_ReturnsTicketsWithNoClaims() {
        // Create mock tickets
        TripTicket ticketWithNoClaim = mock(TripTicket.class);
        when(ticketWithNoClaim.getTripClaims()).thenReturn(new HashSet<>());
        
        TripTicket ticketWithClaim = mock(TripTicket.class);
        TripClaim claim = mock(TripClaim.class);
        Set<TripClaim> claims = new HashSet<>();
        claims.add(claim);
        when(ticketWithClaim.getTripClaims()).thenReturn(claims);
        
        List<TripTicket> tripTickets = List.of(ticketWithNoClaim, ticketWithClaim);
        when(tripTicketDAO.getAvailableTripTickets()).thenReturn(tripTickets);

        List<TripTicket> result = tripTicketAutoExpirationSchedulerService.getAvailableTripTicketsWithNoClaims();

        assertEquals(1, result.size());
        verify(tripTicketDAO).getAvailableTripTickets();
    }

    @Test
    void tripTicketAutoExpiration_ExpiresTickets() {
        // Create mock with proper time settings
        TripTicket tripTicket = mock(TripTicket.class);
        Provider mockProvider = mock(Provider.class);
        // when service looks up provider by id, return a provider with a name to avoid NPE
        Provider providerFromDao = new Provider();
        providerFromDao.setProviderId(1);
        providerFromDao.setProviderName("Test Provider");
        when(mockProvider.getProviderId()).thenReturn(1);
        when(providerDAO.findProviderByProviderId(1)).thenReturn(providerFromDao);
        
        // Setup chain of mock calls
        when(tripTicket.getExpirationDate()).thenReturn(LocalDateTime.now(Clock.systemDefaultZone()).minusDays(1));
        when(tripTicket.getTripClaims()).thenReturn(new HashSet<>());
    when(tripTicket.getOriginProvider()).thenReturn(mockProvider);
    when(mockProvider.getProviderId()).thenReturn(1);
        
        when(tripTicketDAO.getAvailableTripTickets()).thenReturn(List.of(tripTicket));

    // ensure user notifications will be created
    User user = new User();
    user.setIsNotifyTripExpired(true);
    user.setEmail("test@example.com");
    user.setName("Test User");
    // give the user at least one authority so the service code can read userAuthority.get(0)
    com.clearinghouse.entity.UserAuthority ua = new com.clearinghouse.entity.UserAuthority();
    ua.setAuthority("ROLE_PROVIDERADMIN");
    ua.setUser(user);
    java.util.Set<com.clearinghouse.entity.UserAuthority> auths = new java.util.HashSet<>();
    auths.add(ua);
    user.setAuthorities(auths);
    when(userNotificationDataDAO.getUsersOfProvider(anyInt())).thenReturn(List.of(user));

    // ensure tripTicket has pickup date/time values used by the template generation
    when(tripTicket.getRequestedPickupDate()).thenReturn(LocalDate.of(2023, 1, 2));
    when(tripTicket.getRequestedPickupTime()).thenReturn(java.sql.Time.valueOf("10:15:00"));
    when(tripTicket.getCommonTripId()).thenReturn("CT-123");

    tripTicketAutoExpirationSchedulerService.tripTicketAutoExpiration();

    verify(tripTicketDAO).updateTripTicket(tripTicket);
    verify(notificationDAO).createNotification(any(Notification.class));
    verify(activityService).createActivity(any(ActivityDTO.class));
    }

    /*
    @Test
    void sendMailtoOriginatorForTicketExpired_SendsEmail() {
        TripTicket tripTicket = new TripTicket();
        Provider provider = new Provider();
        provider.setProviderId(1);
        tripTicket.setOrigin_provider(provider);

        User user = new User();
        user.setEmail("test@example.com");
        user.setAuthorities(List.of(new UserAuthority("ROLE_PROVIDERADMIN")));

        when(userNotificationDataDAO.getUsersOfProvider(provider.getProviderId())).thenReturn(List.of(user));

        tripTicketAutoExpirationSchedulerService.sendMailtoOriginatorForTicketExpired(tripTicket);

        verify(notificationDAO).createNotification(any(Notification.class));
    }
*/
    @Test
    void addActivtiyForTicketExpired_AddsActivity() {
        TripTicket tripTicket = new TripTicket();
        Provider provider = new Provider();
        provider.setProviderId(1);
        provider.setProviderName("Test Provider");
        tripTicket.setOriginProvider(provider);

        when(providerDAO.findProviderByProviderId(provider.getProviderId())).thenReturn(provider);

        tripTicketAutoExpirationSchedulerService.addActivtiyForTicketExpired(tripTicket);

        verify(activityService).createActivity(any(ActivityDTO.class));
    }
}
