package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.ActivityDTO;
import com.clearinghouse.service.ActivityService;
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

class ActivityControllerTest {

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private ActivityController activityController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListAllActivities() {
        List<ActivityDTO> activities = new ArrayList<>();
        activities.add(new ActivityDTO());
        when(activityService.findAllActivites()).thenReturn(activities);

        ResponseEntity<List<ActivityDTO>> response = activityController.listAllActivites();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(activities, response.getBody());
        verify(activityService).findAllActivites();
    }

    @Test
    void testListAllActivities_NoContent() {
        when(activityService.findAllActivites()).thenReturn(new ArrayList<>());

        ResponseEntity<List<ActivityDTO>> response = activityController.listAllActivites();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(activityService).findAllActivites();
    }

    @Test
    void testGetActivityByActivityId() {
        int activityId = 1;
        ActivityDTO activity = new ActivityDTO();
        when(activityService.findActivityByActivityId(activityId)).thenReturn(activity);

        ResponseEntity<ActivityDTO> response = activityController.getActivityByActivityId(activityId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(activity, response.getBody());
        verify(activityService).findActivityByActivityId(activityId);
    }

    @Test
    void testGetActivityByActivityId_NotFound() {
        int activityId = 1;
        when(activityService.findActivityByActivityId(activityId)).thenReturn(null);

        ResponseEntity<ActivityDTO> response = activityController.getActivityByActivityId(activityId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(activityService).findActivityByActivityId(activityId);
    }

    @Test
    void testCreateActivity() {
        ActivityDTO activity = new ActivityDTO();
        ActivityDTO createdActivity = new ActivityDTO();
        when(activityService.createActivity(activity)).thenReturn(createdActivity);

        ResponseEntity<ActivityDTO> response = activityController.createActivity(activity);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdActivity, response.getBody());
        verify(activityService).createActivity(activity);
    }

    @Test
    void testListAllActivitiesByTripTicketId() {
        int tripTicketId = 1;
        List<ActivityDTO> activities = new ArrayList<>();
        activities.add(new ActivityDTO());
        when(activityService.findAllActivitesByTripTicketId(tripTicketId)).thenReturn(activities);

        ResponseEntity<List<ActivityDTO>> response = activityController.listAllActivitesByTripTicketId(tripTicketId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(activities, response.getBody());
        verify(activityService).findAllActivitesByTripTicketId(tripTicketId);
    }

    @Test
    void testListAllActivitiesByTripTicketId_NoContent() {
        int tripTicketId = 1;
        when(activityService.findAllActivitesByTripTicketId(tripTicketId)).thenReturn(new ArrayList<>());

        ResponseEntity<List<ActivityDTO>> response = activityController.listAllActivitesByTripTicketId(tripTicketId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(activityService).findAllActivitesByTripTicketId(tripTicketId);
    }
}