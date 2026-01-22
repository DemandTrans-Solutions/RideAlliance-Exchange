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

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class NotificationSchedularAutoApprovalClaimsServiceTest {

    @Mock
    private Configuration freemarkerConfiguration;

    @Mock
    private NotificationDAO notificationDAO;

    @Mock
    private TicketFilterService filterService;

    @Mock
    private TripTicketDAO tripTicketDAO;

    @Mock
    private ActivityService activityService;

    @Mock
    private ProviderDAO providerDAO;

    @Mock
    private ApplicationSettingDAO applicationSettingDAO;

    @Mock
    private UserNotificationDataDAO userNotificationDataDAO;

    @InjectMocks
    private NotificationSchedularAutoApprovalClaimsService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetsynchronizedAvailableTripTickets() {
        List<TripTicket> tripTickets = new ArrayList<>();
        tripTickets.add(new TripTicket());

        // service calls the misspelled DAO method getAvailableTripTickets()
        when(tripTicketDAO.getAvailableTripTickets()).thenReturn(tripTickets);

        List<TripTicket> result = service.getsynchronizedAvailableTripTickets();

        assertEquals(tripTickets.size(), result.size());
    verify(tripTicketDAO).getAvailableTripTickets();
    }

    @Test
    void testAutoApprovalClaim_NoTickets() {
        // provide application settings so the method doesn't IndexOutOfBounds on get(0)
        ApplicationSetting appSetting = new ApplicationSetting();
        appSetting.setClaimApprovalTimeInHours(24);
        when(applicationSettingDAO.findAllApplicationSettings()).thenReturn(List.of(appSetting));

        when(tripTicketDAO.getAvailableTripTickets()).thenReturn(Collections.emptyList());

        service.autoApprovalClaim();

        verify(tripTicketDAO).getAvailableTripTickets();
        verifyNoInteractions(userNotificationDataDAO, notificationDAO);
    }

    @Test
    void testCreateActivityForClaimApprovedAndTicketApproved() {
        TripTicket tripTicket = new TripTicket();
        TripClaim tripClaim = new TripClaim();
        Provider provider = new Provider();
        provider.setProviderName("ProviderName");
        Provider claimantProvider = new Provider();
        claimantProvider.setProviderName("ClaimantProviderName");

        // set required nested fields to avoid NPEs inside the service
        Provider lastStatusProvider = new Provider();
        lastStatusProvider.setProviderId(1);
        tripTicket.setLastStatusChangedByProvider(lastStatusProvider);

        Provider originProvider = new Provider();
        originProvider.setProviderId(3);
        tripTicket.setOriginProvider(originProvider);

        claimantProvider.setProviderId(2);
        tripClaim.setClaimantProvider(claimantProvider);
        tripClaim.setTripTicket(tripTicket);

        when(providerDAO.findProviderByProviderId(anyInt())).thenReturn(provider, claimantProvider, originProvider);

        service.createActivityForClaimApprovedAndTicketApproved(tripTicket, tripClaim);

        verify(activityService, times(2)).createActivity(any(ActivityDTO.class));
    }

    @Test
    void testCreateActivityForClaimDeclined() {
        TripTicket tripTicket = new TripTicket();
        TripClaim tripClaim = new TripClaim();
        Provider provider = new Provider();
        provider.setProviderName("ProviderName");
        Provider claimantProvider = new Provider();
        claimantProvider.setProviderName("ClaimantProviderName");

        // set required nested fields to avoid NPEs inside the service
        Provider lastStatusProvider = new Provider();
        lastStatusProvider.setProviderId(1);
        tripTicket.setLastStatusChangedByProvider(lastStatusProvider);

        claimantProvider.setProviderId(2);
        tripClaim.setClaimantProvider(claimantProvider);
        tripClaim.setTripTicket(tripTicket);

        when(providerDAO.findProviderByProviderId(anyInt())).thenReturn(provider, claimantProvider);

        service.createActivityForClaimDeclined(tripTicket, tripClaim);

        verify(activityService).createActivity(any(ActivityDTO.class));
    }

    /*
    @Test
    void testSendMailToOriginatorCheckingAutoApproveFlag() {
        TripTicket tripTicket = new TripTicket();
        tripTicket.setOrigin_provider(new Provider());
        tripTicket.setApprovedTripClaim(new TripClaim());
        tripTicket.getApprovedTripClaim().setClaimant_provider(new Provider());

        User user = new User();
        user.setEmail("test@example.com");
        user.setIsNotifyTripClaimDeclined(true);
        user.setAuthorities(Set.of(new UserAuthority("ROLE_PROVIDERADMIN")));

        when(userNotificationDataDAO.getUsersForAutoApprovalTripClaim(anyInt())).thenReturn(List.of(user));

        service.sendMailToOriginatorCheckingAutoApproveFlag(tripTicket);

        verify(notificationDAO).createNotification(any(Notification.class));
    }

    @Test
    void testSendMailToClaimantsForDeclinedAutoApprovalCase() {
        TripTicket tripTicket = new TripTicket();
        TripClaim tripClaim = new TripClaim();
        tripClaim.setId(1);
        tripTicket.setTripClaims(Set.of(tripClaim));

        User user = new User();
        user.setEmail("test@example.com");
        user.setIsNotifyTripClaimDeclined(true);
        user.setAuthorities(Set.of(new UserAuthority("ROLE_PROVIDERADMIN")));

        when(userNotificationDataDAO.getUsersForTripClaimDeclined(anyInt())).thenReturn(List.of(user));

        service.sendMailToClaimantsForDeclinedAutoApprovalCase(tripTicket, tripClaim);

        verify(notificationDAO).createNotification(any(Notification.class));
    }*/
}