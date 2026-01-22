package com.clearinghouse.tds.generated.model;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;


/**
 * Should normally be the local time.
 *
 * <p>Java class for time complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="time"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="timeAccuracy" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="timeZone" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute name="time" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="dwellTime" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="timeType"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="2101"/&gt;
 *             &lt;enumeration value="scheduledtime"/&gt;
 *             &lt;enumeration value="2102"/&gt;
 *             &lt;enumeration value="estimatedtime"/&gt;
 *             &lt;enumeration value="2103"/&gt;
 *             &lt;enumeration value="promisedtime"/&gt;
 *             &lt;enumeration value="2104"/&gt;
 *             &lt;enumeration value="actual"/&gt;
 *             &lt;enumeration value="2105"/&gt;
 *             &lt;enumeration value="scheduled"/&gt;
 *             &lt;enumeration value="2107"/&gt;
 *             &lt;enumeration value="estimated"/&gt;
 *             &lt;enumeration value="2106"/&gt;
 *             &lt;enumeration value="promised"/&gt;
 *             &lt;enumeration value="2108"/&gt;
 *             &lt;enumeration value="asap"/&gt;
 *             &lt;enumeration value="estimatedEndtime"/&gt;
 *             &lt;enumeration value="estimatedStarttime"/&gt;
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
@XmlType(name = "time")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Time {

    @XmlAttribute(name = "timeAccuracy")
    protected String timeAccuracy;
    @XmlAttribute(name = "timeZone")
    protected BigInteger timeZone;
    @XmlAttribute(name = "time", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar time;
    @XmlAttribute(name = "dwellTime")
    protected Integer dwellTime;
    @XmlAttribute(name = "timeType")
    protected TimeType timeType;

}
