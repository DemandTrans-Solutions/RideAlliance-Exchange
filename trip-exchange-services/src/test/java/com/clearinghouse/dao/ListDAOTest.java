package com.clearinghouse.dao;

import com.clearinghouse.entity.Provider;
import com.clearinghouse.listresponseentity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class ListDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<ProviderList> providerListQuery;

    @Mock
    private TypedQuery<RoleList> roleListQuery;

    @Mock
    private TypedQuery<ServiceAreaList> serviceAreaListQuery;

    @Mock
    private TypedQuery<StatusList> statusListQuery;

    @Mock
    private TypedQuery<AddressListBO> addressListQuery;

    @InjectMocks
    private ListDAO listDAO;

    @BeforeEach
    void setUp() {
        // Common setup for queries
        when(entityManager.createQuery(anyString(), eq(ProviderList.class))).thenReturn(providerListQuery);
        when(entityManager.createQuery(anyString(), eq(RoleList.class))).thenReturn(roleListQuery);
        when(entityManager.createQuery(anyString(), eq(ServiceAreaList.class))).thenReturn(serviceAreaListQuery);
        when(entityManager.createQuery(anyString(), eq(StatusList.class))).thenReturn(statusListQuery);
        when(entityManager.createQuery(anyString(), eq(AddressListBO.class))).thenReturn(addressListQuery);
    }

    @Test
    void getAllProviders_ShouldReturnProviderList() {
        // Arrange
        ProviderList provider = new ProviderList(1, "Test Provider");
        List<ProviderList> expectedList = Arrays.asList(provider);
        when(providerListQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<ProviderList> result = listDAO.getAllProviders();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList.size(), result.size());
        assertEquals(expectedList.get(0).getProviderId(), result.get(0).getProviderId());
        assertEquals(expectedList.get(0).getProviderName(), result.get(0).getProviderName());
    }

    @Test
    void getAllProvidersByProviderLogin_ShouldReturnFilteredList() {
        // Arrange
        int providerId = 1;
        ProviderList provider = new ProviderList(providerId, "Test Provider");
        List<ProviderList> expectedList = Arrays.asList(provider);
        when(providerListQuery.setParameter("providerId", providerId)).thenReturn(providerListQuery);
        when(providerListQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<ProviderList> result = listDAO.getAllProvidersByProviderLogin(providerId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedList.size(), result.size());
        assertEquals(providerId, result.get(0).getProviderId());
    }

    @Test
    void getAllListRoles_ShouldReturnRoleList() {
        // Arrange
        RoleList role = new RoleList(1, "ADMIN");
        List<RoleList> expectedList = Arrays.asList(role);
        when(roleListQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<RoleList> result = listDAO.getAllListRoles();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList.size(), result.size());
        assertEquals(expectedList.get(0).getRoleId(), result.get(0).getRoleId());
        assertEquals(expectedList.get(0).getRoleName(), result.get(0).getRoleName());
    }

    @Test
    void getAllListServiceAreas_ShouldReturnServiceAreaList() {
        // Arrange
        ServiceAreaList serviceArea = new ServiceAreaList(1, "Test Area");
        List<ServiceAreaList> expectedList = Arrays.asList(serviceArea);
        when(serviceAreaListQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<ServiceAreaList> result = listDAO.getAllListServiceAreas();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList.size(), result.size());
        assertEquals(expectedList.get(0).getServiceAreaId(), result.get(0).getServiceAreaId());
        assertEquals(expectedList.get(0).getDescription(), result.get(0).getDescription());
    }

    @Test
    void getAllListAddress_ShouldReturnAddressList() {
        // Arrange
        String addressWord = "Test";
        AddressListBO address = new AddressListBO(1, "Street1", "Street2", "City", "County", "State", "12345", "123-456-7890");
        List<AddressListBO> expectedList = Arrays.asList(address);
        when(addressListQuery.setParameter(eq("addressWord"), contains(addressWord))).thenReturn(addressListQuery);
        when(addressListQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<AddressListBO> result = listDAO.getAllListAddress(addressWord);

        // Assert
        assertNotNull(result);
        assertEquals(expectedList.size(), result.size());
        assertEquals(expectedList.get(0).getAddressId(), result.get(0).getAddressId());
    }

    @Test
    void getAllListAddress_WithComma_ShouldHandleSpecialCase() {
        // Arrange
        String addressWord = "Street, City";
        AddressListBO address = new AddressListBO(1, "Street", null, "City", null, "State", "12345", null);
        List<AddressListBO> expectedList = Arrays.asList(address);
        when(addressListQuery.setParameter(eq("addressWord"), contains("Street"))).thenReturn(addressListQuery);
        when(addressListQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<AddressListBO> result = listDAO.getAllListAddress(addressWord);

        // Assert
        assertNotNull(result);
        assertEquals(expectedList.size(), result.size());
    }

    @Test
    void getProviderListObjByProviderId_ShouldReturnSingleProvider() {
        // Arrange
        int providerId = 1;
        ProviderList expectedProvider = new ProviderList(providerId, "Test Provider");
        when(providerListQuery.setParameter("providerId", providerId)).thenReturn(providerListQuery);
        when(providerListQuery.getSingleResult()).thenReturn(expectedProvider);

        // Act
        ProviderList result = listDAO.getProviderListObjByProviderId(providerId);

        // Assert
        assertNotNull(result);
        assertEquals(providerId, result.getProviderId());
        assertEquals(expectedProvider.getProviderName(), result.getProviderName());
    }
}