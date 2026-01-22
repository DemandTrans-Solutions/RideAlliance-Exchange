/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Geometry;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


/**
 *
 * @author chaitanyaP
 */
@Entity
@Table(name = "service")
public class Service extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ServiceID")
    private int serviceId;

    @OneToOne
    @JoinColumn(name = "ProviderID")
    private Provider provider;

    //    @OneToOne
//    @JoinColumn(name = "FundingSourceID", nullable = true)
//    private FundingSource fundingSource;
    @Column(name = "ServiceName")
    private String serviceName;

    @Column(name = "DropOffRate")
    private float dropOffRate;

    @Column(name = "PickupRate")
    private float pickupRate;

    @Column(name = "CostPerMinute")
    private float costPerMinute;

    @Column(name = "CostPerMile")
    private float costPerMile;

    @Column(name = "WheelchairSpaceCost")
    private float wheelchairSpaceCost;

    //for now taking as string..find the data type to ctake GEOmetry datatype form my sql..
    //@Deprecated
    //@Column(name = "ServiceArea")
    //private String serviceArea;

    @Column(name = "ServiceAreaGeometry", columnDefinition = "GEOMETRY")
    private Geometry serviceAreaGeometry;

    @Column(name = "IsActive")
    private Boolean isActive;

    @Column(name = "Eligibility")
    private String eligibility;

    @Column(name = "ServiceAreaType")
    private String serviceAreaType;

    // newly added
    @Column(name = "UploadFilePath")
    private String uploadFilePath;

    @Column(name = "IsHospitalityArea", nullable = false)
    private Boolean isHospitalityArea;

    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    Set<HospitalityAreaProvider> hospitalAreaProvider = new HashSet<HospitalityAreaProvider>();

    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    Set<ServiceArea> hospitalServiceAreas = new HashSet<ServiceArea>();

    @Column(name = "IsProviderSelected", nullable = false)
    private Boolean isProviderSelected;

    @Column(name = "FileName")
    private String fileName;

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public float getDropOffRate() {
        return dropOffRate;
    }

    public void setDropOffRate(float dropOffRate) {
        this.dropOffRate = dropOffRate;
    }

    public float getPickupRate() {
        return pickupRate;
    }

    public void setPickupRate(float pickupRate) {
        this.pickupRate = pickupRate;
    }

    public float getCostPerMinute() {
        return costPerMinute;
    }

    public void setCostPerMinute(float costPerMinute) {
        this.costPerMinute = costPerMinute;
    }

    public float getCostPerMile() {
        return costPerMile;
    }

    public void setCostPerMile(float costPerMile) {
        this.costPerMile = costPerMile;
    }

    public float getWheelchairSpaceCost() {
        return wheelchairSpaceCost;
    }

    public void setWheelchairSpaceCost(float wheelchairSpaceCost) {
        this.wheelchairSpaceCost = wheelchairSpaceCost;
    }

//    @Deprecated
//    public String getServiceArea() {
//        return serviceArea;
//    }
//
//    @Deprecated
//    public void setServiceArea(String serviceArea) {
//        this.serviceArea = serviceArea;
//    }

    public Geometry getServiceAreaGeometry() {
        return serviceAreaGeometry;
    }

    public void setServiceAreaGeometry(Geometry serviceAreaGeometry) {
        this.serviceAreaGeometry = serviceAreaGeometry;
    }

    public String getEligibility() {
        return eligibility;
    }

    public void setEligibility(String eligibility) {
        this.eligibility = eligibility;
    }

    public String getServiceAreaType() {
        return serviceAreaType;
    }

    public void setServiceAreaType(String serviceAreaType) {
        this.serviceAreaType = serviceAreaType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }


    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }


    public String getUploadFilePath() {
        return uploadFilePath;
    }

    public void setUploadFilePath(String uploadFilePath) {
        this.uploadFilePath = uploadFilePath;
    }

    public boolean isHospitalityArea() {
        return isHospitalityArea;
    }

    public void setHospitalityArea(boolean isHospitalityArea) {
        this.isHospitalityArea = isHospitalityArea;
    }

    public boolean isProviderSelected() {
        return isProviderSelected;
    }

    public void setProviderSelected(boolean isProviderSelected) {
        this.isProviderSelected = isProviderSelected;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Set<HospitalityAreaProvider> getHospitalAreaProvider() {
        return hospitalAreaProvider;
    }

    public void setHospitalAreaProvider(Set<HospitalityAreaProvider> hospitalAreaProvider) {
        this.hospitalAreaProvider = hospitalAreaProvider;
    }

    public Service() {
        super();
        // TODO Auto-generated constructor stub
    }

    public Service(int serviceId) {
        super();
        this.serviceId = serviceId;
    }

    public Set<ServiceArea> getHospitalServiceAreas() {
        return hospitalServiceAreas;
    }

    public void setHospitalServiceAreas(Set<ServiceArea> hospitalServiceAreas) {
        this.hospitalServiceAreas = hospitalServiceAreas;
    }

    public int getProviderId() {
        return provider != null ? provider.getProviderId() : 0;
    }


    @Override
    public String toString() {
        return "Service [serviceId=" + serviceId + ", providerId=" + (provider != null ? provider.getProviderId() : null)
                + ", serviceName=" + serviceName + ", dropOffRate=" + dropOffRate + ", pickupRate=" + pickupRate
                + ", costPerMinute=" + costPerMinute + ", costPerMile=" + costPerMile
                + ", wheelchairSpaceCost=" + wheelchairSpaceCost + ", isActive=" + isActive
                + ", eligibility=" + eligibility + ", serviceAreaType=" + serviceAreaType
                + ", uploadFilePath=" + uploadFilePath + ", isHospitalityArea=" + isHospitalityArea
                + ", isProviderSelected=" + isProviderSelected + ", fileName=" + fileName + "]";
    }


}
