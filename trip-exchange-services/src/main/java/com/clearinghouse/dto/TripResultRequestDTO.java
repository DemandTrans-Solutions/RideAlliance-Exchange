package com.clearinghouse.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@NoArgsConstructor
public class TripResultRequestDTO {

    private int id;
    private int tripTicketId;
    private int trip_claim_id;
    @JsonProperty("noShowFlag")
    private boolean isNoShowFlag;
    private String tripDate;
    private String actualPickupArriveTime;
    private String actualPickupDepartTime;
    private String actualDropOffArriveTime;
    private String actualDropOffDepartTime;
    private double pickUpLatitude;
    private double pickupLongitude;
    private double dropOffLatitude;
    private double dropOffLongitude;
    private float fareCollected;
    private String vehicleId;
    private String driverId;
    private String claimantProvider;
    private String pickUpAddress;
    private String dropOffAddress;
    private int numberOfGuests;
    private int numberOfAttendants;
    private int numberOfPassengers;

    private String cancellationReason;
    private String noShowReason;


    @Override
    public String toString() {
        return "TripResultRequestDTO [id=" + id + ", tripTicketId=" + tripTicketId + ", trip_claim_id="
                + trip_claim_id + ", isNoShowFlag=" + isNoShowFlag + ", tripDate=" + tripDate
                + ", actualPickupArriveTime=" + actualPickupArriveTime + ", actualPickupDepartTime="
                + actualPickupDepartTime + ", actualDropOffArriveTime=" + actualDropOffArriveTime
                + ", actualDropOffDepartTime=" + actualDropOffDepartTime + ", pickUpLatitude=" + pickUpLatitude
                + ", pickupLongitude=" + pickupLongitude + ", dropOffLatitude=" + dropOffLatitude + ", dropOffLongitude=" + dropOffLongitude + ", fareCollected=" + fareCollected + ", vehicleId="
                + vehicleId + ", driverId=" + driverId + ", claimantProvider=" + claimantProvider
                + ", pickUpAddress=" + pickUpAddress + ", dropOffAddress=" + dropOffAddress + ", numberOfGuests="
                + numberOfGuests + ", numberOfAttendants=" + numberOfAttendants + ", numberOfPassengers="
                + numberOfPassengers + "]";
    }


}
