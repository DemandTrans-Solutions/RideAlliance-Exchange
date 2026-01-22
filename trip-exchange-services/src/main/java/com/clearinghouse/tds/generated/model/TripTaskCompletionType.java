package com.clearinghouse.tds.generated.model;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * Telegram #: 4A; From: trip provider(s); To: ordering client; Purpose: performed trip data
 *
 * <p>Java class for tripTaskCompletionType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="tripTaskCompletionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="tripTicketId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="cost" type="{http://www.w3.org/2001/XMLSchema}float"/&gt;
 *         &lt;element name="pickupAddress" type="{}addressType" minOccurs="0"/&gt;
 *         &lt;element name="pickupTime" type="{}time"/&gt;
 *         &lt;element name="dropoffAddress" type="{}addressType" minOccurs="0"/&gt;
 *         &lt;element name="dropoffTime" type="{}time"/&gt;
 *         &lt;element name="scheduledPickupPoint" type="{}addressType" minOccurs="0"/&gt;
 *         &lt;element name="scheduledPickupTime" type="{}time" minOccurs="0"/&gt;
 *         &lt;element name="scheduledDropoffPoint" type="{}addressType" minOccurs="0"/&gt;
 *         &lt;element name="scheduledDropoffTime" type="{}time" minOccurs="0"/&gt;
 *         &lt;element name="performedPickupPoint" type="{}addressType" minOccurs="0"/&gt;
 *         &lt;element name="performedPickupTime" type="{}time" minOccurs="0"/&gt;
 *         &lt;element name="performedDropoffPoint" type="{}addressType" minOccurs="0"/&gt;
 *         &lt;element name="performedDropoffTime" type="{}time" minOccurs="0"/&gt;
 *         &lt;element name="detoursPermissible" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="transferPoint" type="{}addressType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="paymentType" type="{}paymentTypeList" /&gt;
 *       &lt;attribute name="fareAmount" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="specialAttributes" type="{}specialAttributesList" /&gt;
 *       &lt;attribute name="transportServices" type="{}valueList" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tripTaskCompletionType", propOrder = {
        "tripTicketId",
        "cost",
        "pickupAddress",
        "pickupTime",
        "dropoffAddress",
        "dropoffTime",
        "scheduledPickupPoint",
        "scheduledPickupTime",
        "scheduledDropoffPoint",
        "scheduledDropoffTime",
        "performedPickupPoint",
        "performedPickupTime",
        "performedDropoffPoint",
        "performedDropoffTime",
        "detoursPermissible",
        "transferPoint"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripTaskCompletionType {

    @XmlElement(required = true)
    protected String tripTicketId;
    protected float cost;
    protected AddressType pickupAddress;
    @XmlElement(required = true)
    protected Time pickupTime;
    protected AddressType dropoffAddress;
    @XmlElement(required = true)
    protected Time dropoffTime;
    protected AddressType scheduledPickupPoint;
    protected Time scheduledPickupTime;
    protected AddressType scheduledDropoffPoint;
    protected Time scheduledDropoffTime;
    protected AddressType performedPickupPoint;
    protected Time performedPickupTime;
    protected AddressType performedDropoffPoint;
    protected Time performedDropoffTime;
    protected Boolean detoursPermissible;
    protected AddressType transferPoint;
    @XmlAttribute(name = "paymentType")
    protected List<String> paymentType;
    @XmlAttribute(name = "fareAmount")
    protected Float fareAmount;
    @XmlAttribute(name = "specialAttributes")
    protected List<String> specialAttributes;
    @XmlAttribute(name = "transportServices")
    protected List<String> transportServices;


}
