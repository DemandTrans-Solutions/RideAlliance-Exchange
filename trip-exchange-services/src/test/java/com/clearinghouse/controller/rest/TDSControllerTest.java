package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.TripTicketDTO;
import com.clearinghouse.service.TDSConversionService;
import com.clearinghouse.service.TripTicketService;
import com.clearinghouse.tds.generated.model.TripRequestResponseType;
import com.clearinghouse.tds.generated.model.TripRequestType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TDSControllerTest {

    @Mock
    private TDSConversionService tdsConversionService;

    @Mock
    private TripTicketService tripTicketService;

    @InjectMocks
    private TDSController tdsController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSubmitTicket() {
        TripRequestType tripRequestType = new TripRequestType();
        TripTicketDTO tripTicketDTO = new TripTicketDTO();
        TripRequestResponseType responseType = new TripRequestResponseType();

        when(tdsConversionService.convertToTripTicket(tripRequestType)).thenReturn(tripTicketDTO);
        when(tripTicketService.createTripTicket(tripTicketDTO)).thenReturn(tripTicketDTO);
        when(tdsConversionService.convertToTripResponseType(tripTicketDTO)).thenReturn(responseType);

        ResponseEntity<TripRequestResponseType> response = tdsController.submitTicket(tripRequestType);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseType, response.getBody());
        verify(tdsConversionService).convertToTripTicket(tripRequestType);
        verify(tripTicketService).createTripTicket(tripTicketDTO);
        verify(tdsConversionService).convertToTripResponseType(tripTicketDTO);
    }

    @Test
    void testUpdateTripTicket() {
        int id = 1;
        TripRequestType tripRequestType = new TripRequestType();
        TripTicketDTO currentTicketDTO = new TripTicketDTO();
        TripTicketDTO updatedTicketDTO = new TripTicketDTO();
        TripRequestResponseType responseType = new TripRequestResponseType();

        when(tripTicketService.findTripTicketByTripTicketId(id)).thenReturn(currentTicketDTO);
        when(tdsConversionService.convertToTripTicket(tripRequestType)).thenReturn(updatedTicketDTO);
        when(tripTicketService.createTripTicket(updatedTicketDTO)).thenReturn(updatedTicketDTO);
        when(tripTicketService.updateTripTicket(updatedTicketDTO)).thenReturn(updatedTicketDTO);
        when(tdsConversionService.convertToTripResponseType(updatedTicketDTO)).thenReturn(responseType);

        ResponseEntity<TripRequestResponseType> response = tdsController.updateTripTicket(id, tripRequestType);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseType, response.getBody());
        verify(tripTicketService).findTripTicketByTripTicketId(id);
        verify(tdsConversionService).convertToTripTicket(tripRequestType);
        verify(tripTicketService).updateTripTicket(updatedTicketDTO);
        verify(tdsConversionService).convertToTripResponseType(updatedTicketDTO);
    }

    @Test
    void testRescindTripTicket() {
        int id = 1;
        TripTicketDTO currentTicketDTO = new TripTicketDTO();
        TripTicketDTO updatedTicketDTO = new TripTicketDTO();
        TripRequestResponseType responseType = new TripRequestResponseType();

        when(tripTicketService.findTripTicketByTripTicketId(id)).thenReturn(currentTicketDTO);
        when(tripTicketService.rescindTripTicket(id)).thenReturn(updatedTicketDTO);
        when(tdsConversionService.convertToTripResponseType(updatedTicketDTO)).thenReturn(responseType);

        ResponseEntity<TripRequestResponseType> response = tdsController.rescindTripTicket(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseType, response.getBody());
        verify(tripTicketService).findTripTicketByTripTicketId(id);
        verify(tripTicketService).rescindTripTicket(id);
        verify(tdsConversionService).convertToTripResponseType(updatedTicketDTO);
    }

    @Test
    void testDeleteTripTicket() {
        int id = 1;

        when(tripTicketService.findTripTicketByTripTicketId(id)).thenReturn(new TripTicketDTO());
        when(tripTicketService.deleteTripTickeByTripTicketId(id)).thenReturn(true);

        ResponseEntity<Boolean> response = tdsController.deleteTripTicket(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
        verify(tripTicketService).findTripTicketByTripTicketId(id);
        verify(tripTicketService).deleteTripTickeByTripTicketId(id);
    }

    @Test
    void testDeleteTripTicket_NotFound() {
        int id = 1;

        when(tripTicketService.findTripTicketByTripTicketId(id)).thenReturn(null);

        ResponseEntity<Boolean> response = tdsController.deleteTripTicket(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(tripTicketService).findTripTicketByTripTicketId(id);
        verify(tripTicketService, never()).deleteTripTickeByTripTicketId(id);
    }
}