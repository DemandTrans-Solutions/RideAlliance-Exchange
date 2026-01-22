package com.clearinghouse.dao;

import com.clearinghouse.entity.Service;
import com.clearinghouse.entity.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.Query;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ServiceAreaDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Service> typedQuery;

    @Mock
    private TypedQuery<Long> longQuery;

    @Mock
    private Query nativeQuery;

    @Spy
    @InjectMocks
    private ServiceAreaDAO serviceAreaDAO;

    private Service mockService;
    private Provider mockProvider;

    @BeforeEach
    void setUp() {
        mockProvider = new Provider();
        mockProvider.setProviderId(1);
        mockProvider.setProviderName("Test Provider");

        mockService = new Service();
        mockService.setServiceId(1);
        mockService.setServiceName("Test Service");
        mockService.setProvider(mockProvider);
        mockService.setIsActive(true);

        when(entityManager.find(Service.class, 1)).thenReturn(mockService);
        
        // Setup ServiceAreaDAO to return the EntityManager
        doReturn(entityManager).when(serviceAreaDAO).getEntityManager();
    }





}