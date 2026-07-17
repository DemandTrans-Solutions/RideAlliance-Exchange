/*
 * License to Clearing House Project
 * To be used for Clearing House project only
 */
package com.clearinghouse.service;

import com.clearinghouse.dao.NotificationDAO;
import com.clearinghouse.dao.ProviderDAO;
import com.clearinghouse.dao.TripChangeRequestDAO;
import com.clearinghouse.dao.TripTicketDAO;
import com.clearinghouse.dao.UserDAO;
import com.clearinghouse.dao.UserNotificationDataDAO;
import com.clearinghouse.dto.CreateChangeRequestRequest;
import com.clearinghouse.dto.TripChangeRequestDTO;
import com.clearinghouse.entity.*;
import com.clearinghouse.enumentity.TripChangeRequestStatusConstants;
import com.clearinghouse.enumentity.TripTicketStatusConstants;
import com.clearinghouse.exceptions.EditNotAllowedException;
import com.clearinghouse.exceptions.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Scaffold unit tests for {@link TripChangeRequestService}.
 *
 * <p>See {@code doc/CHANGE_REQUEST_TEST_PLAN.md} §1 for the full case list. Cases are stubbed as
 * {@code @Disabled} so the suite stays green until they are implemented. Each stub names the
 * scenario it covers; fill in the body and remove {@code @Disabled}.
 *
 * <p>Convention follows {@code TripClaimServiceTest}: {@code @Mock} collaborators, {@code @InjectMocks}
 * service, {@code MockitoAnnotations.openMocks(this)} in {@link BeforeEach}.
 *
 * <p>TODO(time): {@code isWithinLockWindow} reads {@code ZonedDateTime.now(APP_ZONE)}. These tests
 * set pickup relative to "now" (far = +2 days, near = +2 hours). If boundary flakiness appears,
 * refactor the service to take an injectable {@code Clock}.
 */
class TripChangeRequestServiceTest {

    private static final int PROVIDER_A = 10; // originator
    private static final int PROVIDER_B = 20; // claimant
    private static final int PROVIDER_C = 30; // third party (neither)
    private static final int USER_ID = 100;

    @Mock private TripChangeRequestDAO tripChangeRequestDAO;
    @Mock private TripTicketDAO tripTicketDAO;
    @Mock private ProviderDAO providerDAO;
    @Mock private UserDAO userDAO;
    @Mock private UserNotificationDataDAO userNotificationDataDAO;
    @Mock private NotificationDAO notificationDAO;
    @Mock private FileGenerateService fileGenerateService;
    @Mock private ActivityService activityService;

