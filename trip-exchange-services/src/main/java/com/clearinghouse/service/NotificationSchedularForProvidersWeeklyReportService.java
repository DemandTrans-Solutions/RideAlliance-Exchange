package com.clearinghouse.service;

import com.clearinghouse.dao.NotificationDAO;
import com.clearinghouse.dao.ProviderDAO;
import com.clearinghouse.dao.TripTicketDAO;
import com.clearinghouse.dao.UserNotificationDataDAO;
import com.clearinghouse.dto.ProvidersWeeklyReportDTO;
import com.clearinghouse.entity.Notification;
import com.clearinghouse.entity.NotificationTemplate;
import com.clearinghouse.entity.User;
import com.clearinghouse.enumentity.NotificationStatus;
import com.clearinghouse.enumentity.NotificationTemplateCodeValue;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Year;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;

import static java.util.stream.Collectors.groupingBy;

/**
 *
 * @author shankarI
 */
@Service
@AllArgsConstructor
@Slf4j
public class NotificationSchedularForProvidersWeeklyReportService {

    private final TripTicketDAO tripTicketDAO;


    private final NotificationDAO notificationDAO;


    private final UserNotificationDataDAO userNotificationDataDAO;


    private final FileGenerateService fileGenerateService;

    private final ProviderDAO providerDAO;

