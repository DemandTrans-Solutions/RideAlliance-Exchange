package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.DetailedTripTicketDTO;
import com.clearinghouse.dto.TicketFilterDTO;
import com.clearinghouse.listresponseentity.AddressListByString;
import com.clearinghouse.service.TicketFilterService;
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

class TicketFilterControllerTest {

    @Mock
    private TicketFilterService ticketFilterService;

    @InjectMocks
    private TicketFilterController ticketFilterController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListAllFilters() {
        List<TicketFilterDTO> filters = new ArrayList<>();
        filters.add(new TicketFilterDTO());
        when(ticketFilterService.findAllFilters()).thenReturn(filters);

        ResponseEntity<List<TicketFilterDTO>> response = ticketFilterController.listAllFilters();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(filters, response.getBody());
        verify(ticketFilterService).findAllFilters();
    }

    @Test
    void testListAllFilters_NoContent() {
        when(ticketFilterService.findAllFilters()).thenReturn(new ArrayList<>());

        ResponseEntity<List<TicketFilterDTO>> response = ticketFilterController.listAllFilters();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(ticketFilterService).findAllFilters();
    }

    @Test
    void testGetFiltersByFilterId() {
        int filterId = 1;
        TicketFilterDTO filter = new TicketFilterDTO();
        when(ticketFilterService.findFilterByFilterId(filterId)).thenReturn(filter);

        ResponseEntity<TicketFilterDTO> response = ticketFilterController.getFiltersByFilterId(filterId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(filter, response.getBody());
        verify(ticketFilterService).findFilterByFilterId(filterId);
    }

    @Test
    void testGetFiltersByFilterId_NotFound() {
        int filterId = 1;
        when(ticketFilterService.findFilterByFilterId(filterId)).thenReturn(null);

        ResponseEntity<TicketFilterDTO> response = ticketFilterController.getFiltersByFilterId(filterId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(ticketFilterService).findFilterByFilterId(filterId);
    }

    @Test
    void testCreateFilter() {
        TicketFilterDTO filter = new TicketFilterDTO();
        TicketFilterDTO createdFilter = new TicketFilterDTO();
        when(ticketFilterService.createFilter(filter)).thenReturn(createdFilter);

        ResponseEntity<TicketFilterDTO> response = ticketFilterController.createFilter(filter);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdFilter, response.getBody());
        verify(ticketFilterService).createFilter(filter);
    }

    @Test
    void testUpdateFilter() {
        int filterId = 1;
        TicketFilterDTO filter = new TicketFilterDTO();
        TicketFilterDTO currentFilter = new TicketFilterDTO();
        TicketFilterDTO updatedFilter = new TicketFilterDTO();
        when(ticketFilterService.findFilterByFilterId(filterId)).thenReturn(currentFilter);
        when(ticketFilterService.updateFilter(filter)).thenReturn(updatedFilter);

        ResponseEntity<TicketFilterDTO> response = ticketFilterController.updateFilter(filterId, filter);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedFilter, response.getBody());
        verify(ticketFilterService).findFilterByFilterId(filterId);
        verify(ticketFilterService).updateFilter(filter);
    }

    @Test
    void testUpdateFilter_NotFound() {
        int filterId = 1;
        TicketFilterDTO filter = new TicketFilterDTO();
        when(ticketFilterService.findFilterByFilterId(filterId)).thenReturn(null);

        ResponseEntity<TicketFilterDTO> response = ticketFilterController.updateFilter(filterId, filter);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(ticketFilterService).findFilterByFilterId(filterId);
        verify(ticketFilterService, never()).updateFilter(filter);
    }

    @Test
    void testDeleteFilter() {
        int filterId = 1;
        when(ticketFilterService.findFilterByFilterId(filterId)).thenReturn(new TicketFilterDTO());
        when(ticketFilterService.deleteFilterByFilterId(filterId)).thenReturn(true);

        ResponseEntity<Boolean> response = ticketFilterController.deleteFilter(filterId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
        verify(ticketFilterService).findFilterByFilterId(filterId);
        verify(ticketFilterService).deleteFilterByFilterId(filterId);
    }

    @Test
    void testDeleteFilter_NotFound() {
        int filterId = 1;
        when(ticketFilterService.findFilterByFilterId(filterId)).thenReturn(null);

        ResponseEntity<Boolean> response = ticketFilterController.deleteFilter(filterId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(ticketFilterService).findFilterByFilterId(filterId);
        verify(ticketFilterService, never()).deleteFilterByFilterId(filterId);
    }

    /*
    @Test
    void testListAllTicketsByFilterDTOObject() {
        TicketFilterDTO filter = new TicketFilterDTO();
        List<DetailedTripTicketDTO> tickets = new ArrayList<>();
        tickets.add(new DetailedTripTicketDTO());
        when(ticketFilterService.filterTicketsByFilterObject(filter)).thenReturn(tickets);

        ResponseEntity<List<DetailedTripTicketDTO>> response = ticketFilterController.listAllTicketsByFilterDTOObject(filter);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tickets, response.getBody());
        verify(ticketFilterService).filterTicketsByFilterObject(filter);
    }*/

    @Test
    void testListAllFiltersByUserId() {
        int userId = 1;
        List<TicketFilterDTO> filters = new ArrayList<>();
        filters.add(new TicketFilterDTO());
        when(ticketFilterService.findAllFiltersByUserId(userId)).thenReturn(filters);

        ResponseEntity<List<TicketFilterDTO>> response = ticketFilterController.listAllFiltersbyUserId(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(filters, response.getBody());
        verify(ticketFilterService).findAllFiltersByUserId(userId);
    }

    @Test
    void testListAllFiltersByUserId_NoContent() {
        int userId = 1;
        when(ticketFilterService.findAllFiltersByUserId(userId)).thenReturn(new ArrayList<>());

        ResponseEntity<List<TicketFilterDTO>> response = ticketFilterController.listAllFiltersbyUserId(userId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(ticketFilterService).findAllFiltersByUserId(userId);
    }

    @Test
    void testGetAddressById() {
        int addressId = 1;
        AddressListByString address = new AddressListByString();
        when(ticketFilterService.getAddressById(addressId)).thenReturn(address);

        ResponseEntity<AddressListByString> response = ticketFilterController.getAddressById(addressId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(address, response.getBody());
        verify(ticketFilterService).getAddressById(addressId);
    }
}