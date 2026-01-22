package com.clearinghouse.tds.generated.model;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Telegram #: 5; From: vehicle; To: trip provider; Purpose: GPS and trip status
 *
 * <p>Java class for tripTaskStatusType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="tripTaskStatusType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="vehicleNumber" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="GPS" type="{}geographicLocation"/&gt;
 *         &lt;element name="timecode" type="{}time"/&gt;
 *         &lt;element name="driverId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="tripTaskId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="driverHours" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="odometerReading" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="passengerMiles" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="vehicleHours" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="vehicleMiles" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="boardingsAndAlightingsAtTripEnd" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="otherFleetVariables" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tripTaskStatusType", propOrder = {
        "vehicleNumber",
        "gps",
        "timecode",
        "driverId",
        "tripTaskId"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripTaskStatusType {

    @XmlElement(required = true)
    protected String vehicleNumber;
    @XmlElement(name = "GPS", required = true)
    protected GeographicLocation gps;
    @XmlElement(required = true)
    protected Time timecode;
    protected String driverId;
    protected String tripTaskId;
    @XmlAttribute(name = "driverHours")
    protected Float driverHours;
    @XmlAttribute(name = "odometerReading")
    protected Float odometerReading;
    @XmlAttribute(name = "passengerMiles")
    protected Float passengerMiles;
    @XmlAttribute(name = "vehicleHours")
    protected Float vehicleHours;
    @XmlAttribute(name = "vehicleMiles")
    protected Float vehicleMiles;
    @XmlAttribute(name = "boardingsAndAlightingsAtTripEnd")
    protected String boardingsAndAlightingsAtTripEnd;
    @XmlAttribute(name = "otherFleetVariables")
    protected String otherFleetVariables;

}
