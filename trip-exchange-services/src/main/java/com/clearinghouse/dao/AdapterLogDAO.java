package com.clearinghouse.dao;

import com.clearinghouse.entity.AdapterLog;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Shankar I
 */

@Repository
public class AdapterLogDAO extends AbstractDAO<Integer, AdapterLog> {


    public AdapterLog createAdapterLog(AdapterLog adapterLogBo) {
        add(adapterLogBo);
        return adapterLogBo;
    }


    public List<AdapterLog> findAllAdapterLogs() {
        List<AdapterLog> adapterLogs = getEntityManager()
                .createQuery(" SELECT a FROM AdapterLog a ")
                .getResultList();
        return adapterLogs;
    }


    public List<AdapterLog> findAdapterLogsByProviderId(int providerId) {
        List<AdapterLog> adapterLogs = getEntityManager()
                .createQuery(" SELECT a FROM AdapterLog a where a.provider.providerId=:id ")
                .setParameter("id", providerId)
                .getResultList();
        return adapterLogs;
    }


}
