/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;


import com.clearinghouse.dto.CompletedTripReportDTO;
import com.clearinghouse.dto.ReportFilterDTO;
import com.clearinghouse.entity.TripTicket;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * @author chaitanyaP
 */
@Repository
@Slf4j
public class ReportDAO extends AbstractDAO<Integer, Object> {

    private ZonedDateTime parseDateTime(String dateTimeString) {
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString.replace(" ", "T"));
            return localDateTime.atZone(ZoneId.systemDefault());
        } catch (DateTimeParseException e) {
            log.error("Error parsing date string: {}", dateTimeString, e);
            return null; // Or throw a custom exception
        }
    }


    public String findOldestCreatedDate(int providerId) {
        TripTicket tripTicket = new TripTicket();
        try {
            tripTicket = (TripTicket) getEntityManager()
                    .createQuery("SELECT  t FROM TripTicket  t  WHERE ((t.status.statusId != 7) AND (t.originProvider.providerId=:providerId )) ORDER BY t.createdAt ASC ")
                    .setParameter("providerId", providerId)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (Exception e) {
            log.error("Error finding oldest created date for providerId {}", providerId, e);
            return null;
        }
        return tripTicket.getCreatedAt().toString();

    }


    // All methods now use fromDateTime and toDateTime (ZonedDateTime) fields from ReportFilterDTO
    public List<TripTicket> getTripTicketsByReportFilterObj(ReportFilterDTO reportFilterDTOObj) {
        List<String> ticketStatusList = reportFilterDTOObj.getReportTicketFilterStatus();
        String providerId = Integer.toString(reportFilterDTOObj.getProviderId());
        ZonedDateTime fromDate = reportFilterDTOObj.getFromDateTime();
        ZonedDateTime toDate = reportFilterDTOObj.getToDateTime();

        StringBuilder queryString = new StringBuilder("SELECT t FROM TripTicket t WHERE t.tripTicketInvisible=false AND t.status.statusId NOT IN(11) AND (t.createdAt >= :fromDate AND t.createdAt <= :toDate)  AND (t.originProvider.providerId=:providerId)");

        if (ticketStatusList != null && !ticketStatusList.isEmpty() && !ticketStatusList.get(0).trim().isEmpty()) {
            queryString.append(" AND (");
            for (int i = 0; i < ticketStatusList.size(); i++) {
                queryString.append("t.status.statusId = :statusId" + i);
                if (i < ticketStatusList.size() - 1) {
                    queryString.append(" OR ");
                }
            }
            queryString.append(")");
        }
        var query = getEntityManager().createQuery(queryString.toString(), TripTicket.class)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate)
                .setParameter("providerId", reportFilterDTOObj.getProviderId());
        if (ticketStatusList != null && !ticketStatusList.isEmpty() && !ticketStatusList.get(0).trim().isEmpty()) {
            for (int i = 0; i < ticketStatusList.size(); i++) {
                query.setParameter("statusId" + i, ticketStatusList.get(i));
            }
        }
        return query.getResultList();
    }

    public List<TripTicket> getTripTicketsByReportFilterWithoutCompleted(ReportFilterDTO reportFilterDTOObj) {
        ZonedDateTime fromDate = reportFilterDTOObj.getFromDateTime();
        ZonedDateTime toDate = reportFilterDTOObj.getToDateTime();
        String queryString = "SELECT t FROM TripTicket t WHERE t.tripTicketInvisible=false AND (t.createdAt BETWEEN :fromDate AND :toDate)  AND (t.originProvider.providerId=:providerId) AND (t.status.statusId NOT IN(7,11))";
        return getEntityManager().createQuery(queryString, TripTicket.class)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate)
                .setParameter("providerId", reportFilterDTOObj.getProviderId())
                .getResultList();
    }

    public int countOfTotalTickets(ReportFilterDTO reportFilterDTOObj) {
        ZonedDateTime fromDate = reportFilterDTOObj.getFromDateTime();
        ZonedDateTime toDate = reportFilterDTOObj.getToDateTime();
        StringBuilder queryString = new StringBuilder("SELECT COUNT(*) FROM TripTicket t WHERE t.tripTicketInvisible=false AND t.status.statusId NOT IN(11)");
        if (fromDate != null) {
            queryString.append(" AND t.createdAt >= :fromDate");
        }
        if (toDate != null) {
            queryString.append(" AND t.createdAt <= :toDate");
        }
        queryString.append(" AND (t.originProvider.providerId=:providerId)");
        var query = getEntityManager().createQuery(queryString.toString());
        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        query.setParameter("providerId", reportFilterDTOObj.getProviderId());
        Object tripTicketsCount = query.getSingleResult();
        return Integer.parseInt(tripTicketsCount.toString());
    }

    public int countOfApprovedTickets(ReportFilterDTO reportFilterDTOObj) {
        ZonedDateTime fromDate = reportFilterDTOObj.getFromDateTime();
        ZonedDateTime toDate = reportFilterDTOObj.getToDateTime();
        StringBuilder queryString = new StringBuilder("SELECT COUNT(*) FROM TripTicket t WHERE (t.originProvider.providerId=:providerId) AND (t.status.statusId=1)");
        if (fromDate != null) {
            queryString.append(" AND t.createdAt >= :fromDate");
        }
        if (toDate != null) {
            queryString.append(" AND t.createdAt <= :toDate");
        }
        var query = getEntityManager().createQuery(queryString.toString());
        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        query.setParameter("providerId", reportFilterDTOObj.getProviderId());
        Object tripTicketsCount = query.getSingleResult();
        return Integer.parseInt(tripTicketsCount.toString());
    }

    public int countOfRescindedTickets(ReportFilterDTO reportFilterDTOObj) {
        ZonedDateTime fromDate = reportFilterDTOObj.getFromDateTime();
        ZonedDateTime toDate = reportFilterDTOObj.getToDateTime();
        StringBuilder queryString = new StringBuilder("SELECT COUNT(*) FROM TripTicket t WHERE t.tripTicketInvisible=false AND (t.originProvider.providerId=:providerId) AND (t.status.statusId=12)");
        if (fromDate != null) {
            queryString.append(" AND t.createdAt >= :fromDate");
        }
        if (toDate != null) {
            queryString.append(" AND t.createdAt <= :toDate");
        }
        var query = getEntityManager().createQuery(queryString.toString());
        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        query.setParameter("providerId", reportFilterDTOObj.getProviderId());
        Object tripTicketsCount = query.getSingleResult();
        return Integer.parseInt(tripTicketsCount.toString());
    }

    public int countOfAvaialbleTickets(ReportFilterDTO reportFilterDTOObj) {
        ZonedDateTime fromDate = reportFilterDTOObj.getFromDateTime();
        ZonedDateTime toDate = reportFilterDTOObj.getToDateTime();
        StringBuilder queryString = new StringBuilder("SELECT COUNT(*) FROM TripTicket t WHERE (t.originProvider.providerId=:providerId) AND (t.status.statusId=2)");
        if (fromDate != null) {
            queryString.append(" AND t.createdAt >= :fromDate");
        }
        if (toDate != null) {
            queryString.append(" AND t.createdAt <= :toDate");
        }
        var query = getEntityManager().createQuery(queryString.toString());
        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        query.setParameter("providerId", reportFilterDTOObj.getProviderId());
        Object tripTicketsCount = query.getSingleResult();
        return Integer.parseInt(tripTicketsCount.toString());
    }

    public int countOfExpiredTickets(ReportFilterDTO reportFilterDTOObj) {
        ZonedDateTime fromDate = reportFilterDTOObj.getFromDateTime();
        ZonedDateTime toDate = reportFilterDTOObj.getToDateTime();
        StringBuilder queryString = new StringBuilder("SELECT COUNT(*) FROM TripTicket t WHERE t.tripTicketInvisible=false AND (t.originProvider.providerId=:providerId) AND (t.status.statusId=9)");
        if (fromDate != null) {
            queryString.append(" AND t.createdAt >= :fromDate");
        }
        if (toDate != null) {
            queryString.append(" AND t.createdAt <= :toDate");
        }
        var query = getEntityManager().createQuery(queryString.toString());
        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        query.setParameter("providerId", reportFilterDTOObj.getProviderId());
        Object tripTicketsCount = query.getSingleResult();
        return Integer.parseInt(tripTicketsCount.toString());
    }

    public int countOfCompletedTickets(ReportFilterDTO reportFilterDTOObj) {
        ZonedDateTime fromDate = reportFilterDTOObj.getFromDateTime();
        ZonedDateTime toDate = reportFilterDTOObj.getToDateTime();
        StringBuilder queryString = new StringBuilder("SELECT COUNT(*) FROM TripTicket t WHERE (t.originProvider.providerId=:providerId) AND (t.status.statusId=7)");
        if (fromDate != null) {
            queryString.append(" AND t.createdAt >= :fromDate");
        }
        if (toDate != null) {
            queryString.append(" AND t.createdAt <= :toDate");
        }
        var query = getEntityManager().createQuery(queryString.toString());
        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        query.setParameter("providerId", reportFilterDTOObj.getProviderId());
        Object tripTicketsCount = query.getSingleResult();
        return Integer.parseInt(tripTicketsCount.toString());
    }

    public int countOfTotalClaimsSubmitted(ReportFilterDTO reportFilterDTOObj) {
        ZonedDateTime fromDate = reportFilterDTOObj.getFromDateTime();
        ZonedDateTime toDate = reportFilterDTOObj.getToDateTime();
        StringBuilder queryString = new StringBuilder("SELECT COUNT(*) FROM TripClaim tc WHERE (tc.claimantProvider.providerId=:providerId)");
        if (fromDate != null) {
            queryString.append(" AND tc.createdAt >= :fromDate");
        }
        if (toDate != null) {
            queryString.append(" AND tc.createdAt <= :toDate");
        }
        var query = getEntityManager().createQuery(queryString.toString());
        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        query.setParameter("providerId", reportFilterDTOObj.getProviderId());
        Long tripClaimsCount = (Long) query.getSingleResult();
        return tripClaimsCount.intValue();
    }

    public int countOfClaimPending(ReportFilterDTO reportFilterDTOObj) {
        ZonedDateTime fromDate = reportFilterDTOObj.getFromDateTime();
        ZonedDateTime toDate = reportFilterDTOObj.getToDateTime();
        StringBuilder queryString = new StringBuilder("SELECT COUNT(*) FROM TripClaim tc WHERE (tc.claimantProvider.providerId=:providerId) AND (tc.status.statusId=14)");
        if (fromDate != null) {
            queryString.append(" AND tc.createdAt >= :fromDate");
        }
        if (toDate != null) {
            queryString.append(" AND tc.createdAt <= :toDate");
        }
        var query = getEntityManager().createQuery(queryString.toString());
        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        query.setParameter("providerId", reportFilterDTOObj.getProviderId());
        Object tripTicketsCount = query.getSingleResult();
        return Integer.parseInt(tripTicketsCount.toString());
    }

    public int countOfClaimDeclined(ReportFilterDTO reportFilterDTOObj) {
        ZonedDateTime fromDate = reportFilterDTOObj.getFromDateTime();
        ZonedDateTime toDate = reportFilterDTOObj.getToDateTime();
        StringBuilder queryString = new StringBuilder("SELECT COUNT(*) FROM TripClaim tc WHERE (tc.claimantProvider.providerId=:providerId) AND (tc.status.statusId=8)");
        if (fromDate != null) {
            queryString.append(" AND tc.createdAt >= :fromDate");
        }
        if (toDate != null) {
            queryString.append(" AND tc.createdAt <= :toDate");
        }
        var query = getEntityManager().createQuery(queryString.toString());
        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        query.setParameter("providerId", reportFilterDTOObj.getProviderId());
        Object tripTicketsCount = query.getSingleResult();
        return Integer.parseInt(tripTicketsCount.toString());
    }

    public int countOfClaimRescinded(ReportFilterDTO reportFilterDTOObj) {
        ZonedDateTime fromDate = reportFilterDTOObj.getFromDateTime();
        ZonedDateTime toDate = reportFilterDTOObj.getToDateTime();
        StringBuilder queryString = new StringBuilder("SELECT COUNT(*) FROM TripClaim tc WHERE (tc.claimantProvider.providerId=:providerId) AND (tc.status.statusId=12)");
        if (fromDate != null) {
            queryString.append(" AND tc.createdAt >= :fromDate");
        }
        if (toDate != null) {
            queryString.append(" AND tc.createdAt <= :toDate");
        }
        var query = getEntityManager().createQuery(queryString.toString());
        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        query.setParameter("providerId", reportFilterDTOObj.getProviderId());
        Object tripTicketsCount = query.getSingleResult();
        return Integer.parseInt(tripTicketsCount.toString());
    }

    public int countOfClaimApproved(ReportFilterDTO reportFilterDTOObj) {
        ZonedDateTime fromDate = reportFilterDTOObj.getFromDateTime();
        ZonedDateTime toDate = reportFilterDTOObj.getToDateTime();
        StringBuilder queryString = new StringBuilder("SELECT COUNT(*) FROM TripClaim tc WHERE (tc.claimantProvider.providerId=:providerId) AND (tc.status.statusId=1)");
        if (fromDate != null) {
            queryString.append(" AND tc.createdAt >= :fromDate");
        }
        if (toDate != null) {
            queryString.append(" AND tc.createdAt <= :toDate");
        }
        var query = getEntityManager().createQuery(queryString.toString());
        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        query.setParameter("providerId", reportFilterDTOObj.getProviderId());
        Object tripTicketsCount = query.getSingleResult();
        return Integer.parseInt(tripTicketsCount.toString());
    }

    public List<CompletedTripReportDTO> getCompletedReportDTOList(ReportFilterDTO reportFilterDTO) {
        ZonedDateTime fromDate = reportFilterDTO.getFromDateTime();
        ZonedDateTime toDate = reportFilterDTO.getToDateTime();
        TypedQuery<CompletedTripReportDTO> query = getEntityManager()
                .createQuery("SELECT NEW com.clearinghouse.dto.CompletedTripReportDTO(t.originProvider.providerName,COUNT(t.originProvider.providerId),0) FROM TripTicket t WHERE (t.status.statusId = 7 AND(t.updatedAt >= :fromDate AND t.updatedAt <= :toDate)) GROUP BY t.originProvider.providerId ", CompletedTripReportDTO.class)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate);
        return query.getResultList();
    }

    public List<CompletedTripReportDTO> getTotalNoOfTicketsCompletedReportDTOList(ReportFilterDTO reportFilterDTO) {
        ZonedDateTime fromDate = reportFilterDTO.getFromDateTime();
        ZonedDateTime toDate = reportFilterDTO.getToDateTime();
        TypedQuery<CompletedTripReportDTO> query = getEntityManager()
                .createQuery("SELECT NEW com.clearinghouse.dto.CompletedTripReportDTO(t.originProvider.providerName,0,COUNT(t.originProvider.providerId)) FROM TripTicket t WHERE (t.updatedAt >= :fromDate AND t.updatedAt <= :toDate) GROUP BY t.originProvider.providerId ORDER BY t.originProvider.providerName ASC", CompletedTripReportDTO.class)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate);
        return query.getResultList();
    }

    public List<TripTicket> getTripTicketsForStatusesReportFilter(ReportFilterDTO reportFilterDTOObj) {
        List<String> ticketStatusList = reportFilterDTOObj.getReportTicketFilterStatus();
        ZonedDateTime fromDate = reportFilterDTOObj.getFromDateTime();
        ZonedDateTime toDate = reportFilterDTOObj.getToDateTime();
        StringBuilder queryString = new StringBuilder("SELECT t FROM TripTicket t WHERE (t.createdAt >= :fromDate AND t.createdAt <= :toDate)  AND t.originProvider.providerId " + reportFilterDTOObj.getInClauseQuery());
        if ( reportFilterDTOObj.getInClauseQuery() == null || reportFilterDTOObj.getInClauseQuery().trim().isEmpty()) {
            queryString = new StringBuilder("SELECT t FROM TripTicket t WHERE (t.createdAt >= :fromDate AND t.createdAt <= :toDate)");
        }

        if (ticketStatusList != null && !ticketStatusList.isEmpty() && !ticketStatusList.get(0).trim().isEmpty()) {
            queryString.append(" AND (");
            for (int i = 0; i < ticketStatusList.size(); i++) {
                queryString.append("t.status.statusId = :statusId" + i);
                if (i < ticketStatusList.size() - 1) {
                    queryString.append(" OR ");
                }
            }
            queryString.append(")");
        }
        var query = getEntityManager().createQuery(queryString.toString(), TripTicket.class)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate);
        if (ticketStatusList != null && !ticketStatusList.isEmpty() && !ticketStatusList.get(0).trim().isEmpty()) {
            for (int i = 0; i < ticketStatusList.size(); i++) {
                query.setParameter("statusId" + i, ticketStatusList.get(i));
            }
        }
        return query.getResultList();
    }

    /**
     * Like {@link #getTripTicketsForStatusesReportFilter} but scopes rows by the CLAIMING provider:
     * a trip is included for the filtered provider id(s) when the approved claim's claimant matches,
     * or — when there is no approved claim — when the originating provider matches. This is used by
     * the Provider Monthly Trips report only; the completed/cancelled reports remain originator-scoped.
     *
     * <p>The provider id(s) come from the same {@code getInClauseQuery()} IN-clause (e.g. " IN (1, 2) ").
     *
     * <p>NOT YET TESTED — wired and ready, but the report currently runs originator-based; enable
     * via the commented claimant entry point in ReportService.
     */
    public List<TripTicket> getTripTicketsForStatusesReportFilterByClaimant(ReportFilterDTO reportFilterDTOObj) {
        List<String> ticketStatusList = reportFilterDTOObj.getReportTicketFilterStatus();
        ZonedDateTime fromDate = reportFilterDTOObj.getFromDateTime();
        ZonedDateTime toDate = reportFilterDTOObj.getToDateTime();
        String inClause = reportFilterDTOObj.getInClauseQuery();

        StringBuilder queryString = new StringBuilder(
                "SELECT t FROM TripTicket t WHERE (t.createdAt >= :fromDate AND t.createdAt <= :toDate)");
        if (inClause != null && !inClause.trim().isEmpty()) {
            // Claimant if there is an approved claim; otherwise fall back to the originating provider.
            queryString.append(" AND ( (t.approvedTripClaim IS NOT NULL AND t.approvedTripClaim.claimantProvider.providerId ")
                    .append(inClause)
                    .append(") OR (t.approvedTripClaim IS NULL AND t.originProvider.providerId ")
                    .append(inClause)
                    .append(") )");
        }

        if (ticketStatusList != null && !ticketStatusList.isEmpty() && !ticketStatusList.get(0).trim().isEmpty()) {
            queryString.append(" AND (");
            for (int i = 0; i < ticketStatusList.size(); i++) {
                queryString.append("t.status.statusId = :statusId" + i);
                if (i < ticketStatusList.size() - 1) {
                    queryString.append(" OR ");
                }
            }
            queryString.append(")");
        }
        var query = getEntityManager().createQuery(queryString.toString(), TripTicket.class)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate);
        if (ticketStatusList != null && !ticketStatusList.isEmpty() && !ticketStatusList.get(0).trim().isEmpty()) {
            for (int i = 0; i < ticketStatusList.size(); i++) {
                query.setParameter("statusId" + i, ticketStatusList.get(i));
            }
        }
        return query.getResultList();
    }
}
