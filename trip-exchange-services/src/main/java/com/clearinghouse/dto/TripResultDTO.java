/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Time;
import java.util.Date;

/**
 *
 * @author chaitanyaP
 */

@Getter
@Setter
@NoArgsConstructor
public class TripResultDTO {


    private int id;
    private int tripTicketId;
    @JsonProperty("trip_claim_id")
    private int tripClaimId;
    // Use wrapper Boolean to allow null values from the entity without causing unboxing NPEs

    private Boolean isNoShowFlag;
    private Date tripDate;
    private Time actualPickupArriveTime;
    private Time actualPickupDepartTime;
    private Time actualDropOffArriveTime;
    private Time actualDropOffDepartTime;
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
    private String scheduledPickupTime;
    private String scheduledDropOffTime;
    private int numberOfGuests;
    private int numberOfAttendants;
    private int numberOfPassengers;
    private int orgProviderId;

    @JsonProperty("driver_name")
    private String driverName;
    @JsonProperty("rate_type")
    private String rateType;
    private float rate;
    @JsonProperty("vehicle_type")
    private String vehicleType;
    @JsonProperty("vehicle_name")
    private String vehicleName;
    @JsonProperty("fare_type")
    private String fareType;
    @JsonProperty("base_fare")
    private float baseFare;
    private float fare;
    @JsonProperty("miles_traveled")
    private float milesTraveled;
    @JsonProperty("odometer_start")
    private float odometerStart;
    @JsonProperty("odometer_end")
    private float odometerEnd;
    @JsonProperty("billable_mileage")
    private float billableMileage;
    @JsonProperty("extra_securement_count")
    private int extraSecurementCount;
    @JsonProperty("cancellation_reason")
    private String cancellationReason;
    @JsonProperty("no_show_reason")
    private String noShowReason;

    private String notes;
    private String outcome;
    private int version;

    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;

    @Override
    public String toString() {
        return "TripResultDTO [id=" + id + ", tripTicketId=" + tripTicketId + ", tripClaimId=" + tripClaimId
                + ", isNoShowFlag=" + isNoShowFlag + ", tripDate=" + tripDate + ", actualPickupArriveTime="
                + actualPickupArriveTime + ", actualPickupDepartTime=" + actualPickupDepartTime
                + ", actualDropOffArriveTime=" + actualDropOffArriveTime + ", actualDropOffDepartTime="
                + actualDropOffDepartTime + ", pickUpLatitude=" + pickUpLatitude + ", pickupLongitude="
                + pickupLongitude + ", dropOffLatitude=" + dropOffLatitude + ", dropOffLongitude=" + dropOffLongitude
                + ", FareCollected=" + fareCollected + ", vehicleId=" + vehicleId + ", driverId=" + driverId
                + ", claimantProvider=" + claimantProvider + ", pickUpAddress=" + pickUpAddress + ", dropOffAddress="
                + dropOffAddress + ", scheduledPickupTime=" + scheduledPickupTime + ", scheduledDropOffTime="
                + scheduledDropOffTime + ", numberOfGuests=" + numberOfGuests + ", numberOfAttendants="
                + numberOfAttendants + ", numberOfPassengers=" + numberOfPassengers + ", driverName=" + driverName
                + ", rateType=" + rateType + ", rate=" + rate + ", vehicleType=" + vehicleType + ", vehicleName="
                + vehicleName + ", fareType=" + fareType + ", baseFare=" + baseFare + ", fare=" + fare
                + ", milesTraveled=" + milesTraveled + ", odometerStart=" + odometerStart + ", odometerEnd="
                + odometerEnd + ", billableMileage=" + billableMileage + ", extraSecurementCount="
                + extraSecurementCount + ", notes=" + notes + ", outcome=" + outcome + ", version=" + version
                + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
    }

}
