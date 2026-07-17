/*
 * License to Clearing House Project
 * To be used for Clearing House project only
 */
package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.ChangeRequestResponseRequest;
import com.clearinghouse.dto.CreateChangeRequestRequest;
import com.clearinghouse.dto.TripChangeRequestDTO;
import com.clearinghouse.entity.User;
import com.clearinghouse.service.TripChangeRequestService;
import com.clearinghouse.service.TripTicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Scaffold API-layer tests for {@link TripChangeRequestController}.
 *
 * <p>See {@code doc/CHANGE_REQUEST_TEST_PLAN.md} §2. This scaffold uses the lightweight
 * Mockito-driven style (mock the services, call controller methods directly, assert the
 * {@link ResponseEntity}). It can verify routing, status codes, and that the service is invoked
 * with the right arguments.
 *
 * <p>What this style canNOT verify (needs {@code @WebMvcTest} + MockMvc instead — see A4/A6/A10):
 * <ul>
 *   <li>That {@code EditNotAllowedException} is mapped to HTTP 403 by the exception handler.</li>
 *   <li>That {@code proposed_changes} (snake_case JSON) binds to {@code proposedChanges}.</li>
 *   <li>That {@code TripTicketController}'s update paths invoke {@code assertCanEditDirectly} and
 *       surface 403. Add a separate {@code TripTicketControllerEditLockTest} for A10.</li>
 * </ul>
 */
class TripChangeRequestControllerTest {

    @Mock private TripChangeRequestService tripChangeRequestService;
    @Mock private TripTicketService tripTicketService;

    @InjectMocks private TripChangeRequestController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(tripTicketService.getCurrentUser()).thenReturn(new User());
    }

    @Test
    void list_withResults_ok() {
        TripChangeRequestDTO dto = new TripChangeRequestDTO();
        when(tripChangeRequestService.findAllForTripTicket(1)).thenReturn(List.of(dto));
        ResponseEntity<List<TripChangeRequestDTO>> resp = controller.listChangeRequests(1);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().size());
    }

    @Test
    void list_empty_noContent() {
        when(tripChangeRequestService.findAllForTripTicket(1)).thenReturn(List.of());
        ResponseEntity<List<TripChangeRequestDTO>> resp = controller.listChangeRequests(1);
        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
    }

    @Test
    void create_valid_created() {
        CreateChangeRequestRequest body = new CreateChangeRequestRequest();
        body.setMessage("please move pickup");
        TripChangeRequestDTO dto = new TripChangeRequestDTO();
        when(tripChangeRequestService.createChangeRequest(eq(1), any(), any())).thenReturn(dto);
        ResponseEntity<TripChangeRequestDTO> resp = controller.createChangeRequest(1, body);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertSame(dto, resp.getBody());
    }

    @Test @Disabled("A4: POST when service throws EditNotAllowedException -> 403 (requires MockMvc + exception handler)")
    void create_editNotAllowed_forbidden() { fail("TODO: implement as @WebMvcTest"); }

    @Test @Disabled("A5: POST when service throws InvalidInputException -> 400 (requires MockMvc + exception handler)")
    void create_invalidInput_badRequest() { fail("TODO: implement as @WebMvcTest"); }

    @Test @Disabled("A6: POST binds snake_case proposed_changes -> proposedChanges (requires MockMvc)")
    void create_bindsProposedChanges() { fail("TODO: implement as @WebMvcTest"); }

    @Test
    void approve_ok() {
        ChangeRequestResponseRequest body = new ChangeRequestResponseRequest();
        body.setResponseMessage("ok by us");
        when(tripChangeRequestService.approveChangeRequest(eq(1), eq(5), eq("ok by us"), any(User.class)))
                .thenReturn(new TripChangeRequestDTO());
        ResponseEntity<TripChangeRequestDTO> resp = controller.approveChangeRequest(1, 5, body);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        // Verify the message threaded through; the User arg is the current user (don't pin its identity).
        verify(tripChangeRequestService).approveChangeRequest(eq(1), eq(5), eq("ok by us"), any(User.class));
    }

    @Test
    void approve_nullBody_ok() {
        when(tripChangeRequestService.approveChangeRequest(eq(1), eq(5), isNull(), any()))
                .thenReturn(new TripChangeRequestDTO());
        ResponseEntity<TripChangeRequestDTO> resp = controller.approveChangeRequest(1, 5, null);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void deny_ok() {
        ChangeRequestResponseRequest body = new ChangeRequestResponseRequest();
        body.setResponseMessage("no");
        when(tripChangeRequestService.denyChangeRequest(eq(1), eq(5), eq("no"), any()))
                .thenReturn(new TripChangeRequestDTO());
        ResponseEntity<TripChangeRequestDTO> resp = controller.denyChangeRequest(1, 5, body);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }
}
