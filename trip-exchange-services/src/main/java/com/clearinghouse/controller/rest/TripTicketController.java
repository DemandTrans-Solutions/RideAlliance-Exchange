/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.*;
import com.clearinghouse.entity.TripTicket;
import com.clearinghouse.entity.User;

import com.clearinghouse.enumentity.TripTicketStatusConstants;
import com.clearinghouse.exceptions.HandlingExceptionForOKStatus;
import com.clearinghouse.exceptions.InvalidInputCheckException;
import com.clearinghouse.exceptions.InvalidInputException;
import com.clearinghouse.service.*;
import com.clearinghouse.web.dto.LoginValidateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author chaitanyaP
 */
@RestController
@Slf4j
@RequestMapping(value = {"api/trip_tickets"})
@AllArgsConstructor
public class TripTicketController {

    private final CancelUberTripTicketService cancelUberTripTicketService;
    private final ConvertRequestToTripTicketDTOService convertRequestToTripTicketDTOService;
    private final TripTicketService tripTicketService;
    private final TripTicketDistanceService tripTicketDistanceService;
    private final TripTicketDataService tripTicketDataService;
    private final TripClaimService tripClaimService;
    private final ProviderService providerService;
    private final UberService uberService;
    private final TripResultService tripResultService;



    // Diagnostics: scan for JDBC vs DB DATE read mismatches and log them
    @GetMapping("/date-mismatch/scan")
    public ResponseEntity<List<com.clearinghouse.dto.DateReadMismatchDTO>> scanDateMismatches(
            @RequestParam(name = "maxRows", defaultValue = "10000") int maxRows) {
        var mismatches = tripTicketService.logDateReadMismatches(maxRows);
        return new ResponseEntity<>(mismatches, HttpStatus.OK);
    }

    // Diagnostics: check a single TripTicketID for date read mismatch and log it
    @GetMapping("/{id}/date-mismatch")
    public ResponseEntity<List<com.clearinghouse.dto.DateReadMismatchDTO>> dateMismatchById(
            @PathVariable("id") int id) {
        var mismatches = tripTicketService.logDateReadMismatchesById(id);
        return new ResponseEntity<>(mismatches, HttpStatus.OK);
    }

