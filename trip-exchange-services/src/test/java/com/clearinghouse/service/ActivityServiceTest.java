package com.clearinghouse.service;

import com.clearinghouse.dao.ActivityDAO;
import com.clearinghouse.dto.ActivityDTO;
import com.clearinghouse.entity.Activity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ActivityServiceTest {

    @Mock
    private ActivityDAO activityDAO;

    @Mock
    private ModelMapper activityModelMapper;

    @InjectMocks
    private ActivityService activityService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAllActivities() {
        List<Activity> activities = new ArrayList<>();
        Activity a1 = new Activity();
        a1.setCreatedAt(java.time.ZonedDateTime.now());
        Activity a2 = new Activity();
        a2.setCreatedAt(java.time.ZonedDateTime.now());
        activities.add(a1);
        activities.add(a2);

        List<ActivityDTO> activityDTOs = new ArrayList<>();
        activityDTOs.add(new ActivityDTO());
        activityDTOs.add(new ActivityDTO());

        when(activityDAO.findAllActivities()).thenReturn(activities);
        when(activityModelMapper.map(any(Activity.class), eq(ActivityDTO.class)))
                .thenReturn(new ActivityDTO());

        List<ActivityDTO> result = activityService.findAllActivites();

        assertEquals(activityDTOs.size(), result.size());
        verify(activityDAO).findAllActivities();
        verify(activityModelMapper, times(activities.size())).map(any(Activity.class), eq(ActivityDTO.class));
    }

    @Test
    void testFindActivityByActivityId() {
        int activityId = 1;
        Activity activity = new Activity();
        activity.setCreatedAt(java.time.ZonedDateTime.now());
        ActivityDTO activityDTO = new ActivityDTO();

        when(activityDAO.findActivityByActivityId(activityId)).thenReturn(activity);
        when(activityModelMapper.map(activity, ActivityDTO.class)).thenReturn(activityDTO);

        ActivityDTO result = activityService.findActivityByActivityId(activityId);

        assertEquals(activityDTO, result);
        verify(activityDAO).findActivityByActivityId(activityId);
        verify(activityModelMapper).map(activity, ActivityDTO.class);
    }

    @Test
    void testCreateActivity() {
        ActivityDTO activityDTO = new ActivityDTO();
        Activity activity = new Activity();
        activity.setCreatedAt(java.time.ZonedDateTime.now());

        when(activityModelMapper.map(activityDTO, Activity.class)).thenReturn(activity);
        when(activityDAO.createActivity(activity)).thenReturn(activity);
        when(activityModelMapper.map(activity, ActivityDTO.class)).thenReturn(activityDTO);

        ActivityDTO result = activityService.createActivity(activityDTO);

        assertEquals(activityDTO, result);
        verify(activityModelMapper).map(activityDTO, Activity.class);
        verify(activityDAO).createActivity(activity);
        verify(activityModelMapper).map(activity, ActivityDTO.class);
    }

    @Test
    void testFindAllActivitiesByTripTicketId() {
        int tripTicketId = 1;
        List<Activity> activities = new ArrayList<>();
        Activity a1 = new Activity();
        a1.setCreatedAt(java.time.ZonedDateTime.now());
        Activity a2 = new Activity();
        a2.setCreatedAt(java.time.ZonedDateTime.now());
        activities.add(a1);
        activities.add(a2);

        List<ActivityDTO> activityDTOs = new ArrayList<>();
        activityDTOs.add(new ActivityDTO());
        activityDTOs.add(new ActivityDTO());

        when(activityDAO.findAllActivitesByTripTicketId(tripTicketId)).thenReturn(activities);
        when(activityModelMapper.map(any(Activity.class), eq(ActivityDTO.class)))
                .thenReturn(new ActivityDTO());

        List<ActivityDTO> result = activityService.findAllActivitesByTripTicketId(tripTicketId);

        assertEquals(activityDTOs.size(), result.size());
        verify(activityDAO).findAllActivitesByTripTicketId(tripTicketId);
        verify(activityModelMapper, times(activities.size())).map(any(Activity.class), eq(ActivityDTO.class));
    }

    @Test
    void testToDTO() {
        Activity activity = new Activity();
        activity.setCreatedAt(java.time.ZonedDateTime.now());
        ActivityDTO activityDTO = new ActivityDTO();

        when(activityModelMapper.map(activity, ActivityDTO.class)).thenReturn(activityDTO);

        Object result = activityService.toDTO(activity);

        assertEquals(activityDTO, result);
        verify(activityModelMapper).map(activity, ActivityDTO.class);
    }

    @Test
    void testToBO() {
        ActivityDTO activityDTO = new ActivityDTO();
        Activity activity = new Activity();

        when(activityModelMapper.map(activityDTO, Activity.class)).thenReturn(activity);

        Object result = activityService.toBO(activityDTO);

        assertEquals(activity, result);
        verify(activityModelMapper).map(activityDTO, Activity.class);
    }

    @Test
    void testToDTOCollection() {
        try {
            activityService.toDTOCollection(null);
        } catch (UnsupportedOperationException e) {
            assertEquals("Not supported yet.", e.getMessage());
        }
    }
}