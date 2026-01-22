/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.entity;

import jakarta.persistence.*;

import java.io.Serializable;

/**
 *
 * @author chaitanyaP
 */
@Entity
@Table(name = "activity")

public class Activity extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ActivityID")
    private int activityId;

    @OneToOne
    @JoinColumn(name = "TripTicketID")
    private TripTicket tripTicket;

    @Column(name = "Action")
    private String action;

    @Column(name = "ActionTakenBy")
    private String actionTakenBy;

    @Column(name = "ActionDetails")
    private String actionDetails;

    // Default constructor
    public Activity() {
    }

    // Constructor with ID parameter
    public Activity(int activityId) {
        this.activityId = activityId;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public TripTicket getTripTicket() {
        return tripTicket;
    }

    public void setTripTicket(TripTicket tripTicket) {
        this.tripTicket = tripTicket;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActionTakenBy() {
        return actionTakenBy;
    }

    public void setActionTakenBy(String actionTakenBy) {
        this.actionTakenBy = actionTakenBy;
    }

    public String getActionDetails() {
        return actionDetails;
    }

    public void setActionDetails(String actionDetails) {
        this.actionDetails = actionDetails;
    }

    @Override
    public String toString() {
        return "Activity{" + "activityId=" + activityId + ", tripTicket=" + tripTicket + ", action=" + action + ", actionTakenBy=" + actionTakenBy + ", actionDetails=" + actionDetails + '}';
    }

}
