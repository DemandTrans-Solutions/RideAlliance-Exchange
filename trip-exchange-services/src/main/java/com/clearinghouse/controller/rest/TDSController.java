package com.clearinghouse.controller.rest;


import com.clearinghouse.dto.TripTicketDTO;
import com.clearinghouse.service.TDSConversionService;
import com.clearinghouse.service.TripTicketService;
import com.clearinghouse.tds.generated.model.TripRequestResponseType;
import com.clearinghouse.tds.generated.model.TripRequestType;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping(value = {"api/tds"})
@AllArgsConstructor
public class TDSController {


    private final TDSConversionService tdsConversionService;


    private final TripTicketService tripTicketService;


    @RequestMapping(value = "/submitTicket", method = RequestMethod.POST)
    public ResponseEntity<TripRequestResponseType> submitTicket(TripRequestType tripRequestType) {
        log.info("TDSController submitTicket {}", tripRequestType);
        var tripTicketDTO = tripTicketService.createTripTicket(tdsConversionService.convertToTripTicket(tripRequestType));
        var tdsResponseType = tdsConversionService.convertToTripResponseType(tripTicketDTO);
        return new ResponseEntity<>(tdsResponseType, HttpStatus.CREATED);
    }


    @RequestMapping(value = {"/updateTripTicket/{id}"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripRequestResponseType> updateTripTicket(
            @PathVariable("id") int id,
            @Valid @RequestBody TripRequestType tripRequestType) {
        TripTicketDTO currentTicketDTO = tripTicketService.findTripTicketByTripTicketId(id);
        if (currentTicketDTO == null) {
            log.error("#updateTripTicket tripTicketId not found [" + id + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var tripTicketDTO = tripTicketService.createTripTicket(tdsConversionService.convertToTripTicket(tripRequestType));
        TripTicketDTO updatedTicketDTO = tripTicketService.updateTripTicket(tripTicketDTO);
        var tdsResponseType = tdsConversionService.convertToTripResponseType(updatedTicketDTO);
        return new ResponseEntity<>(tdsResponseType, HttpStatus.OK);
    }


    @RequestMapping(value = {"/rescindTripTicket/{id}"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripRequestResponseType> rescindTripTicket(
            @PathVariable("id") int id) {
        TripTicketDTO currentTicketDTO = tripTicketService.findTripTicketByTripTicketId(id);
        if (currentTicketDTO == null) {
            log.error("#updateTripTicketStatusTo rescind tripTicketId not found [" + id + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        TripTicketDTO updatedTicketDTO = tripTicketService.rescindTripTicket(id);
        var tdsResponseType = tdsConversionService.convertToTripResponseType(updatedTicketDTO);
        return new ResponseEntity<>(tdsResponseType, HttpStatus.OK);
    }


    @RequestMapping(value = {"/deleteTripTicket/{id}"}, method = RequestMethod.DELETE)
    public ResponseEntity<Boolean> deleteTripTicket(
            @PathVariable("id") int id) {
        if (tripTicketService.findTripTicketByTripTicketId(id) == null) {
            log.error("#deletetrip TripTicketId not found [" + id + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            boolean tripTicketDeleteStatus = tripTicketService.deleteTripTickeByTripTicketId(id);
            return new ResponseEntity<>(tripTicketDeleteStatus, HttpStatus.OK);
        }
    }


}
