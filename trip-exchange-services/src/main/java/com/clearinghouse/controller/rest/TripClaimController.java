/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.TripClaimDTO;
import com.clearinghouse.service.TripClaimService;
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
@RequestMapping(value = {"api/trip_tickets/{trip_ticket_id}/trip_claims"})
@Slf4j
@AllArgsConstructor
public class TripClaimController {


    private final TripClaimService tripClaimService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<TripClaimDTO>> listAllTripClaims(@PathVariable("trip_ticket_id") int trip_ticket_id) {
        List<TripClaimDTO> tripClaimDTO = tripClaimService.findAllTripClaims(trip_ticket_id);
        if (tripClaimDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(tripClaimDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{id}"}, method = RequestMethod.GET)
    public ResponseEntity<TripClaimDTO> getTripClaimByTripClaimId(@PathVariable("trip_ticket_id") int trip_ticket_id, @PathVariable("id") int id) {
        var tripClaimDTO = tripClaimService.findTripClaimByTripClaimId(id);
        if (tripClaimDTO == null) {
            log.error("#tripClaimId not found [" + id + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(tripClaimDTO, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripClaimDTO> createTripClaim(@PathVariable("trip_ticket_id") int trip_ticket_id, @Valid @RequestBody TripClaimDTO tripClaimDTO) {
        TripClaimDTO newTripClaimtDTO = tripClaimService.createTripClaim(trip_ticket_id, tripClaimDTO);
        return new ResponseEntity<>(newTripClaimtDTO, HttpStatus.CREATED);

    }

    @RequestMapping(value = {"/{id}"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripClaimDTO> updateTripClaim(
            @PathVariable("trip_ticket_id") int trip_ticket_id,
            @PathVariable("id") int id,
            @Valid @RequestBody TripClaimDTO tripClaimDTO) {

        if (!tripClaimService.tripClaimExists(id)) {
            log.error("#updateTripClaim tripClaimId not found [" + id + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        TripClaimDTO updatedtripClaimDTO = tripClaimService.updateTripClaim(trip_ticket_id, tripClaimDTO);
        return new ResponseEntity<>(updatedtripClaimDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{id}/rescind"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripClaimDTO> rescindTripClaim(
            @PathVariable("trip_ticket_id") int trip_ticket_id,
            @PathVariable("id") int id) {

        if (!tripClaimService.tripClaimExists(id)) {
            log.error("#updateTripClaimStatusTo rescind claimId not found [" + id + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        TripClaimDTO updatedTripClaimDTO = tripClaimService.rescindTripClaim(trip_ticket_id, id);
        return new ResponseEntity<>(updatedTripClaimDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{id}/approve"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripClaimDTO> approveTripClaim(
            @PathVariable("trip_ticket_id") int trip_ticket_id,
            @PathVariable("id") int id) {

        if (!tripClaimService.tripClaimExists(id)) {
            log.error("#updateTripClaimStatusTo rescind claimId not found [" + id + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        TripClaimDTO updatedTripClaimDTO = tripClaimService.approveTripClaim(trip_ticket_id, id);
        return new ResponseEntity<>(updatedTripClaimDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{id}/decline"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripClaimDTO> declineTripClaim(
            @PathVariable("trip_ticket_id") int trip_ticket_id,
            @PathVariable("id") int id) {

        if (!tripClaimService.tripClaimExists(id)) {
            log.error("#updateTripClaimStatusTo rescind claimId not found [" + id + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        TripClaimDTO updatedTripClaimDTO = tripClaimService.declineTripClaim(trip_ticket_id, id);
        return new ResponseEntity<>(updatedTripClaimDTO, HttpStatus.OK);
    }

}
