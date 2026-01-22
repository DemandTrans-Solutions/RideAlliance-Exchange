package com.clearinghouse.entity;

import jakarta.persistence.*;

import java.io.Serializable;

/**
 *
 * @author shankarI
 */
@Entity
@Table(name = "tripticketdistance")
public class TripTicketDistance extends AbstractEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TripTicketDistanceId")
    private int tripTicketDistanceId;

    @OneToOne
    @JoinColumn(name = "TripTicketId", unique = true)
    private TripTicket tripTicket;

    @Column(name = "TripTicketDistance")
    private float tripTicketDistance;

    @Column(name = "TripTicketTime")
    private float tripTicketTime;

    @Column(name = "TimeInString")
    private String timeInString;

    public int getTripTicketDistanceId() {
        return tripTicketDistanceId;
    }

    public void setTripTicketDistanceId(int tripTicketDistanceId) {
        this.tripTicketDistanceId = tripTicketDistanceId;
    }

    public TripTicket getTripTicket() {
        return tripTicket;
    }

    public void setTripTicket(TripTicket tripTicket) {
        this.tripTicket = tripTicket;
    }

    public float getTripTicketDistance() {
        return tripTicketDistance;
    }

    public void setTripTicketDistance(float tripTicketDistance) {
        this.tripTicketDistance = tripTicketDistance;
    }

    public float getTripTicketTime() {
        return tripTicketTime;
    }

    public void setTripTicketTime(float tripTicketTime) {
        this.tripTicketTime = tripTicketTime;
    }

    public String getTimeInString() {
        return timeInString;
    }

    public void setTimeInString(String timeInString) {
        this.timeInString = timeInString;
    }

    @Override
    public String toString() {
        return "TripTicketDistance [tripTicketDistanceId=" + tripTicketDistanceId + ", tripTicket=" + tripTicket
                + ", tripTicketDistance=" + tripTicketDistance + ", tripTicketTime=" + tripTicketTime
                + ", timeInString=" + timeInString + "]";
    }


}
