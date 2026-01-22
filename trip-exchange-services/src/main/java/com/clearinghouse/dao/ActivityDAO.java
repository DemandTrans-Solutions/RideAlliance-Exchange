/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;


import com.clearinghouse.entity.Activity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chaitanyaP
 */
@Repository
public class ActivityDAO extends AbstractDAO<Integer, Activity> {


    public List<Activity> findAllActivities() {

        List<Activity> activities = getEntityManager()
                .createQuery(" SELECT a FROM Activity a ")
                .getResultList();
        return activities;

    }


    public Activity findActivityByActivityId(int activityId) {

        return getByKey(activityId);
    }

    public Activity createActivity(Activity activity) {
        add(activity);
        return activity;
    }


    public List<Activity> findAllActivitesByTripTicketId(int tripTicketId) {

        List<Activity> activities = getEntityManager()
                .createQuery(" SELECT a FROM Activity a WHERE a.tripTicket.id =:tripTicketId ")
                .setParameter("tripTicketId", tripTicketId)
                .getResultList();
        return activities;
    }

}
