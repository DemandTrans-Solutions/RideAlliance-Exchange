/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.TripResultDTO;
import com.clearinghouse.dto.TripResultRequestDTO;
import com.clearinghouse.service.TripResultService;
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
    @RequestMapping(value = {"api/trip_tickets/trip_result"})
@AllArgsConstructor
public class TripResultController {


    private final TripResultService tripResultService;

    @RequestMapping(value = {"/{trip_ticket_id}"},method = RequestMethod.GET)
    public ResponseEntity<List<TripResultDTO>> listAllTripResultByTripTicketId(@PathVariable("trip_ticket_id") int trip_ticket_id) {
        List<TripResultDTO> tripResultDTO = tripResultService.findAllTripResultByTripTicketId(trip_ticket_id);
        if (tripResultDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(tripResultDTO, HttpStatus.OK);
    }

    /*/not used
        @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE )
        public ResponseEntity<TripResultDTO> createTripResult(@PathVariable("trip_ticket_id") int trip_ticket_id, @Valid @RequestBody TripResultDTO tripResultDTO) {
            TripResultDTO newTripResultDTO = tripResultService.createTripResult(trip_ticket_id, tripResultDTO);
            return new ResponseEntity<>(newTripResultDTO, HttpStatus.CREATED);

        }
    */
    @RequestMapping( method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripResultDTO> updateTripResult(
            @Valid @RequestBody TripResultDTO tripResultDTO) {
        int tripResultId = tripResultDTO.getId();
        TripResultDTO currentTripResultDTO = tripResultService.findTripResultByTripResultId(tripResultId);
        if (currentTripResultDTO == null) {
            log.error("#updateTripResult tripResultId not found [" + tripResultId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        TripResultDTO updatedTripResultDTO = tripResultService.updateTripResult(tripResultDTO);
        return new ResponseEntity<>(updatedTripResultDTO, HttpStatus.OK);
    }



    @RequestMapping(value = "/completed", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TripResultDTO>> createTripCompletedResult(@RequestBody List<TripResultRequestDTO> tripResultDTO) {
        List<TripResultDTO> tripResultRequestDTO = tripResultService.createCompletedTripResultList(tripResultDTO);
        return new ResponseEntity<>(tripResultRequestDTO, HttpStatus.CREATED);

    }

}
