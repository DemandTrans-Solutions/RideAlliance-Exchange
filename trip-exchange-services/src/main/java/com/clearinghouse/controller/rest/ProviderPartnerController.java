/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.ProviderPartnerDTO;
import com.clearinghouse.service.ProviderPartnerService;
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
@RequestMapping(value = {"api/providerPartners"})
@AllArgsConstructor
public class ProviderPartnerController {


    private final ProviderPartnerService providerPartnerService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ProviderPartnerDTO>> listAllProviderPartners() {
        List<ProviderPartnerDTO> providerPartnerDTO = providerPartnerService.findAllProviderPartners();
        if (providerPartnerDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(providerPartnerDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/requesterProvider/{requesterProviderId}"}, method = RequestMethod.GET)
    public ResponseEntity<List<ProviderPartnerDTO>> listAllProviderPartnersByRequesterProviderId(@PathVariable("requesterProviderId") int requesterProviderId) {
        List<ProviderPartnerDTO> providerPartnerDTO = providerPartnerService.findAllProviderPartnersByRequesterProviderId(requesterProviderId);
        if (providerPartnerDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(providerPartnerDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{providerPartnerId}"}, method = RequestMethod.GET)
    public ResponseEntity<ProviderPartnerDTO> getProviderPartnerByProviderPartnerId(@PathVariable("providerPartnerId") int providerPartnerId) {
        ProviderPartnerDTO providerPartnerDTO = providerPartnerService.findProviderPartnerByProviderPartnerId(providerPartnerId);
        if (providerPartnerDTO == null) {
            log.error("#getprovideById ProviderProvider ID not found [" + providerPartnerId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(providerPartnerDTO, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProviderPartnerDTO> createProviderPartner(@Valid @RequestBody ProviderPartnerDTO providerPartnerDTO) {
        ProviderPartnerDTO newProviderpartnerDTO = providerPartnerService.createProviderPartner(providerPartnerDTO);
        return new ResponseEntity<>(newProviderpartnerDTO, HttpStatus.CREATED);

    }

    @RequestMapping(value = {"/{providerPartnerId}"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProviderPartnerDTO> updateProviderPartner(
            @PathVariable("providerPartnerId") int providerPartnerId,
            @Valid @RequestBody ProviderPartnerDTO providerPartnerDTO) {
        ProviderPartnerDTO currentProviderPartnerDTO = providerPartnerService.findProviderPartnerByProviderPartnerId(providerPartnerId);
        if (currentProviderPartnerDTO == null) {
            log.error("#updateProvider ProviderPartner ID not found [" + providerPartnerId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ProviderPartnerDTO updatedProviderPartnerDTO = providerPartnerService.updateProviderPartner(providerPartnerDTO);
        return new ResponseEntity<>(updatedProviderPartnerDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{providerPartnerId}"}, method = RequestMethod.DELETE)
    public ResponseEntity<Boolean> deleteProviderPartner(
            @PathVariable("providerPartnerId") int providerPartnerId) {
        if (providerPartnerService.findProviderPartnerByProviderPartnerId(providerPartnerId) == null) {
            log.error("#deleteProvider providerPartnerId not found [" + providerPartnerId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            boolean providerPartnerDeleteStatus = providerPartnerService.deleteProviderpartnerByProviderPartnerId(providerPartnerId);
            return new ResponseEntity<>(providerPartnerDeleteStatus, HttpStatus.OK);
        }
    }

    @RequestMapping(value = {"/approvedProviderPartners/{requesterProviderId}"}, method = RequestMethod.GET)
    public ResponseEntity<List<ProviderPartnerDTO>> listOfApprovedProviderPartnersByRequesterProviderId(@PathVariable("requesterProviderId") int requesterProviderId) {
        List<ProviderPartnerDTO> providerPartnerDTO = providerPartnerService.findApprovedProviderPartnersByRequesterProviderId(requesterProviderId);
        if (providerPartnerDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(providerPartnerDTO, HttpStatus.OK);
    }

}
