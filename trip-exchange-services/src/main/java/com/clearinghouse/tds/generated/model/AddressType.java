package com.clearinghouse.tds.generated.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;


/**
 * The address known to receiving system
 *
 * <p>Java class for addressType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="addressType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="manualDescriptionAddress" type="{}manualDescriptionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="geographicLocation" type="{}geographicLocation" minOccurs="0"/&gt;
 *         &lt;element name="idAddressName" type="{}idType" minOccurs="0"/&gt;
 *         &lt;element name="idStreet" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;extension base="{}idType"&gt;
 *               &lt;/extension&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="idPostalCode" type="{}idType" minOccurs="0"/&gt;
 *         &lt;element name="idCommunity" type="{}idType" minOccurs="0"/&gt;
 *         &lt;element name="idCountry" type="{}idType" minOccurs="0"/&gt;
 *         &lt;element name="idZone" type="{}idType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="idMap" type="{}idType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="addressName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="street" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="streetNo" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" /&gt;
 *       &lt;attribute name="streetNoLetter" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="location" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="community" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="postalNo" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="country" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="mapPage" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addressType", propOrder = {
        "manualDescriptionAddress",
        "geographicLocation",
        "idAddressName",
        "idStreet",
        "idPostalCode",
        "idCommunity",
        "idCountry",
        "idZone",
        "idMap"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressType {

    @JsonProperty
    protected List<ManualDescriptionType> manualDescriptionAddress;
    @JsonProperty

    protected GeographicLocation geographicLocation;
    @JsonProperty

    protected IdType idAddressName;
    @JsonProperty

    protected IdType idStreet;
    @JsonProperty

    protected IdType idPostalCode;
    @JsonProperty

    protected IdType idCommunity;
    @JsonProperty

    protected IdType idCountry;
    @JsonProperty

    protected List<IdType> idZone;
    @JsonProperty

    protected List<IdType> idMap;
    @XmlAttribute(name = "addressName")
    @JsonProperty

    protected String addressName;
    @XmlAttribute(name = "street")
    @JsonProperty

    protected String street;
    @XmlAttribute(name = "streetNo")
    @XmlSchemaType(name = "positiveInteger")
    @JsonProperty

    protected BigInteger streetNo;
    @XmlAttribute(name = "streetNoLetter")
    @JsonProperty

    protected String streetNoLetter;
    @XmlAttribute(name = "location")
    @JsonProperty

    protected String location;
    @XmlAttribute(name = "community")
    @JsonProperty

    protected String community;
    @XmlAttribute(name = "postalNo")
    @JsonProperty

    protected String postalNo;
    @XmlAttribute(name = "country")
    @JsonProperty

    protected String country;
    @XmlAttribute(name = "mapPage")
    @JsonProperty

    protected String mapPage;


}