    // ************newly added by shankarI for provider's weekly report

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<ProvidersWeeklyReportDTO> getSynchronizedWeeklyTripTickets() {

        final ZonedDateTime input = ZonedDateTime.now();
        String startTime = "17:01:00.000000";
        String endTime = "17:00:00.000000";

        final ZonedDateTime startOfLastWeek = input.minusWeeks(1).with(DayOfWeek.FRIDAY);
        String startDate = startOfLastWeek.format(DateTimeFormatter.ISO_LOCAL_DATE) + " " + startTime;

        final ZonedDateTime endOfLastWeek = startOfLastWeek.plusDays(7);
        String endDate = endOfLastWeek.format(DateTimeFormatter.ISO_LOCAL_DATE) + " " + endTime;

        log.debug("StartDateTime= " + startDate);
        log.debug("EndDateTime= " + endDate);

        List<ProvidersWeeklyReportDTO> tripTickets = tripTicketDAO.getWeeklyTripTicketsRecords(startDate, endDate);
        return tripTickets;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Scheduled(cron = "${cronExpression}", zone = "${cronExpressionTimeZone}") // second, minute, hour, day of month,
    // month, day of week[(mon-sun)(1-7)]
    public void sendMailToProvidersForWeeklyReport() {

        List<ProvidersWeeklyReportDTO> tripTicketList = getSynchronizedWeeklyTripTickets();

        // to get providerId as key with all list values Map<K,V>

        Map<Integer, Map<String, List<ProvidersWeeklyReportDTO>>> map = tripTicketList.parallelStream()
                .collect(groupingBy(ProvidersWeeklyReportDTO::getProviderId,
                        groupingBy(ProvidersWeeklyReportDTO::getProviderAs)));

        for (Entry<Integer, Map<String, List<ProvidersWeeklyReportDTO>>> entryIntKey : map.entrySet()) {

            List<User> usersOfClaimant = userNotificationDataDAO.getUsersForTripWeeklyReport(entryIntKey.getKey());

            Map<String, List<ProvidersWeeklyReportDTO>> ProvidersWeeklyReportDTOType = entryIntKey.getValue();

            List<String> filePathList = new ArrayList<String>();
            List<String> fileNameList = new ArrayList<String>();
            // write data into CSV
            for (Entry<String, List<ProvidersWeeklyReportDTO>> stringEntry : ProvidersWeeklyReportDTOType.entrySet()) {

                String filepath1 = null;
                String filepath2 = null;
                String providerName = providerDAO.getProviderNameById(entryIntKey.getKey());

                if (stringEntry.getKey().equalsIgnoreCase("Requester")) {
                    filepath1 = fileGenerateService.createCSVtoGenerateWeeklyReport(entryIntKey.getKey(),
                            stringEntry.getKey(), stringEntry.getValue());
                    filePathList.add(filepath1);
                    fileNameList.add(providerName + "_SubmittedTickets");
                }
                if (stringEntry.getKey().equalsIgnoreCase("Claimant")) {
                    filepath2 = fileGenerateService.createCSVtoGenerateWeeklyReport(entryIntKey.getKey(),
                            stringEntry.getKey(), stringEntry.getValue());
                    filePathList.add(filepath2);
                    fileNameList.add(providerName + "_ClaimedTickets");
                }
            }
            for (User user : usersOfClaimant) {
                // fetching single obj of the user role
			/*	List<UserAuthority> userAuthority = new ArrayList<>();
				userAuthority.addAll(user.getAuthorities());
				String userrole = userAuthority.get(0).getAuthority();
				*/
                if (user.isIsNotifyTripWeeklyReport()) {
                    // NotificationEnginePart.....
                    Notification emailNotification = new Notification();
                    NotificationTemplate notificationTemplate = new NotificationTemplate();
                    emailNotification.setEmailTo(user.getEmail());
                    emailNotification.setIsEMail(true);
                    emailNotification.setStatusId(NotificationStatus.newStatus.status());
                    notificationTemplate.setNotificationTemplateId(
                            NotificationTemplateCodeValue.weeklyReportCreated.templateCodeValue());
                    emailNotification.setNotificationTemplate(notificationTemplate);
                    emailNotification.setNumberOfAttempts(0);
                    emailNotification.setIsActive(true);

                    emailNotification.setEmailAttachment(true);

                    emailNotification.setFileNameList(fileNameList);
                    emailNotification.setFilePathList(filePathList);

                    Map providersWeeklyReportTemplateMap = new HashMap<String, String>();
                    providersWeeklyReportTemplateMap.put("name", user.getName());
                    providersWeeklyReportTemplateMap.put("message",
                            "The weekly summary of your activity on the Ride Alliance Trip Exchange is attached to this email. There is an attached report for all trips that you submitted and another attached report for all trips that you claimed. If you did not have any activity during the past week, no reports will be attached.");
                    providersWeeklyReportTemplateMap.put("year", Year.now().toString());

                    String jsonValueOfTemplate = "";

                    Iterator<Map.Entry<String, String>> entries1 = providersWeeklyReportTemplateMap.entrySet()
                            .iterator();
                    while (entries1.hasNext()) {

                        Map.Entry<String, String> entry1 = entries1.next();
                        jsonValueOfTemplate = jsonValueOfTemplate + "\"" + entry1.getKey() + "\":\"" + entry1.getValue()
                                + "\"";
                        if (entries1.hasNext()) {
                            jsonValueOfTemplate = jsonValueOfTemplate + ",";
                        }
                    }

                    String FinaljsonValueOfTemplate = "{" + jsonValueOfTemplate + "}";

                    emailNotification.setParameterValues(FinaljsonValueOfTemplate);
                    emailNotification.setSubject("Weekly report of your activity on the Ride Alliance Trip Exchange");
                    notificationDAO.createNotification(emailNotification);

                }
            }

        }

        sendMailToProvidersWeeklyReportForNoAnyTransactions();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Integer> getSynchronizedWeeklyTripTicketsForNoTransactions() {

        final ZonedDateTime input = ZonedDateTime.now();
        String startTime = "17:01:00.000000";
        String endTime = "17:00:00.000000";

        final ZonedDateTime startOfLastWeek = input.minusWeeks(1).with(DayOfWeek.FRIDAY);
        String startDate = startOfLastWeek.format(DateTimeFormatter.ISO_LOCAL_DATE) + " " + startTime;

        final ZonedDateTime endOfLastWeek = startOfLastWeek.plusDays(7);
        String endDate = endOfLastWeek.format(DateTimeFormatter.ISO_LOCAL_DATE) + " " + endTime;

        log.debug("StartDateTime= " + startDate);
        log.debug("EndDateTime= " + endDate);

        List<Integer> providersListForNotransactions = tripTicketDAO
                .sendMailToProvidersWeeklyReportForNoAnyTransactions(startDate, endDate);
        return providersListForNotransactions;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void sendMailToProvidersWeeklyReportForNoAnyTransactions() {

        List<Integer> providersIdsWithNoTransactionsForWeek = getSynchronizedWeeklyTripTicketsForNoTransactions();

        if (providersIdsWithNoTransactionsForWeek != null) {
            for (Integer providerId : providersIdsWithNoTransactionsForWeek) {
                List<User> usersOfClaimant = userNotificationDataDAO.getUsersForTripWeeklyReport(providerId);

                for (User user : usersOfClaimant) {
                    // fetching single obj of the user role
				/*	List<UserAuthority> userAuthority = new ArrayList<>();
					userAuthority.addAll(user.getAuthorities());
					String userrole = userAuthority.get(0).getAuthority();
				*/
                    if (user.isIsNotifyTripWeeklyReport()) {
                        // NotificationEnginePart.....
                        Notification emailNotification = new Notification();
                        NotificationTemplate notificationTemplate = new NotificationTemplate();
                        emailNotification.setEmailTo(user.getEmail());
                        emailNotification.setIsEMail(true);
                        emailNotification.setStatusId(NotificationStatus.newStatus.status());
                        notificationTemplate.setNotificationTemplateId(
                                NotificationTemplateCodeValue.weeklyReportCreated.templateCodeValue());
                        emailNotification.setNotificationTemplate(notificationTemplate);
                        emailNotification.setNumberOfAttempts(0);
                        emailNotification.setIsActive(true);

                        Map providersWeeklyReportTemplateMap = new HashMap<String, String>();
                        providersWeeklyReportTemplateMap.put("name", user.getName());
                        providersWeeklyReportTemplateMap.put("message",
                                "The weekly summary of your activity on the Ride Alliance Trip Exchange is attached to this email. There is an attached report for all trips that you submitted and another attached report for all trips that you claimed. If you did not have any activity during the past week, no reports will be attached.");
                        providersWeeklyReportTemplateMap.put("year", Year.now().toString());

                        String jsonValueOfTemplate = "";

                        Iterator<Map.Entry<String, String>> entries1 = providersWeeklyReportTemplateMap.entrySet()
                                .iterator();
                        while (entries1.hasNext()) {

                            Map.Entry<String, String> entry1 = entries1.next();
                            jsonValueOfTemplate = jsonValueOfTemplate + "\"" + entry1.getKey() + "\":\""
                                    + entry1.getValue() + "\"";
                            if (entries1.hasNext()) {
                                jsonValueOfTemplate = jsonValueOfTemplate + ",";
                            }

                        }
                        String FinaljsonValueOfTemplate = "{" + jsonValueOfTemplate + "}";

                        emailNotification.setParameterValues(FinaljsonValueOfTemplate);
                        emailNotification
                                .setSubject("Weekly report of your activity on the Ride Alliance Trip Exchange");
                        notificationDAO.createNotification(emailNotification);

                    }

                }

            }
        }
    }

}