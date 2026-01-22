package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.TripTicketDTO;
import com.clearinghouse.dto.TripTicketDistanceDTO;
import com.clearinghouse.service.TripTicketDistanceService;
import com.clearinghouse.service.TripTicketService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 *
 * @author shankarI
 */
@RestController
@RequestMapping(value = {"api/tripticketdistance"})
@AllArgsConstructor
@Slf4j
public class TripTicketDistanceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TripTicketDistanceController.class);


    private final TripTicketDistanceService tripTicketDistanceService;


    private final TripTicketService tripTicketService;


    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<TripTicketDistanceDTO>> findAllTripTicketCost() {
        List<TripTicketDistanceDTO> tripTicketDistanceDTO = tripTicketDistanceService.findAllTripTicketDistance();
        if (tripTicketDistanceDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(tripTicketDistanceDTO, HttpStatus.OK);
    }


    @RequestMapping(value = {"/{tripTicketId}"}, method = RequestMethod.GET)
    public ResponseEntity<TripTicketDistanceDTO> getDistanceByTripTicketId(@PathVariable("tripTicketId") int tripTicketId) {
        TripTicketDistanceDTO tripTicketDistanceDTO = tripTicketDistanceService.getDistanceByTripTicketId(tripTicketId);
        if (tripTicketDistanceDTO == null) {
            log.error("#getCostByTripTicketId- tripTicket ID not found [" + tripTicketId + "]");
            return new ResponseEntity<>(tripTicketDistanceDTO, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(tripTicketDistanceDTO, HttpStatus.OK);
    }

    // combine create & update API
    @RequestMapping(value = "/createUpdateTripTicketCost", method = RequestMethod.POST)
    public ResponseEntity<TripTicketDistanceDTO> createUpdateCostByProviderId(@RequestBody TripTicketDistanceDTO tripTicketDistanceDTO) {
        TripTicketDistanceDTO updatedTripTicketDistanceDTO = tripTicketDistanceService.createUpdateTripTicketDistance(tripTicketDistanceDTO);
        return new ResponseEntity<>(updatedTripTicketDistanceDTO, HttpStatus.OK);
    }

    //newly added by shankar for add distance and time for tripticket by Id
    @RequestMapping(value = {"/saveDistanceTime/{tripTicketId}"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripTicketDistanceDTO> getTimeAndDistance(@PathVariable("tripTicketId") int tripTicketId, TripTicketDistanceDTO tripTicketDistanceDTO) {

        TripTicketDTO tripTicketDto = tripTicketService.findTripTicketByTripTicketId(tripTicketId);
        if (tripTicketDto == null) {
            log.error("#getCostByTripTicketId- tripTicket ID not found [" + tripTicketId + "]");
            return new ResponseEntity<>(new TripTicketDistanceDTO(), HttpStatus.NOT_FOUND);
        }
        TripTicketDistanceDTO tripTicketDistanceDto = tripTicketDistanceService.saveDistanceTime(tripTicketDto, tripTicketDistanceDTO);

        return new ResponseEntity<>(tripTicketDistanceDto, HttpStatus.OK);
    }


}
