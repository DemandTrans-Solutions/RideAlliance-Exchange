package com.clearinghouse.dao;

import com.clearinghouse.entity.Notification;
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
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class NotificationDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Notification> typedQuery;

    @InjectMocks
    private NotificationDAO notificationDAO;

    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        mockNotification = new Notification();
        mockNotification.setNotificationId(1);
        mockNotification.setStatusId(1);
        mockNotification.setNumberOfAttempts(0);
        when(entityManager.find(Notification.class, 1)).thenReturn(mockNotification);
    }

    @Test
    void findAllNotificationForNewAndErrorStatus_ShouldReturnNotificationList() {
        // Arrange
        List<Notification> expectedNotifications = Arrays.asList(mockNotification);
        when(entityManager.createQuery(anyString())).thenReturn(typedQuery);
        // DAO uses the typed createQuery with Notification.class; mock both overloads
        when(entityManager.createQuery(anyString(), eq(Notification.class))).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(10)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedNotifications);

        // Act
        List<Notification> result = notificationDAO.findAllNotificationForNewAndErrorStatus();

        // Assert
        assertNotNull(result);
        assertEquals(expectedNotifications.size(), result.size());
        assertEquals(expectedNotifications.get(0), result.get(0));
    // Verify DAO uses the typed createQuery with the exact JPQL string
    verify(entityManager).createQuery("SELECT n FROM Notification n where (statusId=1 OR (statusId=4 AND numberOfAttempts<3)) ", Notification.class);
        verify(typedQuery).setMaxResults(10);
        verify(typedQuery).getResultList();
    }

    @Test
    void findNotificationByNotificationId_ShouldReturnNotification() {
        // Act
        Notification result = notificationDAO.findNotificationByNotificationId(1);

        // Assert
        assertNotNull(result);
        assertEquals(mockNotification.getNotificationId(), result.getNotificationId());
        verify(entityManager).find(Notification.class, 1);
    }

    @Test
    void createNotification_ShouldPersistNotification() {
        // Act
        Notification result = notificationDAO.createNotification(mockNotification);

        // Assert
        assertNotNull(result);
        verify(entityManager).persist(mockNotification);
    }

    @Test
    void updateNotification_ShouldUpdateAndReturnNotification() {
        // Arrange
        when(entityManager.merge(mockNotification)).thenReturn(mockNotification);

        // Act
        Notification result = notificationDAO.updateNotification(mockNotification);

        // Assert
        assertNotNull(result);
        assertEquals(mockNotification, result);
        verify(entityManager).merge(mockNotification);
    }

    @Test
    void deleteUserByUserId_ShouldDeactivateNotification() {
        // Arrange
        int notificationId = 1;
        when(entityManager.createQuery(anyString())).thenReturn(typedQuery);
        when(typedQuery.setParameter("notificationId", notificationId)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(mockNotification);

        // Act
        notificationDAO.deleteUserByUserId(notificationId);

        // Assert
        assertFalse(mockNotification.isActive());
    // DAO uses lowercase 'notificationId' property name in JPQL
    verify(entityManager).createQuery("SELECT n FROM Notification n WHERE n.notificationId = :notificationId");
        verify(typedQuery).setParameter("notificationId", notificationId);
        verify(typedQuery).getSingleResult();
    }

    @Test
    void findNotificationByNotificationId_WithInvalidId_ShouldReturnNull() {
        // Arrange
        when(entityManager.find(Notification.class, 999)).thenReturn(null);

        // Act
        Notification result = notificationDAO.findNotificationByNotificationId(999);

        // Assert
        assertNull(result);
        verify(entityManager).find(Notification.class, 999);
    }
}