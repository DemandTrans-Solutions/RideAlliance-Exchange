package com.clearinghouse.dao;

import com.clearinghouse.entity.ProviderPartner;
import com.clearinghouse.entity.Provider;
import com.clearinghouse.enumentity.ProviderPartnerStatusConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ProviderPartnerDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<ProviderPartner> typedQuery;

    @Spy
    @InjectMocks
    private ProviderPartnerDAO providerPartnerDAO;

    private ProviderPartner mockProviderPartner;
    private Provider requesterProvider;
    private Provider coordinatorProvider;

    @BeforeEach
    void setUp() {
        requesterProvider = new Provider();
        requesterProvider.setProviderId(1);
        requesterProvider.setProviderName("Requester Provider");

        coordinatorProvider = new Provider();
        coordinatorProvider.setProviderId(2);
        coordinatorProvider.setProviderName("Coordinator Provider");

        mockProviderPartner = new ProviderPartner();
        mockProviderPartner.setProviderPartnerId(1);
        mockProviderPartner.setRequesterProvider(requesterProvider);
        mockProviderPartner.setCoordinatorProvider(coordinatorProvider);
        mockProviderPartner.setIsActive(true);

        when(entityManager.find(ProviderPartner.class, 1)).thenReturn(mockProviderPartner);
        
        // Setup ProviderPartnerDAO to return the EntityManager
        doReturn(entityManager).when(providerPartnerDAO).getEntityManager();
    }

    @Test
    void findAllProviderPartners_ShouldReturnProviderPartnerList() {
        // Arrange
        List<ProviderPartner> expectedPartners = Arrays.asList(mockProviderPartner);
        when(entityManager.createQuery(anyString())).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedPartners);

        // Act
        List<ProviderPartner> result = providerPartnerDAO.findAllProviderPartners();

        // Assert
        assertNotNull(result);
        assertEquals(expectedPartners.size(), result.size());
        assertEquals(expectedPartners.get(0), result.get(0));
        verify(entityManager).createQuery(contains("SELECT p FROM ProviderPartner p"));
    }

    @Test
    void findProviderPartnerByProviderPartnerId_ShouldReturnProviderPartner() {
        // Act
        ProviderPartner result = providerPartnerDAO.findProviderPartnerByProviderPartnerId(1);

        // Assert
        assertNotNull(result);
        assertEquals(mockProviderPartner.getProviderPartnerId(), result.getProviderPartnerId());
        verify(entityManager).find(ProviderPartner.class, 1);
    }

    @Test
    void createProviderPartner_ShouldPersistProviderPartner() {
        // Act
        ProviderPartner result = providerPartnerDAO.createProviderPartner(mockProviderPartner);

        // Assert
        assertNotNull(result);
        assertEquals(mockProviderPartner, result);
        verify(entityManager).persist(mockProviderPartner);
    }

    @Test
    void updateProviderPartner_ShouldUpdateAndReturnProviderPartner() {
        // Arrange
        when(entityManager.merge(mockProviderPartner)).thenReturn(mockProviderPartner);

        // Act
        ProviderPartner result = providerPartnerDAO.updateProviderPartner(mockProviderPartner);

        // Assert
        assertNotNull(result);
        assertEquals(mockProviderPartner, result);
        verify(entityManager).merge(mockProviderPartner);
    }

    @Test
    void providerPartnershipCheck_ShouldReturnTrueWhenPartnershipExists() {
        // Arrange
        List<ProviderPartner> partnerList = Arrays.asList(mockProviderPartner);
        when(entityManager.createQuery(anyString())).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(partnerList);

        // Act
        boolean result = providerPartnerDAO.providerPartnershipCheck(1, 2);

        // Assert
        assertTrue(result);
        verify(entityManager, times(2)).createQuery(anyString());
        // Don't verify exact number of parameter settings
        verify(typedQuery, atLeastOnce()).setParameter(anyString(), any());
        verify(typedQuery, times(2)).getResultList();
    }

    @Test
    void providerPartnershipCheck_ShouldReturnFalseWhenNoPartnership() {
        // Arrange
        List<ProviderPartner> emptyList = Arrays.asList();
        when(entityManager.createQuery(anyString())).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(emptyList);

        // Act
        boolean result = providerPartnerDAO.providerPartnershipCheck(1, 2);

        // Assert
        assertFalse(result);
        verify(entityManager, times(2)).createQuery(anyString());
        // Don't verify exact number of parameter settings
        verify(typedQuery, atLeastOnce()).setParameter(anyString(), any());
        verify(typedQuery, times(2)).getResultList();
    }
}