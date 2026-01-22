package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.TripClaimDTO;
import com.clearinghouse.service.TripClaimService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TripClaimControllerTest {

    @Mock
    private TripClaimService tripClaimService;

    @InjectMocks
    private TripClaimController tripClaimController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListAllTripClaims() {
        int tripTicketId = 1;
        List<TripClaimDTO> tripClaims = new ArrayList<>();
        tripClaims.add(new TripClaimDTO());
        when(tripClaimService.findAllTripClaims(tripTicketId)).thenReturn(tripClaims);

        ResponseEntity<List<TripClaimDTO>> response = tripClaimController.listAllTripClaims(tripTicketId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tripClaims, response.getBody());
        verify(tripClaimService).findAllTripClaims(tripTicketId);
    }

    @Test
    void testListAllTripClaims_NoContent() {
        int tripTicketId = 1;
        when(tripClaimService.findAllTripClaims(tripTicketId)).thenReturn(new ArrayList<>());

        ResponseEntity<List<TripClaimDTO>> response = tripClaimController.listAllTripClaims(tripTicketId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(tripClaimService).findAllTripClaims(tripTicketId);
    }

    @Test
    void testGetTripClaimByTripClaimId() {
        int tripTicketId = 1;
        int tripClaimId = 1;
        TripClaimDTO tripClaim = new TripClaimDTO();
        when(tripClaimService.findTripClaimByTripClaimId( tripClaimId)).thenReturn(tripClaim);

        ResponseEntity<TripClaimDTO> response = tripClaimController.getTripClaimByTripClaimId(tripTicketId, tripClaimId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tripClaim, response.getBody());
        verify(tripClaimService).findTripClaimByTripClaimId( tripClaimId);
    }

    @Test
    void testGetTripClaimByTripClaimId_NotFound() {
        int tripTicketId = 1;
        int tripClaimId = 1;
        when(tripClaimService.findTripClaimByTripClaimId( tripClaimId)).thenReturn(null);

        ResponseEntity<TripClaimDTO> response = tripClaimController.getTripClaimByTripClaimId(tripTicketId, tripClaimId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(tripClaimService).findTripClaimByTripClaimId( tripClaimId);
    }

    @Test
    void testCreateTripClaim() {
        int tripTicketId = 1;
        TripClaimDTO tripClaim = new TripClaimDTO();
        TripClaimDTO createdTripClaim = new TripClaimDTO();
        when(tripClaimService.createTripClaim(tripTicketId, tripClaim)).thenReturn(createdTripClaim);

        ResponseEntity<TripClaimDTO> response = tripClaimController.createTripClaim(tripTicketId, tripClaim);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdTripClaim, response.getBody());
        verify(tripClaimService).createTripClaim(tripTicketId, tripClaim);
    }

    @Test
    void testUpdateTripClaim() {
        int tripTicketId = 1;
        int tripClaimId = 1;
        TripClaimDTO tripClaim = new TripClaimDTO();
    TripClaimDTO updatedTripClaim = new TripClaimDTO();
        when(tripClaimService.tripClaimExists(tripClaimId)).thenReturn(true);
        when(tripClaimService.updateTripClaim(tripTicketId, tripClaim)).thenReturn(updatedTripClaim);

        ResponseEntity<TripClaimDTO> response = tripClaimController.updateTripClaim(tripTicketId, tripClaimId, tripClaim);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedTripClaim, response.getBody());
        verify(tripClaimService).tripClaimExists(tripClaimId);
        verify(tripClaimService).updateTripClaim(tripTicketId, tripClaim);
    }

    @Test
    void testUpdateTripClaim_NotFound() {
        int tripTicketId = 1;
        int tripClaimId = 1;
        TripClaimDTO tripClaim = new TripClaimDTO();
        when(tripClaimService.tripClaimExists(tripClaimId)).thenReturn(false);

        ResponseEntity<TripClaimDTO> response = tripClaimController.updateTripClaim(tripTicketId, tripClaimId, tripClaim);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(tripClaimService).tripClaimExists(tripClaimId);
        verify(tripClaimService, never()).updateTripClaim(tripTicketId, tripClaim);
    }

    @Test
    void testRescindTripClaim() {
        int tripTicketId = 1;
        int tripClaimId = 1;
        TripClaimDTO rescindedTripClaim = new TripClaimDTO();
        when(tripClaimService.tripClaimExists(tripClaimId)).thenReturn(true);
        when(tripClaimService.rescindTripClaim(tripTicketId, tripClaimId)).thenReturn(rescindedTripClaim);

        ResponseEntity<TripClaimDTO> response = tripClaimController.rescindTripClaim(tripTicketId, tripClaimId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(rescindedTripClaim, response.getBody());
        verify(tripClaimService).tripClaimExists(tripClaimId);
        verify(tripClaimService).rescindTripClaim(tripTicketId, tripClaimId);
    }

    @Test
    void testRescindTripClaim_NotFound() {
        int tripTicketId = 1;
        int tripClaimId = 1;
        when(tripClaimService.tripClaimExists(tripClaimId)).thenReturn(false);

        ResponseEntity<TripClaimDTO> response = tripClaimController.rescindTripClaim(tripTicketId, tripClaimId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(tripClaimService).tripClaimExists(tripClaimId);
        verify(tripClaimService, never()).rescindTripClaim(tripTicketId, tripClaimId);
    }

    @Test
    void testApproveTripClaim() {
        int tripTicketId = 1;
        int tripClaimId = 1;
        TripClaimDTO approvedTripClaim = new TripClaimDTO();
        when(tripClaimService.tripClaimExists(tripClaimId)).thenReturn(true);
        when(tripClaimService.approveTripClaim(tripTicketId, tripClaimId)).thenReturn(approvedTripClaim);

        ResponseEntity<TripClaimDTO> response = tripClaimController.approveTripClaim(tripTicketId, tripClaimId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(approvedTripClaim, response.getBody());
        verify(tripClaimService).tripClaimExists(tripClaimId);
        verify(tripClaimService).approveTripClaim(tripTicketId, tripClaimId);
    }

    @Test
    void testApproveTripClaim_NotFound() {
        int tripTicketId = 1;
        int tripClaimId = 1;
        when(tripClaimService.tripClaimExists(tripClaimId)).thenReturn(false);

        ResponseEntity<TripClaimDTO> response = tripClaimController.approveTripClaim(tripTicketId, tripClaimId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(tripClaimService).tripClaimExists(tripClaimId);
        verify(tripClaimService, never()).approveTripClaim(tripTicketId, tripClaimId);
    }

    @Test
    void testDeclineTripClaim() {
        int tripTicketId = 1;
        int tripClaimId = 1;
        TripClaimDTO declinedTripClaim = new TripClaimDTO();
        when(tripClaimService.tripClaimExists(tripClaimId)).thenReturn(true);
        when(tripClaimService.declineTripClaim(tripTicketId, tripClaimId)).thenReturn(declinedTripClaim);

        ResponseEntity<TripClaimDTO> response = tripClaimController.declineTripClaim(tripTicketId, tripClaimId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(declinedTripClaim, response.getBody());
        verify(tripClaimService).tripClaimExists(tripClaimId);
        verify(tripClaimService).declineTripClaim(tripTicketId, tripClaimId);
    }

    @Test
    void testDeclineTripClaim_NotFound() {
        int tripTicketId = 1;
        int tripClaimId = 1;
        when(tripClaimService.tripClaimExists(tripClaimId)).thenReturn(false);

        ResponseEntity<TripClaimDTO> response = tripClaimController.declineTripClaim(tripTicketId, tripClaimId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(tripClaimService).tripClaimExists(tripClaimId);
        verify(tripClaimService, never()).declineTripClaim(tripTicketId, tripClaimId);
    }
}