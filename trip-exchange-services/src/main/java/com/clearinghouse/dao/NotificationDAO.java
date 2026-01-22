/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;


import com.clearinghouse.entity.Notification;
import com.clearinghouse.events.NotificationCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chaitanyaP
 */
@Repository
public class NotificationDAO extends AbstractDAO<Integer, Notification> {

    @Autowired
    private ApplicationEventPublisher eventPublisher;



    public List<Notification> findAllNotificationForNewAndErrorStatus() {

    List<Notification> notifications = getEntityManager()
        .createQuery("SELECT n FROM Notification n where (statusId=1 OR (statusId=4 AND numberOfAttempts<3)) ", Notification.class)
        .setMaxResults(10)
        .getResultList();
        return notifications;

    }

    public Notification findNotificationByNotificationId(int notificationId) {
        return getByKey(notificationId);
    }

    /**
     * Atomically update notification status from NEW to IN_PROGRESS.
     * Returns the number of rows updated (1 if successful, 0 if already claimed).
     */
    public int claimNotificationForProcessing(int notificationId, int newStatusId, int inProgressStatusId) {
        return getEntityManager()
            .createQuery("UPDATE Notification n SET n.statusId = :inProgress WHERE n.notificationId = :id AND n.statusId = :newStatus")
            .setParameter("inProgress", inProgressStatusId)
            .setParameter("id", notificationId)
            .setParameter("newStatus", newStatusId)
            .executeUpdate();
    }

    public Notification createNotification(Notification notification) {
        add(notification);
        // Ensure ID is generated for IDENTITY strategies before publishing event
        getEntityManager().flush();
        // Publish event so async consumer can send immediately after commit

        if (eventPublisher != null) {
            eventPublisher.publishEvent(NotificationCreatedEvent.from(notification));
        }

        return notification;

    }


    public Notification updateNotification(Notification notification) {

        Notification notificationTemp = update(notification);
        return notificationTemp;

    }


    public void deleteUserByUserId(int notificationId) {

        Notification notification = (Notification) getEntityManager()
                .createQuery("SELECT n FROM Notification n WHERE n.notificationId = :notificationId")
                .setParameter("notificationId", notificationId)
                .getSingleResult();
        notification.setIsActive(false);

    }

}
