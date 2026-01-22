package com.clearinghouse.tds.generated.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;


/**
 * Telegram #: 2A; From: ordering client; To: trip provider(s); Purpose: order confirmation
 *
 * <p>Java class for clientOrderConfirmationType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="clientOrderConfirmationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="tripTicketId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
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
 *         &lt;element name="detailedDropoffLocationDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="detailedPickupLocationDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="customerName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="customerMobilePhone" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="customerLocInDropoffSequence" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/&gt;
 *         &lt;element name="numOtherReservedPassengers" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="fundingEntityId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="customerId" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="paymentType" type="{}paymentTypeList" /&gt;
 *       &lt;attribute name="fareAmount" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="tripPurpose" type="{}tripPurposeList" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "clientOrderConfirmationType", propOrder = {
        "tripTicketId",
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
        "hardConstraintOnDropoffTime",
        "detailedDropoffLocationDescription",
        "detailedPickupLocationDescription",
        "customerName",
        "customerMobilePhone",
        "customerLocInDropoffSequence",
        "numOtherReservedPassengers",
        "fundingEntityId"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientOrderConfirmationType {

    @XmlElement(required = true)
    @JsonProperty
    protected String tripTicketId;

    @XmlElement(required = true)
    @JsonProperty
    protected AddressType pickupAddress;
    @XmlElement(required = true)
    @JsonProperty
    protected AddressType dropoffAddress;
    @XmlElement(required = true)
    @JsonProperty
    protected Time pickupTime;

    @JsonProperty
    protected Time appointmentTime;

    @JsonProperty
    protected Time dropoffTime;

    @JsonProperty
    protected Time pickupWindowStartTime;

    @JsonProperty
    protected Time pickupWindowEndTime;

    @JsonProperty
    protected Boolean detoursPermissible;

    @JsonProperty
    protected Time negotiatedPickupTime;

    @JsonProperty
    protected Boolean hardConstraintOnPickupTime;

    @JsonProperty
    protected Boolean hardConstraintOnDropoffTime;

    @JsonProperty
    protected String detailedDropoffLocationDescription;

    @JsonProperty
    protected String detailedPickupLocationDescription;

    @XmlElement(required = true)
    @JsonProperty
    protected String customerName;

    @JsonProperty
    protected BigInteger customerMobilePhone;

    @XmlSchemaType(name = "positiveInteger")
    @JsonProperty
    protected BigInteger customerLocInDropoffSequence;

    @JsonProperty
    protected BigInteger numOtherReservedPassengers;

    @XmlElement(required = true)
    @JsonProperty
    protected String fundingEntityId;

    @XmlAttribute(name = "customerId")
    @JsonProperty
    protected String customerId;

    @XmlAttribute(name = "paymentType")
    @JsonProperty
    protected List<String> paymentType;

    @XmlAttribute(name = "fareAmount")
    @JsonProperty
    protected Float fareAmount;

    @XmlAttribute(name = "tripPurpose")
    @JsonProperty
    protected List<String> tripPurpose;


}
