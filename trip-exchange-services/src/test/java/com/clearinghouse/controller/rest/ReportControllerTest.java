package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.DetailedTripTicketDTO;
import com.clearinghouse.dto.ReportFilterDTO;
import com.clearinghouse.dto.ReportSummaryDTO;
import com.clearinghouse.service.ReportService;
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

class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListAllCurrentTicketsReport() {
        ReportFilterDTO filter = new ReportFilterDTO();
        List<DetailedTripTicketDTO> tickets = new ArrayList<>();
        tickets.add(new DetailedTripTicketDTO());
        when(reportService.findDetailedTripTicketByReportFilterOBJ(filter)).thenReturn(tickets);

        ResponseEntity<List<DetailedTripTicketDTO>> response = reportController.listAllCurrentTicketsReport(filter);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tickets, response.getBody());
        verify(reportService).findDetailedTripTicketByReportFilterOBJ(filter);
    }

    @Test
    void testListAllCurrentTicketsReport_NoContent() {
        ReportFilterDTO filter = new ReportFilterDTO();
        when(reportService.findDetailedTripTicketByReportFilterOBJ(filter)).thenReturn(new ArrayList<>());

        ResponseEntity<List<DetailedTripTicketDTO>> response = reportController.listAllCurrentTicketsReport(filter);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(reportService).findDetailedTripTicketByReportFilterOBJ(filter);
    }

    @Test
    void testListAllCurrentTicketsReportWithoutCompletedTicket() {
        ReportFilterDTO filter = new ReportFilterDTO();
        List<DetailedTripTicketDTO> tickets = new ArrayList<>();
        tickets.add(new DetailedTripTicketDTO());
        when(reportService.getTripTicketsByReportFilterWithoutCompleted(filter)).thenReturn(tickets);

        ResponseEntity<List<DetailedTripTicketDTO>> response = reportController.listAllCurrentTicketsReportWithoutCompletedTicket(filter);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tickets, response.getBody());
        verify(reportService).getTripTicketsByReportFilterWithoutCompleted(filter);
    }

    @Test
    void testListAllCurrentTicketsReportWithoutCompletedTicket_NoContent() {
        ReportFilterDTO filter = new ReportFilterDTO();
        when(reportService.getTripTicketsByReportFilterWithoutCompleted(filter)).thenReturn(new ArrayList<>());

        ResponseEntity<List<DetailedTripTicketDTO>> response = reportController.listAllCurrentTicketsReportWithoutCompletedTicket(filter);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(reportService).getTripTicketsByReportFilterWithoutCompleted(filter);
    }

    @Test
    void testFindOldestCreatedDate() {
        int providerId = 1;
        String date = "2023-01-01";
        when(reportService.findOldestCreatedDate(providerId)).thenReturn(date);

        ResponseEntity<String> response = reportController.findOldestCreatedDate(providerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{ \"date\" :\"2023-01-01\"}", response.getBody());
        verify(reportService).findOldestCreatedDate(providerId);
    }

    @Test
    void testListSummaryReport() {
        ReportFilterDTO filter = new ReportFilterDTO();
        ReportSummaryDTO summary = new ReportSummaryDTO();
        when(reportService.findSummaryReport(filter)).thenReturn(summary);

        ResponseEntity<ReportSummaryDTO> response = reportController.listSummaryReport(filter);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(summary, response.getBody());
        verify(reportService).findSummaryReport(filter);
    }

    @Test
    void testCompletedTripsReport() {
        ReportFilterDTO filter = new ReportFilterDTO();
        List<DetailedTripTicketDTO> reports = new ArrayList<>();
        reports.add(new DetailedTripTicketDTO());
        when(reportService.findCompletedTripTicketDetailsByReportFilterOBJ(filter)).thenReturn(reports);

        ResponseEntity<List<DetailedTripTicketDTO>> response = reportController.completedTicketReport(filter);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reports, response.getBody());
        verify(reportService).findCompletedTripTicketDetailsByReportFilterOBJ(filter);
    }
}