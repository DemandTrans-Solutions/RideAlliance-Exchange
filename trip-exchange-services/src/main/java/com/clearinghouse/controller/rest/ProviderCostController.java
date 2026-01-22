package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.ProviderCostDTO;
import com.clearinghouse.service.ProviderCostService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * @author Shankar I
 */

@RestController
@RequestMapping(value = {"api/providercost"})
@AllArgsConstructor
@Slf4j
public class ProviderCostController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderCostController.class);


    private final ProviderCostService providerCostService;


    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ProviderCostDTO>> listAllProviderCost() {
        List<ProviderCostDTO> providerCostDTO = providerCostService.findAllProvidersCost();
        if (providerCostDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(providerCostDTO, HttpStatus.OK);
    }


    @RequestMapping(value = {
            "/{providerId}"}, method = RequestMethod.GET)
    public ResponseEntity<ProviderCostDTO> getCostByProviderId(@PathVariable("providerId") int providerId) {
        ProviderCostDTO providerCostDTO = providerCostService.getCostByProviderId(providerId);
        if (providerCostDTO == null) {
            log.error("#getCostByProviderId- provider ID not found [" + providerId + "]");
            return new ResponseEntity<>(providerCostDTO, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(providerCostDTO, HttpStatus.OK);
    }

    // combine create & update API
    @RequestMapping(value = "/createUpdateProviderCost", method = RequestMethod.POST)
    public ResponseEntity<ProviderCostDTO> createUpdateCostByProviderId(@RequestBody ProviderCostDTO providerCostDTO) {
        ProviderCostDTO updatedProviderCostDTOList = providerCostService.createUpdateProviderCost(providerCostDTO);
        return new ResponseEntity<>(updatedProviderCostDTOList, HttpStatus.OK);
    }

}
