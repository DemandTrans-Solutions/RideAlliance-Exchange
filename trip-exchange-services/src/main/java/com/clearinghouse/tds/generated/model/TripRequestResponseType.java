package com.clearinghouse.tds.generated.model;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * Telegram #: 1B; From: trip provider; To: ordering client; Purpose: reply to telegram 1A
 *
 * <p>Java class for tripRequestResponseType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="tripRequestResponseType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="tripAvailable" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="scheduledPickupTime" type="{}time" minOccurs="0"/&gt;
 *         &lt;element name="scheduledPickupPoint" type="{}addressType" minOccurs="0"/&gt;
 *         &lt;element name="scheduledDropoffPoint" type="{}addressType" minOccurs="0"/&gt;
 *         &lt;element name="transferPoint" type="{}addressType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="paymentTypeList" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *       &lt;attribute name="fareAmount" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="transportServices" type="{}valueList" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tripRequestResponseType", propOrder = {
        "tripAvailable",
        "scheduledPickupTime",
        "scheduledPickupPoint",
        "scheduledDropoffPoint",
        "transferPoint"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripRequestResponseType {

    protected Boolean tripAvailable;
    protected Time scheduledPickupTime;
    protected AddressType scheduledPickupPoint;
    protected AddressType scheduledDropoffPoint;
    protected AddressType transferPoint;
    @XmlAttribute(name = "paymentTypeList")
    @XmlSchemaType(name = "anySimpleType")
    protected String paymentTypeList;
    @XmlAttribute(name = "fareAmount")
    protected Float fareAmount;
    @XmlAttribute(name = "transportServices")
    protected List<String> transportServices;

}
