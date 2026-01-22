/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.controller.rest;

import com.clearinghouse.listresponseentity.*;
import com.clearinghouse.service.ListService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 * @author chaitanyaP
 */
@RestController
@Slf4j
@RequestMapping(value = {"api/list"}, method = RequestMethod.GET)
@AllArgsConstructor
public class ListController {


    private final ListService listService;


    /* This function is for listing providers */
    @RequestMapping(value = {"/providers"})
    public ResponseEntity<List<ProviderList>> getAllListProviders() {
        List<ProviderList> providersList = listService.getAllProviders();
        log.error(String.valueOf(providersList));
        return new ResponseEntity<>(providersList, HttpStatus.OK);
    }

    /* This function is for listing providers */
    @RequestMapping(value = {"/providers/{providerId}"})
    public ResponseEntity<List<ProviderList>> getAllListProvidersByProviderLogin(@PathVariable("providerId") int providerId) {
        List<ProviderList> providersList = listService.getAllProvidersByProviderLogin(providerId);
        log.error(String.valueOf(providersList));
        return new ResponseEntity<>(providersList, HttpStatus.OK);
    }

    /* This function is for listing Roles */
    @RequestMapping(value = {"/roles"})
    public ResponseEntity<List<RoleList>> getAllListRoles() {
        List<RoleList> roleList = listService.getAllListRoles();
        log.error(String.valueOf(roleList));
        return new ResponseEntity<>(roleList, HttpStatus.OK);
    }

    //      /* This function is for listing Titles */
//    @RequestMapping(value = {"/titles"} )
//    public ResponseEntity<List<TitleList>> getAllListTitles() {
//        List<TitleList> titleList = listService.getAllListTitles();
//        log.debug(titleList);
//        return new ResponseEntity<>(titleList, HttpStatus.OK);
//    }
    /* This function is for listing serviceAreas */
    @RequestMapping(value = {"/serviceAreas"})
    public ResponseEntity<List<ServiceAreaList>> getAllListServiceAreas() {
        List<ServiceAreaList> serviceAreaList = listService.getAllListServiceAreas();
        log.debug(String.valueOf(serviceAreaList));
        return new ResponseEntity<>(serviceAreaList, HttpStatus.OK);
    }

    //this will give the list of the provider partner excpet thet logged in providerId
    @RequestMapping(value = {"/providerPartners/{providerId}"})
    public ResponseEntity<List<ProviderPartnerList>> getAllListProviderPartners(@PathVariable("providerId") int providerId) {
        List<ProviderPartnerList> providerPartnerList = listService.getAllListProviderPartners(providerId);
        log.debug(String.valueOf(providerPartnerList));
        return new ResponseEntity<>(providerPartnerList, HttpStatus.OK);
    }

    //this will  give the list for the ticket filters according to the userId  
    @RequestMapping(value = {"/ticketFilters/{userId}"})
    public ResponseEntity<List<TicketFilterList>> getAllListTicketFilters(@PathVariable("userId") int userId) {
        List<TicketFilterList> ticketFilterList = listService.getAllListTicketFilters(userId);
        // Ensure all items have filterName populated
        for (int i = 0; i < ticketFilterList.size(); i++) {
            TicketFilterList filter = ticketFilterList.get(i);
            if (filter.getFilterName() == null || filter.getFilterName().trim().isEmpty()) {
                filter.setFilterName("Filter" + (i + 1));
            }
        }
        log.debug(String.valueOf(ticketFilterList));
        return new ResponseEntity<>(ticketFilterList, HttpStatus.OK);
    }

    //this will give the status list
    @RequestMapping(value = {"/status"})
    public ResponseEntity<List<StatusList>> getAllListServiceList() {
        List<StatusList> statusList = listService.getAllListStatus();
        log.debug(String.valueOf(statusList));
        return new ResponseEntity<>(statusList, HttpStatus.OK);
    }

    //    this will give the address list accrding to the keyword
    @RequestMapping(value = {"/address/{addressWord}"})
    public ResponseEntity<List<AddressListByString>> getAllAddressListByString(@PathVariable("addressWord") String addressWord) {
        List<AddressListByString> addressList = listService.getAllListAddress(addressWord);
        log.debug(String.valueOf(addressList));
        return new ResponseEntity<>(addressList, HttpStatus.OK);
    }

    /* This function is for listing originatingProviders which are providerPartners for filters */
    @RequestMapping(value = {"/originatingProviderPartners/{providerId}"})
    public ResponseEntity<List<ProviderList>> getAllListOriginatingProvidersByProviderId(@PathVariable("providerId") int providerId) {
        List<ProviderList> providersList = listService.getAllListOriginatingProvidersByProviderId(providerId);
        log.debug(String.valueOf(providersList));
        return new ResponseEntity<>(providersList, HttpStatus.OK);
    }

    /* This function is for listing originatingProviders which are providerPartners for filters  */
    @RequestMapping(value = {"/claimingProviderPartners/{providerId}"})
    public ResponseEntity<List<ProviderList>> getAllListClaimingProvidersByProviderId(@PathVariable("providerId") int providerId) {
        List<ProviderList> providersList = listService.getAllListClaimingProvidersByProviderId(providerId);
        log.debug(String.valueOf(providersList));
        return new ResponseEntity<>(providersList, HttpStatus.OK);
    }

}
