/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;


import com.clearinghouse.entity.ServiceArea;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @author chaitanyaP
 */
@Slf4j
@Repository
public class ServiceAreaDAO extends AbstractDAO<Integer, ServiceArea> {


    public ServiceArea createServicearea(ServiceArea serviceArea) {
        add(serviceArea);
        return serviceArea;
    }


    public ServiceArea updateServicearea(ServiceArea serviceArea) {
        return update(serviceArea);
    }
}
