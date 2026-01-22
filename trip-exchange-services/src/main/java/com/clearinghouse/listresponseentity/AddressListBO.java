/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.listresponseentity;

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
public class AddressListBO {

    private int addressId;
    private String street1;
    private String street2;
    private String city;
    private String county;
    private String state;
    private String zipcode;
    private String phoneNumber;

    public AddressListBO(int addressId, String street1, String street2, String city, String county, String state, String zipcode, String phoneNumber) {
        this.addressId = addressId;
        this.street1 = street1;
        this.street2 = street2;
        this.city = city;
        this.county = county;
        this.state = state;
        this.zipcode = zipcode;
        this.phoneNumber = phoneNumber;
    }

}
