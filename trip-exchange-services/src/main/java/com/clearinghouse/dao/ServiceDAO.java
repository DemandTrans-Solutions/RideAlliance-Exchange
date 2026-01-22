/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;

import com.clearinghouse.dto.MasterDTO;
import com.clearinghouse.dto.ServiceAreaDTO;
import com.clearinghouse.entity.Service;
import com.clearinghouse.entity.ServiceArea;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chaitanyaP
 */
@Repository
@Slf4j
public class ServiceDAO extends AbstractDAO<Integer, Service> {


    public boolean checkAddressInServicearea(ServiceArea serviceArea, double latitude, double longitude) {
        // pointString example "39.7359 -104.75272"
        // geometryString example "39.7359 -104.75272,40.07418 -104.75994,40.10865 -105.00953,40.02489 -105.19046,39.7084 -105.15408,39.76676 -105.00696,39.78089 -104.88087,39.7359 -104.75272"

        log.debug("Checking if point is in service area: " + latitude + " " + longitude + " for serviceArea: " + serviceArea.getServiceAreaId());


        // Force both geometries to SRID 0 to ensure a consistent, simple cartesian comparison
        // This avoids issues with inconsistent SRIDs in the source data and axis-order problems in MySQL 8+
        String sql = "SELECT ST_Intersects(ST_SRID(s.ServiceAreaGeometry, 0), ST_GeomFromText(CONCAT('POINT(', ? , ' ', ? , ')'), 0)) " +
                "FROM servicearea s WHERE s.ServiceAreaID = ?";

        Query query = getEntityManager().createNativeQuery(sql);

        // For SRID 0 (cartesian), the order is (X, Y) which corresponds to (longitude, latitude)
        query.setParameter(1, longitude);
        query.setParameter(2, latitude);
        query.setParameter(3, serviceArea.getServiceAreaId());


        Object result = query.getSingleResult();
        if (result == null) {
            return false;
        }

        if (result instanceof Number) {
            return ((Number) result).intValue() == 1;
        } else if (result instanceof Boolean) {
            return (Boolean) result;
        }
        return false;
    }


    public boolean checkAddressInService(Service service, double latitude, double longitude) {
        // pointString example "39.7359 -104.75272"
        // geometryString example "39.7359 -104.75272,40.07418 -104.75994,40.10865 -105.00953,40.02489 -105.19046,39.7084 -105.15408,39.76676 -105.00696,39.78089 -104.88087,39.7359 -104.75272"
        try {
            log.info("Checking if point is in service area: " + latitude + " " + longitude + " for service: " + service.getServiceId());

            // Force both geometries to SRID 0 to ensure a consistent, simple cartesian comparison
            // This avoids issues with inconsistent SRIDs in the source data and axis-order problems in MySQL 8+
            String sql = "SELECT ST_Intersects(ST_SRID(s.ServiceAreaGeometry, 0), ST_GeomFromText(CONCAT('POINT(', ? , ' ', ? , ')'), 0)) " +
                    "FROM service s WHERE s.ServiceID = ?";

            Query query = getEntityManager().createNativeQuery(sql);

            // For SRID 0 (cartesian), the order is (X, Y) which corresponds to (longitude, latitude)
            query.setParameter(1, longitude);
            query.setParameter(2, latitude);
            query.setParameter(3, service.getServiceId());

            Object result = query.getSingleResult();
            if (result == null) {
                return false;
            }

            if (result instanceof Number) {
                return ((Number) result).intValue() == 1;
            } else if (result instanceof Boolean) {
                return (Boolean) result;
            }
            return false;
        } catch (Exception e) {
            log.error("Error checking if address is in service " + service.getServiceId() + " with lat " + latitude + " and longitude " + longitude, e);
            return false;
        }
    }


    public List<Service> findAllSerivearea() {

        List<Service> servicearea = getEntityManager()
                .createQuery(" SELECT s FROM Service s where s.isHospitalityArea=false ORDER BY s.serviceName")
                .getResultList();
        return servicearea;

    }


    public List<Service> findAllSeriveareaByProviderId(int providerId) {

        List<Service> serviceareaByPeoviderIdList = getEntityManager()
                .createQuery(" SELECT s FROM Service s WHERE s.isHospitalityArea=false and (s.provider.providerId=:providerId ) ORDER BY s.serviceName")
                .setParameter("providerId", providerId)
                .getResultList();
        return serviceareaByPeoviderIdList;
    }


    public List<Service> findAllSeriveAreaByUserId(Integer userId) {
        List<Service> serviceareaByUserList = getEntityManager().createQuery(
                        " SELECT s FROM Service s, Provider p, User u WHERE s.isHospitalityArea=false and s.provider.providerId=p.providerId and p.providerId=u.provider.providerId and u.id=:userId ORDER BY s.serviceName asc")
                .setParameter("userId", userId).getResultList();
        return serviceareaByUserList;
    }


    public Service findServiceareaByServiceId(int serviceId) {

        return getByKey(serviceId);
    }


    public Service createServicearea(Service service) {

        add(service);
        return service;
    }


