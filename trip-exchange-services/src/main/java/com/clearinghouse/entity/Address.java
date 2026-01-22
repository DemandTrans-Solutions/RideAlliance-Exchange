/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.entity;

import jakarta.persistence.*;

import java.io.Serializable;

/**
 *
 * @author chaitanyaP
 */
@Entity
@Table(name = "address")
public class Address extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AddressID")
    private int addressId;

    // Default constructor
    public Address() {
    }

    // Constructor with ID parameter
    public Address(int addressId) {
        this.addressId = addressId;
    }

    @Column(name = "Street1")
    private String street1;

    @Column(name = "Street2")
    private String street2;

    @Column(name = "City")
    private String city;

    @Column(name = "County")
    private String county;

    @Column(name = "State")
    private String state;

    @Column(name = "Longitude")
    private double longitude;

    @Column(name = "Latitude")
    private double latitude;

    @Column(name = "ZipCode")
    private String zipcode;

    //how to store geometricPoint
//    OnHOLD
    @Column(name = "CommonName")
    private String commonName;

    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @Column(name = "PhoneExtension")
    private String phoneExtension;

    @Column(name = "AddressType")
    private String addressType;

    public int getAddressId() {
        return addressId;
    }

    //    @OneToOne
//    @JoinColumn(name = "serviceAreaId")
//    private ServiceArea serviceArea;
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public String getStreet1() {
        return street1;
    }

    public void setStreet1(String street1) {
        this.street1 = street1;
    }

    public String getStreet2() {
        return street2;
    }

    public void setStreet2(String street2) {
        this.street2 = street2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    //    public double getLatitude() {
//        return latitude;
//    }
//
//    public void setLatitude(double latitude) {
//        this.latitude = latitude;
//    }
//
//    public double getLongitude() {
//        return longitude;
//    }
//
//    public void setLongitude(double longitude) {
//        this.longitude = longitude;
//    }
//    
//    public ServiceArea getServiceArea() {
//        return serviceArea;
//    }
//
//    public void setServiceArea(ServiceArea serviceArea) {
//        this.serviceArea = serviceArea;
//    }
    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getPhoneExtension() {
        return phoneExtension;
    }

    public void setPhoneExtension(String phoneExtension) {
        this.phoneExtension = phoneExtension;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    @Override
    public String toString() {
        return "Address{" + "addressId=" + addressId + ", street1=" + street1 + ", street2=" + street2 + ", city=" + city + ", county=" + county + ", state=" + state + ", longitude=" + longitude + ", latitude=" + latitude + ", zipcode=" + zipcode + ", commonName=" + commonName + ", phoneNumber=" + phoneNumber + ", phoneExtension=" + phoneExtension + ", addressType=" + addressType + '}';
    }

}
