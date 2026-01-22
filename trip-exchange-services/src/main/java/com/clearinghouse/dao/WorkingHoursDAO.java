package com.clearinghouse.dao;

import com.clearinghouse.entity.WorkingHours;
import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author shankarI
 */

@Repository
@lombok.extern.slf4j.Slf4j
public class WorkingHoursDAO extends AbstractDAO<Integer, WorkingHours> {


    public List<WorkingHours> getAllWorkingHours() {
        List<WorkingHours> workingHours = getEntityManager()
                .createQuery(" SELECT w FROM WorkingHours w order by w.provider.providerId")
                .getResultList();
        return workingHours;
    }


    public List<WorkingHours> findWorkingHoursByProviderId(int providerId) {
        List<WorkingHours> workingHoursByProviderIdList = getEntityManager()
                .createQuery(" SELECT w FROM WorkingHours w WHERE (w.provider.providerId=:providerId ) ORDER BY w.workingHoursId")
                .setParameter("providerId", providerId)
                .getResultList();
        return workingHoursByProviderIdList;
    }


    public List<WorkingHours> createWorkingHoursList(List<WorkingHours> workingHoursList) {

        List<WorkingHours> workingHoursSaveList = new ArrayList<>();

        for (WorkingHours workingHours : workingHoursList) {
            add(workingHours);
            workingHoursSaveList.add(workingHours);
        }
        return workingHoursSaveList;
    }


    public List<WorkingHours> updateWorkingHoursList(List<WorkingHours> newWorkingHoursList) {
        List<WorkingHours> workingHoursUpdatedList = new ArrayList<>();

        for (WorkingHours workingHours : newWorkingHoursList) {
            update(workingHours);
            workingHoursUpdatedList.add(workingHours);
        }
        return workingHoursUpdatedList;
    }

    public String getDayName(Date requestedDate) {
        if (requestedDate == null) {
            return null;
        }
        LocalDate localDate;
        if (requestedDate instanceof java.sql.Date) {
            localDate = ((java.sql.Date) requestedDate).toLocalDate();
        } else {
            localDate = requestedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        return dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }





    public WorkingHours getWorkingHourForDay(String dayName, int claimantProviderId) {

        WorkingHours workingHoursByDayNdProviderId = new WorkingHours();
        try {
            workingHoursByDayNdProviderId = (WorkingHours) getEntityManager()
                    .createQuery(" SELECT w FROM WorkingHours w WHERE (w.provider.providerId=:providerId) AND w.day=:dayOfWeek ")
                    .setParameter("providerId", claimantProviderId)
                    .setParameter("dayOfWeek", dayName)
                    .getSingleResult();
        } catch (NoResultException e) {
            log.debug("No working hours found for day {} and providerId {}", dayName, claimantProviderId);
            return null;
        }
        return workingHoursByDayNdProviderId;
    }


    public boolean checkPickupTimeInWorkingHours(int id, Time startTime, Time endTime) {
        boolean status = false;
        long result = (long) getEntityManager()
                .createQuery("SELECT COUNT(t.id) from TripTicket t WHERE t.id=:tripTicketId and t.requestedPickupTime BETWEEN :startTime AND :endTime ")
                .setParameter("tripTicketId", id)
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .getSingleResult();

        if (result != 0) {
            status = true;
        }

        return status;
    }


    public boolean checkDropOffTimeInWorkingHours(int id, Time startTime, Time endTime) {
        boolean status = false;
        long result = (long) getEntityManager()
                .createQuery("SELECT COUNT(t.id) from TripTicket t WHERE t.id=:tripTicketId and t.requestedDropOffTime BETWEEN :startTime AND :endTime ")
                .setParameter("tripTicketId", id)
                .setParameter("startTime", startTime)
                .setParameter("endTime", endTime)
                .getSingleResult();

        if (result != 0) {
            status = true;
        }

        return status;
    }
}
