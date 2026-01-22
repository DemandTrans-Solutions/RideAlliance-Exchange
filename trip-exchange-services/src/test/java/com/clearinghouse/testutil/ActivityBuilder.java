package com.clearinghouse.testutil;

import com.clearinghouse.dto.ActivityDTO;
import com.clearinghouse.entity.Activity;
import com.clearinghouse.entity.TripTicket;
import java.time.ZonedDateTime;

public class ActivityBuilder {
    private Activity activity;
    private ActivityDTO activityDTO;
    
    public ActivityBuilder() {
        activity = new Activity();
        activityDTO = new ActivityDTO();
        
        // Set default values
        activity.setActivityId(1);
        activity.setCreatedAt(ZonedDateTime.now());
        activity.setAction("Test Action");
        activity.setActionDetails("Test Action Details");
        activity.setActionTakenBy("Test User");

        // Set trip ticket
        TripTicket tripTicket = TestData.tripTicket().withId(1).build();
        activity.setTripTicket(tripTicket);
        
        // Mirror to DTO
        activityDTO.setActivityId(activity.getActivityId());
        activityDTO.setTripTicketId(activity.getTripTicket().getId());
        activityDTO.setAction(activity.getAction());
        activityDTO.setActionDetails(activity.getActionDetails());
        activityDTO.setActionTakenBy(activity.getActionTakenBy());
        activityDTO.setCreatedAt(activity.getCreatedAt().toString());
    }
    
    public ActivityBuilder withId(int id) {
        activity.setActivityId(id);
        activityDTO.setActivityId(id);
        return this;
    }
    
    public ActivityBuilder withTripTicket(TripTicket tripTicket) {
        activity.setTripTicket(tripTicket);
        activityDTO.setTripTicketId(tripTicket.getId());
        return this;
    }
    
    public ActivityBuilder withAction(String action) {
        activity.setAction(action);
        activityDTO.setAction(action);
        return this;
    }
    
    public ActivityBuilder withActionDetails(String actionDetails) {
        activity.setActionDetails(actionDetails);
        activityDTO.setActionDetails(actionDetails);
        return this;
    }
    
    public ActivityBuilder withActionTakenBy(String actionTakenBy) {
        activity.setActionTakenBy(actionTakenBy);
        activityDTO.setActionTakenBy(actionTakenBy);
        return this;
    }
    
    public Activity build() {
        return activity;
    }
    
    public ActivityDTO buildDTO() {
        return activityDTO;
    }
}