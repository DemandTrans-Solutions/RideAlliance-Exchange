package com.clearinghouse.dao;

import com.clearinghouse.entity.Provider;
import com.clearinghouse.exceptionentity.ProviderEmailExistsExceptionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ProviderDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Provider> providerQuery;

    @Mock
    private TypedQuery<ProviderEmailExistsExceptionEntity> emailExistsQuery;

    @InjectMocks
    private ProviderDAO providerDAO;

    private Provider mockProvider;

    @BeforeEach
    void setUp() {
        mockProvider = new Provider();
        mockProvider.setProviderId(1);
        mockProvider.setProviderName("Test Provider");
        mockProvider.setContactEmail("test@example.com");
        when(entityManager.find(Provider.class, 1)).thenReturn(mockProvider);
    }

    @Test
    void findAllProviders_ShouldReturnProviderList() {
        // Arrange
        List<Provider> expectedProviders = Arrays.asList(mockProvider);
        when(entityManager.createQuery(anyString())).thenReturn(providerQuery);
        when(providerQuery.setParameter("id", 1)).thenReturn(providerQuery);
        when(providerQuery.getResultList()).thenReturn(expectedProviders);

        // Act
        List<Provider> result = providerDAO.findAllProviders();

        // Assert
        assertNotNull(result);
        assertEquals(expectedProviders.size(), result.size());
        assertEquals(expectedProviders.get(0), result.get(0));
        verify(entityManager).createQuery("SELECT p FROM Provider p where p.providerId != :id order by providerName  ");
    }

    @Test
    void findProviderByProviderId_ShouldReturnProvider() {
        // Act
        Provider result = providerDAO.findProviderByProviderId(1);

        // Assert
        assertNotNull(result);
        assertEquals(mockProvider.getProviderId(), result.getProviderId());
        verify(entityManager).find(Provider.class, 1);
    }

    @Test
    void createProvider_ShouldPersistProvider() {
        // Act
        Provider result = providerDAO.createProvider(mockProvider);

        // Assert
        assertNotNull(result);
        assertEquals(mockProvider, result);
        verify(entityManager).persist(mockProvider);
    }

    @Test
    void updateProvider_ShouldUpdateAndReturnProvider() {
        // Arrange
        when(entityManager.merge(mockProvider)).thenReturn(mockProvider);

        // Act
        Provider result = providerDAO.updateProvider(mockProvider);

        // Assert
        assertNotNull(result);
        assertEquals(mockProvider, result);
        verify(entityManager).merge(mockProvider);
    }

    @Test
    void deleteProviderByProviderId_ShouldDeactivateProvider() {
        // Arrange
        when(entityManager.createQuery(anyString())).thenReturn(providerQuery);
        when(providerQuery.setParameter("providerId", 1)).thenReturn(providerQuery);
        when(providerQuery.getSingleResult()).thenReturn(mockProvider);

        // Act
        providerDAO.deleteProviderByProviderId(1);

        // Assert
        assertFalse(mockProvider.isActive());
        verify(entityManager).createQuery("SELECT p FROM Provider p WHERE p.providerId = :providerId");
        verify(providerQuery).setParameter("providerId", 1);
    }

    @Test
    void findProviderByEmail_ShouldReturnTrueWhenEmailExists() {
        // Arrange
        String email = "test@example.com";
        ProviderEmailExistsExceptionEntity emailEntity = new ProviderEmailExistsExceptionEntity(email);
        List<ProviderEmailExistsExceptionEntity> emailList = Arrays.asList(emailEntity);
        
        when(entityManager.createQuery(anyString(), eq(ProviderEmailExistsExceptionEntity.class))).thenReturn(emailExistsQuery);
        when(emailExistsQuery.getResultList()).thenReturn(emailList);

        // Act
        boolean result = providerDAO.findProviderByEmail(email);

        // Assert
        assertTrue(result);
        verify(emailExistsQuery).getResultList();
    }

    @Test
    void findProviderByEmail_ShouldReturnFalseWhenEmailDoesNotExist() {
        // Arrange
        String email = "nonexistent@example.com";
        ProviderEmailExistsExceptionEntity emailEntity = new ProviderEmailExistsExceptionEntity("different@example.com");
        List<ProviderEmailExistsExceptionEntity> emailList = Arrays.asList(emailEntity);
        
        when(entityManager.createQuery(anyString(), eq(ProviderEmailExistsExceptionEntity.class))).thenReturn(emailExistsQuery);
        when(emailExistsQuery.getResultList()).thenReturn(emailList);

        // Act
        boolean result = providerDAO.findProviderByEmail(email);

        // Assert
        assertFalse(result);
        verify(emailExistsQuery).getResultList();
    }
}