    @InjectMocks private TripChangeRequestService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(providerDAO.findProviderByProviderId(anyInt())).thenAnswer(inv -> provider(inv.getArgument(0)));
    }

    // ------------------------------------------------------------- test helpers

    private Provider provider(int id) {
        Provider p = new Provider();
        p.setProviderId(id);
        p.setProviderName("Provider-" + id);
        return p;
    }

    private Status status(int id) {
        Status s = new Status();
        s.setStatusId(id);
        return s;
    }

    private User user(int providerId, String... roles) {
        User u = new User();
        u.setId(USER_ID);
        u.setName("user-" + providerId);
        u.setEmail("user" + providerId + "@example.com");
        u.setProvider(provider(providerId));
        Set<UserAuthority> auth = new HashSet<>();
        for (String r : roles) {
            UserAuthority a = new UserAuthority();
            a.setAuthority(r);
            auth.add(a);
        }
        u.setAuthorities(auth);
        return u;
    }

    /** A claimed trip (status claimed=6) originated by A, claimed by B, pickup {@code daysOut} away. */
    private TripTicket claimedTrip(int daysOut) {
        TripTicket t = new TripTicket();
        t.setId(1);
        t.setCommonTripId("CT-1");
        t.setOriginProvider(provider(PROVIDER_A));
        t.setStatus(status(TripTicketStatusConstants.claimed.tripTicketStatusUpdate())); // 6
        TripClaim claim = new TripClaim();
        claim.setClaimantProvider(provider(PROVIDER_B));
        t.setApprovedTripClaim(claim);
        LocalDate date = LocalDate.now().plusDays(daysOut);
        t.setRequestedPickupDate(date);
        t.setRequestedPickupTime(Time.valueOf(LocalTime.NOON));
        return t;
    }

    private TripTicket unclaimedAvailableTrip() {
        TripTicket t = new TripTicket();
        t.setId(2);
        t.setOriginProvider(provider(PROVIDER_A));
        t.setStatus(status(TripTicketStatusConstants.available.tripTicketStatusUpdate())); // 2
        return t;
    }

    private CreateChangeRequestRequest request(String message) {
        CreateChangeRequestRequest r = new CreateChangeRequestRequest();
        r.setMessage(message);
        return r;
    }

    // ===================================================== createChangeRequest

    @Nested
    class CreateChangeRequest {

        @Test
        void tripNotFound_throws() {
            when(tripTicketDAO.findTripTicketByTripTicketId(1)).thenReturn(null);
            assertThrows(InvalidInputException.class,
                    () -> service.createChangeRequest(1, request("x"), user(PROVIDER_A, "ROLE_PROVIDERADMIN")));
        }

        @Test @Disabled("C2: current user has no provider -> EditNotAllowedException")
        void noProvider_throws() { fail("TODO"); }

        @Test @Disabled("C3: unclaimed trip -> InvalidInputException")
        void unclaimedTrip_throws() { fail("TODO"); }

        @Test @Disabled("C4: claimed but within 24h -> EditNotAllowedException")
        void within24h_throws() { fail("TODO"); }

        @Test @Disabled("C5: claimed trip with no approved claimant -> InvalidInputException")
        void noClaimant_throws() { fail("TODO"); }

        @Test @Disabled("C6: caller not a party -> EditNotAllowedException")
        void notAParty_throws() { fail("TODO"); }

        @Test @Disabled("C7: party but not provider admin -> EditNotAllowedException")
        void notProviderAdmin_throws() { fail("TODO"); }

        @Test @Disabled("C8: pending request already exists -> InvalidInputException")
        void pendingExists_throws() { fail("TODO"); }

        @Test @Disabled("C9: empty/whitespace/null message -> InvalidInputException")
        void emptyMessage_throws() { fail("TODO"); }

        @Test @Disabled("C10: originator requests -> target = claimant; status pending; saved")
        void originatorRequests_targetsClaimant() {
            // Sketch:
            // TripTicket trip = claimedTrip(2);
            // when(tripTicketDAO.findTripTicketByTripTicketId(1)).thenReturn(trip);
            // when(tripChangeRequestDAO.hasPendingRequest(1)).thenReturn(false);
            // when(tripChangeRequestDAO.createTripChangeRequest(any())).thenAnswer(inv -> inv.getArgument(0));
            // TripChangeRequestDTO dto = service.createChangeRequest(1, request("please move pickup"),
            //         user(PROVIDER_A, "ROLE_PROVIDERADMIN"));
            // ArgumentCaptor<TripChangeRequest> cap = ArgumentCaptor.forClass(TripChangeRequest.class);
            // verify(tripChangeRequestDAO).createTripChangeRequest(cap.capture());
            // assertEquals(PROVIDER_B, cap.getValue().getTargetProvider().getProviderId());
            // assertEquals(TripChangeRequestStatusConstants.pending.tripChangeRequestStatusValue(),
            //         cap.getValue().getStatus().getStatusId());
            fail("TODO");
        }

        @Test @Disabled("C11: claimant requests -> target = originator")
        void claimantRequests_targetsOriginator() { fail("TODO"); }

        @Test @Disabled("C12: notifies TARGET provider admins with template 34 (received)")
        void notifiesTargetAdmins() { fail("TODO"); }

        @Test @Disabled("C13: records 'Change request created' activity")
        void recordsActivity() { fail("TODO"); }

        @Test @Disabled("C14: proposed_changes serialized to JSON; empty map -> null")
        void serializesProposedChanges() { fail("TODO"); }
    }

    // ============================================== approve / deny (respond)

    @Nested
    class Respond {

        @Test @Disabled("R1: request not found or trip id mismatch -> InvalidInputException")
        void notFound_throws() { fail("TODO"); }

        @Test @Disabled("R2: request not pending -> InvalidInputException")
        void notPending_throws() { fail("TODO"); }

        @Test @Disabled("R3: no provider -> EditNotAllowedException")
        void noProvider_throws() { fail("TODO"); }

        @Test @Disabled("R4: caller not target and not super admin -> EditNotAllowedException")
        void notTarget_throws() { fail("TODO"); }

        @Test @Disabled("R5: target but not provider admin -> EditNotAllowedException")
        void targetNotAdmin_throws() { fail("TODO"); }

        @Test @Disabled("R6: approve -> status approved; responseMessage + respondedByUserId set")
        void approve_setsApproved() { fail("TODO"); }

        @Test @Disabled("R7: deny -> status denied")
        void deny_setsDenied() { fail("TODO"); }

        @Test @Disabled("R8: approve notifies REQUESTER admins with template 35 (approved)")
        void approveNotifiesRequester() { fail("TODO"); }

        @Test @Disabled("R9: deny notifies REQUESTER admins with template 36 (denied)")
        void denyNotifiesRequester() { fail("TODO"); }

        @Test @Disabled("R10: super admin (not target) may respond")
        void superAdminMayRespond() { fail("TODO"); }

        @Test @Disabled("R11: approve/deny record activity")
        void recordsActivity() { fail("TODO"); }
    }

    // ================================================== assertCanEditDirectly

    @Nested
    class AssertCanEditDirectly {

        @Test
        void nullTrip_noop() {
            assertDoesNotThrow(() -> service.assertCanEditDirectly(null, user(PROVIDER_A, "ROLE_PROVIDERADMIN")));
        }

        @Test
        void superAdmin_allowed() {
            assertDoesNotThrow(() ->
                    service.assertCanEditDirectly(claimedTrip(2), user(PROVIDER_C, "ROLE_ADMIN")));
        }

        @Test @Disabled("E3: non-admin with no provider -> EditNotAllowedException")
        void noProvider_throws() { fail("TODO"); }

        @Test
        void unclaimedOriginator_allowed() {
            assertDoesNotThrow(() ->
                    service.assertCanEditDirectly(unclaimedAvailableTrip(), user(PROVIDER_A, "ROLE_PROVIDERADMIN")));
        }

        @Test @Disabled("E5: unclaimed trip, not originator -> EditNotAllowedException")
        void unclaimedNotOriginator_throws() { fail("TODO"); }

        @Test @Disabled("E6: claimed within 24h, non-super-admin -> EditNotAllowedException")
        void claimedWithin24h_throws() { fail("TODO"); }

        @Test @Disabled("E7: claimed outside 24h, not a party -> EditNotAllowedException")
        void claimedNotParty_throws() { fail("TODO"); }

        @Test
        void claimedParty_alwaysBlocked() {
            // A claimed trip can never be edited directly by a party. pickup 5 days out so we are
            // clearly OUTSIDE the 24h window and reach the claimed-trip block (not the 24h one).
            // Tested as PROVIDER_A (the originator) and PROVIDER_B (the claimant): both are parties
            // and both must be blocked, regardless of any prior approved request.
            TripTicket trip = claimedTrip(5);
            for (int partyProviderId : new int[]{PROVIDER_A, PROVIDER_B}) {
                EditNotAllowedException ex = assertThrows(EditNotAllowedException.class,
                        () -> service.assertCanEditDirectly(trip, user(partyProviderId, "ROLE_PROVIDERADMIN")),
                        "provider " + partyProviderId + " should be blocked from editing a claimed trip");
                // Assert it failed for the CLAIMED reason, not the 24h reason, so the test can't pass falsely.
                assertTrue(ex.getMessage().toLowerCase().contains("send a change request"),
                        "expected the claimed-trip message but got: " + ex.getMessage());
            }
            // Editing now goes solely through the change-request workflow; assertCanEditDirectly must
            // not consult or mutate any change request when blocking a claimed trip.
            verify(tripChangeRequestDAO, never()).updateTripChangeRequest(any());
        }
    }

    // ================================================= helper-logic edge cases

    @Nested
    class HelperEdges {

        @Test @Disabled("H1: pickup null -> falls back to dropoff date")
        void pickupNull_usesDropoff() { fail("TODO"); }

        @Test @Disabled("H2: both dates null -> not within window")
        void bothNull_notWithin() { fail("TODO"); }

        @Test @Disabled("H3: pickup well outside 24h -> not within")
        void farPickup_notWithin() { fail("TODO"); }

        @Test @Disabled("H4: pickup in the past -> within (locked)")
        void pastPickup_within() { fail("TODO"); }

        @Test @Disabled("H5: each of available/cancelled/completed/expired/rescinded -> not claimed")
        void terminalStates_notClaimed() { fail("TODO"); }

        @Test @Disabled("H6: claimed status + approved claim -> claimed")
        void claimedState_isClaimed() { fail("TODO"); }

        @Test @Disabled("H7: summarizeChanges/humanizeField formatting")
        void summarizeChanges_formats() { fail("TODO"); }
    }
}
