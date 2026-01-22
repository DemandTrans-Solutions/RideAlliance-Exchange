package com.clearinghouse.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "hospitalityareaprovider")
public class HospitalityAreaProvider extends AbstractEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HospitalityProviderId")
    private Integer hospitalityProviderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ServiceId", nullable = false)
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProviderId", nullable = false)
    private Provider provider;

    @Column(name = "providerServiceName", nullable = true)
    private String providerServiceName;

    public Integer getHospitalityProviderId() {
        return hospitalityProviderId;
    }

    public void setHospitalityProviderId(Integer hospitalityProviderId) {
        this.hospitalityProviderId = hospitalityProviderId;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public String getProviderServiceName() {
        return providerServiceName;
    }

    public void setProviderServiceName(String providerServiceName) {
        this.providerServiceName = providerServiceName;
    }

    public HospitalityAreaProvider(Integer provider, String providerServiceName) {
        super();
        this.provider = new Provider(provider);
        this.providerServiceName = providerServiceName;
    }

    public HospitalityAreaProvider(Integer hospitalityProviderId, Integer service, Integer provider,
                                   String providerServiceName) {
        super();
        this.provider = new Provider(provider);
        this.service = new Service(service);
        this.providerServiceName = providerServiceName;
        this.hospitalityProviderId = hospitalityProviderId;
    }

    public HospitalityAreaProvider(Integer provider) {
        super();
        this.provider = new Provider(provider);
    }


    public HospitalityAreaProvider() {
        super();
    }

    public HospitalityAreaProvider(Integer hospitalityProviderId, Service service, Provider provider,
                                   String providerServiceName) {
        super();
        this.hospitalityProviderId = hospitalityProviderId;
        this.service = service;
        this.provider = provider;
        this.providerServiceName = providerServiceName;
    }

    @Override
    public String toString() {
        return "HospitalityAreaProvider [hospitalityProviderId=" + hospitalityProviderId + ", serviceId=" + (service != null ? service.getServiceId() : null) +
                ", providerId=" + (provider != null ? provider.getProviderId() : null) + "]";
    }

}
