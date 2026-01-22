package com.clearinghouse.tds.generated.model;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * <p>Java class for vehicleDistance complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="vehicleDistance"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="startTime" type="{}time"/&gt;
 *         &lt;element name="stopTime" type="{}time"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="range" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="rangeUnit"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="meter"/&gt;
 *             &lt;enumeration value="seconds"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "vehicleDistance", propOrder = {
        "startTime",
        "stopTime"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleDistance {

    @XmlElement(required = true)
    protected Time startTime;
    @XmlElement(required = true)
    protected Time stopTime;
    @XmlAttribute(name = "range")
    protected Integer range;
    @XmlAttribute(name = "rangeUnit")
    protected String rangeUnit;

}
