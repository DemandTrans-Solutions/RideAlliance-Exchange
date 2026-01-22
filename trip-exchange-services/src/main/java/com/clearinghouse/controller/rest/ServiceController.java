/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.*;
import com.clearinghouse.service.ServiceService;
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
@RequestMapping(value = {"api/servicearea"})
@AllArgsConstructor
@Slf4j
public class ServiceController {


    private final ServiceService service;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ServiceDTO>> listAllServicearea() {
        List<ServiceDTO> serviceDTOs = service.findAllServicearea();
        if (serviceDTOs.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(serviceDTOs, HttpStatus.OK);
    }

    @RequestMapping(value = "providerIdWiseServicearea/{providerId}", method = RequestMethod.GET)
    public ResponseEntity<List<ServiceDTO>> listAllServiceareaByproviderId(@PathVariable("providerId") int providerId) {

        List<ServiceDTO> serviceDTOs = service.findServiceareaByProviderId(providerId);
        if (serviceDTOs.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(serviceDTOs, HttpStatus.OK);

    }

    @RequestMapping(value = {"/{serviceId}"}, method = RequestMethod.GET)
    public ResponseEntity<ServiceDTO> getServicearaByServiceId(@PathVariable("serviceId") int serviceId) {
        ServiceDTO serviceDTO = service.findServiceareaByServiceId(serviceId);
        if (serviceDTO == null) {
            log.error("#servicetId not found [" + serviceId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(serviceDTO, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServiceDTO> createServicearea(@Valid @RequestBody ServiceDTO serviceDTO) {
        ServiceDTO newserviceDTO = service.createServicearea(serviceDTO);
        return new ResponseEntity<>(newserviceDTO, HttpStatus.CREATED);

    }

    @RequestMapping(value = {"/serviceareaCheck"}, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CheckServiceareaDTO> checkServicearea(@Valid @RequestBody CheckServiceareaDTO checkServiceareaDTO) {
        CheckServiceareaDTO CheckServiceareaDTOObj = service.checkServicearea(checkServiceareaDTO);
        return new ResponseEntity<>(CheckServiceareaDTOObj, HttpStatus.CREATED);

    }

    @RequestMapping(value = {"/{serviceId}"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServiceDTO> updateServiceArea(
            @PathVariable("serviceId") int serviceId,
            @Valid @RequestBody ServiceDTO serviceDTO) {
        ServiceDTO currentServiceDTO = service.findServiceareaByServiceId(serviceId);
        if (currentServiceDTO == null) {
            log.error("#updateServicearea serviceId not found [" + serviceId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ServiceDTO updatedServiceDTO = service.updateServicearea(serviceDTO);
        return new ResponseEntity<>(updatedServiceDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/isLastActiveServicearea"}, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> isLastActiveSeriveareaCheck(@RequestBody IsLastActiveSeriveareaDTO isLastActiveSeriveareaDTO) {
        String result = "{\"value\":\"" + service.isLastActiveSerivearea(isLastActiveSeriveareaDTO) + "\"}";

        return new ResponseEntity<>(result, HttpStatus.CREATED);

    }

    @RequestMapping(value = "/hospitality", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServiceDTO> createHospitalityServicearea(@Valid @RequestBody ServiceDTO serviceDTO) {
        ServiceDTO newserviceDTO = service.createHospitalityServicearea(serviceDTO);
        return new ResponseEntity<>(newserviceDTO, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/hospitality", method = RequestMethod.GET)
    public ResponseEntity<List<ServiceDTO>> listAllHosptalityServicearea() {
        List<ServiceDTO> serviceDTOs = service.findAllHosptalityServicearea();
        if (serviceDTOs.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(serviceDTOs, HttpStatus.OK);
    }

    @RequestMapping(value = "/providerIdWiseHospitalityServicearea", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ServiceDTO>> listAllHospitalityServiceareaByproviderIds(
            @RequestBody List<Integer> providerIds) {
        List<ServiceDTO> serviceDTOs = service.findHospitalityServiceareaByProviderId(providerIds);
        if (serviceDTOs.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(serviceDTOs, HttpStatus.OK);
    }

    @RequestMapping(value = "/hospitalityFileUpload", method = RequestMethod.POST)
    public ResponseEntity<ServiceDTO> hospitalityServiceAreaFileUpload(@RequestBody ServiceDTO serviceDTO) {
        ServiceDTO newserviceDTO = service.hospitalityServiceAreaFileUpload(serviceDTO);
        return new ResponseEntity<>(newserviceDTO, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/hospitality", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServiceDTO> hospitalityServiceAreaUpdate(@Valid @RequestBody ServiceDTO serviceDTO) {
        ServiceDTO newserviceDTO = service.hospitalityServiceAreaUpdate(serviceDTO);
        return new ResponseEntity<>(newserviceDTO, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/hospitalityarealist", method = RequestMethod.GET)
    public ResponseEntity<List<MasterDTO>> listAllHosptalityServiceareaName() {
        List<MasterDTO> serviceAreas = service.listOfHosptalitySAName();
        if (serviceAreas.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(serviceAreas, HttpStatus.OK);
    }


    @RequestMapping(value = "/serviceAreaFileUpload", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServiceDTO> createServiceAreaWithFileUpload(@Valid @RequestBody ServiceDTO serviceDTO) {
        ServiceDTO newserviceDTO = service.createServiceAreaWithFileUpload(serviceDTO);
        return new ResponseEntity<>(newserviceDTO, HttpStatus.CREATED);

    }

    @RequestMapping(value = "/serviceAreaFileUpload/{serviceId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServiceDTO> updateServiceAreaWithFileUpload(@PathVariable("serviceId") int serviceId, @Valid @RequestBody ServiceDTO serviceDTO) {
        ServiceDTO newserviceDTO = service.updateServiceAreaWithFileUpload(serviceDTO);
        return new ResponseEntity<>(newserviceDTO, HttpStatus.CREATED);

    }

    @RequestMapping(value = {"/medicalServiceareaCheck"}, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CheckServiceareaDTO> checkMediacalServicearea(@Valid @RequestBody CheckServiceareaDTO checkServiceareaDTO) {
        CheckServiceareaDTO CheckServiceareaDTOObj = service.checkMedicalServicearea(checkServiceareaDTO);
        return new ResponseEntity<>(CheckServiceareaDTOObj, HttpStatus.CREATED);

    }

    @RequestMapping(value = "/validateServiceAreaFileUpload", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> validateServiceAreaFileUpload(@Valid @RequestBody UploadFile uploadFile) {
        List<String> serviceAreaList = service.validateServiceAreaFileUpload(uploadFile);
        return new ResponseEntity<>(serviceAreaList, HttpStatus.CREATED);
    }
}