    public Service updateServicearea(Service service) {

        return update(service);
    }

/*
    public boolean checkAddressInServicearea(String geometry, String point) {

        geometry = " POLYGON((" + geometry + "))";
        point = " POINT(" + point + ")";
        String sql = "SELECT fn_IsPointInServiceLocation_new(:geometry , :point)";
        Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("geometry", geometry).
                setParameter("point", point);
        boolean result = (Boolean) query.getSingleResult();
        return result;

    }
+*/

    public int getCountOfInactiveServicearea(int providerId) {

        Query query = getEntityManager()
                .createQuery(" SELECT COUNT(*) FROM Service s WHERE s.isHospitalityArea=false and (s.provider.providerId=:providerId AND s.isActive=FALSE)")
                .setParameter("providerId", providerId);
        Long count = (Long) query.getSingleResult();

        return count.intValue();

    }


    public List<Service> findAllHospitalityServicearea() {

        var servicearea = getEntityManager()
                .createQuery(" SELECT s FROM Service s where s.isHospitalityArea=true ORDER BY s.serviceName")
                .getResultList();
        return servicearea;

    }


    public List<Service> findHospitalityServiceareaByProviderId(List<Integer> providerIds) {

        Query query = entityManager.createNativeQuery(
                "SELECT s.ServiceID,s.ServiceName,s.ServiceArea,s.IsHospitalityArea,s.UploadFilePath,s.Eligibility,s.IsActive FROM service s, hospitalityareaprovider h WHERE s.IsHospitalityArea=true and s.ServiceID=h.ServiceId AND h.ProviderId IN (13,14) ORDER BY s.ServiceName");

        List<Object[]> result = query.getResultList();

        List<Service> serviceList = new ArrayList<Service>();
        if (result != null) {
            for (Object[] obj : result) {
                Service service = new Service();

                service.setServiceId((int) obj[0]);
                service.setServiceName((String) obj[1]);
                service.setServiceAreaGeometry((Geometry) obj[2]);
                service.setHospitalityArea((boolean) obj[3]);
                service.setUploadFilePath((String) obj[4]);
                service.setEligibility((String) obj[5]);
                service.setIsActive((boolean) obj[6]);

                serviceList.add(service);
            }
        }

        return serviceList;
    }


    public List<MasterDTO> listOfHosptalitySAName() {
        List<Object[]> result = getEntityManager().createQuery(
                        "SELECT s.serviceId,s.serviceName FROM Service s where s.isHospitalityArea=true ORDER BY s.serviceName ")
                .getResultList();

        List<MasterDTO> listOfHosptalitySAName = new ArrayList<MasterDTO>();
        if (result != null) {
            for (Object[] obj : result) {
                MasterDTO service = new MasterDTO();

                service.setId((int) obj[0]);
                service.setName((String) obj[1]);
                listOfHosptalitySAName.add(service);
            }
        }
        return listOfHosptalitySAName;
    }


    public List<Service> findAllHospitalityServiceareaByIds(List<Integer> serviceIds) {

        List<Service> servicearea = getEntityManager()
                .createQuery(" SELECT s FROM Service s where s.isHospitalityArea=true AND s.serviceId IN(:Ids) ORDER BY s.serviceName")
                .setParameter("Ids", serviceIds)
                .getResultList();
        return servicearea;

    }


    public List<ServiceAreaDTO> findAllMedicalServiceareaByProviderId(int providerId) {
        List<Object[]> result = getEntityManager().createNativeQuery(
                        "SELECT sa.ServiceId,sa.ServiceArea FROM servicearea sa,service s,hospitalityareaprovider h WHERE s.ServiceID=sa.ServiceId "
                                + "AND h.ServiceId=sa.ServiceId " + "AND s.IsHospitalityArea= ? "
                                + "AND s.IsProviderSelected= ?  " + "AND  h.ProviderId= ? "
                                + "ORDER BY s.ServiceName")
                .setParameter(1, true).setParameter(2, true)
                .setParameter(3, providerId).getResultList();

        List<ServiceAreaDTO> serviceAreaList = new ArrayList<ServiceAreaDTO>();
        if (result != null) {
            for (Object[] obj : result) {
                ServiceAreaDTO serviceArea = new ServiceAreaDTO();

                serviceArea.setServiceId((int) obj[0]);
                serviceArea.setServiceArea((String) obj[1]);

                serviceAreaList.add(serviceArea);
            }
        }
        return serviceAreaList;
    }


    public List<ServiceArea> findAllMedicalServiceAreasByProviderId(int providerId) {
        return getEntityManager().createQuery(
                        "SELECT sa FROM ServiceArea sa " +
                                "JOIN sa.service s " +
                                "JOIN HospitalityAreaProvider h ON h.service.id = s.serviceId " +
                                "WHERE s.isHospitalityArea = :medicalSAflag " +
                                "AND s.isProviderSelected = :providerSelectedFlag " +
                                "AND h.provider.id = :providerId " +
                                "ORDER BY s.serviceName", ServiceArea.class)
                .setParameter("medicalSAflag", true)
                .setParameter("providerSelectedFlag", true)
                .setParameter("providerId", providerId)
                .getResultList();
    }


}
