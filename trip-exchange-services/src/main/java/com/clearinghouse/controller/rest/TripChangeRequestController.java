/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.ChangeRequestResponseRequest;
import com.clearinghouse.dto.CreateChangeRequestRequest;
import com.clearinghouse.dto.TripChangeRequestDTO;
import com.clearinghouse.entity.User;
import com.clearinghouse.service.TripChangeRequestService;
import com.clearinghouse.service.TripTicketService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints for the change-request approval workflow on claimed trip tickets.
 */
@RestController
@RequestMapping(value = {"api/trip_tickets/{trip_ticket_id}/change_requests"})
@Slf4j
@AllArgsConstructor
public class TripChangeRequestController {

    private final TripChangeRequestService tripChangeRequestService;
    private final TripTicketService tripTicketService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<TripChangeRequestDTO>> listChangeRequests(
            @PathVariable("trip_ticket_id") int tripTicketId) {
        List<TripChangeRequestDTO> result = tripChangeRequestService.findAllForTripTicket(tripTicketId);
        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripChangeRequestDTO> createChangeRequest(
            @PathVariable("trip_ticket_id") int tripTicketId,
            @Valid @RequestBody CreateChangeRequestRequest request) {
        User currentUser = tripTicketService.getCurrentUser();
        TripChangeRequestDTO dto = tripChangeRequestService.createChangeRequest(tripTicketId, request, currentUser);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @RequestMapping(value = {"/{id}"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripChangeRequestDTO> updateChangeRequest(
            @PathVariable("trip_ticket_id") int tripTicketId,
            @PathVariable("id") int id,
            @Valid @RequestBody CreateChangeRequestRequest request) {
        User currentUser = tripTicketService.getCurrentUser();
        TripChangeRequestDTO dto = tripChangeRequestService.updateChangeRequest(tripTicketId, id, request, currentUser);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{id}/cancel"}, method = RequestMethod.PUT)
    public ResponseEntity<TripChangeRequestDTO> cancelChangeRequest(
            @PathVariable("trip_ticket_id") int tripTicketId,
            @PathVariable("id") int id) {
        User currentUser = tripTicketService.getCurrentUser();
        TripChangeRequestDTO dto = tripChangeRequestService.cancelChangeRequest(tripTicketId, id, currentUser);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{id}/approve"}, method = RequestMethod.PUT)
    public ResponseEntity<TripChangeRequestDTO> approveChangeRequest(
            @PathVariable("trip_ticket_id") int tripTicketId,
            @PathVariable("id") int id,
            @RequestBody(required = false) ChangeRequestResponseRequest body) {
        User currentUser = tripTicketService.getCurrentUser();
        String message = body != null ? body.getResponseMessage() : null;
        TripChangeRequestDTO dto = tripChangeRequestService.approveChangeRequest(tripTicketId, id, message, currentUser);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{id}/deny"}, method = RequestMethod.PUT)
    public ResponseEntity<TripChangeRequestDTO> denyChangeRequest(
            @PathVariable("trip_ticket_id") int tripTicketId,
            @PathVariable("id") int id,
            @RequestBody(required = false) ChangeRequestResponseRequest body) {
        User currentUser = tripTicketService.getCurrentUser();
        String message = body != null ? body.getResponseMessage() : null;
        TripChangeRequestDTO dto = tripChangeRequestService.denyChangeRequest(tripTicketId, id, message, currentUser);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
}
