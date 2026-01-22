package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.CheckWorkingHoursDTO;
import com.clearinghouse.dto.WorkingHoursDTO;
import com.clearinghouse.service.WorkingHoursService;
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
 * @author shankarI
 */

@RestController
@RequestMapping("api/workinghours")
@AllArgsConstructor
@Slf4j
public class WorkingHoursController {


    private final WorkingHoursService workingHoursService;


    @RequestMapping(value = "/getAll", method = RequestMethod.GET)
    public ResponseEntity<List<WorkingHoursDTO>> getAllWorkingHours() {

        List<WorkingHoursDTO> workingHoursDTOs = workingHoursService.getAllWorkingHours();
        if (workingHoursDTOs.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(workingHoursDTOs, HttpStatus.OK);
    }

    @RequestMapping(value = "/providerIdWiseWorkingHours/{providerId}", method = RequestMethod.GET)
    public ResponseEntity<List<WorkingHoursDTO>> listAllWorkingHoursByproviderId(
            @PathVariable("providerId") int providerId) {

        List<WorkingHoursDTO> workingHoursDTOs = workingHoursService.findWorkingHoursByProviderId(providerId);
        if (workingHoursDTOs.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(workingHoursDTOs, HttpStatus.OK);
    }

    // save workingHours for Weekdays (not in use)

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WorkingHoursDTO>> createWorkingHours(
            @Valid @RequestBody List<WorkingHoursDTO> workingHoursDTO) {
        List<WorkingHoursDTO> newWorkingHoursDTOs = workingHoursService.createWorkingHours(workingHoursDTO);
        return new ResponseEntity<>(newWorkingHoursDTOs, HttpStatus.CREATED);

    }

    //not in use
    @RequestMapping(value = "/updateProviderIdWiseWorkingHours/{providerId}", method = RequestMethod.PUT)
    public ResponseEntity<List<WorkingHoursDTO>> updateWorkingHoursByproviderId(
            @PathVariable("providerId") int providerId, @RequestBody List<WorkingHoursDTO> workHoursDTOList) {

        List<WorkingHoursDTO> workingHoursDTOs = workingHoursService.findWorkingHoursByProviderId(providerId);
        if (workingHoursDTOs.isEmpty()) {
            log.error("#updateWorkingHoursByproviderId- providerId not found [" + providerId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<WorkingHoursDTO> updatedWorkingHoursDTOList = workingHoursService.updateWorkingHours(workHoursDTOList);
        return new ResponseEntity<>(updatedWorkingHoursDTOList, HttpStatus.OK);
    }

    //replacement for create & update API
    @RequestMapping(value = "/createUpdateWorkingHours", method = RequestMethod.POST)
    public ResponseEntity<List<WorkingHoursDTO>> createUpdateWorkingHoursById(@RequestBody List<WorkingHoursDTO> workHoursDTOList) {

	/*	List<WorkingHoursDTO> workingHoursDTOs = workingHoursService.findWorkingHoursByProviderId(providerId);
		
		if (workingHoursDTOs.isEmpty()) {	
			log.error("#createUpdateWorkingHours- providerId not found [" + providerId + "]");
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}*/

        List<WorkingHoursDTO> updatedWorkingHoursDTOList = workingHoursService.createUpdateWorkingHours(workHoursDTOList);
        return new ResponseEntity<>(updatedWorkingHoursDTOList, HttpStatus.OK);
    }

    @RequestMapping(value = {"/workingHoursCheck"}, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CheckWorkingHoursDTO> checkWorkingHours(@RequestBody CheckWorkingHoursDTO checkWorkingHoursDTO) {
        CheckWorkingHoursDTO CheckWorkingHoursDTOObj = workingHoursService.checkWorkingHours(checkWorkingHoursDTO);
        return new ResponseEntity<>(CheckWorkingHoursDTOObj, HttpStatus.CREATED);

    }
}
