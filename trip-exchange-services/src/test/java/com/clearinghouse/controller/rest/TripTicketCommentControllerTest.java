package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.TripTicketCommentDTO;
import com.clearinghouse.service.TripTicketCommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TripTicketCommentControllerTest {

    @Mock
    private TripTicketCommentService tripTicketCommentService;

    @InjectMocks
    private TripTicketCommentController tripTicketCommentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListAllTripResultByTripTicketId() {
        int tripTicketId = 1;
        List<TripTicketCommentDTO> comments = new ArrayList<>();
        comments.add(new TripTicketCommentDTO());
        when(tripTicketCommentService.findAllTripTicketCommetstByTripTicketId(tripTicketId)).thenReturn(comments);

        ResponseEntity<List<TripTicketCommentDTO>> response = tripTicketCommentController.listAllTripResultByTripTicketId(tripTicketId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(comments, response.getBody());
        verify(tripTicketCommentService).findAllTripTicketCommetstByTripTicketId(tripTicketId);
    }

    @Test
    void testListAllTripResultByTripTicketId_NoContent() {
        int tripTicketId = 1;
        when(tripTicketCommentService.findAllTripTicketCommetstByTripTicketId(tripTicketId)).thenReturn(new ArrayList<>());

        ResponseEntity<List<TripTicketCommentDTO>> response = tripTicketCommentController.listAllTripResultByTripTicketId(tripTicketId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(tripTicketCommentService).findAllTripTicketCommetstByTripTicketId(tripTicketId);
    }

    @Test
    void testGetTripTicketCommentById() {
        int commentId = 1;
        TripTicketCommentDTO comment = new TripTicketCommentDTO();
        when(tripTicketCommentService.findTripTicketCommentById(commentId)).thenReturn(comment);

        ResponseEntity<TripTicketCommentDTO> response = tripTicketCommentController.getTripTicketCommentById(commentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(comment, response.getBody());
        verify(tripTicketCommentService).findTripTicketCommentById(commentId);
    }

    @Test
    void testGetTripTicketCommentById_NotFound() {
        int commentId = 1;
        when(tripTicketCommentService.findTripTicketCommentById(commentId)).thenReturn(null);

        ResponseEntity<TripTicketCommentDTO> response = tripTicketCommentController.getTripTicketCommentById(commentId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(tripTicketCommentService).findTripTicketCommentById(commentId);
    }

    @Test
    void testCreateTripTicketComment() {
        int tripTicketId = 1;
        TripTicketCommentDTO comment = new TripTicketCommentDTO();
        TripTicketCommentDTO createdComment = new TripTicketCommentDTO();
        when(tripTicketCommentService.createTripTicketComment(tripTicketId, comment)).thenReturn(createdComment);

        ResponseEntity<TripTicketCommentDTO> response = tripTicketCommentController.createTripTicketComment(tripTicketId, comment);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdComment, response.getBody());
        verify(tripTicketCommentService).createTripTicketComment(tripTicketId, comment);
    }

    @Test
    void testUpdateTripTicketComment() {
        int tripTicketId = 1;
        int commentId = 1;
        TripTicketCommentDTO comment = new TripTicketCommentDTO();
        TripTicketCommentDTO currentComment = new TripTicketCommentDTO();
        TripTicketCommentDTO updatedComment = new TripTicketCommentDTO();
        when(tripTicketCommentService.findTripTicketCommentById(commentId)).thenReturn(currentComment);
        when(tripTicketCommentService.updateTripTicketComment(tripTicketId, comment, commentId)).thenReturn(updatedComment);

        ResponseEntity<TripTicketCommentDTO> response = tripTicketCommentController.updateTripTicketComment(tripTicketId, commentId, comment);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedComment, response.getBody());
        verify(tripTicketCommentService).findTripTicketCommentById(commentId);
        verify(tripTicketCommentService).updateTripTicketComment(tripTicketId, comment, commentId);
    }

    @Test
    void testUpdateTripTicketComment_NotFound() {
        int tripTicketId = 1;
        int commentId = 1;
        TripTicketCommentDTO comment = new TripTicketCommentDTO();
        when(tripTicketCommentService.findTripTicketCommentById(commentId)).thenReturn(null);

        ResponseEntity<TripTicketCommentDTO> response = tripTicketCommentController.updateTripTicketComment(tripTicketId, commentId, comment);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(tripTicketCommentService).findTripTicketCommentById(commentId);
        verify(tripTicketCommentService, never()).updateTripTicketComment(tripTicketId, comment, commentId);
    }
}