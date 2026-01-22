package com.clearinghouse.testutil;

import com.clearinghouse.dto.AddressDTO;
import com.clearinghouse.entity.Address;

import java.time.ZonedDateTime;

public class AddressBuilder {
    private Address address;
    private AddressDTO addressDTO;
    
    public AddressBuilder() {
        address = new Address();
        addressDTO = new AddressDTO();
        
        // Set default values
        address.setAddressId(1);
        address.setCreatedAt(ZonedDateTime.now());
        address.setUpdatedAt(ZonedDateTime.now());
        address.setStreet1("123 Test St");
        address.setStreet2("Suite 100");
        address.setCity("Test City");
        address.setState("TS");
        address.setZipcode("12345");
        address.setLatitude(40.7128f);
        address.setLongitude(-74.0060f);
        
        // Mirror to DTO
        addressDTO.setAddressId(address.getAddressId());
        addressDTO.setStreet1(address.getStreet1());
        addressDTO.setStreet2(address.getStreet2());
        addressDTO.setCity(address.getCity());
        addressDTO.setState(address.getState());
        addressDTO.setZipcode(address.getZipcode());
        addressDTO.setLatitude(address.getLatitude());
        addressDTO.setLongitude(address.getLongitude());
    }
    
    public AddressBuilder withId(int id) {
        address.setAddressId(id);
        addressDTO.setAddressId(id);
        return this;
    }
    
    public AddressBuilder withLocation(float lat, float lon) {
        address.setLatitude(lat);
        address.setLongitude(lon);
        addressDTO.setLatitude(lat);
        addressDTO.setLongitude(lon);
        return this;
    }
    
    public AddressBuilder withAddress(String line1, String line2) {
        address.setStreet1(line1);
        address.setStreet2(line2);
        addressDTO.setStreet1(line1);
        addressDTO.setStreet2(line2);
        return this;
    }
    
    public AddressBuilder withZipCode(String zipCode) {
        address.setZipcode(zipCode);
        addressDTO.setZipcode(zipCode);
        return this;
    }
    
    public Address build() {
        return address;
    }
    
    public AddressDTO buildDTO() {
        return addressDTO;
    }
}