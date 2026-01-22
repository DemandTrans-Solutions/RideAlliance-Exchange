package com.clearinghouse.testutil;

import com.clearinghouse.dto.ProviderDTO;
import com.clearinghouse.dto.ServiceDTO;
import com.clearinghouse.entity.Provider;
import com.clearinghouse.entity.Service;
import java.time.ZonedDateTime;

public class ServiceAreaBuilder {
    private Service service;
    private ServiceDTO serviceDTO;
    
    public ServiceAreaBuilder() {
        service = new Service();
        serviceDTO = new ServiceDTO();
        
        // Set default values
        service.setServiceId(1);
        service.setServiceName("Test Service");
        service.setCreatedAt(ZonedDateTime.now());
        service.setUpdatedAt(ZonedDateTime.now());
        service.setIsActive(true);
        
        // Mirror to DTO
        serviceDTO.setServiceId(service.getServiceId());
        serviceDTO.setServiceName(service.getServiceName());
        serviceDTO.setActive(service.isActive());
    }
    
    public ServiceAreaBuilder withId(int id) {
        service.setServiceId(id);
        serviceDTO.setServiceId(id);
        return this;
    }
    
    public ServiceAreaBuilder withName(String name) {
        service.setServiceName(name);
        serviceDTO.setServiceName(name);
        return this;
    }

    
    public ServiceAreaBuilder withServiceArea(String serviceArea) {
        service.setServiceAreaType(serviceArea);
        serviceDTO.setServiceAreaType(serviceArea);
        return this;
    }
    
    public ServiceAreaBuilder asInactive() {
        service.setIsActive(false);
        serviceDTO.setActive(false);
        return this;
    }
    
    public Service build() {
        return service;
    }
    
    public ServiceDTO buildDTO() {
        return serviceDTO;
    }
}