/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;


import com.clearinghouse.entity.Provider;
import com.clearinghouse.exceptionentity.ProviderEmailExistsExceptionEntity;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chaitanyaP
 */
@Repository
public class ProviderDAO extends AbstractDAO<Integer, Provider> {


    public List<Provider> findAllProviders() {
        int id = 1;
        List<Provider> providers = getEntityManager()
                .createQuery("SELECT p FROM Provider p where p.providerId != :id order by providerName  ")
                .setParameter("id", id)
                .getResultList();
        return providers;
    }


    public Provider findProviderByName(String providerName) {
        var provider = (Provider) getEntityManager()
                .createQuery("SELECT p FROM Provider p WHERE p.providerName = :providerName")
                .setParameter("providerName", providerName)
                .getSingleResult();
        return provider;
    }

    public Provider findProviderByProviderId(int providerId) {
        Provider provider = getByKey(providerId);
        return provider;
    }


    public Provider createProvider(Provider provider) {

        add(provider);
        return provider;
    }


    public Provider updateProvider(Provider provider) {
        return update(provider);
    }


    public void deleteProviderByProviderId(int providerId) {

        Provider provider = (Provider) getEntityManager()
                .createQuery("SELECT p FROM Provider p WHERE p.providerId = :providerId")
                .setParameter("providerId", providerId)
                .getSingleResult();
        provider.setIsActive(false);
    }


    public boolean findProviderByEmail(String email) {

        boolean checkStatus = false;

        TypedQuery<ProviderEmailExistsExceptionEntity> query = entityManager.createQuery("SELECT NEW com.clearinghouse.exceptionentity.ProviderEmailExistsExceptionEntity(p.contactEmail) FROM Provider AS p", ProviderEmailExistsExceptionEntity.class);
        List<ProviderEmailExistsExceptionEntity> listOfProviders = query.getResultList();

        for (ProviderEmailExistsExceptionEntity providerEmailExistsExceptionEntity : listOfProviders) {

            checkStatus = providerEmailExistsExceptionEntity.getContactEmail().equalsIgnoreCase(email);

            if (checkStatus) {
                break;

            }

        }

        return checkStatus;

    }


    public String getProviderNameById(Integer providerId) {
        String tripTicketStutus = (String) getEntityManager()
                .createQuery("SELECT p.providerName FROM Provider p WHERE p.providerId = :id ")
                .setParameter("id", providerId)
                .getSingleResult();
        return tripTicketStutus;
    }

    public String getProviderLastSyncDateTime(int providerId) {
        String lastSyncDateTime = (String) getEntityManager()
                .createQuery("SELECT p.lastSyncDateTime FROM Provider p WHERE p.providerId = :id ")
                .setParameter("id", providerId)
                .getSingleResult();

        return lastSyncDateTime;
    }

}
