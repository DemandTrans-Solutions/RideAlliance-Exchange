/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Geometry;

import java.io.Serializable;

/**
 *
 * @author chaitanyaP
 */

/**
 *
 * @author Prasad J
 */
@Entity
@Table(name = "servicearea")
public class ServiceArea extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = -1774438299241012430L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ServiceAreaID")
    private Integer serviceAreaId;

    /*
    @Column(name = "ServiceArea")
    private String serviceArea;
*/
    @Column(name = "ServiceName")
    private String serviceName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ServiceId", nullable = false)
    private Service service;

    @Column(name = "ServiceAreaGeometry", columnDefinition = "GEOMETRY")
    private Geometry serviceAreaGeometry;


    // Default constructor
    public ServiceArea() {
        super();
    }

    // Constructor with ID parameter
    public ServiceArea(Integer serviceAreaId) {
        this.serviceAreaId = serviceAreaId;
    }

    public ServiceArea(Geometry serviceAreaGeometry) {
        super();
        this.serviceAreaGeometry = serviceAreaGeometry;
    }

    public ServiceArea(Geometry serviceAreaGeometry, Service service) {
        super();
        this.serviceAreaGeometry = serviceAreaGeometry;
        this.service = service;
    }

    public ServiceArea(Integer serviceAreaId, Geometry serviceAreaGeometry, String serviceName, Integer serviceId) {
        super();
        this.serviceAreaId = serviceAreaId;
        this.serviceAreaGeometry = serviceAreaGeometry;
        this.serviceName = serviceName;
        this.service = new Service(serviceId);
    }

    public Integer getServiceAreaId() {
        return serviceAreaId;
    }

    public void setServiceAreaId(Integer serviceAreaId) {
        this.serviceAreaId = serviceAreaId;
    }

    /*
    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceAreaName) {
        this.serviceArea = serviceAreaName;
    }*/

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Geometry getServiceAreaGeometry() {
        return serviceAreaGeometry;
    }

    public void setServiceAreaGeometry(Geometry serviceAreaGeometry) {
        this.serviceAreaGeometry = serviceAreaGeometry;
    }


    @Override
    public String toString() {
        return "ServiceArea{" +
                "serviceAreaId=" + serviceAreaId +
                ", serviceName='" + serviceName + '\'' +
                ", serviceId=" + (service != null ? service.getServiceId() : null) +
                ", serviceAreaGeometry=" + serviceAreaGeometry +
                '}';
    }
}
