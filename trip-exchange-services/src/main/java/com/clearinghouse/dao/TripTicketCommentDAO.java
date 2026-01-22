/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;


import com.clearinghouse.entity.Provider;
import com.clearinghouse.entity.TripTicketComment;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chaitanyaP
 */
@Repository
public class TripTicketCommentDAO extends AbstractDAO<Integer, TripTicketComment> {


    public List<TripTicketComment> findAllTripTicketCommentsByTripTicketId(int trip_ticket_id) {

        List<TripTicketComment> tripTicketComments = getEntityManager()
                .createQuery(" SELECT tc FROM TripTicketComment tc where tc.tripTicket.id =:trip_ticket_id ORDER BY tc.createdAt DESC ")
                .setParameter("trip_ticket_id", trip_ticket_id)
                .getResultList();
        return tripTicketComments;
    }


    public TripTicketComment findTripTicketCommentById(int id) {

        Object dbObj = getEntityManager()
                .createQuery(" SELECT tc FROM  TripTicketComment tc where tc.id =:id ")
                .setParameter("id", id)
                .getSingleResult();

        return (TripTicketComment) dbObj;
    }


    public TripTicketComment createTripTicketComment(TripTicketComment tripTicketComment) {

        add(tripTicketComment);
        return tripTicketComment;
    }


    public TripTicketComment updateTripTicketComment(TripTicketComment tripTicketComment) {

        return update(tripTicketComment);
    }


    public String getProviderName(int providerId) {
        Provider provider = (Provider) getEntityManager()
                .createQuery(" SELECT p  FROM  Provider p where p.providerId =:id ")
                .setParameter("id", providerId)
                .getSingleResult();

        return provider.getProviderName();
    }

}
