/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;


import com.clearinghouse.entity.ProviderPartner;
import com.clearinghouse.enumentity.ProviderPartnerStatusConstants;
import com.clearinghouse.listresponseentity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chaitanyaP
 */
@Repository
public class ListDAO {

    @PersistenceContext
    EntityManager entityManager;


    public List<ProviderList> getAllProviders() {

        TypedQuery<ProviderList> query = entityManager.createQuery("SELECT NEW com.clearinghouse.listresponseentity.ProviderList(p.providerId, p.providerName) FROM Provider AS p  where isActive=true order by p.providerName", ProviderList.class);
        List<ProviderList> listProviders = query.getResultList();

        return listProviders;

    }


    public List<ProviderList> getAllProvidersByProviderLogin(int providerId) {

        TypedQuery<ProviderList> query = entityManager.createQuery("SELECT NEW com.clearinghouse.listresponseentity.ProviderList(p.providerId, p.providerName) FROM Provider AS p  where p.providerId=:providerId order by p.providerName", ProviderList.class)
                .setParameter("providerId", providerId);

        List<ProviderList> listProviders = query.getResultList();

        return listProviders;
    }


    public List<RoleList> getAllListRoles() {

        TypedQuery<RoleList> query = entityManager.createQuery("SELECT NEW com.clearinghouse.listresponseentity.RoleList(r.roleId, r.roleName) FROM Roles AS r WHERE r.isActive=TRUE ORDER BY r.roleName", RoleList.class);
        List<RoleList> listRoles = query.getResultList();

        return listRoles;

    }


    public List<ServiceAreaList> getAllListServiceAreas() {
        TypedQuery<ServiceAreaList> query = entityManager.createQuery("SELECT NEW com.clearinghouse.listresponseentity.ServiceAreaList(s.serviceAreaId, s.description) FROM ServiceArea AS s ORDER BY s.description", ServiceAreaList.class);
        List<ServiceAreaList> serviceAreaList = query.getResultList();

        return serviceAreaList;
    }


    public List<ProviderPartnerList> getAllListProviderPartners(int providerId) {

        TypedQuery<ProviderPartnerList> query = entityManager.createQuery("SELECT NEW com.clearinghouse.listresponseentity.ProviderPartnerList(p.providerId, p.providerName) FROM Provider AS p where p.providerId != :providerId AND p.providerId!=1  ORDER BY p.providerName", ProviderPartnerList.class)
                .setParameter("providerId", providerId);

        List<ProviderPartnerList> providerPartnerList = query.getResultList();

        return providerPartnerList;
    }


    public List<StatusList> getAllListStatus() {

        TypedQuery<StatusList> query = entityManager.createQuery("SELECT NEW com.clearinghouse.listresponseentity.StatusList(s.statusId, s.type) FROM Status AS s WHERE s.statusId !=14 AND s.statusId !=6 AND s.statusId!=8 ORDER BY s.type", StatusList.class);
        List<StatusList> statusList = query.getResultList();

        return statusList;
    }

//    @Override
//    public List<ProviderPartnerStatus> getAllListProviderPartnerStatus() {
//        TypedQuery<ProviderPartnerStatus> query = entityManager.createQuery("SELECT NEW com.clearingHouse.listResponseEntity.StatusList(s.statusId, s.type) FROM Status AS s  ", StatusList.class);
//        List<StatusList> statusList = query.getResultList();
//
//        return statusList;
//    }

    public List<TicketFilterList> getAllListTicketFilters(int userId) {

        TypedQuery<TicketFilterList> query = entityManager.createQuery("SELECT NEW com.clearinghouse.listresponseentity.TicketFilterList(tf.filterId, tf.filterName) FROM TicketFilter AS tf where tf.user.id = :userId AND tf.isActive=true ORDER BY tf.filterName", TicketFilterList.class)
                .setParameter("userId", userId);
        List<TicketFilterList> ticketFilterList = query.getResultList();

        return ticketFilterList;
    }


