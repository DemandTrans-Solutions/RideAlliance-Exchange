/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clearinghouse.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 *
 * @author chaitanyaP
 */

@Getter
@Setter
@NoArgsConstructor
public class AddressDTO {

    private int addressId;


    @Size(min = 5, max = 1000)
    private String street1;

    @Size(max = 250)
    private String street2;


    @Size(min = 1, max = 100)
    private String city;

    private String county;


    //@Size(min = 1, max = 100)
    private String state;


    //@Size(min = 4, max = 10)
    private String zipcode;


    private double latitude;


    private double longitude;


    private String commonName;
    private String phoneNumber;
    private String phoneExtension;
    private String addressType;

    private String created_at;
    private String updated_at;


//    @Override
//    public String toString() {
//        return "AddressDTO{" + "addressId=" + addressId + ", street1=" + street1 + ", street2=" + street2 + ", city=" + city + ", county=" + county + ", state=" + state + ", zipcode=" + zipcode + ", latitude=" + latitude + ", longitude=" + longitude + ", serviceAreaId=" + serviceAreaId + ", commonName=" + commonName + ", phoneNumber=" + phoneNumber + ", phoneExtension=" + phoneExtension + ", addressType=" + addressType + '}';
//    }
}
