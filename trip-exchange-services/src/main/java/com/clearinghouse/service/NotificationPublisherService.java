package com.clearinghouse.service;

import com.clearinghouse.dao.NotificationDAO;
import com.clearinghouse.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Wrapper service to persist a Notification and trigger the async processing pipeline.
 *
 * Note: Currently, NotificationDAO publishes the NotificationCreatedEvent after persist/flush
 * to maintain backward compatibility with existing call sites. This wrapper provides a
 * single entry point for new code. Over time, event publishing can be moved exclusively
 * to this service and removed from the DAO.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisherService {

    private final NotificationDAO notificationDAO;

    @Transactional
    public Notification createAndPublish(Notification notification) {
        log.debug("Creating and publishing notification via wrapper");
        // DAO handles persist + event publish (after flush)
        return notificationDAO.createNotification(notification);
    }
}
