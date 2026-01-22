package com.clearinghouse.controller.rest;

import com.clearinghouse.listresponseentity.*;
import com.clearinghouse.service.ListService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ListControllerTest {

    @Mock
    private ListService listService;

    @InjectMocks
    private ListController listController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllListProviders() {
        List<ProviderList> providers = new ArrayList<>();
        providers.add(new ProviderList());
        when(listService.getAllProviders()).thenReturn(providers);

        ResponseEntity<List<ProviderList>> response = listController.getAllListProviders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(providers, response.getBody());
        verify(listService).getAllProviders();
    }

    @Test
    void testGetAllListProvidersByProviderLogin() {
        int providerId = 1;
        List<ProviderList> providers = new ArrayList<>();
        providers.add(new ProviderList());
        when(listService.getAllProvidersByProviderLogin(providerId)).thenReturn(providers);

        ResponseEntity<List<ProviderList>> response = listController.getAllListProvidersByProviderLogin(providerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(providers, response.getBody());
        verify(listService).getAllProvidersByProviderLogin(providerId);
    }

    @Test
    void testGetAllListRoles() {
        List<RoleList> roles = new ArrayList<>();
        roles.add(new RoleList());
        when(listService.getAllListRoles()).thenReturn(roles);

        ResponseEntity<List<RoleList>> response = listController.getAllListRoles();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(roles, response.getBody());
        verify(listService).getAllListRoles();
    }

    @Test
    void testGetAllListServiceAreas() {
        List<ServiceAreaList> serviceAreas = new ArrayList<>();
        serviceAreas.add(new ServiceAreaList());
        when(listService.getAllListServiceAreas()).thenReturn(serviceAreas);

        ResponseEntity<List<ServiceAreaList>> response = listController.getAllListServiceAreas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(serviceAreas, response.getBody());
        verify(listService).getAllListServiceAreas();
    }

    @Test
    void testGetAllListProviderPartners() {
        int providerId = 1;
        List<ProviderPartnerList> providerPartners = new ArrayList<>();
        providerPartners.add(new ProviderPartnerList());
        when(listService.getAllListProviderPartners(providerId)).thenReturn(providerPartners);

        ResponseEntity<List<ProviderPartnerList>> response = listController.getAllListProviderPartners(providerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(providerPartners, response.getBody());
        verify(listService).getAllListProviderPartners(providerId);
    }

    @Test
    void testGetAllListTicketFilters() {
        int userId = 1;
        List<TicketFilterList> ticketFilters = new ArrayList<>();
        ticketFilters.add(new TicketFilterList());
        when(listService.getAllListTicketFilters(userId)).thenReturn(ticketFilters);

        ResponseEntity<List<TicketFilterList>> response = listController.getAllListTicketFilters(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ticketFilters, response.getBody());
        verify(listService).getAllListTicketFilters(userId);
    }

    @Test
    void testGetAllListServiceList() {
        List<StatusList> statuses = new ArrayList<>();
        statuses.add(new StatusList());
        when(listService.getAllListStatus()).thenReturn(statuses);

        ResponseEntity<List<StatusList>> response = listController.getAllListServiceList();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(statuses, response.getBody());
        verify(listService).getAllListStatus();
    }

    @Test
    void testGetAllAddressListByString() {
        String addressWord = "test";
        List<AddressListByString> addresses = new ArrayList<>();
        addresses.add(new AddressListByString());
        when(listService.getAllListAddress(addressWord)).thenReturn(addresses);

        ResponseEntity<List<AddressListByString>> response = listController.getAllAddressListByString(addressWord);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(addresses, response.getBody());
        verify(listService).getAllListAddress(addressWord);
    }

    @Test
    void testGetAllListOriginatingProvidersByProviderId() {
        int providerId = 1;
        List<ProviderList> providers = new ArrayList<>();
        providers.add(new ProviderList());
        when(listService.getAllListOriginatingProvidersByProviderId(providerId)).thenReturn(providers);

        ResponseEntity<List<ProviderList>> response = listController.getAllListOriginatingProvidersByProviderId(providerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(providers, response.getBody());
        verify(listService).getAllListOriginatingProvidersByProviderId(providerId);
    }

    @Test
    void testGetAllListClaimingProvidersByProviderId() {
        int providerId = 1;
        List<ProviderList> providers = new ArrayList<>();
        providers.add(new ProviderList());
        when(listService.getAllListClaimingProvidersByProviderId(providerId)).thenReturn(providers);

        ResponseEntity<List<ProviderList>> response = listController.getAllListClaimingProvidersByProviderId(providerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(providers, response.getBody());
        verify(listService).getAllListClaimingProvidersByProviderId(providerId);
    }
}