/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.ActivityDTO;
import com.clearinghouse.entity.Activity;
import com.clearinghouse.service.ActivityService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author chaitanyaP
 */
@Slf4j
@RestController
@RequestMapping(value = {"api/activity"})
@AllArgsConstructor
public class ActivityController {


    private final ActivityService activityService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ActivityDTO>> listAllActivites() {
        List<ActivityDTO> activityDTO = activityService.findAllActivites();
        if (activityDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(activityDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{activityId}"}, method = RequestMethod.GET)
    public ResponseEntity<ActivityDTO> getActivityByActivityId(@PathVariable("activityId") int activityId) {
        ActivityDTO activityDTO = activityService.findActivityByActivityId(activityId);
        if (activityDTO == null) {
            log.error("#tactivityId not found [" + activityId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(activityDTO, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActivityDTO> createActivity(@Valid @RequestBody ActivityDTO activityDTO) {
        ActivityDTO newActivityDTO = activityService.createActivity(activityDTO);
        return new ResponseEntity<>(newActivityDTO, HttpStatus.CREATED);

    }

    @RequestMapping(value = {"tripTicket/{tripTicketId}"}, method = RequestMethod.GET)
    public ResponseEntity<List<ActivityDTO>> listAllActivitesByTripTicketId(@PathVariable("tripTicketId") int tripTicketId) {
        List<ActivityDTO> activityDTO = activityService.findAllActivitesByTripTicketId(tripTicketId);
        if (activityDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(activityDTO, HttpStatus.OK);
    }

}
