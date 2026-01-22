/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.DetailedTripTicketDTO;
import com.clearinghouse.dto.ReportFilterDTO;
import com.clearinghouse.dto.ReportSummaryDTO;
import com.clearinghouse.service.ReportService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * @author chaitanyaP
 */
@RestController
@Slf4j
@RequestMapping(value = {"api/reports"})
@AllArgsConstructor
public class ReportController {


    private final ReportService reportService;

    @RequestMapping(value = "/currentTicketsReport", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DetailedTripTicketDTO>> listAllCurrentTicketsReport(@Valid @RequestBody ReportFilterDTO reportFilterDTOObj) {

        List<DetailedTripTicketDTO> detailedTripTicketDTO = reportService.findDetailedTripTicketByReportFilterOBJ(reportFilterDTOObj);
        if (detailedTripTicketDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(detailedTripTicketDTO, HttpStatus.OK);

    }

    /*initial method for the current ticket report*/
    @RequestMapping(value = "/currentTicketsReportInit", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DetailedTripTicketDTO>> listAllCurrentTicketsReportWithoutCompletedTicket(@Valid @RequestBody ReportFilterDTO reportFilterDTOObj) {

        List<DetailedTripTicketDTO> detailedTripTicketDTO = reportService.getTripTicketsByReportFilterWithoutCompleted(reportFilterDTOObj);
        if (detailedTripTicketDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(detailedTripTicketDTO, HttpStatus.OK);

    }

    /*method for getting oldestCreatedDateTime*/
    @RequestMapping(value = "/oldestCreatedDate/{providerId}", method = RequestMethod.GET)
    public ResponseEntity<String> findOldestCreatedDate(@PathVariable("providerId") int providerId) {
        String value = reportService.findOldestCreatedDate(providerId);
        String oldestCreatedDateTime = "{ \"date\" :";
        oldestCreatedDateTime = oldestCreatedDateTime + "\"" + value + "\"}";

        return new ResponseEntity<>(oldestCreatedDateTime, HttpStatus.OK);

    }

    @RequestMapping(value = "/summaryReport", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReportSummaryDTO> listSummaryReport(@Valid @RequestBody ReportFilterDTO reportFilterDTOObj) {

        ReportSummaryDTO summaryDTO = reportService.findSummaryReport(reportFilterDTOObj);

        return new ResponseEntity<>(summaryDTO, HttpStatus.OK);

    }

    @RequestMapping(value = "/completedTripsReport", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DetailedTripTicketDTO>> completedTicketReport(@Valid @RequestBody ReportFilterDTO reportFilterDTOObj) {

        List<DetailedTripTicketDTO> detailedTripTicketDTO = reportService.findCompletedTripTicketDetailsByReportFilterOBJ(reportFilterDTOObj);
        if (detailedTripTicketDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(detailedTripTicketDTO, HttpStatus.OK);

    }


    @RequestMapping(value = "/cancelTicketsReport", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DetailedTripTicketDTO>> cancelTicketsReport(@Valid @RequestBody ReportFilterDTO reportFilterDTOObj) {

        List<DetailedTripTicketDTO> detailedTripTicketDTO = reportService.findCancelTripTicketDetailsByReportFilterOBJ(reportFilterDTOObj);
        if (detailedTripTicketDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(detailedTripTicketDTO, HttpStatus.OK);

    }

}
