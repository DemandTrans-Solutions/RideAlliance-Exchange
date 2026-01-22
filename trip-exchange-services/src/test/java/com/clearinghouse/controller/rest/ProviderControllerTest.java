package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.ProviderDTO;
import com.clearinghouse.service.ProviderService;
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

class ProviderControllerTest {

    @Mock
    private ProviderService providerService;

    @InjectMocks
    private ProviderController providerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListAllProviders() {
        List<ProviderDTO> providers = new ArrayList<>();
        providers.add(new ProviderDTO());
        when(providerService.findAllProviders()).thenReturn(providers);

        ResponseEntity<List<ProviderDTO>> response = providerController.listAllProviders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(providers, response.getBody());
        verify(providerService).findAllProviders();
    }

    @Test
    void testListAllProviders_NoContent() {
        when(providerService.findAllProviders()).thenReturn(new ArrayList<>());

        ResponseEntity<List<ProviderDTO>> response = providerController.listAllProviders();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(providerService).findAllProviders();
    }

    @Test
    void testGetProviderById() {
        int providerId = 1;
        ProviderDTO provider = new ProviderDTO();
        when(providerService.findProviderByProviderId(providerId)).thenReturn(provider);

        ResponseEntity<ProviderDTO> response = providerController.getProviderById(providerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(provider, response.getBody());
        verify(providerService).findProviderByProviderId(providerId);
    }

    @Test
    void testGetProviderById_NotFound() {
        int providerId = 1;
        when(providerService.findProviderByProviderId(providerId)).thenReturn(null);

        ResponseEntity<ProviderDTO> response = providerController.getProviderById(providerId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(providerService).findProviderByProviderId(providerId);
    }

    @Test
    void testCreateProvider() {
        ProviderDTO provider = new ProviderDTO();
        ProviderDTO createdProvider = new ProviderDTO();
        when(providerService.createProvider(provider)).thenReturn(createdProvider);

        ResponseEntity<ProviderDTO> response = providerController.createProvider(provider);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdProvider, response.getBody());
        verify(providerService).createProvider(provider);
    }

    @Test
    void testUpdateProvider() {
        int providerId = 1;
        ProviderDTO provider = new ProviderDTO();
        ProviderDTO currentProvider = new ProviderDTO();
        ProviderDTO updatedProvider = new ProviderDTO();
        when(providerService.findProviderByProviderId(providerId)).thenReturn(currentProvider);
        when(providerService.updateProvider(provider)).thenReturn(updatedProvider);

        ResponseEntity<ProviderDTO> response = providerController.updateProvider(providerId, provider);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedProvider, response.getBody());
        verify(providerService).findProviderByProviderId(providerId);
        verify(providerService).updateProvider(provider);
    }

    @Test
    void testUpdateProvider_NotFound() {
        int providerId = 1;
        ProviderDTO provider = new ProviderDTO();
        when(providerService.findProviderByProviderId(providerId)).thenReturn(null);

        ResponseEntity<ProviderDTO> response = providerController.updateProvider(providerId, provider);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(providerService).findProviderByProviderId(providerId);
        verify(providerService, never()).updateProvider(provider);
    }

    @Test
    void testActivateProvider() {
        int providerId = 1;
        ProviderDTO currentProvider = new ProviderDTO();
        ProviderDTO activatedProvider = new ProviderDTO();
        when(providerService.findProviderByProviderId(providerId)).thenReturn(currentProvider);
        when(providerService.activateProvider(providerId)).thenReturn(activatedProvider);

        ResponseEntity<ProviderDTO> response = providerController.activateProvider(providerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(activatedProvider, response.getBody());
        verify(providerService).findProviderByProviderId(providerId);
        verify(providerService).activateProvider(providerId);
    }

    @Test
    void testActivateProvider_NotFound() {
        int providerId = 1;
        when(providerService.findProviderByProviderId(providerId)).thenReturn(null);

        ResponseEntity<ProviderDTO> response = providerController.activateProvider(providerId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(providerService).findProviderByProviderId(providerId);
        verify(providerService, never()).activateProvider(providerId);
    }

    @Test
    void testDeactivateProvider() {
        int providerId = 1;
        ProviderDTO currentProvider = new ProviderDTO();
        ProviderDTO deactivatedProvider = new ProviderDTO();
        when(providerService.findProviderByProviderId(providerId)).thenReturn(currentProvider);
        when(providerService.deactivateProvider(providerId)).thenReturn(deactivatedProvider);

        ResponseEntity<ProviderDTO> response = providerController.deactivateProvider(providerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(deactivatedProvider, response.getBody());
        verify(providerService).findProviderByProviderId(providerId);
        verify(providerService).deactivateProvider(providerId);
    }

    @Test
    void testDeactivateProvider_NotFound() {
        int providerId = 1;
        when(providerService.findProviderByProviderId(providerId)).thenReturn(null);

        ResponseEntity<ProviderDTO> response = providerController.deactivateProvider(providerId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(providerService).findProviderByProviderId(providerId);
        verify(providerService, never()).deactivateProvider(providerId);
    }

    @Test
    void testDeleteProvider() {
        int providerId = 1;
        when(providerService.findProviderByProviderId(providerId)).thenReturn(new ProviderDTO());
        when(providerService.deleteProviderByProviderId(providerId)).thenReturn(true);

        ResponseEntity<Boolean> response = providerController.deleteProvider(providerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
        verify(providerService).findProviderByProviderId(providerId);
        verify(providerService).deleteProviderByProviderId(providerId);
    }

    @Test
    void testDeleteProvider_NotFound() {
        int providerId = 1;
        when(providerService.findProviderByProviderId(providerId)).thenReturn(null);

        ResponseEntity<Boolean> response = providerController.deleteProvider(providerId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(providerService).findProviderByProviderId(providerId);
        verify(providerService, never()).deleteProviderByProviderId(providerId);
    }
}