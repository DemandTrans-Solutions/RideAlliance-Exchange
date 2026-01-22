package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.ProviderPartnerDTO;
import com.clearinghouse.service.ProviderPartnerService;
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

class ProviderPartnerControllerTest {

    @Mock
    private ProviderPartnerService providerPartnerService;

    @InjectMocks
    private ProviderPartnerController providerPartnerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListAllProviderPartners() {
        List<ProviderPartnerDTO> providerPartners = new ArrayList<>();
        providerPartners.add(new ProviderPartnerDTO());
        when(providerPartnerService.findAllProviderPartners()).thenReturn(providerPartners);

        ResponseEntity<List<ProviderPartnerDTO>> response = providerPartnerController.listAllProviderPartners();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(providerPartners, response.getBody());
        verify(providerPartnerService).findAllProviderPartners();
    }

    @Test
    void testListAllProviderPartners_NoContent() {
        when(providerPartnerService.findAllProviderPartners()).thenReturn(new ArrayList<>());

        ResponseEntity<List<ProviderPartnerDTO>> response = providerPartnerController.listAllProviderPartners();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(providerPartnerService).findAllProviderPartners();
    }

    @Test
    void testListAllProviderPartnersByRequesterProviderId() {
        int requesterProviderId = 1;
        List<ProviderPartnerDTO> providerPartners = new ArrayList<>();
        providerPartners.add(new ProviderPartnerDTO());
        when(providerPartnerService.findAllProviderPartnersByRequesterProviderId(requesterProviderId)).thenReturn(providerPartners);

        ResponseEntity<List<ProviderPartnerDTO>> response = providerPartnerController.listAllProviderPartnersByRequesterProviderId(requesterProviderId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(providerPartners, response.getBody());
        verify(providerPartnerService).findAllProviderPartnersByRequesterProviderId(requesterProviderId);
    }

    @Test
    void testListAllProviderPartnersByRequesterProviderId_NoContent() {
        int requesterProviderId = 1;
        when(providerPartnerService.findAllProviderPartnersByRequesterProviderId(requesterProviderId)).thenReturn(new ArrayList<>());

        ResponseEntity<List<ProviderPartnerDTO>> response = providerPartnerController.listAllProviderPartnersByRequesterProviderId(requesterProviderId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(providerPartnerService).findAllProviderPartnersByRequesterProviderId(requesterProviderId);
    }

    @Test
    void testGetProviderPartnerByProviderPartnerId() {
        int providerPartnerId = 1;
        ProviderPartnerDTO providerPartner = new ProviderPartnerDTO();
        when(providerPartnerService.findProviderPartnerByProviderPartnerId(providerPartnerId)).thenReturn(providerPartner);

        ResponseEntity<ProviderPartnerDTO> response = providerPartnerController.getProviderPartnerByProviderPartnerId(providerPartnerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(providerPartner, response.getBody());
        verify(providerPartnerService).findProviderPartnerByProviderPartnerId(providerPartnerId);
    }

    @Test
    void testGetProviderPartnerByProviderPartnerId_NotFound() {
        int providerPartnerId = 1;
        when(providerPartnerService.findProviderPartnerByProviderPartnerId(providerPartnerId)).thenReturn(null);

        ResponseEntity<ProviderPartnerDTO> response = providerPartnerController.getProviderPartnerByProviderPartnerId(providerPartnerId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(providerPartnerService).findProviderPartnerByProviderPartnerId(providerPartnerId);
    }

    @Test
    void testCreateProviderPartner() {
        ProviderPartnerDTO providerPartner = new ProviderPartnerDTO();
        ProviderPartnerDTO createdProviderPartner = new ProviderPartnerDTO();
        when(providerPartnerService.createProviderPartner(providerPartner)).thenReturn(createdProviderPartner);

        ResponseEntity<ProviderPartnerDTO> response = providerPartnerController.createProviderPartner(providerPartner);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdProviderPartner, response.getBody());
        verify(providerPartnerService).createProviderPartner(providerPartner);
    }

    @Test
    void testUpdateProviderPartner() {
        int providerPartnerId = 1;
        ProviderPartnerDTO providerPartner = new ProviderPartnerDTO();
        ProviderPartnerDTO currentProviderPartner = new ProviderPartnerDTO();
        ProviderPartnerDTO updatedProviderPartner = new ProviderPartnerDTO();
        when(providerPartnerService.findProviderPartnerByProviderPartnerId(providerPartnerId)).thenReturn(currentProviderPartner);
        when(providerPartnerService.updateProviderPartner(providerPartner)).thenReturn(updatedProviderPartner);

        ResponseEntity<ProviderPartnerDTO> response = providerPartnerController.updateProviderPartner(providerPartnerId, providerPartner);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedProviderPartner, response.getBody());
        verify(providerPartnerService).findProviderPartnerByProviderPartnerId(providerPartnerId);
        verify(providerPartnerService).updateProviderPartner(providerPartner);
    }

    @Test
    void testUpdateProviderPartner_NotFound() {
        int providerPartnerId = 1;
        ProviderPartnerDTO providerPartner = new ProviderPartnerDTO();
        when(providerPartnerService.findProviderPartnerByProviderPartnerId(providerPartnerId)).thenReturn(null);

        ResponseEntity<ProviderPartnerDTO> response = providerPartnerController.updateProviderPartner(providerPartnerId, providerPartner);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(providerPartnerService).findProviderPartnerByProviderPartnerId(providerPartnerId);
        verify(providerPartnerService, never()).updateProviderPartner(providerPartner);
    }

    @Test
    void testDeleteProviderPartner() {
        int providerPartnerId = 1;
        when(providerPartnerService.findProviderPartnerByProviderPartnerId(providerPartnerId)).thenReturn(new ProviderPartnerDTO());
        when(providerPartnerService.deleteProviderpartnerByProviderPartnerId(providerPartnerId)).thenReturn(true);

        ResponseEntity<Boolean> response = providerPartnerController.deleteProviderPartner(providerPartnerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
        verify(providerPartnerService).findProviderPartnerByProviderPartnerId(providerPartnerId);
        verify(providerPartnerService).deleteProviderpartnerByProviderPartnerId(providerPartnerId);
    }

    @Test
    void testDeleteProviderPartner_NotFound() {
        int providerPartnerId = 1;
        when(providerPartnerService.findProviderPartnerByProviderPartnerId(providerPartnerId)).thenReturn(null);

        ResponseEntity<Boolean> response = providerPartnerController.deleteProviderPartner(providerPartnerId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(providerPartnerService).findProviderPartnerByProviderPartnerId(providerPartnerId);
        verify(providerPartnerService, never()).deleteProviderpartnerByProviderPartnerId(providerPartnerId);
    }
}