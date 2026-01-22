package com.clearinghouse.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class HospitalityAreaProviderDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer hospitalityProviderId;
    private Integer serviceId;
    private Integer providerId;
    private String providerServiceName;

    public HospitalityAreaProviderDTO(Integer hospitalityProviderId, Integer serviceId, Integer providerId,
                                      String providerServiceName) {
        super();
        this.hospitalityProviderId = hospitalityProviderId;
        this.serviceId = serviceId;
        this.providerId = providerId;
        this.providerServiceName = providerServiceName;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HospitalityAreaProviderDTO ua)) {
            return false;
        }

        return (ua.getProviderId() == this.getProviderId());
    }

    @Override
    public int hashCode() {
        return getProviderId() + 31;
    }

}
