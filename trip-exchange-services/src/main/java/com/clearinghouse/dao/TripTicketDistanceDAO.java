package com.clearinghouse.dao;

import com.clearinghouse.entity.TripTicketDistance;
import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author shankarI
 */

@Repository
public class TripTicketDistanceDAO extends AbstractDAO<Integer, TripTicketDistance> {


    public List<TripTicketDistance> findAllTripTicketDistance() {
        List<TripTicketDistance> TripTicketDistances = getEntityManager()
                .createQuery("SELECT t FROM TripTicketDistance t order by t.tripTicketDistanceId ").getResultList();
        return TripTicketDistances;
    }


    public TripTicketDistance createTripTicketDistance(TripTicketDistance tripTicketDistanceBO) {
        add(tripTicketDistanceBO);
        return tripTicketDistanceBO;
    }


    public TripTicketDistance updateTripTicketDistance(TripTicketDistance tripTicketDistanceBO) {
        update(tripTicketDistanceBO);
        return tripTicketDistanceBO;
    }


    public TripTicketDistance getDistanceByTripTicketId(int tripTicketId) {
        try {
            TripTicketDistance TripTicketDistanceById = (TripTicketDistance) getEntityManager()
                    .createQuery(" SELECT t FROM TripTicketDistance t WHERE (t.tripTicket.id=:tripTicketId )")
                    .setParameter("tripTicketId", tripTicketId).getSingleResult();

            return TripTicketDistanceById;
        } catch (NoResultException e) {
            return null;
        }
    }


    public TripTicketDistance saveDistanceTime(TripTicketDistance tripTicketDistance) {
        add(tripTicketDistance);
        return tripTicketDistance;
    }

}
