package com.clearinghouse.tds.generated.model;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;


/**
 * Telegram #: 3A; From: trip provider; To: vehicle; Purpose: control vehicle
 *
 * <p>Java class for tripTaskType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="tripTaskType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="tripTaskId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="tripTicketId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="customerPickupLocInVehPerformanceSequence" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/&gt;
 *         &lt;element name="pickupNodeAddress" type="{}addressType"/&gt;
 *         &lt;element name="pickupNodeScheduledTime" type="{}time"/&gt;
 *         &lt;element name="detailedPickupLocationDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="customerDropoffLocInVehPerformanceSequence" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/&gt;
 *         &lt;element name="dropoffNodeAddress" type="{}addressType"/&gt;
 *         &lt;element name="dropoffNodeScheduledTime" type="{}time"/&gt;
 *         &lt;element name="detailedDropoffLocationDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="customerMobilePhone" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="customerName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="numOtherReservedPassengers" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="notesForDriver" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="specialAttributes" type="{}specialAttributesList" /&gt;
 *       &lt;attribute name="paymentType" type="{}paymentTypeList" /&gt;
 *       &lt;attribute name="fareAmount" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tripTaskType", propOrder = {
        "tripTaskId",
        "tripTicketId",
        "customerPickupLocInVehPerformanceSequence",
        "pickupNodeAddress",
        "pickupNodeScheduledTime",
        "detailedPickupLocationDescription",
        "customerDropoffLocInVehPerformanceSequence",
        "dropoffNodeAddress",
        "dropoffNodeScheduledTime",
        "detailedDropoffLocationDescription",
        "customerMobilePhone",
        "customerName",
        "numOtherReservedPassengers"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripTaskType {

    @XmlElement(required = true)
    protected String tripTaskId;
    @XmlElement(required = true)
    protected String tripTicketId;
    @XmlElement(required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger customerPickupLocInVehPerformanceSequence;
    @XmlElement(required = true)
    protected AddressType pickupNodeAddress;
    @XmlElement(required = true)
    protected Time pickupNodeScheduledTime;
    protected String detailedPickupLocationDescription;
    @XmlElement(required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger customerDropoffLocInVehPerformanceSequence;
    @XmlElement(required = true)
    protected AddressType dropoffNodeAddress;
    @XmlElement(required = true)
    protected Time dropoffNodeScheduledTime;
    protected String detailedDropoffLocationDescription;
    protected BigInteger customerMobilePhone;
    @XmlElement(required = true)
    protected String customerName;
    protected BigInteger numOtherReservedPassengers;
    @XmlAttribute(name = "notesForDriver")
    protected String notesForDriver;
    @XmlAttribute(name = "specialAttributes")
    protected List<String> specialAttributes;
    @XmlAttribute(name = "paymentType")
    protected List<String> paymentType;
    @XmlAttribute(name = "fareAmount")
    protected Float fareAmount;

}
