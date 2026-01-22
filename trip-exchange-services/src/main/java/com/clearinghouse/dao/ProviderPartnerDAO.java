/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;


import com.clearinghouse.entity.ProviderPartner;
import com.clearinghouse.enumentity.ProviderPartnerStatusConstants;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chaitanyaP
 */
@Repository
public class ProviderPartnerDAO extends AbstractDAO<Integer, ProviderPartner> {


    public List<ProviderPartner> findAllProviderPartners() {

        List<ProviderPartner> providerPartners = getEntityManager()
                .createQuery("SELECT p FROM ProviderPartner p where p.isActive=true AND p.requestStatus.providerPartnerStatusId !=:requesterStatusId")
                .setParameter("requesterStatusId", ProviderPartnerStatusConstants.approved.providerPartnerStatusChek())
                .getResultList();
        return providerPartners;

    }


    public ProviderPartner findProviderPartnerByProviderPartnerId(int providerPartnerId) {

        return getByKey(providerPartnerId);

    }


    public ProviderPartner createProviderPartner(ProviderPartner providerPartner) {

        add(providerPartner);
        return providerPartner;
    }


    public ProviderPartner updateProviderPartner(ProviderPartner providerPartner) {

        return update(providerPartner);
    }


    public void deleteProviderpartnerByProviderPartnerId(int providerPartnerId) {

        ProviderPartner providerPartner = (ProviderPartner) getEntityManager()
                .createQuery("SELECT p FROM ProviderPartner p WHERE p.providerPartnerId = :providerPartnerId")
                .setParameter("providerPartnerId", providerPartnerId)
                .getSingleResult();
        providerPartner.setIsActive(false);
    }


    public List<ProviderPartner> findAllProviderPartnersByRequesterProviderId(int requesterProviderId) {

        List<ProviderPartner> providerPartners = getEntityManager()
                .createQuery("SELECT p FROM ProviderPartner p where ( p.requesterProvider.providerId = :requesterProviderId OR p.coordinatorProvider.providerId = :requesterProviderId) ORDER BY p.requestStatus.providerPartnerStatusId ASC")
                .setParameter("requesterProviderId", requesterProviderId)
                .getResultList();
        return providerPartners;

    }


    public boolean providerPartnershipCheck(int requesterProviderId, int coordinatorProviderId) {

        boolean isRequestPresent = false;
        boolean isRequesterProviderRequestExists = false;
        boolean isCoordinatorProviderrequestExists = false;
        List<ProviderPartner> providerPartnersforRquester = getEntityManager()
                .createQuery("SELECT p FROM ProviderPartner p where (( p.requesterProvider.providerId = :requesterProviderId AND p.coordinatorProvider.providerId = :coordinatorProviderId) AND p.isActive=true) OR (( p.requesterProvider.providerId = :requesterProviderId AND p.coordinatorProvider.providerId = :coordinatorProviderId) AND p.requestStatus.providerPartnerStatusId = :requestStatusId) OR (( p.requesterProvider.providerId = :requesterProviderId AND p.coordinatorProvider.providerId = :coordinatorProviderId) AND p.requestStatus.providerPartnerStatusId = :requestStatusIsPendingId)")
                .setParameter("requesterProviderId", requesterProviderId)
                .setParameter("coordinatorProviderId", coordinatorProviderId)
                .setParameter("requestStatusId", ProviderPartnerStatusConstants.approved.providerPartnerStatusChek())
                .setParameter("requestStatusIsPendingId", ProviderPartnerStatusConstants.pending.providerPartnerStatusChek())
                .getResultList();

        if (providerPartnersforRquester.iterator().hasNext()) {
            isRequesterProviderRequestExists = true;
        }

        List<ProviderPartner> providerPartnersforCoordinator = getEntityManager()
                .createQuery("SELECT p FROM ProviderPartner p where (( p.requesterProvider.providerId = :requesterProviderId AND p.coordinatorProvider.providerId = :coordinatorProviderId) AND p.isActive=true) OR (( p.requesterProvider.providerId = :requesterProviderId AND p.coordinatorProvider.providerId = :coordinatorProviderId) AND p.requestStatus.providerPartnerStatusId = :requestStatusId) OR (( p.requesterProvider.providerId = :requesterProviderId AND p.coordinatorProvider.providerId = :coordinatorProviderId) AND p.requestStatus.providerPartnerStatusId = :requestStatusIsPendingId)")
                .setParameter("requesterProviderId", coordinatorProviderId)
                .setParameter("coordinatorProviderId", requesterProviderId)
                .setParameter("requestStatusId", ProviderPartnerStatusConstants.approved.providerPartnerStatusChek())
                .setParameter("requestStatusIsPendingId", ProviderPartnerStatusConstants.pending.providerPartnerStatusChek())
                .getResultList();

        if (providerPartnersforCoordinator.iterator().hasNext()) {
            isCoordinatorProviderrequestExists = true;
        }

        if ((isRequesterProviderRequestExists) || (isCoordinatorProviderrequestExists)) {
            isRequestPresent = true;
        }

        return isRequestPresent;

    }


    public List<ProviderPartner> findApprovedProviderPartnersByRequesterProviderId(int requesterProviderId) {

        List<ProviderPartner> providerPartners = getEntityManager()
                .createQuery("SELECT p FROM ProviderPartner p where ( p.requesterProvider.providerId = :requesterProviderId OR p.coordinatorProvider.providerId = :requesterProviderId) AND p.requestStatus.providerPartnerStatusId=:providerPartnerStatusId ORDER BY p.requestStatus.providerPartnerStatusId ASC")
                .setParameter("requesterProviderId", requesterProviderId)
                .setParameter("providerPartnerStatusId", ProviderPartnerStatusConstants.approved.providerPartnerStatusChek())
                .getResultList();
        return providerPartners;

    }

}
