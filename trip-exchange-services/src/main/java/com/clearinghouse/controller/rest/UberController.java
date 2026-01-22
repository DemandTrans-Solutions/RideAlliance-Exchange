package com.clearinghouse.controller.rest;


import com.clearinghouse.dto.*;
import com.clearinghouse.service.UberService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@RestController
@Slf4j
@RequestMapping(value = {"api/uber"})
@AllArgsConstructor
public class UberController {

    private final UberService uberService;


    @RequestMapping(value = "/options", method = RequestMethod.POST)
    public ResponseEntity<UberResponseDTO> requestUberOptions(@Valid @RequestBody UberRequestDTO uberRequestDTO) {
        log.debug("Requesting uber options for " + uberRequestDTO);
        var uberResponse = uberService.getUberEstimates(uberRequestDTO);
        return new ResponseEntity<>(uberResponse, HttpStatus.OK);
    }



    @RequestMapping(value = "/book", method = RequestMethod.POST)
    public ResponseEntity<UberCreateGuestTripResponse> bookUberRide(@RequestBody UberRideRequest uberRideRequest) {
        log.debug("Booking uber ride for " + uberRideRequest);
        var uberConfirmation = uberService.bookUberRide(uberRideRequest);
        return new ResponseEntity<>(uberConfirmation, HttpStatus.OK);
    }



    @RequestMapping(value = "/cancelUberTrip/{tripTicketId}", method = RequestMethod.PUT)
    public ResponseEntity<UberCancellationResponse> bookUberRide(@PathVariable int tripTicketId) {
        log.debug("Cancelling uber ride for trip ticket " + tripTicketId);
        var cancellationRequest = uberService.cancelTripTicket(tripTicketId);
        return new ResponseEntity<>(new UberCancellationResponse(cancellationRequest), HttpStatus.OK);
    }



}
