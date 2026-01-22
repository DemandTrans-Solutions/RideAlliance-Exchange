package com.clearinghouse.tds.generated.model;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Any text aimed for operators, drivers etc
 *
 * <p>Java class for manualDescriptionType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="manualDescriptionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="idActionText" type="{}idType" minOccurs="0"/&gt;
 *         &lt;element name="textTimestamp" type="{}time" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="sendtoInvoice" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="sendtoVehicle" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="sendtoOperator" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="manualText" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="vehicleConfirmation" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="sendingOperator" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "manualDescriptionType", propOrder = {
        "idActionText",
        "textTimestamp"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManualDescriptionType {

    protected IdType idActionText;
    protected Time textTimestamp;
    @XmlAttribute(name = "sendtoInvoice", required = true)
    protected boolean sendtoInvoice;
    @XmlAttribute(name = "sendtoVehicle", required = true)
    protected boolean sendtoVehicle;
    @XmlAttribute(name = "sendtoOperator", required = true)
    protected boolean sendtoOperator;
    @XmlAttribute(name = "manualText", required = true)
    protected String manualText;
    @XmlAttribute(name = "vehicleConfirmation", required = true)
    protected boolean vehicleConfirmation;
    @XmlAttribute(name = "sendingOperator")
    @XmlSchemaType(name = "anySimpleType")
    protected String sendingOperator;

}
