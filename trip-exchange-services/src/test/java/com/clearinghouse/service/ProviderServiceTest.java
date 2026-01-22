package com.clearinghouse.service;

import com.clearinghouse.dao.ProviderDAO;
import com.clearinghouse.dao.UserNotificationDataDAO;
import com.clearinghouse.dto.ProviderDTO;
import com.clearinghouse.entity.Provider;
import com.clearinghouse.entity.User;
import com.clearinghouse.exceptions.ProviderExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProviderServiceTest {

    @Mock
    private ProviderDAO providerDAO;

    @Mock
    private UserNotificationDataDAO userNotificationDataDAO;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper providerModelMapper;

    @InjectMocks
    private ProviderService providerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAllProviders_ReturnsAllProviders() {
        List<Provider> providers = List.of(new Provider(), new Provider());
        when(providerDAO.findAllProviders()).thenReturn(providers);
        when(providerModelMapper.map(any(Provider.class), eq(ProviderDTO.class))).thenReturn(new ProviderDTO());

        List<ProviderDTO> result = providerService.findAllProviders();

        assertEquals(providers.size(), result.size());
        verify(providerDAO).findAllProviders();
    }

    @Test
    void findProviderByProviderId_ReturnsProvider() {
        int providerId = 1;
        Provider provider = new Provider();
        when(providerDAO.findProviderByProviderId(providerId)).thenReturn(provider);
        when(providerModelMapper.map(provider, ProviderDTO.class)).thenReturn(new ProviderDTO());

        ProviderDTO result = providerService.findProviderByProviderId(providerId);

        assertNotNull(result);
        verify(providerDAO).findProviderByProviderId(providerId);
    }

    @Test
    void findProviderByProviderId_ReturnsNullIfNotFound() {
        int providerId = 1;
        when(providerDAO.findProviderByProviderId(providerId)).thenReturn(null);

        ProviderDTO result = providerService.findProviderByProviderId(providerId);

        assertNull(result);
        verify(providerDAO).findProviderByProviderId(providerId);
    }

    @Test
    void activateProvider_ActivatesProviderAndUsers() {
        int providerId = 1;
        Provider provider = new Provider();
        provider.setIsActive(false);
        List<User> users = List.of(new User(), new User());
        when(providerDAO.findProviderByProviderId(providerId)).thenReturn(provider);
        when(userNotificationDataDAO.getUsersOfProvider(providerId)).thenReturn(users);
        when(providerModelMapper.map(provider, ProviderDTO.class)).thenReturn(new ProviderDTO());

        ProviderDTO result = providerService.activateProvider(providerId);

        assertTrue(provider.isActive());
        verify(providerDAO).updateProvider(provider);
        verify(userService, times(users.size())).updateUserForAccountActivation(anyInt());
        assertNotNull(result);
    }

    @Test
    void deactivateProvider_DeactivatesProviderAndUsers() {
        int providerId = 1;
        Provider provider = new Provider();
        provider.setIsActive(true);
        List<User> users = List.of(new User(), new User());
        when(providerDAO.findProviderByProviderId(providerId)).thenReturn(provider);
        when(userNotificationDataDAO.getUsersOfProvider(providerId)).thenReturn(users);
        when(providerModelMapper.map(provider, ProviderDTO.class)).thenReturn(new ProviderDTO());

        ProviderDTO result = providerService.deactivateProvider(providerId);

        assertFalse(provider.isActive());
        verify(providerDAO).updateProvider(provider);
        verify(userService, times(users.size())).updateUserForAccountDeactivation(anyInt());
        assertNotNull(result);
    }

    @Test
    void createProvider_ThrowsExceptionIfEmailExists() {
        ProviderDTO providerDTO = new ProviderDTO();
        providerDTO.setContactEmail("test@example.com");
        when(providerDAO.findProviderByEmail(providerDTO.getContactEmail())).thenReturn(true);

        assertThrows(ProviderExistsException.class, () -> providerService.createProvider(providerDTO));
        verify(providerDAO).findProviderByEmail(providerDTO.getContactEmail());
    }

    @Test
    void createProvider_CreatesNewProvider() {
        ProviderDTO providerDTO = new ProviderDTO();
        providerDTO.setContactEmail("test@example.com");
        Provider provider = new Provider();
        when(providerDAO.findProviderByEmail(providerDTO.getContactEmail())).thenReturn(false);
        when(providerModelMapper.map(providerDTO, Provider.class)).thenReturn(provider);
        when(providerModelMapper.map(provider, ProviderDTO.class)).thenReturn(providerDTO);

        ProviderDTO result = providerService.createProvider(providerDTO);

        assertNotNull(result);
        assertTrue(provider.isActive());
        verify(providerDAO).createProvider(provider);
    }

    @Test
    void updateProvider_UpdatesProvider() {
        ProviderDTO providerDTO = new ProviderDTO();
        Provider provider = new Provider();
        when(providerModelMapper.map(providerDTO, Provider.class)).thenReturn(provider);
        when(providerModelMapper.map(provider, ProviderDTO.class)).thenReturn(providerDTO);

        ProviderDTO result = providerService.updateProvider(providerDTO);

        assertNotNull(result);
        verify(providerDAO).updateProvider(provider);
    }

    @Test
    void deleteProviderByProviderId_DeletesProvider() {
        int providerId = 1;

        boolean result = providerService.deleteProviderByProviderId(providerId);

        assertTrue(result);
        verify(providerDAO).deleteProviderByProviderId(providerId);
    }
}