    // Fix date mismatches by rewriting entity dates from DB CAST strings
    @PostMapping("/date-mismatch/fix")
    public ResponseEntity<Map<String, Object>> fixDateMismatches(
            @RequestParam(name = "maxRows", defaultValue = "10000") int maxRows,
            @RequestParam(name = "dryRun", defaultValue = "true") boolean dryRun) {
        var result = tripTicketService.fixDateMismatches(maxRows, dryRun);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // Verify a specific ticket's dates: show both JDBC read and raw DB value
    @GetMapping("/{id}/date-verify")
    public ResponseEntity<Map<String, Object>> verifyTicketDates(@PathVariable("id") int id) {
        var result = tripTicketService.verifyTicketDates(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<TripTicketDTO>> listAllTripTickets() {
        List<TripTicketDTO> tripTicketDTO = tripTicketService.findAllTripTicket();
        if (tripTicketDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(tripTicketDTO, HttpStatus.OK);
    }


    @RequestMapping(value = "/export", method = RequestMethod.POST)
    public ResponseEntity<List<DetailedTripTicketDTO>> exportTripTicketIds(@RequestBody TripTicketDownloadRequest tripTicketIds) {
        var tickets = tripTicketService.findTicketsByIds(tripTicketIds);
        if (tickets.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        // Populates trip notes with any comments
        for ( var ticket : tickets ) {
            ticket.extractComments();
        }
        return new ResponseEntity<>(tickets, HttpStatus.OK);
    }

    /*
        @RequestMapping(value = "/sync", method = RequestMethod.GET )
        public ResponseEntity<List<DetailedTripTicketDTO>> listAllDeatiedTripTickets(
                @RequestParam(value = "updated_since", required = false) String updated_since, HttpServletRequest request) {
            Map<String, String[]> parameterMap = request.getParameterMap();
            List<DetailedTripTicketDTO> detailedTripTicketDTOList = tripTicketService
                    .findTicketsForAdapterByTripTicketId(parameterMap, updated_since);
            return new ResponseEntity<>(detailedTripTicketDTOList, HttpStatus.OK);
        }
        */
    @RequestMapping(value = "/sync", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> listAllDeatiedTripTickets(
            @RequestParam(value = "updated_since", required = false) String updated_since, HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> detailedTripTicketDTOList = tripTicketService
                .findTicketsForAdapterByTripTicketId(parameterMap, updated_since);
        return new ResponseEntity<>(detailedTripTicketDTOList, HttpStatus.OK);
    }

    @RequestMapping(value = "/syncForUI", method = RequestMethod.GET)
    public ResponseEntity<List<DetailedTripTicketDTO>> listAllDeatiedTripTicketsForUI(
            @RequestParam(value = "updated_since", required = false) String updated_since, HttpServletRequest request) {
        /* add code here if userId is greater than o then do this else do below code */
        List<DetailedTripTicketDTO> detailedTripTicketDTO = tripTicketService.findAllDeatiledTripTicket(updated_since);
        if (detailedTripTicketDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(detailedTripTicketDTO, HttpStatus.OK);
    }

    @RequestMapping(value = "/pagination", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> listAllDeatiedTripTicketsWithpagination(
            @RequestParam(value = "pagesize", required = true) String pagesize,
            @RequestParam(value = "pagenumber", required = true) String pagenumber,
            @RequestParam(value = "sortField", required = false) String sortField,
            @RequestParam(value = "sortOrder", required = false) String sortOrder, HttpServletRequest request) {
        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setPageSize(Integer.parseInt(pagesize));
        paginationDTO.setCurrentPageNumber(Integer.parseInt(pagenumber));

        try {
            paginationDTO.setSortOrder(Integer.parseInt(sortOrder));
        } catch ( Exception e) {
            paginationDTO.setSortOrder(-1);
        }

        // Convert snake_case to camelCase
        if (sortField != null && sortField.contains("_")) {
            String camelCaseField = convertSnakeToCamelCase(sortField);
            if (camelCaseField.equals("originProviderId")) {
                camelCaseField = "originProvider.providerId";
            } else if (camelCaseField.equals("originProviderName")) {
                camelCaseField = "originProvider.providerName";
            }

            paginationDTO.setSortField(camelCaseField);
        } else {
            paginationDTO.setSortField("status");
        }

        var detailedTripTicketDTOMap = tripTicketService.findAllDeatiledTripTicketWithpagination(paginationDTO);
        return new ResponseEntity<>(detailedTripTicketDTOMap, HttpStatus.OK);

    }


    public static String convertSnakeToCamelCase(String snakeCase) {
        StringBuilder camelCase = new StringBuilder();
        boolean nextUpper = false;

        for (int i = 0; i < snakeCase.length(); i++) {
            char c = snakeCase.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    camelCase.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    camelCase.append(c);
                }
            }
        }

        return camelCase.toString();
    }

    /**
     * Get paginated trip tickets
     *
     * @param first The index of the first record to fetch
     * @param rows  The number of rows to fetch
     * @return List of paginated trip tickets
     */
    @GetMapping("/all")
    public ResponseEntity<Page<TripTicketDTO>> getPaginatedTripTickets(
            @RequestParam(defaultValue = "0") int first,
            @RequestParam(defaultValue = "10") int rows) {
        var tripTickets = tripTicketService.getPaginatedTripTickets(first, rows);
        return new ResponseEntity<>(tripTickets, HttpStatus.OK);
    }

    @RequestMapping(value = "/providerIdWiseTickets/{providerId}", method = RequestMethod.GET)
    public ResponseEntity<List<DetailedTripTicketDTO>> listAllDeatiedTripTicketsByproviderId(
            @PathVariable("providerId") int providerId) {

        List<DetailedTripTicketDTO> detailedTripTicketDTO = tripTicketService
                .findDetailedTripTicketByOriginProviderId(providerId);
        if (detailedTripTicketDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(detailedTripTicketDTO, HttpStatus.OK);

    }

    @RequestMapping(value = {"/{id}"}, method = RequestMethod.GET)
    public ResponseEntity<DetailedTripTicketDTO> getTripTicketByTripTicketId(@PathVariable("id") int id,
                                                                             @RequestParam("providerId") int providerId) {
        var tripTicketDTO = tripTicketService.findDetailedTripTicketById(id, providerId);
        if (tripTicketDTO == null) {
            log.error("#tripTicketId not found [" + id + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(tripTicketDTO, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
//      public ResponseEntity<TripTicketDTO> createTripTicket(@Valid @RequestBody TripTicketDTO tripTicketDTO, BindingResult result, boolean isCallFromAdapter) {
    public ResponseEntity<TripTicketDTO> createTripTicket(@Valid @RequestBody TripTicketDTO tripTicketDTO,
                                                          BindingResult result) {
        List<String> messageList = new ArrayList<>();
        /* check if requested provider exists or not */
        if (!tripTicketService.isOriginatorProviderExists(tripTicketDTO.getOriginProviderId())) {
            messageList.add("originProviderId : not exists in trip exchange ");
            if (!result.hasErrors()) {
                throw new InvalidInputCheckException(messageList, 0);
            }

        }
        /* filtering the binding result ..checking is there any error */
        if (result.hasErrors()) {
            List<FieldError> fieldErrorList = result.getFieldErrors();
            for (FieldError e : fieldErrorList) {
                messageList.add(e.getField() + ":" + e.getDefaultMessage());
            }
            tripTicketService.sendMailToOriginatorForInvalidInput(messageList, tripTicketDTO.getOriginProviderId());
            throw new InvalidInputCheckException(messageList, tripTicketDTO.getOriginProviderId());

        } else {
            TripTicketDTO newTripTicketDTO = tripTicketService.createTripTicket(tripTicketDTO);
            return new ResponseEntity<>(newTripTicketDTO, HttpStatus.CREATED);
        }

    }


    @RequestMapping(value = {"/{id}"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripTicketDTO> updateTripTicket(@PathVariable("id") int id,
                                                          @Valid @RequestBody TripTicketDTO tripTicketDTO) {
        TripTicketDTO currentTicketDTO = tripTicketService.findTripTicketByTripTicketId(id);
        if (currentTicketDTO == null) {
            log.error("#updateTripTicket tripTicketId not found [" + id + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        TripTicketDTO updatedTicketDTO = tripTicketService.updateTripTicket(tripTicketDTO);
        return new ResponseEntity<>(updatedTicketDTO, HttpStatus.OK);
    }


    @RequestMapping(value = {"/partialUpdate/{id}"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripTicketDTO> updatePartialTripTicket(@PathVariable("id") int id,
                                                                 @Valid @RequestBody TicketUpdateRequest updateRequest) {
        TripTicketDTO currentTicketDTO = tripTicketService.findTripTicketByTripTicketId(id);
        if (currentTicketDTO == null) {
            log.error("#updateTripTicket tripTicketId not found [" + id + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // customer name fields
        if ( updateRequest.getCustomer_first_name() != null ) {
            currentTicketDTO.setCustomerFirstName(updateRequest.getCustomer_first_name());
        }
        if ( updateRequest.getCustomer_last_name() != null ) {
            currentTicketDTO.setCustomerLastName(updateRequest.getCustomer_last_name());
        }

        // seats required (integer)
        if ( updateRequest.getCustomer_seats_required() != null ) {
            try {
                String seats = updateRequest.getCustomer_seats_required();
                if (StringUtils.hasText(seats)) {
                    currentTicketDTO.setCustomerSeatsRequired(Integer.valueOf(seats));
                }
            } catch (NumberFormatException nfe) {
                log.warn("Unable to parse customer_seats_required: {}", updateRequest.getCustomer_seats_required());
            }
        }

        // trip notes
        if ( updateRequest.getTrip_notes() != null ) {
            currentTicketDTO.setTripNotes(updateRequest.getTrip_notes());
        }

        // requested pickup date
        if ( updateRequest.getRequested_pickup_date() != null ) {
            try {
                currentTicketDTO.setRequestedPickupDate(LocalDate.parse(updateRequest.getRequested_pickup_date()));
            } catch (Exception e) {
                log.warn("Unable to parse requested_pickup_date: {}", updateRequest.getRequested_pickup_date());
            }
        }

        // requested pickup time
        if ( updateRequest.getRequested_pickup_time() != null ) {
            try {
                String t = normalizeTimeString(updateRequest.getRequested_pickup_time());
                currentTicketDTO.setRequestedPickupTime(Time.valueOf(t));
            } catch (Exception e) {
                log.warn("Unable to parse requested_pickup_time: {}", updateRequest.getRequested_pickup_time());
            }
        }

        // requested dropoff date
        if ( updateRequest.getRequested_dropoff_date() != null ) {
            try {
                currentTicketDTO.setRequestedDropoffDate(LocalDate.parse(updateRequest.getRequested_dropoff_date()));
            } catch (Exception e) {
                log.warn("Unable to parse requested_dropoff_date: {}", updateRequest.getRequested_dropoff_date());
            }
        }

        // requested dropoff time
        if ( updateRequest.getRequested_dropoff_time() != null ) {
            try {
                String t = normalizeTimeString(updateRequest.getRequested_dropoff_time());
                currentTicketDTO.setRequestedDropOffTime(Time.valueOf(t));
            } catch (Exception e) {
                log.warn("Unable to parse requested_dropoff_time: {}", updateRequest.getRequested_dropoff_time());
            }
        }

        // status id
        if ( updateRequest.getStatus_id() != null ) {
            try {
                String s = updateRequest.getStatus_id();
                if (StringUtils.hasText(s)) {
                    StatusDTO statusDTO = new StatusDTO();
                    statusDTO.setStatusId(Integer.parseInt(s));
                    currentTicketDTO.setStatus(statusDTO);
                }
            } catch (NumberFormatException nfe) {
                log.warn("Unable to parse status_id: {}", updateRequest.getStatus_id());
            }
        }


        TripTicketDTO updatedTicketDTO = tripTicketService.updateTripTicket(currentTicketDTO);
        return new ResponseEntity<>(updatedTicketDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {
            "/{id}/rescind"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripTicketDTO> rescindTripTicket(@PathVariable("id") int id) {
        TripTicketDTO currentTicketDTO = tripTicketService.findTripTicketByTripTicketId(id);
        if (currentTicketDTO == null) {
            log.error("#updateTripTicketStatusTo rescind tripTicketId not found [" + id + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        TripTicketDTO updatedTicketDTO = tripTicketService.rescindTripTicket(id);
        return new ResponseEntity<>(updatedTicketDTO, HttpStatus.OK);
    }


    @RequestMapping(value = {
            "/{id}/cancel"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TripTicketDTO> cancelTripTicket(@PathVariable("id") int id, @RequestBody CancelRequest cancelRequest) {;
        TripTicket availableTripTicket = tripTicketService.getTripTicketByTripTicketId(id);
        if (availableTripTicket == null) {
            log.error("#updateTripTicketStatusTo rescind tripTicketId not found [" + id + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.debug("Cancelling trip id " + id + " with reason " + cancelRequest.reason());

        if (availableTripTicket.getStatus().getStatusId() != TripTicketStatusConstants.noShow.tripTicketStatusUpdate()) {

            if ( tripTicketService.isUberRide(id) ) {
                cancelUberTripTicketService.cancelTripTicket(id);
            }

            tripTicketService.changeTripTicketStatusToCancel(availableTripTicket, cancelRequest);
        } else {
            log.error("Cancelling trip id " + id + " failed. Ticket is already no show.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = {"/{id}"}, method = RequestMethod.DELETE)
    public ResponseEntity<Boolean> deleteTripTicket(@PathVariable("id") int id) {
        var entity = tripTicketService.getTripTicketByTripTicketId(id);
        if (entity == null) {
            log.error("#deletetrip TripTicketId not found [" + id + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            tripTicketService.deleteTripTickeByTripTicketId(id);
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
    }

    @RequestMapping(value = {"/{id}"}, method = RequestMethod.PUT)
    public ResponseEntity<TripTicketDTO> updateTripTicketByTripTicketId(@PathVariable("id") int id, @RequestBody TripTicketDTO tripTicketPayload) {
        TripTicketDTO updatedTicket = new TripTicketDTO();
        if (tripTicketPayload.isTripCancel()) {
            TripTicket availableTripTicket = tripTicketService.getTripTicketByTripTicketId(id);
            if (availableTripTicket.getStatus().getStatusId() != TripTicketStatusConstants.noShow.tripTicketStatusUpdate()) {
                if ( tripTicketService.isUberRide(id) ) {
                    cancelUberTripTicketService.cancelTripTicket(id);
                }
                tripTicketService.changeTripTicketStatusToCancel(availableTripTicket, null);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } else {
            TripTicketDTO tripTicketDTO = tripTicketService.findTripTicketByTripTicketId(id);
            if (tripTicketDTO == null) {
                log.error("#tripTicketId not found [" + id + "]");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                updatedTicket = tripTicketService.updateFundingSource(tripTicketDTO,
                        tripTicketPayload.getTripFundersList());
                return new ResponseEntity<>(updatedTicket, HttpStatus.OK);
            }
        }
        return null;

    }

    // newly added by shankar for Azure api integration
    @RequestMapping(value = {
            "/getTripticketCostForProvider/{tripticketId}/claimantProviderId/{providerId}"}, method = RequestMethod.GET)
    public ResponseEntity<ProviderTripCostDTO> getTimeAndDistance(@PathVariable("tripticketId") int tripticketId,
                                                                  @PathVariable("providerId") int providerId) {

        var tripticketCost = tripTicketService.getTripticketCostForProvider(tripticketId, providerId);
        return new ResponseEntity<>(tripticketCost, HttpStatus.OK);
    }

    @RequestMapping(value = {
            "/listOfCustomerEligibility"}, method = RequestMethod.GET)
    public ResponseEntity<List<String>> listOfDistinctCustomerEligibility() {
        List<String> listOfAllDistinctCustomerEligibility = tripTicketService.listOfDistinctCustomerEligibility();
        if (listOfAllDistinctCustomerEligibility.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(listOfAllDistinctCustomerEligibility, HttpStatus.OK);
    }

    @RequestMapping(value = "/trip_tickets_create_api_adapter", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createTripTicketForAdapterByJson(
            @Valid @RequestBody TripTicketRequestDTO tripTicketRequestDTO, BindingResult result) {
        TripTicketDTO newTripTicketDTO = processAdapterTripTicket(tripTicketRequestDTO, result);
        return new ResponseEntity<>(newTripTicketDTO, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/updateAdapterSync", method = RequestMethod.PUT)
    public ResponseEntity<String> listAllDeatiedTripTickets(@RequestBody InputDTO inputDTO) {
        //	Map<String, String[]> parameterMap = request.getParameterMap();
        String message = tripTicketService
                .updatedLastSyncDateAndTripStatus(inputDTO);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @RequestMapping(value = "/uploadTripTickets", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createTripTicketJsonCollection(
            @Valid @RequestBody List<TripTicketRequestDTO> tripTicketRequestDTOList, BindingResult result) {
        List<TripTicketDTO> createdTripTickets = new ArrayList<>();

        for (TripTicketRequestDTO tripTicketRequestDTO : tripTicketRequestDTOList) {
            TripTicketDTO newTripTicketDTO = processAdapterTripTicket(tripTicketRequestDTO, result);
            createdTripTickets.add(newTripTicketDTO);
        }

        return new ResponseEntity<>(createdTripTickets, HttpStatus.CREATED);
    }

    /**
     * Shared helper to process a single TripTicketRequestDTO from the adapter endpoints.
     * It performs conversion, validation, create/update logic, distance/time checks, and
     * returns the created/updated TripTicketDTO or throws appropriate exceptions.
     */
    private TripTicketDTO processAdapterTripTicket(TripTicketRequestDTO tripTicketRequestDTO, BindingResult result) {

        TripTicketDTO tripTicketDTO;

        if ( tripTicketRequestDTO.isComplete() ) {
            // convert to a TripResultDTO and process completion logic
            var tripResultDTO = tripResultService.createTripResultDTO(tripTicketRequestDTO);
            if ( tripResultDTO == null ) {
                throw new InvalidInputException("Unable to convert to TripResultDTO for completion of trip with origin trip ID " + tripTicketRequestDTO.getOriginTripId());
            }
            return new TripTicketDTO();
        }

        // Convert incoming request DTO into the internal TripTicketDTO and capture any binding/validation results
        Map<String, Object> resultMap = convertRequestToTripTicketDTOService.convertRequestJsonToTicketDTO(tripTicketRequestDTO, result);
        tripTicketDTO = (TripTicketDTO) resultMap.get("tripTicketDTO");
        @SuppressWarnings("unchecked")
        List<String> bindingResultList = (List<String>) resultMap.get("bindingResultList");

        if (bindingResultList != null && !bindingResultList.isEmpty()) {
            tripTicketService.sendMailToOriginatorForInvalidInput(bindingResultList, tripTicketDTO.getOriginProviderId());
            throw new InvalidInputCheckException(bindingResultList, tripTicketDTO.getOriginProviderId());
        }

        TripTicketDTO newTripTicketDTO;
        boolean checkForTicketExists = tripTicketService.checkForTicketExistsByTripId(tripTicketDTO);
        if (checkForTicketExists) {
            TripTicket availableTripTicket = tripTicketService.getTripTicketByTripTicketId(tripTicketDTO.getId());

            if (availableTripTicket.getOriginProvider().getProviderId() != tripTicketDTO.getOriginProviderId()
                    && !Objects.equals(availableTripTicket.getRequesterTripId(), tripTicketDTO.getRequesterTripId())) {
                throw new HandlingExceptionForOKStatus("Claimant OriginTrip Id = " + tripTicketDTO.getRequesterTripId() + " is mapped successfully to Hub tripTicketId = " + availableTripTicket.getId());
            } else {
                // checking for trip cancellation
                if (availableTripTicket.getStatus().getStatusId() == TripTicketStatusConstants.noShow.tripTicketStatusUpdate()) {
                    throw new InvalidInputException("Can not update the ticket with origin ticket id "
                            + tripTicketDTO.getRequesterTripId() + " as it has been cancelled.");
                }
                if (tripTicketDTO.getStatus() != null) {
                    if (tripTicketDTO.getStatus().getStatusId() == TripTicketStatusConstants.cancelled.tripTicketStatusUpdate()) {
                        tripTicketService.changedTripTicketStatusToNoShow(availableTripTicket);
                    }
                }
                if (availableTripTicket.getStatus().getStatusId() == TripTicketStatusConstants.noShow.tripTicketStatusUpdate()) {
                    // sending ACK mail to requester and claimant provider for cancel trip
                    tripTicketService.sendACKMailToOriginatorForTripCancellation(availableTripTicket);

                    throw new HandlingExceptionForOKStatus("ticket with origin ticket id "
                            + tripTicketDTO.getRequesterTripId() + " is cancelled.");
                }
                newTripTicketDTO = tripTicketService.getUpdatedTripTicket(availableTripTicket, tripTicketDTO);
            }
        } else {
            // handling exception for duplicate requesterTripId for origin provider
            boolean isTicketExist = tripTicketService.checkForTicketExistsWithRequesterTripIdForProvider(tripTicketDTO);
            if (isTicketExist) {
                throw new HandlingExceptionForOKStatus(
                        "Ticket is already created for this Provider with trip id = " + tripTicketDTO.getRequesterTripId());
            }

            // if tripticket not present with requesterTripId then create new tripticket
            TripTicketDistanceDTO timeAndDistanceDTO = tripTicketDistanceService.checkDistanceTime(tripTicketDTO);
            if (timeAndDistanceDTO == null) {
                throw new InvalidInputException(
                        "The Lat-Long is invalid, Ticket is not created. Please try again.");
            }
            newTripTicketDTO = tripTicketService.createTripTicket(tripTicketDTO);
            // newly added to save distance and time using azure api
            tripTicketDistanceService.saveDistanceTime(newTripTicketDTO, timeAndDistanceDTO);
        }

        // No parsing needed - the date is already a Date object
        try {
            if (newTripTicketDTO != null && newTripTicketDTO.getRequestedPickupDate() != null) {
                tripTicketDTO.setRequestedPickupDate(newTripTicketDTO.getRequestedPickupDate());
            }
        } catch (Exception e) {
            log.error("Exception in processAdapterTripTicket: {}", e.getMessage(), e);
        }

        // customerService.createCustomerByTripTicketDTO(newTripTicketDTO);
        if (newTripTicketDTO != null) {
            newTripTicketDTO.setCustomerStatusForDuplication(tripTicketDTO.getCustomerStatusForDuplication());
        }
        return newTripTicketDTO;
    }

    @RequestMapping(value = "/{tripTicketId}/rideStatus", method = RequestMethod.GET)
    public ResponseEntity<TripSummary> currentRideStatus(@PathVariable ("tripTicketId") int tripTicketId) {
        log.debug("Received request to get current ride status for trip ticket ID: {}",  tripTicketId);
        var tripTicket = tripTicketService.findTripTicketByTripTicketId(tripTicketId);

        // If the trip ticket doesn't exist, return 404
        if (tripTicket == null) {
            log.warn("TripTicket not found for id {}", tripTicketId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // If the trip exists but isn't an Uber ride, return 400 (bad request) with clear log
        if (!tripTicketService.isUberRide(tripTicketId)) {
            log.info("TripTicket {} exists but is not an Uber ride.", tripTicketId, tripTicket.getProvisionalProviderId());
            var summary = TripSummary.builder()
                    .build();
            return new ResponseEntity<>(summary, HttpStatus.OK);
        } else {
            try {
                var tripSummary = uberService.getTripSummary(tripTicket.getCommonTripId());
                return new ResponseEntity<>(tripSummary, HttpStatus.OK);
            } catch (Exception e) {
                log.error("Error fetching ride status from Uber: {}", e.getMessage(), e);
                return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
            }

        }


    }


    @RequestMapping(value = "/activeClaimedTrips", method = RequestMethod.GET)
    public ResponseEntity<List<DetailedTripTicketDTO>> activeClaimedTrips() {
        var downloadRequest = new TripTicketDownloadRequest();
        var tickets = tripTicketService.findTicketsByIds(downloadRequest);
        if (tickets.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        // Populates trip notes with any comments
        for ( var ticket : tickets ) {
            ticket.extractComments();
        }
        return new ResponseEntity<>(tickets, HttpStatus.OK);
    }



    @RequestMapping(value = "/generate-test-data", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> generateTestData() {
        log.info("Received request to generate test trip ticket data");

        int generatedCount = tripTicketDataService.generateTestTripTickets();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Successfully generated test trip tickets");
        response.put("count", generatedCount);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Helper to normalize incoming time strings to HH:mm:ss which java.sql.Time.valueOf expects
    private static String normalizeTimeString(String timeStr) {
        if (timeStr == null) return null;
        String s = timeStr.trim();

        // If the client sends a date-time (contains 'T'), take the time portion after 'T'
        if (s.contains("T")) {
            s = s.substring(s.indexOf('T') + 1);
        }

        // Remove trailing Z (UTC) if present
        if (s.endsWith("Z")) {
            s = s.substring(0, s.length() - 1);
        }

        // Remove timezone offsets like +00:00 or -0700
        s = s.replaceAll("([+\\-]\\d{2}:?\\d{2})$", "");

        // Remove fractional seconds (e.g., .123 or .123Z)
        int dot = s.indexOf('.');
        if (dot >= 0) {
            s = s.substring(0, dot);
        }

        // Find a HH:mm or HH:mm:ss substring
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{1,2}:\\d{2}(?::\\d{2})?)").matcher(s);
        if (m.find()) {
            String found = m.group(1);
            // If only HH:mm, append :00
            if (found.matches("^\\d{1,2}:\\d{2}$")) {
                found = found + ":00";
            }
            // Ensure hour is two digits
            String[] parts = found.split(":");
            if (parts[0].length() == 1) {
                parts[0] = "0" + parts[0];
            }
            return parts[0] + ":" + parts[1] + ":" + parts[2];
        }

        // Fallback: if input already looks like HH:mm or HH:mm:ss try to normalize simple cases
        if (s.matches("^\\d{1,2}:\\d{2}$")) return (s.length() == 4 ? "0" + s : s) + ":00";
        if (s.matches("^\\d{1,2}:\\d{2}:\\d{2}$")) {
            // pad hour if needed
            if (s.length() == 7) return "0" + s;
            return s;
        }

        // As last resort, return the trimmed string and let caller handle parse errors
        return s;
    }


}
