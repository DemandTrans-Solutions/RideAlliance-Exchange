package com.clearinghouse.service;

import com.clearinghouse.dao.ListDAO;
import com.clearinghouse.listresponseentity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ListServiceTest {

    @Mock
    private ListDAO listDao;

    @InjectMocks
    private ListService listService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllProviders() {
        List<ProviderList> providers = new ArrayList<>();
        providers.add(new ProviderList());
        providers.add(new ProviderList());

        when(listDao.getAllProviders()).thenReturn(providers);

        List<ProviderList> result = listService.getAllProviders();

        assertEquals(providers.size(), result.size());
        verify(listDao).getAllProviders();
    }

    @Test
    void testGetAllProvidersByProviderLogin() {
        int providerId = 1;
        List<ProviderList> providers = new ArrayList<>();
        providers.add(new ProviderList());

        when(listDao.getAllProvidersByProviderLogin(providerId)).thenReturn(providers);

        List<ProviderList> result = listService.getAllProvidersByProviderLogin(providerId);

        assertEquals(providers.size(), result.size());
        verify(listDao).getAllProvidersByProviderLogin(providerId);
    }

    @Test
    void testGetAllListRoles() {
        List<RoleList> roles = new ArrayList<>();
        roles.add(new RoleList());

        when(listDao.getAllListRoles()).thenReturn(roles);

        List<RoleList> result = listService.getAllListRoles();

        assertEquals(roles.size(), result.size());
        verify(listDao).getAllListRoles();
    }

    @Test
    void testGetAllListServiceAreas() {
        List<ServiceAreaList> serviceAreas = new ArrayList<>();
        serviceAreas.add(new ServiceAreaList());

        when(listDao.getAllListServiceAreas()).thenReturn(serviceAreas);

        List<ServiceAreaList> result = listService.getAllListServiceAreas();

        assertEquals(serviceAreas.size(), result.size());
        verify(listDao).getAllListServiceAreas();
    }

    @Test
    void testGetAllListProviderPartners() {
        int providerId = 1;
        List<ProviderPartnerList> partners = new ArrayList<>();
        partners.add(new ProviderPartnerList());

        when(listDao.getAllListProviderPartners(providerId)).thenReturn(partners);

        List<ProviderPartnerList> result = listService.getAllListProviderPartners(providerId);

        assertEquals(partners.size(), result.size());
        verify(listDao).getAllListProviderPartners(providerId);
    }

    @Test
    void testGetAllListStatus() {
        List<StatusList> statuses = new ArrayList<>();
        statuses.add(new StatusList());

        when(listDao.getAllListStatus()).thenReturn(statuses);

        List<StatusList> result = listService.getAllListStatus();

        assertEquals(statuses.size(), result.size());
        verify(listDao).getAllListStatus();
    }

    @Test
    void testGetAllListTicketFilters() {
        int userId = 1;
        List<TicketFilterList> filters = new ArrayList<>();
        filters.add(new TicketFilterList());

        when(listDao.getAllListTicketFilters(userId)).thenReturn(filters);

        List<TicketFilterList> result = listService.getAllListTicketFilters(userId);

        assertEquals(filters.size(), result.size());
        verify(listDao).getAllListTicketFilters(userId);
    }

    @Test
    void testGetAllListAddress() {
        String addressWord = "test";
        List<AddressListBO> addressListBOs = new ArrayList<>();
        AddressListBO addressListBO = new AddressListBO();
        addressListBO.setAddressId(1);
        addressListBO.setStreet1("Street1");
        addressListBO.setStreet2("Street2");
        addressListBO.setCity("City");
        addressListBO.setCounty("County");
        addressListBO.setState("State");
        addressListBO.setZipcode("12345");
        addressListBO.setPhoneNumber("1234567890");
        addressListBOs.add(addressListBO);

        when(listDao.getAllListAddress(addressWord)).thenReturn(addressListBOs);

        List<AddressListByString> result = listService.getAllListAddress(addressWord);

        assertEquals(1, result.size());
        assertEquals("Street1,Street2,City,County,State,12345,1234567890", result.get(0).getAddress());
        verify(listDao).getAllListAddress(addressWord);
    }

    @Test
    void testGetAllListOriginatingProvidersByProviderId() {
        int providerId = 1;
        List<ProviderList> providers = new ArrayList<>();
        providers.add(new ProviderList());

        when(listDao.getOriginatorProviderListByProviderId(providerId)).thenReturn(providers);

        List<ProviderList> result = listService.getAllListOriginatingProvidersByProviderId(providerId);

        assertEquals(providers.size(), result.size());
        verify(listDao).getOriginatorProviderListByProviderId(providerId);
    }

    @Test
    void testGetAllListClaimingProvidersByProviderId() {
        int providerId = 1;
        List<ProviderList> providers = new ArrayList<>();
        providers.add(new ProviderList());

        when(listDao.getClaimantProviderListByProviderId(providerId)).thenReturn(providers);

        List<ProviderList> result = listService.getAllListClaimingProvidersByProviderId(providerId);

        assertEquals(providers.size(), result.size());
        verify(listDao).getClaimantProviderListByProviderId(providerId);
    }
}