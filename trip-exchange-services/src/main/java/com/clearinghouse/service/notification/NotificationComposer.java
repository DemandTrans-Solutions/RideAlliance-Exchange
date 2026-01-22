package com.clearinghouse.service.notification;

import com.clearinghouse.dao.NotificationDAO;
import com.clearinghouse.entity.Notification;
import com.clearinghouse.entity.NotificationTemplate;
import com.clearinghouse.entity.TripTicket;
import com.clearinghouse.enumentity.NotificationStatus;
import com.clearinghouse.service.FileGenerateService;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralizes creating Notification entity from a NotificationRequest and enqueuing it.
 */
public class NotificationComposer {

    private final NotificationDAO notificationDAO;
    private final FileGenerateService fileGenerateService;

    public NotificationComposer(NotificationDAO notificationDAO, FileGenerateService fileGenerateService) {
        this.notificationDAO = notificationDAO;
        this.fileGenerateService = fileGenerateService;
    }

    public void enqueue(NotificationRequest req, TripTicket ticketForCsvIfAny) {
        Notification n = new Notification();
        n.setEmailTo(req.email());
        n.setIsEMail(true);
        n.setStatusId(NotificationStatus.newStatus.status());
        NotificationTemplate template = new NotificationTemplate();
        template.setNotificationTemplateId(req.templateCode().templateCodeValue());
        n.setNotificationTemplate(template);
        n.setNumberOfAttempts(0);
        n.setIsActive(true);
        n.setParameterValues(toJson(req.params()));
        if (req.subject() != null) {
            n.setSubject(req.subject());
        }
        if (req.attachCsv() && ticketForCsvIfAny != null) {
            String filepath = fileGenerateService.createCSVForClaimApprovedDeclineRescind(ticketForCsvIfAny, n);
            n.setEmailAttachment(true);
            List<String> filePathList = new ArrayList<>();
            List<String> fileNameList = new ArrayList<>();
            filePathList.add(filepath);
            fileNameList.add(req.csvBaseName() != null ? req.csvBaseName() : "TripTicket");
            n.setFilePathList(filePathList);
            n.setFileNameList(fileNameList);
        }
        notificationDAO.createNotification(n);
    }

    private static String toJson(java.util.Map<String, String> params) {
        StringBuilder sb = new StringBuilder("{");
        var it = params.entrySet().iterator();
        while (it.hasNext()) {
            var e = it.next();
            sb.append("\"").append(e.getKey()).append("\":\"").append(e.getValue()).append("\"");
            if (it.hasNext()) sb.append(",");
        }
        sb.append("}");
        return sb.toString();
    }
}