    public List<AddressListBO> getAllListAddress(String addressWord) {

        if (addressWord.contains(",") && (addressWord.length() > 1)) {
            String[] splitedOnQuamaStringArr = addressWord.split(",");
            addressWord = splitedOnQuamaStringArr[0];

            TypedQuery<AddressListBO> query = entityManager.createQuery("SELECT NEW com.clearinghouse.listresponseentity.AddressListBO(ad.addressId, ad.street1,ad.street2,ad.city,ad.county,ad.state,ad.zipcode,ad.phoneNumber) " +
                            "FROM Address AS ad where ((ad.street1 LIKE :addressWord) OR (ad.street2 LIKE :addressWord) " +
                            "OR (ad.city LIKE :addressWord) OR (ad.county LIKE :addressWord) OR (ad.state LIKE :addressWord) " +
                            "OR (ad.zipcode LIKE :addressWord) OR (ad.phoneNumber LIKE :addressWord)) " +
                            "AND ad.addressId !=1 " +
                            "GROUP BY ad.street1, ad.street2, ad.city", AddressListBO.class)
                    .setParameter("addressWord", "%" + addressWord + "%");
            List<AddressListBO> ticketFilterList = query.getResultList();
            return ticketFilterList;

        }

        TypedQuery<AddressListBO> query = entityManager.createQuery("SELECT NEW com.clearinghouse.listresponseentity.AddressListBO(ad.addressId, ad.street1,ad.street2,ad.city,ad.county,ad.state,ad.zipcode,ad.phoneNumber) " +
                        "FROM Address AS ad where ((ad.street1 LIKE :addressWord) OR (ad.street2 LIKE :addressWord) OR (ad.city LIKE :addressWord) " +
                        "OR (ad.county LIKE :addressWord) OR (ad.state LIKE :addressWord) OR (ad.zipcode LIKE :addressWord) " +
                        "OR (ad.phoneNumber LIKE :addressWord)) AND ad.addressId !=1 " +
                        "GROUP BY ad.street1, ad.street2, ad.city", AddressListBO.class)
                .setParameter("addressWord", "%" + addressWord + "%");
        List<AddressListBO> ticketFilterList = query.getResultList();

        return ticketFilterList;
    }

    public ProviderList getProviderListObjByProviderId(int providerId) {
        TypedQuery<ProviderList> query = entityManager.createQuery("SELECT NEW com.clearinghouse.listresponseentity.ProviderList(p.providerId, p.providerName) FROM Provider AS p  where p.providerId =:providerId ", ProviderList.class)
                .setParameter("providerId", providerId);

        ProviderList listProviders = query.getSingleResult();

        return listProviders;
    }


    public List<ProviderList> getOriginatorProviderListByProviderId(int providerId) {

        List<ProviderPartner> providerPartners = entityManager
                .createQuery("SELECT p FROM ProviderPartner p where (p.coordinatorProvider.providerId =:providerId  OR p.requesterProvider.providerId =:providerId) AND p.requestStatus.providerPartnerStatusId =:providerPartnerStatusId")
                .setParameter("providerId", providerId)
                .setParameter("providerPartnerStatusId", ProviderPartnerStatusConstants.approved.providerPartnerStatusChek())
                .getResultList();

        List<ProviderList> finalProviderList = new ArrayList<>();
        for (ProviderPartner providerPartner : providerPartners) {
            if (providerPartner.getRequesterProvider().getProviderId() == providerId) {
                finalProviderList.add(getProviderListObjByProviderId(providerPartner.getCoordinatorProvider().getProviderId()));
            } else if (providerPartner.getCoordinatorProvider().getProviderId() == providerId) {
                finalProviderList.add(getProviderListObjByProviderId(providerPartner.getRequesterProvider().getProviderId()));

            }

        }
        finalProviderList.add(getProviderListObjByProviderId(providerId));
        return finalProviderList;
    }


    public List<ProviderList> getClaimantProviderListByProviderId(int providerId) {

        List<ProviderPartner> providerPartners = entityManager
                .createQuery("SELECT p FROM ProviderPartner p where (p.coordinatorProvider.providerId =:providerId  OR p.requesterProvider.providerId =:providerId) AND p.requestStatus.providerPartnerStatusId =:providerPartnerStatusId")
                .setParameter("providerId", providerId)
                .setParameter("providerPartnerStatusId", ProviderPartnerStatusConstants.approved.providerPartnerStatusChek())
                .getResultList();

        List<ProviderList> finalProviderList = new ArrayList<>();
        for (ProviderPartner providerPartner : providerPartners) {
            if (providerPartner.getRequesterProvider().getProviderId() == providerId) {
                finalProviderList.add(getProviderListObjByProviderId(providerPartner.getCoordinatorProvider().getProviderId()));
            } else if (providerPartner.getCoordinatorProvider().getProviderId() == providerId) {
                finalProviderList.add(getProviderListObjByProviderId(providerPartner.getRequesterProvider().getProviderId()));

            }

        }
        return finalProviderList;
    }

}
