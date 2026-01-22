package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.FundingSourceDTO;
import com.clearinghouse.service.FundingSourceService;
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
 * @author Shankar I
 */

@RestController
@RequestMapping(value = {"api/fundingSource"})
@AllArgsConstructor
@Slf4j
public class FundingSourceController {


    private final FundingSourceService fundindSourceService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResponseEntity<List<FundingSourceDTO>> listAllFundingSources() {
        List<FundingSourceDTO> fundingSourceDTOs = fundindSourceService.findAllFundingSources();
        if (fundingSourceDTOs.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<List<FundingSourceDTO>>(fundingSourceDTOs, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{fundingSourceId}"}, method = RequestMethod.GET)
    public ResponseEntity<FundingSourceDTO> getFundingSourceById(@PathVariable("fundingSourceId") int fundingSourceId) {
        FundingSourceDTO fundingSourceDTO = fundindSourceService.findFundingSourceById(fundingSourceId);
        if (fundingSourceDTO == null) {
            log.error("#getFundingSourceById- fundingSource ID not found [" + fundingSourceId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(fundingSourceDTO, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FundingSourceDTO> createFundingSource(@Valid @RequestBody FundingSourceDTO fundingSourceDTO)
            throws Exception {
        boolean checkForPresent = fundindSourceService.findFundingSourceByName(fundingSourceDTO.getName());
        if (checkForPresent) {
            // String msg=fundingSourceDTO.getName() + " FundingSource Already Exists";
            log.error("#FundingSource Already Exists [" + fundingSourceDTO.getName() + "]");
            throw new Exception("FundingSource Already Exists");
        } else {
            FundingSourceDTO newFundingSourceDTO = fundindSourceService.createFundingSource(fundingSourceDTO);
            return new ResponseEntity<>(newFundingSourceDTO, HttpStatus.OK);
        }
    }

    @RequestMapping(value = {"/{fundingSourceId}"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FundingSourceDTO> updateFundingSource(@PathVariable("fundingSourceId") int fundingSourceId,
                                                                @Valid @RequestBody FundingSourceDTO fundingSourceDTO) {
        FundingSourceDTO currentfundingSourceDTO = fundindSourceService.findFundingSourceById(fundingSourceId);
        if (currentfundingSourceDTO == null) {
            log.error("#updateFundingSource- FundingSource ID not found [" + fundingSourceId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        FundingSourceDTO updatedFundingSourceDTO = fundindSourceService.updateFundingSource(fundingSourceDTO);
        return new ResponseEntity<FundingSourceDTO>(updatedFundingSourceDTO, HttpStatus.OK);

    }

    @RequestMapping(value = {"/{fundingSourceId}/activate"}, method = RequestMethod.PUT)
    public ResponseEntity<FundingSourceDTO> activateProvider(@PathVariable("fundingSourceId") int fundingSourceId) {
        FundingSourceDTO currentFundingSourceDTO = fundindSourceService.findFundingSourceById(fundingSourceId);
        if (currentFundingSourceDTO == null) {
            log.error("#updateFundingSource- FundingSource ID not found [" + fundingSourceId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        FundingSourceDTO activatedFundingSourceDTO = fundindSourceService.activateFundingSource(fundingSourceId);
        return new ResponseEntity<>(activatedFundingSourceDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{fundingSourceId}/deactivate"}, method = RequestMethod.PUT)
    public ResponseEntity<FundingSourceDTO> deactivateProvider(@PathVariable("fundingSourceId") int fundingSourceId) {
        FundingSourceDTO currentFundingSourceDTO = fundindSourceService.findFundingSourceById(fundingSourceId);
        if (currentFundingSourceDTO == null) {
            log.error("#updateFundingSource- FundingSource ID not found [" + fundingSourceId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        FundingSourceDTO deactivatedFundingSourceDTO = fundindSourceService.deactivateFundingSource(fundingSourceId);
        return new ResponseEntity<>(deactivatedFundingSourceDTO, HttpStatus.OK);
    }

}