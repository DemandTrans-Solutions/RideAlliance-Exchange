/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;


import com.clearinghouse.entity.TripClaim;
import com.clearinghouse.entity.TripTicket;
import com.clearinghouse.enumentity.TripClaimStatusConstants;
import org.springframework.stereotype.Repository;

import com.clearinghouse.entity.Provider;
import java.util.List;

/**
 * @author chaitanyaP
 */
@Repository
public class TripClaimDAO extends AbstractDAO<Integer, TripClaim> {


    public List<TripClaim> findAllTripClaims(int trip_ticket_id) {

        List<TripClaim> tripClaims = getEntityManager()
                .createQuery(" SELECT tc FROM TripClaim tc where  tc.tripTicket.id = :trip_ticket_id ORDER BY tc.status.description, tc.createdAt DESC ")
                .setParameter("trip_ticket_id", trip_ticket_id)
                .getResultList();
        return tripClaims;
    }


    public TripClaim findTripClaimByTripClaimId(int id) {

        return getByKey(id);
    }


    public TripClaim createTripTripClaim(TripClaim tripClaim) {

        add(tripClaim);
        return tripClaim;

    }


    public TripClaim updateTripClaim(TripClaim tripClaim) {

        return update(tripClaim);
    }


    public boolean isClaimPresent(int tripTicketId, int claimantProviderId) {
        Long countOfClaim = (Long) getEntityManager()
                .createQuery(" SELECT COUNT(*) FROM TripClaim tc where tc.tripTicket.id = :trip_ticket_id AND tc.claimantProvider.providerId=:claimantProviderId "
                        + " AND tc.status.statusId IN(:approved,:pending,:priceMismatch)")
                .setParameter("trip_ticket_id", tripTicketId)
                .setParameter("claimantProviderId", claimantProviderId)
                .setParameter("approved", TripClaimStatusConstants.approved.tripClaimStatusUpdate())
                .setParameter("pending", TripClaimStatusConstants.pending.tripClaimStatusUpdate())
                .setParameter("priceMismatch", TripClaimStatusConstants.priceMismatch.tripClaimStatusUpdate())
                .getSingleResult();
        boolean result = countOfClaim.intValue() > 0;
        return result;
    }


    public boolean checkForTripTicketPresentWithPendingStatus(int tripTicketId) {
        boolean resultFlag = false;
        var query = getEntityManager().createQuery(
                        " SELECT COUNT(tc.id) FROM TripClaim tc where  tc.tripTicket.id = :trip_ticket_id AND tc.status.statusId=:statusId ")
                .setParameter("trip_ticket_id", tripTicketId)
                .setParameter("statusId", TripClaimStatusConstants.pending.tripClaimStatusUpdate());
        long count = (long) query.getSingleResult();
        if (count > 0) {
            resultFlag = true;
        }
        return resultFlag;

    }


    public List<TripClaim> getTripClaimDataBytripTicketIdPendingStatus(int tripTicketId) {
        List<TripClaim> tripClaims = getEntityManager()
                .createQuery(" SELECT tc FROM TripClaim tc where  tc.tripTicket.id = :trip_ticket_id AND tc.status.statusId=:statusId ")
                .setParameter("trip_ticket_id", tripTicketId)
                .setParameter("statusId", TripClaimStatusConstants.pending.tripClaimStatusUpdate())
                .getResultList();
        return tripClaims;
    }


    /**
     * Find all TripTickets that have a TripClaim with the specified provider and status
     *
     * @param provider The provider to match against the claimantProvider
     * @param statusId The status ID to match against the claim status
     * @return List of TripTickets matching the criteria
     */
    public List<TripTicket> findTripTicketsByProviderAndExceptStatus(Provider provider, int statusId) {
        List<TripTicket> tripTickets = getEntityManager()
                .createQuery(
                        "SELECT DISTINCT tc.tripTicket FROM TripClaim tc " +
                                "WHERE tc.claimantProvider.providerId = :providerId " +
                                "AND tc.tripTicket.status.id <> :statusId " +
                                "ORDER BY tc.tripTicket.requestedPickupDate DESC", TripTicket.class)
                .setParameter("providerId", provider.getProviderId())
                .setParameter("statusId", statusId)
                .getResultList();

        return tripTickets;
    }


    public TripClaim deleteTripClaim(TripClaim tripClaim) {

        delete(tripClaim);
        return tripClaim;

    }


    public List<TripClaim> findAllTripClaimsForProvider(List<Integer> tripticketIds, int providerId) {

        List<TripClaim> tripClaims = getEntityManager()
                .createQuery(" SELECT tc FROM TripClaim tc where  tc.tripTicket.id IN(:trip_ticket_id) AND tc.claimantProvider.providerId=:providerId  ORDER BY tc.status.description, tc.createdAt DESC ")
                .setParameter("trip_ticket_id", tripticketIds)
                .setParameter("providerId", providerId)
                .getResultList();
        return tripClaims;
    }

}
