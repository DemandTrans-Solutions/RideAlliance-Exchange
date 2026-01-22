/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;


import com.clearinghouse.dao.NotificationDAO;
import com.clearinghouse.entity.Notification;
import com.clearinghouse.enumentity.NotificationStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author chaitanyaP
 */
@Service

@AllArgsConstructor
@Slf4j
public class NotificationEngineSchedulerService {
    private final NotificationService notificationService;
    private final NotificationDAO notificationDAO;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Notification> getPendingNotifications() {
        var allNotifications = notificationDAO.findAllNotificationForNewAndErrorStatus();
        for (Notification notification : allNotifications) {
            //updating the notification status to the inProgress.....
            notification.setStatusId(NotificationStatus.inProgressStatus.status());
            notificationDAO.updateNotification(notification);
        }
        return allNotifications;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    //@Scheduled(initialDelay = 1000, fixedDelay = 30000)
    public void mailScheduler() {
        try {
            List<Notification> allNotifications = getPendingNotifications();
            if ( !allNotifications.isEmpty() )  log.debug("Starting mail scheduler, total notifications to be sent: {}", allNotifications.size());
            for (Notification notification : allNotifications) {
                //creating input stream..
                InputStream from = null;
                boolean cheeckIsSent = false;
                try {
                    if (notification.isIsSMS()) {
                        //notification for SMS
                    }
                    if (notification.isIsEmail()) {
                        log.debug("Sending email to {} with notificationId {}", notification.getEmailTo(), notification.getNotificationId());
                        //notification for email
                        String strParameters = notification.getParameterValues();
                        ObjectMapper mapper = new ObjectMapper();
                        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
                        };
                        from = new ByteArrayInputStream(strParameters.getBytes(StandardCharsets.UTF_8));
                        HashMap<String, Object> model = mapper.readValue(from, typeRef);
                        String templateName = notification.getNotificationTemplate().getTemplateName();
                        //new added for csv
                        boolean isAttachment = notification.isEmailAttachment();
                        List<String> filename = notification.getFileNameList();
                        List<String> filepath = notification.getFilePathList();
                        cheeckIsSent = notificationService.sendMailWithTemplate(notification.getEmailTo(), notification.getEmailCC(), notification.getEmailBCC(), notification.getSubject(), templateName, model, isAttachment, filename, filepath);
                    }
                } catch (Exception exception) {
                    cheeckIsSent = false;
                    log.error("Mail sending is failed due to invalid mailId or Other Error.!! OR {}", exception.getMessage(), exception);
                } finally {
                    if (cheeckIsSent) {
                        notification.setStatusId(NotificationStatus.successStatus.status());
                    } else {
                        notification.setStatusId(NotificationStatus.errorStatus.status());
                        int noOfAttemptsCount = notification.getNumberOfAttempts();
                        noOfAttemptsCount++;
                        notification.setNumberOfAttempts(noOfAttemptsCount);
                        log.error("Mail sending is failed due to invalid mailId or Other Error.!! Please check credentials!!");
                    }
                }
                //updating status of the notification...
                notificationDAO.updateNotification(notification);
            }
            if ( !allNotifications.isEmpty() ) log.debug("Mail sending process completed successfully");
        } catch (Exception e) {
            log.error("Error in the outer try catch block: {}", e.getMessage(), e);
            //explictly killing excpetion cause nothing can be done in schedullar..!!
        }
        log.debug("Mail scheduler completed...");
    }
}
