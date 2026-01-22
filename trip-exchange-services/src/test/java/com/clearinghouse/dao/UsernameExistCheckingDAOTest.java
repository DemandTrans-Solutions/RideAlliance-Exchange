package com.clearinghouse.dao;

import com.clearinghouse.exceptionentity.EmailExistCheckingEntity;
import com.clearinghouse.exceptionentity.UsernameExistCheckingEntity;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class UsernameExistCheckingDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<UsernameExistCheckingEntity> usernameQuery;

    @Mock
    private TypedQuery<EmailExistCheckingEntity> emailQuery;

    @Spy
    @InjectMocks
    private UsernameExistCheckingDAO usernameExistCheckingDAO;

    private UsernameExistCheckingEntity mockUsernameCheck;
    private EmailExistCheckingEntity mockEmailCheck;

    @BeforeEach
    void setUp() {
        mockUsernameCheck = new UsernameExistCheckingEntity("testuser");
        mockEmailCheck = new EmailExistCheckingEntity("test@example.com");
        
        // Setup UsernameExistCheckingDAO to return the EntityManager
        doReturn(entityManager).when(usernameExistCheckingDAO).getEntityManager();
    }

    @Test
    void findUserByUsername_ShouldReturnTrue_WhenUsernameExists() {
        // Arrange
        List<UsernameExistCheckingEntity> expectedChecks = Arrays.asList(mockUsernameCheck);
        when(entityManager.createQuery(anyString(), eq(UsernameExistCheckingEntity.class))).thenReturn(usernameQuery);
        when(usernameQuery.setParameter(anyString(), anyString())).thenReturn(usernameQuery);
        when(usernameQuery.getResultList()).thenReturn(expectedChecks);

        // Act
        boolean result = usernameExistCheckingDAO.findUserByUsername("testuser");

        // Assert
        assertTrue(result);
        verify(entityManager).createQuery(anyString(), eq(UsernameExistCheckingEntity.class));
        verify(usernameQuery).getResultList();
    }

    @Test
    void findUserByUsername_ShouldReturnFalse_WhenUsernameDoesNotExist() {
        // Arrange
        List<UsernameExistCheckingEntity> emptyList = Collections.emptyList();
        when(entityManager.createQuery(anyString(), eq(UsernameExistCheckingEntity.class))).thenReturn(usernameQuery);
        when(usernameQuery.setParameter(anyString(), anyString())).thenReturn(usernameQuery);
        when(usernameQuery.getResultList()).thenReturn(emptyList);

        // Act
        boolean result = usernameExistCheckingDAO.findUserByUsername("nonexistent");

        // Assert
        assertFalse(result);
        verify(entityManager).createQuery(anyString(), eq(UsernameExistCheckingEntity.class));
        verify(usernameQuery).getResultList();
    }

    @Test
    void findUserByEmail_ShouldReturnTrue_WhenEmailExists() {
        // Arrange
        List<EmailExistCheckingEntity> expectedChecks = Arrays.asList(mockEmailCheck);
        when(entityManager.createQuery(anyString(), eq(EmailExistCheckingEntity.class))).thenReturn(emailQuery);
        when(emailQuery.setParameter(anyString(), anyString())).thenReturn(emailQuery);
        when(emailQuery.getResultList()).thenReturn(expectedChecks);

        // Act
        boolean result = usernameExistCheckingDAO.findUserByEmail("test@example.com");

        // Assert
        assertTrue(result);
        verify(entityManager).createQuery(anyString(), eq(EmailExistCheckingEntity.class));
        verify(emailQuery).getResultList();
    }

    @Test
    void findUserByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        // Arrange
        List<EmailExistCheckingEntity> emptyList = Collections.emptyList();
        when(entityManager.createQuery(anyString(), eq(EmailExistCheckingEntity.class))).thenReturn(emailQuery);
        when(emailQuery.setParameter(anyString(), anyString())).thenReturn(emailQuery);
        when(emailQuery.getResultList()).thenReturn(emptyList);

        // Act
        boolean result = usernameExistCheckingDAO.findUserByEmail("nonexistent@example.com");

        // Assert
        assertFalse(result);
        verify(entityManager).createQuery(anyString(), eq(EmailExistCheckingEntity.class));
        verify(emailQuery).getResultList();
    }

    @Test
    void findUserByUsername_ShouldHandleNullUsername() {
        // Act
        boolean result = usernameExistCheckingDAO.findUserByUsername(null);

        // Assert
        assertFalse(result);
        verify(entityManager, never()).createQuery(anyString(), any());
    }

    @Test
    void findUserByEmail_ShouldHandleNullEmail() {
        // Act
        boolean result = usernameExistCheckingDAO.findUserByEmail(null);

        // Assert
        assertFalse(result);
        verify(entityManager, never()).createQuery(anyString(), any());
    }
}