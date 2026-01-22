package com.clearinghouse.tds.generated.model;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;


/**
 * Telegram #: 3B; From: vehicle; To: trip provider; Purpose: confirm vehicle performed task
 *
 * <p>Java class for tripTaskConfirmationType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="tripTaskConfirmationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="tripTaskId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="tripTicketId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="customerLocInDropoffSequence" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/&gt;
 *         &lt;element name="nodePerformedTime" type="{}time"/&gt;
 *         &lt;element name="vehicleNumber" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="tripStatus" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="performed"/&gt;
 *             &lt;enumeration value="no show"/&gt;
 *             &lt;enumeration value="no pickup"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="driverId" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tripTaskConfirmationType", propOrder = {
        "tripTaskId",
        "tripTicketId",
        "customerLocInDropoffSequence",
        "nodePerformedTime",
        "vehicleNumber"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripTaskConfirmationType {

    @XmlElement(required = true)
    protected String tripTaskId;
    @XmlElement(required = true)
    protected String tripTicketId;
    @XmlElement(required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger customerLocInDropoffSequence;
    @XmlElement(required = true)
    protected Time nodePerformedTime;
    @XmlElement(required = true)
    protected String vehicleNumber;
    @XmlAttribute(name = "tripStatus", required = true)
    protected String tripStatus;
    @XmlAttribute(name = "driverId")
    protected String driverId;

}
