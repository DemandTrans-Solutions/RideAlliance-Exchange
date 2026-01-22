package com.clearinghouse.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author shankarI
 */
@Getter
@Setter
@NoArgsConstructor
public class TripTicketDistanceDTO {

    private int tripTicketDistanceId;
    private int tripTicketId;
    private float TripTicketDistance;
    private float TripTicketTime;
    private String timeInString;


    @Override
    public String toString() {
        return "TripTicketDistanceDTO [tripTicketDistanceId=" + tripTicketDistanceId + ", tripTicketId=" + tripTicketId
                + ", TripTicketDistance=" + TripTicketDistance + ", TripTicketTime=" + TripTicketTime
                + ", timeInString=" + timeInString + "]";
    }
}
