package com.clearinghouse.tds.generated.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;


/**
 * The coordinates for the address and potentially a known zone
 *
 * <p>Java class for geographicLocation complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="geographicLocation"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="vehicleDistance" type="{}vehicleDistance" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="zone" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="typeOfCoordinate" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="WGS84"/&gt;
 *             &lt;enumeration value="WGS-84"/&gt;
 *             &lt;enumeration value="RT90"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="lat" use="required" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="long" use="required" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="precision" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute name="speed" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="speedSource"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="gpsunit"/&gt;
 *             &lt;enumeration value="taximeter"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="direction" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute name="height" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="deviationSpeed" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "geographicLocation", propOrder = {
        "vehicleDistance"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeographicLocation {

    protected List<VehicleDistance> vehicleDistance;
    @XmlAttribute(name = "zone")
    protected String zone;
    @XmlAttribute(name = "typeOfCoordinate", required = true)
    protected String typeOfCoordinate;
    @XmlAttribute(name = "lat", required = true)
    @JsonProperty("lat")
    protected double latitude;
    @XmlAttribute(name = "long", required = true)
    @JsonProperty("long")
    protected double longitude;
    @XmlAttribute(name = "precision", required = true)
    protected BigInteger precision;
    @XmlAttribute(name = "speed")
    protected Float speed;
    @XmlAttribute(name = "speedSource")
    protected String speedSource;
    @XmlAttribute(name = "direction")
    protected BigInteger direction;
    @XmlAttribute(name = "height")
    protected Float height;
    @XmlAttribute(name = "deviationSpeed")
    protected Float deviationSpeed;


}
