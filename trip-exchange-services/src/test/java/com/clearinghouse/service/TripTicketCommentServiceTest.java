package com.clearinghouse.service;

import com.clearinghouse.dao.*;
import com.clearinghouse.dto.ActivityDTO;
import com.clearinghouse.dto.TripTicketCommentDTO;
import com.clearinghouse.entity.*;
import com.clearinghouse.enumentity.TripTicketStatusConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TripTicketCommentServiceTest {

    @Mock
    private TripTicketCommentDAO tripTicketCommentDAO;

    @Mock
    private TripTicketDAO tripTicketDAO;

    @Mock
    private TripClaimService tripClaimService;

    @Mock
    private UserDAO userDAO;

    @Mock
    private ListDAO listDAO;

    @Mock
    private ActivityService activityService;

    @Mock
    private ProviderDAO providerDAO;

    @Mock
    private UserNotificationDataDAO userNotificationDataDAO;

    @Mock
    private NotificationDAO notificationDAO;

    @Mock
    private ModelMapper tripTicketCommentModelMapper;

    @InjectMocks
    private TripTicketCommentService tripTicketCommentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAllTripTicketCommetstByTripTicketId_ReturnsComments() {
        int tripTicketId = 1;
        List<TripTicketComment> comments = List.of(new TripTicketComment(), new TripTicketComment());
        when(tripTicketCommentDAO.findAllTripTicketCommentsByTripTicketId(tripTicketId)).thenReturn(comments);
        when(tripTicketCommentModelMapper.map(any(TripTicketComment.class), eq(TripTicketCommentDTO.class)))
                .thenReturn(new TripTicketCommentDTO());

        List<TripTicketCommentDTO> result = tripTicketCommentService.findAllTripTicketCommetstByTripTicketId(tripTicketId);

        assertEquals(comments.size(), result.size());
        verify(tripTicketCommentDAO).findAllTripTicketCommentsByTripTicketId(tripTicketId);
    }

    @Test
    void findTripTicketCommentById_ReturnsComment() {
        int commentId = 1;
        TripTicketComment comment = new TripTicketComment();
        when(tripTicketCommentDAO.findTripTicketCommentById(commentId)).thenReturn(comment);
        when(tripTicketCommentModelMapper.map(comment, TripTicketCommentDTO.class)).thenReturn(new TripTicketCommentDTO());

        TripTicketCommentDTO result = tripTicketCommentService.findTripTicketCommentById(commentId);

        assertNotNull(result);
        verify(tripTicketCommentDAO).findTripTicketCommentById(commentId);
    }

    @Test
    void createTripTicketComment_CreatesComment() {
        int tripTicketId = 1;
        var provider = new Provider();
        provider.setProviderId(1);
        TripTicketCommentDTO commentDTO = new TripTicketCommentDTO();
        commentDTO.setId(1);
        TripTicket tripTicket = new TripTicket();
        tripTicket.setId(1);
        tripTicket.setOriginProvider(provider);


        TripTicketComment comment = new TripTicketComment();
        comment.setId(1);
        comment.setTripTicket(tripTicket);
        User user = new User();
        user.setProvider(new Provider());

        var status = new Status();
        status.setStatusId(TripTicketStatusConstants.claimPending.tripTicketStatusUpdate());
        tripTicket.setStatus(status);

        when(userDAO.findUserByUserId(commentDTO.getUserId())).thenReturn(user);
        when(tripTicketCommentDAO.getProviderName(anyInt())).thenReturn("ProviderName");
        when(tripTicketCommentModelMapper.map(commentDTO, TripTicketComment.class)).thenReturn(comment);
        when(tripTicketDAO.findTripTicketByTripTicketId(tripTicketId)).thenReturn(tripTicket);
        when(tripClaimService.updateTicketForClaimAction(tripTicketId)).thenReturn("success");
        when(tripTicketCommentModelMapper.map(comment, TripTicketCommentDTO.class)).thenReturn(commentDTO);

        TripTicketCommentDTO result = tripTicketCommentService.createTripTicketComment(tripTicketId, commentDTO);

        assertNotNull(result);
        verify(tripTicketCommentDAO).createTripTicketComment(comment);
        verify(activityService).createActivity(any(ActivityDTO.class));
    }

    @Test
    void updateTripTicketComment_UpdatesComment() {
        int tripTicketId = 1;
        int commentId = 1;
        TripTicketCommentDTO commentDTO = new TripTicketCommentDTO();
        TripTicketComment comment = new TripTicketComment();
        when(tripTicketCommentModelMapper.map(commentDTO, TripTicketComment.class)).thenReturn(comment);
        when(tripTicketCommentModelMapper.map(comment, TripTicketCommentDTO.class)).thenReturn(commentDTO);

        TripTicketCommentDTO result = tripTicketCommentService.updateTripTicketComment(tripTicketId, commentDTO, commentId);

        assertNotNull(result);
        verify(tripTicketCommentDAO).updateTripTicketComment(comment);
    }

    @Test
    void createActivityForTripTicketComment_CreatesActivity() {
        TripTicketCommentDTO commentDTO = new TripTicketCommentDTO();
        commentDTO.setTripTicketId(1);
        commentDTO.setBody("Test comment");
        commentDTO.setNameOfProvider("ProviderName");

        tripTicketCommentService.createActivityForTripTicketComment(commentDTO);

        verify(activityService).createActivity(any(ActivityDTO.class));
    }
}