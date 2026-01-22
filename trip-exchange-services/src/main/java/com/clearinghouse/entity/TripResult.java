/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.sql.Time;
import java.util.Date;
//import org.hibernate.annotations.Type;
//import org.joda.time.DateTime;

/**
 * Add a comment to this line
 *
 * @author chaitanyaP
 */
@Entity
@Table(name = "tripresult")
public class TripResult extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TripResultID")
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TripTicketID")
    private TripTicket tripTicket;

    @Column(name = "NoShowFlag")
    private Boolean isNoShowFlag;

    @Column(name = "TripDate")
    private Date tripDate;

    @Column(name = "ActualPickupArriveTime")
    private Time actualPickupArriveTime;

    @Column(name = "ActualPickupDepartTime")
    private Time actualPickupDepartTime;

    @Column(name = "ActualDropOffArriveTime")
    private Time actualDropOffArriveTime;

    @Column(name = "ActualDropOffDepartTime")
    private Time actualDropOffDepartTime;

    @Column(name = "PickupLatitude")
    private String pickUpLatitude;

    @Column(name = "PickupLongitude")
    private String pickupLongitude;

    @Column(name = "DropOffLatitude")
    private String dropOffLatitude;

    @Column(name = "DropOffLongitude")
    private String dropOffLongitude;

    @Column(name = "FareCollected")
    private float fareCollected;

    @Column(name = "VehicleID")
    private String vehicleId;

    @Column(name = "DriverID")
    private String driverId;

    @Column(name = "NumberOfGuests")
    private int numberOfGuests;

    @Column(name = "NumberOfAttendants")
    private int numberOfAttendants;

    @Column(name = "NumberOfPassengers")
    private int numberOfPassengers;

    @OneToOne
    @JoinColumn(name = "TripClaimID")
    private TripClaim tripClaim;

    @Column(name = "RateType")
    private String rate_type;

    @Column(name = "Rate")
    private float rate;


    @Column(name = "DriverName")
    private String driver_name;


    @Column(name = "VehicleType")
    private String vehicle_type;

    @Column(name = "VehicleName")
    private String vehicle_name;

    @Column(name = "FareType")
    private String fare_type;

    @Column(name = "BaseFare")
    private float base_fare;

    @Column(name = "Fare")
    private float fare;

    @Column(name = "MilesTraveled")
    private float miles_traveled;

    @Column(name = "OdometerStart")
    private float odometer_start;

    @Column(name = "OdometerEnd")
    private float odometer_end;

    @Column(name = "BillableMileage")
    private float billable_mileage;

    @Column(name = "ExtraSecurementCount")
    private int extra_securement_count;

    @Column(name = "Notes")
    private String notes;

    @Column(name = "Outcome")
    private String outcome;

    @Column(name = "CancellationReason")
    private String cancellationReason;

    @Column(name = "NoShowReason")
    private String noShowReason;

    @Column(name = "Version")
    private int version;

    // Default constructor
    public TripResult() {
    }

    // Constructor with ID parameter
    public TripResult(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }


    public void setNoShowReason(String noShowReason) {
        this.noShowReason = noShowReason;
    }

    public String getNoShowReason() {
        return noShowReason;
    }




    public TripTicket getTripTicket() {
        return tripTicket;
    }

    public void setTripTicket(TripTicket tripTicket) {
        this.tripTicket = tripTicket;
    }

    public TripClaim getTripClaim() {
        return tripClaim;
    }

    public void setTripClaim(TripClaim tripClaim) {
        this.tripClaim = tripClaim;
    }

    public String getRate_type() {
        return rate_type;
    }

    public void setRate_type(String rate_type) {
        this.rate_type = rate_type;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }


    public String getDriver_name() {
        return driver_name;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public String getVehicle_type() {
        return vehicle_type;
    }

    public void setVehicle_type(String vehicle_type) {
        this.vehicle_type = vehicle_type;
    }

    public String getVehicle_name() {
        return vehicle_name;
    }

    public void setVehicle_name(String vehicle_name) {
        this.vehicle_name = vehicle_name;
    }

    public String getFare_type() {
        return fare_type;
    }

    public void setFare_type(String fare_type) {
        this.fare_type = fare_type;
    }

    public float getBase_fare() {
        return base_fare;
    }

    public void setBase_fare(float base_fare) {
        this.base_fare = base_fare;
    }

    public float getFare() {
        return fare;
    }

    public void setFare(float fare) {
        this.fare = fare;
    }

    public float getMiles_traveled() {
        return miles_traveled;
    }

    public void setMiles_traveled(float miles_traveled) {
        this.miles_traveled = miles_traveled;
    }

    public float getOdometer_start() {
        return odometer_start;
    }

    public void setOdometer_start(float odometer_start) {
        this.odometer_start = odometer_start;
    }

    public float getOdometer_end() {
        return odometer_end;
    }

    public void setOdometer_end(float odometer_end) {
        this.odometer_end = odometer_end;
    }

    public float getBillable_mileage() {
        return billable_mileage;
    }

    public void setBillable_mileage(float billable_mileage) {
        this.billable_mileage = billable_mileage;
    }

    public int getExtra_securement_count() {
        return extra_securement_count;
    }

    public void setExtra_securement_count(int extra_securement_count) {
        this.extra_securement_count = extra_securement_count;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    // Use Boolean (wrapper) for getter/setter to avoid NPE from unboxing when value is null.
    public Boolean getIsNoShowFlag() {
        return isNoShowFlag;
    }

    public void setIsNoShowFlag(Boolean isNoShowFlag) {
        this.isNoShowFlag = isNoShowFlag;
    }

    public Date getTripDate() {
        return tripDate;
    }

    public void setTripDate(Date tripDate) {
        this.tripDate = tripDate;
    }

    public Time getActualPickupArriveTime() {
        return actualPickupArriveTime;
    }

    public void setActualPickupArriveTime(Time actualPickupArriveTime) {
        this.actualPickupArriveTime = actualPickupArriveTime;
    }

    public Time getActualPickupDepartTime() {
        return actualPickupDepartTime;
    }

    public void setActualPickupDepartTime(Time actualPickupDepartTime) {
        this.actualPickupDepartTime = actualPickupDepartTime;
    }

    public Time getActualDropOffArriveTime() {
        return actualDropOffArriveTime;
    }

    public void setActualDropOffArriveTime(Time actualDropOffArriveTime) {
        this.actualDropOffArriveTime = actualDropOffArriveTime;
    }

    public Time getActualDropOffDepartTime() {
        return actualDropOffDepartTime;
    }

    public void setActualDropOffDepartTime(Time actualDropOffDepartTime) {
        this.actualDropOffDepartTime = actualDropOffDepartTime;
    }

    public String getPickUpLatitude() {
        return pickUpLatitude;
    }

    public void setPickUpLatitude(String pickUpLatitude) {
        this.pickUpLatitude = pickUpLatitude;
    }

    public String getPickupLongitude() {
        return pickupLongitude;
    }

    public void setPickupLongitude(String pickupLongitude) {
        this.pickupLongitude = pickupLongitude;
    }

    public String getDropOffLatitude() {
        return dropOffLatitude;
    }

    public void setDropOffLatitude(String dropOffLatitude) {
        this.dropOffLatitude = dropOffLatitude;
    }

    public String getDropOffLongitude() {
        return dropOffLongitude;
    }

    public void setDropOffLongitude(String dropOffLongitude) {
        this.dropOffLongitude = dropOffLongitude;
    }

    public float getFareCollected() {
        return fareCollected;
    }

    public void setFareCollected(float fareCollected) {
        this.fareCollected = fareCollected;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public int getNumberOfAttendants() {
        return numberOfAttendants;
    }

    public void setNumberOfAttendants(int numberOfAttendants) {
        this.numberOfAttendants = numberOfAttendants;
    }

    public int getNumberOfPassengers() {
        return numberOfPassengers;
    }

    public void setNumberOfPassengers(int numberOfPassengers) {
        this.numberOfPassengers = numberOfPassengers;
    }

    @Override
    public String toString() {
        return "TripResult [id=" + id + ", tripTicket=" + tripTicket + ", isNoShowFlag=" + isNoShowFlag
                + ", tripDate=" + tripDate + ", actualPickupArriveTime=" + actualPickupArriveTime
                + ", actualPickupDepartTime=" + actualPickupDepartTime + ", actualDropOffArriveTime="
                + actualDropOffArriveTime + ", actualDropOffDepartTime=" + actualDropOffDepartTime + ", pickUpLatitude="
                + pickUpLatitude + ", pickupLongitude=" + pickupLongitude + ", dropOffLatitude=" + dropOffLatitude
                + ", dropOffLongitude=" + dropOffLongitude + ", fareCollected=" + fareCollected + ", vehicleId="
                + vehicleId + ", driverId=" + driverId + ", version=" + version + "]";
    }

}
