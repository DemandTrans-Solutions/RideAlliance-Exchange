package com.clearinghouse.tds.generated.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;


/**
 * Telegram #: 2BB; From: trip provider; To: ordering client; Purpose: confirm vehicle
 *
 * <p>Java class for vehicleConfirmationType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="vehicleConfirmationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="vehicleNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="driverId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="vehicleInformation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="availabilityForService" type="{}time" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="fuelRange" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="ambulatorySpacePoints" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute name="largeWheelchairSpacePoints" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute name="hasRamp" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="hasLift" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="standardWheelchairSpacePoints" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute name="cargoSpacePoints" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute name="vehicleId" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute name="vehicleModel" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="conversionFactorForAmbulatoryPointsToStandardWheelchairPoints" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="conversionFactorForAmbulatoryPointsToLargeWheelchairPoints" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="flatFloor" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="owner" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="rideVibrationQuality" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "vehicleConfirmationType", propOrder = {
        "vehicleNumber",
        "driverId",
        "vehicleInformation",
        "availabilityForService"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleConfirmationType {

    protected String vehicleNumber;
    protected String driverId;
    protected String vehicleInformation;
    protected Time availabilityForService;
    @XmlAttribute(name = "fuelRange")
    protected Float fuelRange;
    @XmlAttribute(name = "ambulatorySpacePoints")
    protected BigInteger ambulatorySpacePoints;
    @XmlAttribute(name = "largeWheelchairSpacePoints")
    protected BigInteger largeWheelchairSpacePoints;
    @XmlAttribute(name = "hasRamp")
    protected Boolean hasRamp;
    @XmlAttribute(name = "hasLift")
    protected Boolean hasLift;
    @XmlAttribute(name = "standardWheelchairSpacePoints")
    protected BigInteger standardWheelchairSpacePoints;
    @XmlAttribute(name = "cargoSpacePoints")
    protected BigInteger cargoSpacePoints;
    @XmlAttribute(name = "vehicleId")
    protected BigInteger vehicleId;
    @XmlAttribute(name = "vehicleModel")
    protected String vehicleModel;
    @XmlAttribute(name = "conversionFactorForAmbulatoryPointsToStandardWheelchairPoints")
    protected Float conversionFactorForAmbulatoryPointsToStandardWheelchairPoints;
    @XmlAttribute(name = "conversionFactorForAmbulatoryPointsToLargeWheelchairPoints")
    protected Float conversionFactorForAmbulatoryPointsToLargeWheelchairPoints;
    @XmlAttribute(name = "flatFloor")
    protected String flatFloor;
    @XmlAttribute(name = "owner")
    protected String owner;
    @XmlAttribute(name = "rideVibrationQuality")
    protected String rideVibrationQuality;

}
