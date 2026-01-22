/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.ProviderDTO;
import com.clearinghouse.service.ProviderService;
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
@RequestMapping(value = {"api/provider"})
@AllArgsConstructor
public class ProviderController {


    private final ProviderService providerService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ProviderDTO>> listAllProviders() {
        List<ProviderDTO> providerDTO = providerService.findAllProviders();
        if (providerDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(providerDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{providerId}"}, method = RequestMethod.GET)
    public ResponseEntity<ProviderDTO> getProviderById(@PathVariable("providerId") int providerId) {
        ProviderDTO providerDTO = providerService.findProviderByProviderId(providerId);
        if (providerDTO == null) {
            log.error("#getprovideById provider ID not found [" + providerId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(providerDTO, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProviderDTO> createProvider(@Valid @RequestBody ProviderDTO providerDTO) {
        ProviderDTO newProviderDTO = providerService.createProvider(providerDTO);
        return new ResponseEntity<>(newProviderDTO, HttpStatus.CREATED);

    }

    @RequestMapping(value = {"/{providerId}"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProviderDTO> updateProvider(
            @PathVariable("providerId") int providerId,
            @Valid @RequestBody ProviderDTO providerDTO) {
        ProviderDTO currentProviderDTO = providerService.findProviderByProviderId(providerId);
        if (currentProviderDTO == null) {
            log.error("#updateProvider Provider ID not found [" + providerId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ProviderDTO updatedProviderDTO = providerService.updateProvider(providerDTO);
        return new ResponseEntity<>(updatedProviderDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{providerId}/activateProvider"}, method = RequestMethod.PUT)
    public ResponseEntity<ProviderDTO> activateProvider(
            @PathVariable("providerId") int providerId) {
        ProviderDTO currentProviderDTO = providerService.findProviderByProviderId(providerId);
        if (currentProviderDTO == null) {
            log.error("#updateProvider Provider ID not found [" + providerId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ProviderDTO activatedProviderDTO = providerService.activateProvider(providerId);
        return new ResponseEntity<>(activatedProviderDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{providerId}/deactivateProvider"}, method = RequestMethod.PUT)
    public ResponseEntity<ProviderDTO> deactivateProvider(
            @PathVariable("providerId") int providerId
    ) {
        ProviderDTO currentProviderDTO = providerService.findProviderByProviderId(providerId);
        if (currentProviderDTO == null) {
            log.error("#updateProvider Provider ID not found [" + providerId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ProviderDTO deactivatedProviderDTO = providerService.deactivateProvider(providerId);
        return new ResponseEntity<>(deactivatedProviderDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{providerId}"}, method = RequestMethod.DELETE)
    public ResponseEntity<Boolean> deleteProvider(
            @PathVariable("providerId") int providerId) {
        if (providerService.findProviderByProviderId(providerId) == null) {
            log.error("#deleteProvider providerId not found [" + providerId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            boolean providerDeleteStatus = providerService.deleteProviderByProviderId(providerId);
            return new ResponseEntity<>(providerDeleteStatus, HttpStatus.OK);
        }
    }
}
