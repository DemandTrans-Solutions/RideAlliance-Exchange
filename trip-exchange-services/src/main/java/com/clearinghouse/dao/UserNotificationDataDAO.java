/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;


import com.clearinghouse.entity.ProviderPartner;
import com.clearinghouse.entity.User;
import com.clearinghouse.enumentity.ProviderPartnerStatusConstants;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chaitanyaP
 */
@Repository
@lombok.extern.slf4j.Slf4j
public class UserNotificationDataDAO extends AbstractDAO<Integer, Object> {


    public List<User> getUsersForPartnerCreateTicket(int providerId) {

        List<User> users = getEntityManager()
                .createQuery("SELECT u FROM User u where u.provider.providerId =:providerId AND u.isNotifyPartnerCreatesTicket=true")
                .setParameter("providerId", providerId)
                .getResultList();
        return users;
    }


    public List<ProviderPartner> getProviderPartners(int providerId) {

        List<ProviderPartner> providerPartners = getEntityManager()
                .createQuery("SELECT p FROM ProviderPartner p where (p.coordinatorProvider.providerId =:providerId  OR p.requesterProvider.providerId =:providerId) AND p.requestStatus.providerPartnerStatusId =:providerPartnerStatusId")
                .setParameter("providerId", providerId)
                .setParameter("providerPartnerStatusId", ProviderPartnerStatusConstants.approved.providerPartnerStatusChek())
                .getResultList();
        return providerPartners;
    }

//    @Override
//    public List<User> getUsersWithRoleProviderAdmin(int providerId) {
//
//        List<User> users = getEntityManager()
//                .createQuery("SELECT u FROM User u where u.provider.providerId =:providerId AND u.authorities.authority =: userAuthority")
//                .setParameter("providerId", providerId)
//                .setParameter("userAuthority", "ROLE_PROVIDERADMIN")
//                .getResultList();
//        return users;
//    }

    public List<User> getUsersForClaimedTripticketRescinded(int providerId) {
        List<User> users = getEntityManager()
                .createQuery("SELECT u FROM User u where u.provider.providerId =:providerId AND u.isNotifyClaimedTicketRescinded=true")
                .setParameter("providerId", providerId)
                .getResultList();
        return users;

    }


    public List<User> getUsersForTripClaimRescinded(int providerId) {

        List<User> users = getEntityManager()
                .createQuery("SELECT u FROM User u where u.provider.providerId =:providerId AND u.isNotifyTripClaimRescinded=true")
                .setParameter("providerId", providerId)
                .getResultList();
        return users;
    }


    public List<User> getUsersForTripCommentAdded(int providerId) {

        List<User> users = getEntityManager()
                .createQuery("SELECT u FROM User u where u.provider.providerId =:providerId AND u.isNotifyTripCommentAdded=true")
                .setParameter("providerId", providerId)
                .getResultList();
        return users;
    }


    public List<User> getUsersForTripResultSubmitted(int providerId) {

        List<User> users = getEntityManager()
                .createQuery("SELECT u FROM User u where u.provider.providerId =:providerId AND u.isNotifyTripResultSubmitted=true")
                .setParameter("providerId", providerId)
                .getResultList();
        return users;
    }


    public List<User> getUsersForTripClaimDeclined(int providerId) {

        List<User> users = getEntityManager()
                .createQuery("SELECT u FROM User u where u.provider.providerId =:providerId AND u.isNotifyTripClaimDeclined=true")
                .setParameter("providerId", providerId)
                .getResultList();
        return users;
    }


    public List<User> getUsersForTripClaimApproved(int providerId) {

        List<User> users = getEntityManager()
                .createQuery("SELECT u FROM User u where u.provider.providerId =:providerId AND u.isNotifyTripClaimApproved=true")
                .setParameter("providerId", providerId)
                .getResultList();
        return users;
    }


    public List<User> getUsersForAutoApprovalTripClaim(int providerId) {

        List<User> users = getEntityManager()
                .createQuery("SELECT u FROM User u where u.provider.providerId =:providerId AND u.isNotifyNewTripClaimAutoApproved=true")
                .setParameter("providerId", providerId)
                .getResultList();
        return users;
    }


