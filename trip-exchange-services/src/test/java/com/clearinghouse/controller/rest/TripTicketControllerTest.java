package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.DetailedTripTicketDTO;
import com.clearinghouse.dto.PaginationDTO;
import com.clearinghouse.dto.TripTicketDTO;
import com.clearinghouse.service.TripTicketService;
import com.clearinghouse.service.ConvertRequestToTripTicketDTOService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.clearinghouse.service.TripTicketDistanceService;
import com.clearinghouse.service.TripTicketDataService;
import com.clearinghouse.entity.TripTicket;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest; // Ensure this import is added
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TripTicketControllerTest {

    @Mock
    private TripTicketService tripTicketService;

    @Mock
    private ConvertRequestToTripTicketDTOService convertRequestToTripTicketDTOService;

    @Mock
    private TripTicketDistanceService tripTicketDistanceService;

    @Mock
    private TripTicketDataService tripTicketDataService;

    @InjectMocks
    private TripTicketController tripTicketController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListAllDeatiedTripTicketsWithpagination() {
        String pagesize = "10";
        String pagenumber = "1";
    String sortField = "id";
    String sortOrder = "1";
        HttpServletRequest request = mock(HttpServletRequest.class);

        Map<String, Object> detailedTripTicketDTOMap = new HashMap<>();
        when(tripTicketService.findAllDeatiledTripTicketWithpagination(any(PaginationDTO.class)))
                .thenReturn(detailedTripTicketDTOMap);

        ResponseEntity<Map<String, Object>> response = tripTicketController.listAllDeatiedTripTicketsWithpagination(
                pagesize, pagenumber, sortField, sortOrder, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(detailedTripTicketDTOMap, response.getBody());
        verify(tripTicketService).findAllDeatiledTripTicketWithpagination(any(PaginationDTO.class));
    }

    @Test
    void testListAllDeatiedTripTicketsByproviderId() {
        int providerId = 1;
        List<DetailedTripTicketDTO> detailedTripTicketDTOs = new ArrayList<>();
        when(tripTicketService.findDetailedTripTicketByOriginProviderId(providerId)).thenReturn(detailedTripTicketDTOs);

        ResponseEntity<List<DetailedTripTicketDTO>> response = tripTicketController.listAllDeatiedTripTicketsByproviderId(providerId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(tripTicketService).findDetailedTripTicketByOriginProviderId(providerId);
    }

    @Test
    void testGetTripTicketByTripTicketId() {
        int id = 1;
        DetailedTripTicketDTO detailed = new DetailedTripTicketDTO();
        when(tripTicketService.findDetailedTripTicketById(id, 1)).thenReturn(detailed);

        ResponseEntity<DetailedTripTicketDTO> response = tripTicketController.getTripTicketByTripTicketId(id, 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(detailed, response.getBody());
        verify(tripTicketService).findDetailedTripTicketById(id, 1);
    }

    @Test
    void testGetTripTicketByTripTicketId_NotFound() {
        int id = 1;
        when(tripTicketService.findDetailedTripTicketById(id, 0)).thenReturn(null);

        ResponseEntity<DetailedTripTicketDTO> response = tripTicketController.getTripTicketByTripTicketId(id, 0);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(tripTicketService).findDetailedTripTicketById(id, 0);
    }



    @Test
    void testCreateTripTicket() {
        TripTicketDTO tripTicketDTO = new TripTicketDTO();
        BindingResult result = mock(BindingResult.class);
        when(tripTicketService.isOriginatorProviderExists(tripTicketDTO.getOriginProviderId())).thenReturn(true);
        when(result.hasErrors()).thenReturn(false);
        when(tripTicketService.createTripTicket(tripTicketDTO)).thenReturn(tripTicketDTO);

        ResponseEntity<TripTicketDTO> response = tripTicketController.createTripTicket(tripTicketDTO, result);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(tripTicketDTO, response.getBody());
        verify(tripTicketService).createTripTicket(tripTicketDTO);
    }

    @Test
    void testUpdateTripTicket() {
        int id = 1;
        TripTicketDTO tripTicketDTO = new TripTicketDTO();
        when(tripTicketService.findTripTicketByTripTicketId(id)).thenReturn(tripTicketDTO);
        when(tripTicketService.updateTripTicket(tripTicketDTO)).thenReturn(tripTicketDTO);

        ResponseEntity<TripTicketDTO> response = tripTicketController.updateTripTicket(id, tripTicketDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tripTicketDTO, response.getBody());
        verify(tripTicketService).updateTripTicket(tripTicketDTO);
    }

    @Test
    void testUpdateTripTicket_NotFound() {
        int id = 1;
        TripTicketDTO tripTicketDTO = new TripTicketDTO();
        when(tripTicketService.findTripTicketByTripTicketId(id)).thenReturn(null);

        ResponseEntity<TripTicketDTO> response = tripTicketController.updateTripTicket(id, tripTicketDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(tripTicketService, never()).updateTripTicket(tripTicketDTO);
    }

    @Test
    void testRescindTripTicket() {
        int id = 1;
        TripTicketDTO tripTicketDTO = new TripTicketDTO();
        when(tripTicketService.findTripTicketByTripTicketId(id)).thenReturn(tripTicketDTO);
        when(tripTicketService.rescindTripTicket(id)).thenReturn(tripTicketDTO);

        ResponseEntity<TripTicketDTO> response = tripTicketController.rescindTripTicket(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tripTicketDTO, response.getBody());
        verify(tripTicketService).rescindTripTicket(id);
    }

    @Test
    void testRescindTripTicket_NotFound() {
        int id = 1;
        when(tripTicketService.findTripTicketByTripTicketId(id)).thenReturn(null);

        ResponseEntity<TripTicketDTO> response = tripTicketController.rescindTripTicket(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(tripTicketService, never()).rescindTripTicket(id);
    }

    @Test
    void testDeleteTripTicket() {
        int id = 1;
        TripTicket tt = new TripTicket();
        when(tripTicketService.getTripTicketByTripTicketId(id)).thenReturn(tt);
        when(tripTicketService.deleteTripTickeByTripTicketId(id)).thenReturn(true);

        ResponseEntity<Boolean> response = tripTicketController.deleteTripTicket(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
        verify(tripTicketService).deleteTripTickeByTripTicketId(id);
    }

    @Test
    void testDeleteTripTicket_NotFound() {
        int id = 1;
        when(tripTicketService.getTripTicketByTripTicketId(id)).thenReturn(null);

        ResponseEntity<Boolean> response = tripTicketController.deleteTripTicket(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(tripTicketService, never()).deleteTripTickeByTripTicketId(anyInt());
    }
}