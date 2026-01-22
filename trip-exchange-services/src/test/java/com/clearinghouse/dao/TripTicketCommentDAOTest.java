package com.clearinghouse.dao;

import com.clearinghouse.entity.TripTicketComment;
import com.clearinghouse.entity.TripTicket;
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
class TripTicketCommentDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<TripTicketComment> commentQuery;

    @Mock
    private TypedQuery<String> stringQuery;
    
    @Mock
    private Query query;

    @Spy
    @InjectMocks
    private TripTicketCommentDAO tripTicketCommentDAO;

    private TripTicketComment mockComment;
    private TripTicket mockTicket;
    private Provider mockProvider;

    @BeforeEach
    void setUp() {
        mockProvider = new Provider();
        mockProvider.setProviderId(1);
        mockProvider.setProviderName("Test Provider");

        mockTicket = new TripTicket();
        mockTicket.setId(1);
        mockTicket.setOriginProvider(mockProvider);

        mockComment = new TripTicketComment();
        mockComment.setId(1);
        mockComment.setTripTicket(mockTicket);

        when(entityManager.find(TripTicketComment.class, 1)).thenReturn(mockComment);
        
        // Setup TripTicketCommentDAO to return the EntityManager
        doReturn(entityManager).when(tripTicketCommentDAO).getEntityManager();
    }

    @Test
    void findAllTripTicketCommentsByTripTicketId_ShouldReturnCommentList() {
        // Arrange
        List<TripTicketComment> expectedComments = Arrays.asList(mockComment);
    when(entityManager.createQuery(anyString())).thenReturn(query);
    // DAO uses parameter name "trip_ticket_id"
    when(query.setParameter("trip_ticket_id", 1)).thenReturn(query);
        when(query.getResultList()).thenReturn(expectedComments);

        // Act
        List<TripTicketComment> result = tripTicketCommentDAO.findAllTripTicketCommentsByTripTicketId(1);

        // Assert
        assertNotNull(result);
        assertEquals(expectedComments.size(), result.size());
        assertEquals(expectedComments.get(0), result.get(0));
        // DAO builds the query using TripTicketComment with alias tc
        verify(entityManager).createQuery(contains("TripTicketComment"));
        verify(query).setParameter("trip_ticket_id", 1);
        verify(query).getResultList();
    }

    @Test
    void findTripTicketCommentById_ShouldReturnComment() {
        // Arrange: DAO uses createQuery for this method, stub it
        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter("id", 1)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(mockComment);

        // Act
        TripTicketComment result = tripTicketCommentDAO.findTripTicketCommentById(1);

        // Assert
        assertNotNull(result);
        assertEquals(mockComment.getId(), result.getId());
        verify(entityManager).createQuery(contains("TripTicketComment"));
        verify(query).setParameter("id", 1);
        verify(query).getSingleResult();
    }

    @Test
    void createTripTicketComment_ShouldPersistComment() {
        // Act
        TripTicketComment result = tripTicketCommentDAO.createTripTicketComment(mockComment);

        // Assert
        assertNotNull(result);
        assertEquals(mockComment, result);
        verify(entityManager).persist(mockComment);
    }

    @Test
    void updateTripTicketComment_ShouldUpdateAndReturnComment() {
        // Arrange
        when(entityManager.merge(mockComment)).thenReturn(mockComment);

        // Act
        TripTicketComment result = tripTicketCommentDAO.updateTripTicketComment(mockComment);

        // Assert
        assertNotNull(result);
        assertEquals(mockComment, result);
        verify(entityManager).merge(mockComment);
    }

    @Test
    void getProviderName_ShouldReturnProviderName() {
        // Arrange
        when(entityManager.createQuery(anyString())).thenReturn(query);
        // DAO uses parameter name "id" for provider queries
        when(query.setParameter("id", 1)).thenReturn(query);
        // Return a Provider instance (DAO casts the query result to Provider)
        Provider provider = new Provider();
        provider.setProviderId(1);
        provider.setProviderName("Test Provider");
        when(query.getSingleResult()).thenReturn(provider);

        // Act
        String result = tripTicketCommentDAO.getProviderName(1);

        // Assert
        assertEquals("Test Provider", result);
        verify(entityManager).createQuery(contains("Provider"));
        verify(query).setParameter("id", 1);
        verify(query).getSingleResult();
    }

    @Test
    void findTripTicketCommentById_WithInvalidId_ShouldReturnNull() {
        // Arrange
        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter("id", 999)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(null);

        // Act
        TripTicketComment result = tripTicketCommentDAO.findTripTicketCommentById(999);

        // Assert
        assertNull(result);
        verify(entityManager).createQuery(contains("TripTicketComment"));
        verify(query).setParameter("id", 999);
        verify(query).getSingleResult();
    }
}