    public List<User> getUsersForTripClaimCancel(int providerId) {

        List<User> users = getEntityManager()
                .createQuery("SELECT u FROM User u where u.provider.providerId =:providerId AND u.isNotifyTripClaimCancelled=true")
                .setParameter("providerId", providerId)
                .getResultList();
        return users;
    }


    public List<User> getUsersForTripCancel(int providerId) {

        List<User> users = getEntityManager()
                .createQuery("SELECT u FROM User u where u.provider.providerId =:providerId AND u.isNotifyTripCancelled=true")
                .setParameter("providerId", providerId)
                .getResultList();
        return users;
    }


    public List<User> getUsersForTripExpired(int providerId) {

        List<User> users = getEntityManager()
                .createQuery("SELECT u FROM User u where u.provider.providerId =:providerId AND u.isNotifyTripExpired=true")
                .setParameter("providerId", providerId)
                .getResultList();
        return users;
    }


    public List<User> getUsersForTripPriceMismatch(int providerId) {

        List<User> users = getEntityManager()
                .createQuery("SELECT u FROM User u where u.provider.providerId =:providerId AND u.isNotifyTripPriceMismatched=true")
                .setParameter("providerId", providerId)
                .getResultList();
        return users;
    }


    public List<User> getUsersForTripReceived(int providerId) {

        List<User> users = getEntityManager()
                .createQuery("SELECT u FROM User u where u.provider.providerId =:providerId AND u.isNotifyTripReceived=true")
                .setParameter("providerId", providerId)
                .getResultList();
        return users;
    }


    public List<User> getUsersForTripWeeklyReport(int providerId) {

        List<User> users = getEntityManager()
                .createQuery("SELECT u FROM User u where u.provider.providerId =:providerId AND u.isNotifyTripWeeklyReport=true")
                .setParameter("providerId", providerId)
                .getResultList();
        return users;
    }


    public List<User> getUsersOfProvider(int providerId) {

        List<User> users = getEntityManager()
                .createQuery("SELECT u FROM User u where u.provider.providerId =:providerId ")
                .setParameter("providerId", providerId)
                .getResultList();
        return users;
    }


    public boolean isProviderPartnerTrustedForOrginator(int originatorProviderId, int claimantProviderId) {
        boolean isPartnerTrusted = false;
        List<ProviderPartner> providerPartners = null;
        try {
            providerPartners = getEntityManager()
                    .createQuery("SELECT p FROM ProviderPartner p WHERE ((p.coordinatorProvider.providerId = :claimantProviderId AND p.requesterProvider.providerId = :originatorProviderId) OR (p.coordinatorProvider.providerId = :originatorProviderId AND p.requesterProvider.providerId = :claimantProviderId)) AND p.requestStatus.providerPartnerStatusId = :providerPartnerStatusId", ProviderPartner.class)
                    .setParameter("originatorProviderId", originatorProviderId)
                    .setParameter("claimantProviderId", claimantProviderId)
                    .setParameter("providerPartnerStatusId", ProviderPartnerStatusConstants.approved.providerPartnerStatusChek())
                    .getResultList();
            if (providerPartners == null || providerPartners.isEmpty()) {
                log.debug("No trusted provider partner found for originatorProviderId {} and claimantProviderId {}", originatorProviderId, claimantProviderId);
                return false;
            }
        } catch (Exception e) {
            log.error("Error checking trusted provider partner for originatorProviderId {} and claimantProviderId {}", originatorProviderId, claimantProviderId, e);
            return false;
        }

        ProviderPartner providerPartner = providerPartners.get(0);
        if (providerPartner != null) {
            if (providerPartner.getRequesterProvider().getProviderId() == originatorProviderId && providerPartner.isIsTrustedPartnerForRequester()) {
                isPartnerTrusted = true;
            } else if (providerPartner.getCoordinatorProvider().getProviderId() == originatorProviderId && providerPartner.isIsTrustedPartnerForCoordinator()) {
                isPartnerTrusted = true;
            }
        }
        return isPartnerTrusted;
    }
}
