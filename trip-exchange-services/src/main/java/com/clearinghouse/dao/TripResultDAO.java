/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;


import com.clearinghouse.entity.TripResult;
import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chaitanyaP
 */
@Repository
public class TripResultDAO extends AbstractDAO<Integer, TripResult> {


    public List<TripResult> findAllTripResultByTripTicketId(int trip_ticket_id) {

        List<TripResult> tripResults = getEntityManager()
                .createQuery(" SELECT tr FROM TripResult tr where tr.tripTicket.id = :id ")
                .setParameter("id", trip_ticket_id)
                .getResultList();
        return tripResults;
    }


    public TripResult createTripResult(TripResult tripResult) {
        add(tripResult);
        return tripResult;
    }


    public TripResult updateTripResult(TripResult tripResult) {

        return update(tripResult);
    }

    public TripResult findTripResultByTripResultId(int id) {

        return getByKey(id);
    }


    public TripResult updateTripTicket(TripResult tripResult) {

        return update(tripResult);
    }


    public void deletTripResult(int tripResultId) {
        var tripResult = findTripResultByTripResultId(tripResultId);
        delete(tripResult);
    }

    public TripResult findTripResultByTripTicketId(int trip_ticket_id) {
        try {
            TripResult tripResult = (TripResult) getEntityManager()
                    .createQuery("SELECT tr FROM TripResult tr where tr.tripTicket.id = :id ")
                    .setParameter("id", trip_ticket_id).getSingleResult();
            return tripResult;
        } catch (NoResultException e) {
            return null;
        }
    }

}
