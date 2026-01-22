package com.clearinghouse.service;

import com.clearinghouse.dao.NotificationDAO;
import com.clearinghouse.entity.Notification;
import com.clearinghouse.enumentity.NotificationStatus;
import com.clearinghouse.events.NotificationCreatedEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

/**
 * Consumes NotificationCreatedEvent and sends the notification immediately, without polling.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final NotificationDAO notificationDAO;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(NotificationCreatedEvent event) {
        try {
            // ATOMIC STATUS UPDATE: Only one thread will succeed in changing status from NEW to IN_PROGRESS
            int rowsUpdated = notificationDAO.claimNotificationForProcessing(
                event.notificationId(),
                NotificationStatus.newStatus.status(),
                NotificationStatus.inProgressStatus.status()
            );
            
            if (rowsUpdated == 0) {
                // Another thread already picked this up, skip processing
                log.debug("Notification {} already claimed by another thread, skipping", event.notificationId());
                return;
            }

            // Load a fresh copy inside the new transaction
            Notification notification = notificationDAO.findNotificationByNotificationId(event.notificationId());
            if (notification == null) {
                log.warn("Notification not found for id {}", event.notificationId());
                return;
            }

            log.debug("Async processing notification {}", notification.getNotificationId());

            boolean sent = false;
            InputStream from = null;
            try {
                if (notification.isIsSMS()) {
                    // TODO: implement SMS
                }
                if (notification.isIsEmail()) {
                    String strParameters = notification.getParameterValues();
                    ObjectMapper mapper = new ObjectMapper();
                    TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {};
                    from = new ByteArrayInputStream(strParameters.getBytes(StandardCharsets.UTF_8));
                    HashMap<String, Object> model = mapper.readValue(from, typeRef);
                    String templateName = notification.getNotificationTemplate().getTemplateName();
                    boolean isAttachment = notification.isEmailAttachment();
                    List<String> filename = notification.getFileNameList();
                    List<String> filepath = notification.getFilePathList();
                    sent = notificationService.sendMailWithTemplate(
                            notification.getEmailTo(),
                            notification.getEmailCC(),
                            notification.getEmailBCC(),
                            notification.getSubject(),
                            templateName,
                            model,
                            isAttachment,
                            filename,
                            filepath);
                }
            } catch (Exception exception) {
                sent = false;
                log.error("Async mail sending failed for notification {}: {}", notification.getNotificationId(), exception.getMessage(), exception);
            } finally {
                if (sent) {
                    notification.setStatusId(NotificationStatus.successStatus.status());
                } else {
                    notification.setStatusId(NotificationStatus.errorStatus.status());
                    notification.setNumberOfAttempts(notification.getNumberOfAttempts() + 1);
                }
                notificationDAO.updateNotification(notification);
            }
            log.debug("Async mail processing completed for {}", notification.getNotificationId());
        } catch (Exception e) {
            log.error("Async NotificationEventConsumer outer error: {}", e.getMessage(), e);
        }
    }
}
