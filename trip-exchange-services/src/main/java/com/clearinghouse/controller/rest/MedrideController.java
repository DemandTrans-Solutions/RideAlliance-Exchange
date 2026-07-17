package com.clearinghouse.controller.rest;


import com.clearinghouse.dto.*;
import com.clearinghouse.service.MedrideService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping(value = {"api/medride"})
@AllArgsConstructor
public class MedrideController {

    private final MedrideService medrideService;


    @RequestMapping(value = "/claim", method = RequestMethod.POST)
    public ResponseEntity claimRide(@RequestBody MedrideRideRequest medrideRideRequest) {
        log.debug("Having MedRide claim ride: " + medrideRideRequest);
        medrideService.claimRide(medrideRideRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
