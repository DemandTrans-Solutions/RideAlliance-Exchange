package com.clearinghouse.testutil;

import com.clearinghouse.dto.AddressDTO;
import com.clearinghouse.dto.ProviderDTO;
import com.clearinghouse.entity.Address;
import com.clearinghouse.entity.Provider;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class ProviderBuilder {
    private Provider provider;
    private ProviderDTO providerDTO;
    
    public ProviderBuilder() {
        provider = new Provider();
        providerDTO = new ProviderDTO();
        
        // Set default values
        provider.setProviderId(1);
        provider.setProviderName("Test Provider");
        provider.setCreatedAt(ZonedDateTime.now());
        provider.setUpdatedAt(ZonedDateTime.now());
        provider.setIsActive(true);
        
        // Set address
        provider.setAddress(TestData.address().withId(1).build());
        
        // Mirror to DTO
        providerDTO.setProviderId(provider.getProviderId());
        providerDTO.setProviderName(provider.getProviderName());
        providerDTO.setActive(provider.isActive());
    }
    
    public ProviderBuilder withId(int id) {
        provider.setProviderId(id);
        providerDTO.setProviderId(id);
        return this;
    }
    
    public ProviderBuilder withName(String name) {
        provider.setProviderName(name);
        providerDTO.setProviderName(name);
        return this;
    }
    

    
    public ProviderBuilder withContact(String email) {
        provider.setContactEmail(email);
        providerDTO.setContactEmail(email);
        return this;
    }
    
    public ProviderBuilder withAddress(Address address) {
        provider.setAddress(address);
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressId(address.getAddressId());
        addressDTO.setStreet1(address.getStreet1());
        addressDTO.setStreet2(address.getStreet2());
        addressDTO.setCity(address.getCity());
        addressDTO.setState(address.getState());
        addressDTO.setZipcode(address.getZipcode());
        providerDTO.setProviderAddress(addressDTO);
        return this;
    }
    
    public ProviderBuilder asInactive() {
        provider.setIsActive(false);
        providerDTO.setActive(false);
        return this;
    }
    
    public Provider build() {
        return provider;
    }
    
    public ProviderDTO buildDTO() {
        return providerDTO;
    }
}