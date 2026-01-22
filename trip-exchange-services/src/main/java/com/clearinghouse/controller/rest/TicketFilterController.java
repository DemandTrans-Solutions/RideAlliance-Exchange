/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.DetailedTripTicketDTO;
import com.clearinghouse.dto.TicketFilterDTO;
import com.clearinghouse.listresponseentity.AddressListByString;
import com.clearinghouse.service.TicketFilterService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 *
 * @author chaitanyaP
 */
@RestController
@Slf4j
@RequestMapping(value = {"api/ticket_filters"})
@AllArgsConstructor
public class TicketFilterController {


    private final TicketFilterService ticketFilterService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<TicketFilterDTO>> listAllFilters() {
        List<TicketFilterDTO> filterDTO = ticketFilterService.findAllFilters();
        if (filterDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(filterDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{filterId}"}, method = RequestMethod.GET)
    public ResponseEntity<TicketFilterDTO> getFiltersByFilterId(@PathVariable("filterId") int filterId) {
        TicketFilterDTO filterDTO = ticketFilterService.findFilterByFilterId(filterId);
        if (filterDTO == null) {
            log.error("#FilterID not found [" + filterId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(filterDTO, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TicketFilterDTO> createFilter(@Valid @RequestBody TicketFilterDTO filterDTO) {
        TicketFilterDTO newFilterDTO = ticketFilterService.createFilter(filterDTO);
        return new ResponseEntity<>(newFilterDTO, HttpStatus.CREATED);

    }

    @RequestMapping(value = {"/{filterId}"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TicketFilterDTO> updateFilter(
            @PathVariable("filterId") int filterId,
            @Valid @RequestBody TicketFilterDTO filterDTO) {
        TicketFilterDTO currentFilterDTO = ticketFilterService.findFilterByFilterId(filterId);
        if (currentFilterDTO == null) {
            log.error("#updateFilter FILTER ID not found [" + filterId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        TicketFilterDTO updatedFilterDTO = ticketFilterService.updateFilter(filterDTO);
        return new ResponseEntity<>(updatedFilterDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{filterId}"}, method = RequestMethod.DELETE)
    public ResponseEntity<Boolean> deleteFilter(
            @PathVariable("filterId") int filterId) {
        if (ticketFilterService.findFilterByFilterId(filterId) == null) {
            log.error("#deleteFilter filterId not found [" + filterId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            boolean filterDeleteStatus = ticketFilterService.deleteFilterByFilterId(filterId);
            return new ResponseEntity<>(filterDeleteStatus, HttpStatus.OK);
        }
    }

    @RequestMapping(value = {"/filterByObject"}, method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> listAllTicketsByFilterDTOObject(@Valid @RequestBody TicketFilterDTO filterDTO) {
        Map<String, Object> filterdTickets = ticketFilterService.filterTicketsByFilterObject(filterDTO);
        return new ResponseEntity<>(filterdTickets, HttpStatus.OK);
    }

    @RequestMapping(value = {"user/{userId}"}, method = RequestMethod.GET)
    public ResponseEntity<List<TicketFilterDTO>> listAllFiltersbyUserId(@PathVariable("userId") int userId) {
        List<TicketFilterDTO> filterDTO = ticketFilterService.findAllFiltersByUserId(userId);
        if (filterDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(filterDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/address/{addressId}"}, method = RequestMethod.GET)
    public ResponseEntity<AddressListByString> getAddressById(@PathVariable("addressId") int addressId) {
        AddressListByString addressObj = ticketFilterService.getAddressById(addressId);

        return new ResponseEntity<>(addressObj, HttpStatus.OK);
    }

}
