/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;

import com.clearinghouse.entity.TripChangeRequest;
import com.clearinghouse.enumentity.TripChangeRequestStatusConstants;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access for {@link TripChangeRequest}.
 */
@Repository
public class TripChangeRequestDAO extends AbstractDAO<Integer, TripChangeRequest> {

    public TripChangeRequest createTripChangeRequest(TripChangeRequest req) {
        add(req);
        return req;
    }

    public TripChangeRequest updateTripChangeRequest(TripChangeRequest req) {
        return update(req);
    }

    public TripChangeRequest findById(int id) {
        return getByKey(id);
    }

    public List<TripChangeRequest> findAllForTripTicket(int tripTicketId) {
        return getEntityManager()
                .createQuery(" SELECT r FROM TripChangeRequest r WHERE r.tripTicket.id = :tripTicketId "
                        + " ORDER BY r.createdAt DESC ", TripChangeRequest.class)
                .setParameter("tripTicketId", tripTicketId)
                .getResultList();
    }

    /** Returns true if there is already a pending change request for this trip ticket. */
    public boolean hasPendingRequest(int tripTicketId) {
        Long count = getEntityManager()
                .createQuery(" SELECT COUNT(r.id) FROM TripChangeRequest r WHERE r.tripTicket.id = :tripTicketId "
                        + " AND r.status.statusId = :pending ", Long.class)
                .setParameter("tripTicketId", tripTicketId)
                .setParameter("pending", TripChangeRequestStatusConstants.pending.tripChangeRequestStatusValue())
                .getSingleResult();
        return count != null && count > 0;
    }
}
