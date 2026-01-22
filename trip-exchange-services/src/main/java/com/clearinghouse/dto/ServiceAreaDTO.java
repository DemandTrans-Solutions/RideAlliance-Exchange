package com.clearinghouse.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ServiceAreaDTO {

    private Integer serviceAreaId;

    private String serviceArea;

    private String serviceName;

    private Integer serviceId;


    public ServiceAreaDTO(Integer serviceAreaId, String serviceArea, String serviceName, Integer serviceId) {
        super();
        this.serviceAreaId = serviceAreaId;
        this.serviceArea = serviceArea;
        this.serviceName = serviceName;
        this.serviceId = serviceId;
    }

    @Override
    public String toString() {
        return "ServiceAreaDTO [serviceAreaId=" + serviceAreaId + ", serviceArea=" + serviceArea + ", serviceName="
                + serviceName + ", serviceId=" + serviceId + "]";
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((serviceAreaId == null) ? 0 : serviceAreaId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServiceAreaDTO other = (ServiceAreaDTO) obj;
        if (serviceAreaId == null) {
            return other.serviceAreaId == null;
        } else return serviceAreaId.equals(other.serviceAreaId);
    }

}
