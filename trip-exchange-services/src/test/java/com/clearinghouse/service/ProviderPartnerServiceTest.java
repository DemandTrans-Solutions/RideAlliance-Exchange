package com.clearinghouse.service;

import com.clearinghouse.dao.*;
import com.clearinghouse.dto.ProviderPartnerDTO;
import com.clearinghouse.entity.Provider;
import com.clearinghouse.entity.ProviderPartner;
import com.clearinghouse.testutil.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProviderPartnerServiceTest {

    @Mock
    private ProviderPartnerDAO providerPartnerDAO;

    @Mock
    private ModelMapper providerPartnerModelMapper;

    @Mock
    private UserNotificationDataDAO userNotificationDataDAO;

    @Mock
    private NotificationDAO notificationDAO;

    @Mock
    private ProviderDAO providerDAO;

    @InjectMocks
    private ProviderPartnerService providerPartnerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAllProviderPartners() {
        // Arrange
        Provider requester = TestData.provider()
            .withId(1)
            .withName("Requester Transit")
            .build();
            
        Provider coordinator = TestData.provider()
            .withId(2)
            .withName("Coordinator Transit")
            .build();
            
        ProviderPartner partner1 = TestData.providerPartner()
            .withId(1)
            .withPartners(requester, coordinator)
            .build();
            
        ProviderPartner partner2 = TestData.providerPartner()
            .withId(2)
            .withPartners(coordinator, requester)
            .build();
            
        List<ProviderPartner> partners = Arrays.asList(partner1, partner2);

        ProviderPartnerDTO partnerDTO1 = TestData.providerPartner()
            .withId(1)
            .withPartners(requester, coordinator)
            .buildDTO();

        ProviderPartnerDTO partnerDTO2 = TestData.providerPartner()
            .withId(2)
            .withPartners(coordinator, requester)
            .buildDTO();

        when(providerPartnerDAO.findAllProviderPartners()).thenReturn(partners);
        when(providerPartnerModelMapper.map(partner1, ProviderPartnerDTO.class)).thenReturn(partnerDTO1);
        when(providerPartnerModelMapper.map(partner2, ProviderPartnerDTO.class)).thenReturn(partnerDTO2);

        // Act
        List<ProviderPartnerDTO> result = providerPartnerService.findAllProviderPartners();

        // Assert
        assertEquals(2, result.size());
        assertEquals(partnerDTO1.getProviderPartnerId(), result.get(0).getProviderPartnerId());
        assertEquals(partnerDTO2.getProviderPartnerId(), result.get(1).getProviderPartnerId());
        assertEquals(requester.getProviderId(), result.get(0).getRequesterProviderId());
        assertEquals(coordinator.getProviderId(), result.get(0).getCoordinatorProviderId());
        verify(providerPartnerDAO).findAllProviderPartners();
    }

    @Test
    void testCreateProviderPartnership() {
        // Arrange
        Provider requester = TestData.provider()
            .withId(1)
            .withName("Requester Transit")
            .build();
            
        Provider coordinator = TestData.provider()
            .withId(2)
            .withName("Coordinator Transit")
            .build();
            
        ProviderPartner partnership = TestData.providerPartner()
            .withPartners(requester, coordinator)
            .build();
            
        ProviderPartnerDTO partnershipDTO = TestData.providerPartner()
            .withPartners(requester, coordinator)
            .buildDTO();

        when(providerPartnerModelMapper.map(partnershipDTO, ProviderPartner.class)).thenReturn(partnership);
        when(providerPartnerDAO.createProviderPartner(partnership)).thenReturn(partnership);
        when(providerPartnerModelMapper.map(partnership, ProviderPartnerDTO.class)).thenReturn(partnershipDTO);

    // providerDAO is used inside createProviderPartner to fetch coordinator and requester providers
    when(providerDAO.findProviderByProviderId(requester.getProviderId())).thenReturn(requester);
    when(providerDAO.findProviderByProviderId(coordinator.getProviderId())).thenReturn(coordinator);

    // userNotificationDataDAO should return an empty list to avoid NPE while iterating
    when(userNotificationDataDAO.getUsersOfProvider(anyInt())).thenReturn(java.util.Collections.emptyList());

        // Act
        ProviderPartnerDTO result = providerPartnerService.createProviderPartner(partnershipDTO);

        // Assert
        assertNotNull(result);
        assertEquals(requester.getProviderId(), result.getRequesterProviderId());
        assertEquals(coordinator.getProviderId(), result.getCoordinatorProviderId());
        verify(providerPartnerDAO).createProviderPartner(partnership);
    }

    @Test
    void testDeactivatePartnership() {
        // Arrange
        int partnershipId = 1;
        Provider requester = TestData.provider()
            .withId(1)
            .withName("Requester Transit")
            .build();
            
        Provider coordinator = TestData.provider()
            .withId(2)
            .withName("Coordinator Transit")
            .build();
            
        ProviderPartner existingPartnership = TestData.providerPartner()
            .withId(partnershipId)
            .withPartners(requester, coordinator)
            .build();
            
        ProviderPartner deactivatedPartnership = TestData.providerPartner()
            .withId(partnershipId)
            .withPartners(requester, coordinator)
            .asInactive()
            .build();

        ProviderPartnerDTO expectedDTO = TestData.providerPartner()
            .withId(partnershipId)
            .withPartners(requester, coordinator)
            .asInactive()
            .buildDTO();

        when(providerPartnerDAO.findProviderPartnerByProviderPartnerId(partnershipId)).thenReturn(existingPartnership);
        when(providerPartnerDAO.updateProviderPartner(any(ProviderPartner.class))).thenReturn(deactivatedPartnership);
        when(providerPartnerModelMapper.map(deactivatedPartnership, ProviderPartnerDTO.class)).thenReturn(expectedDTO);

        // Act
        ProviderPartnerDTO result = providerPartnerService.deactivateProviderPartner(partnershipId);

        // Assert
        assertNotNull(result);
        assertEquals(partnershipId, result.getProviderPartnerId());
        assertFalse(result.isActive());
        verify(providerPartnerDAO).updateProviderPartner(any(ProviderPartner.class));
    }
}