package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.TripResultDTO;
import com.clearinghouse.service.TripResultService;
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

class TripResultControllerTest {

    @Mock
    private TripResultService tripResultService;

    @InjectMocks
    private TripResultController tripResultController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListAllTripResultByTripTicketId() {
        int tripTicketId = 1;
        List<TripResultDTO> tripResults = new ArrayList<>();
        tripResults.add(new TripResultDTO());
        when(tripResultService.findAllTripResultByTripTicketId(tripTicketId)).thenReturn(tripResults);

        ResponseEntity<List<TripResultDTO>> response = tripResultController.listAllTripResultByTripTicketId(tripTicketId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tripResults, response.getBody());
        verify(tripResultService).findAllTripResultByTripTicketId(tripTicketId);
    }

    @Test
    void testListAllTripResultByTripTicketId_NoContent() {
        int tripTicketId = 1;
        when(tripResultService.findAllTripResultByTripTicketId(tripTicketId)).thenReturn(new ArrayList<>());

        ResponseEntity<List<TripResultDTO>> response = tripResultController.listAllTripResultByTripTicketId(tripTicketId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(tripResultService).findAllTripResultByTripTicketId(tripTicketId);
    }


    /*
    @Test
    void testUpdateTripResult() {
        int tripTicketId = 1;
        int tripResultId = 1;
        TripResultDTO tripResultDTO = new TripResultDTO();
        TripResultDTO currentTripResult = new TripResultDTO();
        TripResultDTO updatedTripResult = new TripResultDTO();
        when(tripResultService.findTripResultByTripResultId(tripResultId)).thenReturn(currentTripResult);
        when(tripResultService.updateTripResult(tripTicketId, tripResultDTO, tripResultId)).thenReturn(updatedTripResult);

        ResponseEntity<TripResultDTO> response = tripResultController.updateTripResult(tripTicketId, tripResultId, tripResultDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedTripResult, response.getBody());
        verify(tripResultService).findTripResultByTripResultId(tripResultId);
        verify(tripResultService).updateTripResult(tripTicketId, tripResultDTO, tripResultId);
    }*/

    @Test
    void testUpdateTripResult_NotFound() {
        int tripTicketId = 1;
        int tripResultId = 1;
        TripResultDTO tripResultDTO = new TripResultDTO();
        tripResultDTO.setId(tripResultId);
        when(tripResultService.findTripResultByTripResultId(tripResultId)).thenReturn(null);

        ResponseEntity<TripResultDTO> response = tripResultController.updateTripResult(tripResultDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(tripResultService).findTripResultByTripResultId(tripResultId);
        verify(tripResultService, never()).updateTripResult(tripResultDTO);
    }
}