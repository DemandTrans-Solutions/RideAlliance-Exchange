package com.clearinghouse.tds.generated.model;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * Telegram #: 1A; From: ordering client; To: trip provider(s); Purpose: query for trip availability
 *
 * <p>Java class for tripRequestType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="tripRequestType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="pickupAddress" type="{}addressType"/&gt;
 *         &lt;element name="dropoffAddress" type="{}addressType"/&gt;
 *         &lt;element name="pickupTime" type="{}time"/&gt;
 *         &lt;element name="appointmentTime" type="{}time" minOccurs="0"/&gt;
 *         &lt;element name="dropoffTime" type="{}time" minOccurs="0"/&gt;
 *         &lt;element name="pickupWindowStartTime" type="{}time" minOccurs="0"/&gt;
 *         &lt;element name="pickupWindowEndTime" type="{}time" minOccurs="0"/&gt;
 *         &lt;element name="detoursPermissible" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="negotiatedPickupTime" type="{}time" minOccurs="0"/&gt;
 *         &lt;element name="hardConstraintOnPickupTime" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="hardConstraintOnDropoffTime" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="specialAttributes" type="{}specialAttributesList" /&gt;
 *       &lt;attribute name="transportServices" type="{}valueList" /&gt;
 *       &lt;attribute name="openAttribute" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tripRequestType", propOrder = {
        "pickupAddress",
        "dropoffAddress",
        "pickupTime",
        "appointmentTime",
        "dropoffTime",
        "pickupWindowStartTime",
        "pickupWindowEndTime",
        "detoursPermissible",
        "negotiatedPickupTime",
        "hardConstraintOnPickupTime",
        "hardConstraintOnDropoffTime"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripRequestType {

    @XmlElement(required = true)
    protected AddressType pickupAddress;
    @XmlElement(required = true)
    protected AddressType dropoffAddress;
    @XmlElement(required = true)
    protected Time pickupTime;
    protected Time appointmentTime;
    protected Time dropoffTime;
    protected Time pickupWindowStartTime;
    protected Time pickupWindowEndTime;
    protected Boolean detoursPermissible;
    protected Time negotiatedPickupTime;
    protected Boolean hardConstraintOnPickupTime;
    protected Boolean hardConstraintOnDropoffTime;
    @XmlAttribute(name = "specialAttributes")
    protected List<String> specialAttributes;
    @XmlAttribute(name = "transportServices")
    protected List<String> transportServices;
    @XmlAttribute(name = "openAttribute")
    protected String openAttribute;
}
