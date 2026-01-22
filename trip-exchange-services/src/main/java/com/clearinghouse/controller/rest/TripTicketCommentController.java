/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.TripTicketCommentDTO;
import com.clearinghouse.service.TripTicketCommentService;
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
@RequestMapping(value = {"api/trip_tickets/{trip_ticket_id}/trip_ticket_comments"})
@AllArgsConstructor
public class TripTicketCommentController {


    private final TripTicketCommentService tripTicketCommentService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<TripTicketCommentDTO>> listAllTripResultByTripTicketId(@PathVariable("trip_ticket_id") int trip_ticket_id) {
        List<TripTicketCommentDTO> tripTicketCommentDTO = tripTicketCommentService.findAllTripTicketCommetstByTripTicketId(trip_ticket_id);
        if (tripTicketCommentDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(tripTicketCommentDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{id}"}, method = RequestMethod.GET)
    public ResponseEntity<TripTicketCommentDTO> getTripTicketCommentById(@PathVariable("id") int id) {
        TripTicketCommentDTO tripTicketCommentDTO = tripTicketCommentService.findTripTicketCommentById(id);
        if (tripTicketCommentDTO == null) {
            log.error("#tripTicketCommentId not found [" + id + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(tripTicketCommentDTO, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripTicketCommentDTO> createTripTicketComment(@PathVariable("trip_ticket_id") int trip_ticket_id, @Valid @RequestBody TripTicketCommentDTO ticketCommentDTO) {
        TripTicketCommentDTO newTripTicketCommentDTO = tripTicketCommentService.createTripTicketComment(trip_ticket_id, ticketCommentDTO);
        return new ResponseEntity<>(newTripTicketCommentDTO, HttpStatus.CREATED);

    }

    @RequestMapping(value = {"/{id}"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripTicketCommentDTO> updateTripTicketComment(
            @PathVariable("trip_ticket_id") int trip_ticket_id,
            @PathVariable("id") int id,
            @Valid @RequestBody TripTicketCommentDTO tripTicketCommentDTO) {
        TripTicketCommentDTO currentTripTicketCommentDTO = tripTicketCommentService.findTripTicketCommentById(id);
        if (currentTripTicketCommentDTO == null) {
            log.error("#updateTripTicketComment tripcommentId not found [" + id + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        TripTicketCommentDTO updatedTripTicketCommentDTO = tripTicketCommentService.updateTripTicketComment(trip_ticket_id, tripTicketCommentDTO, id);
        return new ResponseEntity<>(updatedTripTicketCommentDTO, HttpStatus.OK);
    }

}
