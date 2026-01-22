package com.clearinghouse.tds.generated.model;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * Telegram #: 2B; From: trip provider; To: ordering client; Purpose: confirm trip is scheduled
 *
 * <p>Java class for providerOrderConfirmationType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="providerOrderConfirmationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="tripTicketId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="scheduledPickupTime" type="{}time"/&gt;
 *         &lt;element name="scheduledPickupPoint" type="{}addressType"/&gt;
 *         &lt;element name="scheduledDropoffPoint" type="{}addressType"/&gt;
 *         &lt;element name="transferPoint" type="{}addressType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="paymentType" type="{}paymentTypeList" /&gt;
 *       &lt;attribute name="fareAmount" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="transportServices" type="{}valueList" /&gt;
 *       &lt;attribute name="vehicleNumber" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="driverId" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="vehicleInformation" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "providerOrderConfirmationType", propOrder = {
        "tripTicketId",
        "scheduledPickupTime",
        "scheduledPickupPoint",
        "scheduledDropoffPoint",
        "transferPoint"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProviderOrderConfirmationType {

    @XmlElement(required = true)
    protected String tripTicketId;
    @XmlElement(required = true)
    protected Time scheduledPickupTime;
    @XmlElement(required = true)
    protected AddressType scheduledPickupPoint;
    @XmlElement(required = true)
    protected AddressType scheduledDropoffPoint;
    protected AddressType transferPoint;
    @XmlAttribute(name = "paymentType")
    protected List<String> paymentType;
    @XmlAttribute(name = "fareAmount")
    protected Float fareAmount;
    @XmlAttribute(name = "transportServices")
    protected List<String> transportServices;
    @XmlAttribute(name = "vehicleNumber")
    protected String vehicleNumber;
    @XmlAttribute(name = "driverId")
    protected String driverId;
    @XmlAttribute(name = "vehicleInformation")
    protected String vehicleInformation;

}
