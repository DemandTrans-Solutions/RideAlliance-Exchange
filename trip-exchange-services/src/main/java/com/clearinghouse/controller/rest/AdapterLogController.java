package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.AdapterLogDTO;
import com.clearinghouse.service.AdapterLogService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = {"api/adapter_log"})
@AllArgsConstructor
@Slf4j
public class AdapterLogController {


    private final AdapterLogService adapterLogService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AdapterLogController.class);

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdapterLogDTO> createAdapterLog(@Valid @RequestBody AdapterLogDTO adapterLogDTO) {
        AdapterLogDTO newAdapterLogDTO = adapterLogService.createAdapterLog(adapterLogDTO);
        return new ResponseEntity<>(newAdapterLogDTO, HttpStatus.CREATED);

    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<AdapterLogDTO>> listAllAdapterLogs() {
        List<AdapterLogDTO> adapterLogDTOs = adapterLogService.findAllAdapterLogs();
        if (adapterLogDTOs.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(adapterLogDTOs, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{providerId}"}, method = RequestMethod.GET)
    public ResponseEntity<List<AdapterLogDTO>> getActivityByActivityId(@PathVariable("providerId") int providerId) {
        List<AdapterLogDTO> adapterLogDTOs = adapterLogService.findAdapterLogsByProviderId(providerId);
        if (adapterLogDTOs == null) {
            log.error("#providerId not found [" + providerId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(adapterLogDTOs, HttpStatus.OK);
    }

}
