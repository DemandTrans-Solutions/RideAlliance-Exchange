/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.ApplicationSettingDTO;
import com.clearinghouse.service.ApplicationSettingService;
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
@RequestMapping(value = {"api/applicationSettings"})
@Slf4j
@AllArgsConstructor
public class ApplicationSettingController {


    private final ApplicationSettingService applicationSettingService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ApplicationSettingDTO>> listAllApplicationSettings() {
        List<ApplicationSettingDTO> applicationSettingDTO = applicationSettingService.findAllApplicationSettings();
        if (applicationSettingDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(applicationSettingDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{applicationSettingId}"}, method = RequestMethod.GET)
    public ResponseEntity<ApplicationSettingDTO> getApplicationSettingById(@PathVariable("applicationSettingId") int applicationSettingId) {
        ApplicationSettingDTO applicationSettingDTO = applicationSettingService.findApplicationSettingByApplicationId(applicationSettingId);
        if (applicationSettingDTO == null) {
            log.error("#getApplicationSettingById Application ID not found [" + applicationSettingId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(applicationSettingDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{applicationSettingId}"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApplicationSettingDTO> updateApplicationSetting(
            @PathVariable("applicationSettingId") int applicationSettingId,
            @Valid @RequestBody ApplicationSettingDTO applicationSettingDTO) {
        ApplicationSettingDTO currentApplicationSettingDTO = applicationSettingService.findApplicationSettingByApplicationId(applicationSettingId);
        if (currentApplicationSettingDTO == null) {
            log.error("#updateUser User ID not found [" + applicationSettingId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ApplicationSettingDTO applicationSettingDTOUpdated = applicationSettingService.updateApplicationSetting(applicationSettingDTO);
        return new ResponseEntity<>(applicationSettingDTOUpdated, HttpStatus.OK);
    }

}
