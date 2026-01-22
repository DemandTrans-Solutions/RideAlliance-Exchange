package com.clearinghouse.tds.generated.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.List;


/**
 * Telegram #: 2A1; From: ordering client; To: trip provider(s); Purpose: customer info for trip
 *
 * <p>Java class for customerInfoType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="customerInfoType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="customerName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="customerAddress" type="{}addressType" minOccurs="0"/&gt;
 *         &lt;element name="customerPhone" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="customerMobilePhone" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="customerBillingAddress" type="{}addressType" minOccurs="0"/&gt;
 *         &lt;element name="fundingEntityBillingInformation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="fundingType" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="gender" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="caregiverContactInformation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="customerEmergencyPhoneNumber" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="customerEmergencyContactName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="requiredCareComments" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="dateOfBirth" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="customerId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="paymentType" type="{}paymentTypeList" minOccurs="0"/&gt;
 *         &lt;element name="notesForDriver" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "customerInfoType", propOrder = {
        "customerName",
        "customerAddress",
        "customerPhone",
        "customerMobilePhone",
        "customerBillingAddress",
        "fundingEntityBillingInformation",
        "fundingType",
        "gender",
        "caregiverContactInformation",
        "customerEmergencyPhoneNumber",
        "customerEmergencyContactName",
        "requiredCareComments",
        "dateOfBirth",
        "customerId",
        "paymentType",
        "notesForDriver"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerInfoType {

    @JsonProperty
    protected String customerName;
    @JsonProperty
    protected AddressType customerAddress;
    @JsonProperty
    protected BigInteger customerPhone;
    @JsonProperty
    protected BigInteger customerMobilePhone;
    @JsonProperty
    protected AddressType customerBillingAddress;
    @JsonProperty
    protected String fundingEntityBillingInformation;
    @JsonProperty
    protected Boolean fundingType;
    @JsonProperty
    protected String gender;
    @JsonProperty
    protected String caregiverContactInformation;
    @JsonProperty
    protected BigInteger customerEmergencyPhoneNumber;
    @JsonProperty
    protected String customerEmergencyContactName;
    @JsonProperty
    protected String requiredCareComments;
    @XmlSchemaType(name = "date")
    @JsonProperty
    protected XMLGregorianCalendar dateOfBirth;
    @JsonProperty
    protected String customerId;
    @XmlList
    @JsonProperty
    protected List<String> paymentType;
    @JsonProperty
    protected String notesForDriver;


}
