package com.clearinghouse.testutil;

import com.clearinghouse.dto.ProviderDTO;
import com.clearinghouse.dto.ProviderPartnerDTO;
import com.clearinghouse.entity.Provider;
import com.clearinghouse.entity.ProviderPartner;
import java.time.ZonedDateTime;

public class ProviderPartnerBuilder {
    private ProviderPartner providerPartner;
    private ProviderPartnerDTO providerPartnerDTO;
    
    public ProviderPartnerBuilder() {
        providerPartner = new ProviderPartner();
        providerPartnerDTO = new ProviderPartnerDTO();
        
        // Set default values
        providerPartner.setProviderPartnerId(1);
        providerPartner.setCreatedAt(ZonedDateTime.now());
        providerPartner.setUpdatedAt(ZonedDateTime.now());
        providerPartner.setIsActive(true);
        
        // Mirror to DTO
        providerPartnerDTO.setProviderPartnerId(providerPartner.getProviderPartnerId());
        providerPartnerDTO.setActive(providerPartner.isActive());
    }
    
    public ProviderPartnerBuilder withId(int id) {
        providerPartner.setProviderPartnerId(id);
        providerPartnerDTO.setProviderPartnerId(id);
        return this;
    }
    
    public ProviderPartnerBuilder withPartners(Provider requester, Provider coordinator) {
        providerPartner.setRequesterProvider(requester);
        providerPartner.setCoordinatorProvider(coordinator);
        
        // Populate DTO fields used in tests
        providerPartnerDTO.setRequesterProviderId(requester.getProviderId());
        providerPartnerDTO.setRequesterProviderName(requester.getProviderName());
        providerPartnerDTO.setCoordinatorProviderId(coordinator.getProviderId());
        providerPartnerDTO.setCoordinatorProviderName(coordinator.getProviderName());

        return this;
    }
    
    public ProviderPartnerBuilder asInactive() {
        providerPartner.setIsActive(false);
        providerPartnerDTO.setActive(false);
        return this;
    }
    
    public ProviderPartner build() {
        return providerPartner;
    }
    
    public ProviderPartnerDTO buildDTO() {
        return providerPartnerDTO;
    }
}