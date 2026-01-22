package com.clearinghouse.tds.generated.model;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Telegram #: 4B; From: trip provider; To: ordering client; Purpose: confirm trip is scheduled for vehicle
 *
 * <p>Java class for tripScheduledConfirmationType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="tripScheduledConfirmationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="tripTicketId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dataReceived" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="paymentRejected" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tripScheduledConfirmationType", propOrder = {
        "tripTicketId",
        "dataReceived"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripScheduledConfirmationType {

    @XmlElement(required = true)
    protected String tripTicketId;
    protected boolean dataReceived;
    @XmlAttribute(name = "paymentRejected")
    protected Boolean paymentRejected;

}
