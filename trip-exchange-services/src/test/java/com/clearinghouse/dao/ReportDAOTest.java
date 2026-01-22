package com.clearinghouse.dao;

import com.clearinghouse.dto.CompletedTripReportDTO;
import com.clearinghouse.dto.ReportFilterDTO;
import com.clearinghouse.entity.TripTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
// removed unused Sql import
import org.springframework.test.context.TestPropertySource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(com.clearinghouse.dao.ReportDAO.class)
class ReportDAOTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ReportDAO reportDAO;

    private ReportFilterDTO mockReportFilter;
    private TripTicket mockTripTicket;

    @BeforeEach
    void setUp() {
        // ensure AbstractEntity timezone is set for tests (avoids NullPointerException in entity callbacks)
        com.clearinghouse.entity.AbstractEntity.setTimezone("UTC");
        mockReportFilter = new ReportFilterDTO();
        mockReportFilter.setFromDate("2025-01-01T00:00:00");
        mockReportFilter.setToDate("2025-12-31T23:59:59");
        // set provider id and parsed ZonedDateTime fields used by DAO
        mockReportFilter.setProviderId(1);
        java.time.ZonedDateTime from = java.time.ZonedDateTime.parse("2025-01-01T00:00:00Z");
        java.time.ZonedDateTime to = java.time.ZonedDateTime.parse("2025-12-31T23:59:59Z");
        mockReportFilter.setFromDateTime(from);
        mockReportFilter.setToDateTime(to);

        mockTripTicket = new TripTicket();
        mockTripTicket.setId(1);

        // Debug: print DB counts and sample tripticket rows to ensure data.sql executed
        try {
            Number tripCount = (Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM tripticket").getSingleResult();
            System.out.println("DEBUG DB tripticket count=" + tripCount.longValue());
            Number providerCount = (Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM provider").getSingleResult();
            System.out.println("DEBUG DB provider count=" + providerCount.longValue());
            @SuppressWarnings("unchecked")
            java.util.List<Object[]> rows = entityManager.createNativeQuery("SELECT trip_ticketid, requester_providerid, statusid, is_invisible, added_on, updated_on FROM tripticket").getResultList();
            System.out.println("DEBUG tripticket rows=" + rows.size());
            for (Object[] r : rows) {
                System.out.println("DEBUG row: id=" + r[0] + ", requester_providerid=" + r[1] + ", statusid=" + r[2] + ", is_invisible=" + r[3] + ", added_on=" + r[4] + ", updated_on=" + r[5]);
            }
        } catch (Exception e) {
            System.out.println("DEBUG error reading DB: " + e.getMessage());
        }
    }

    @Test
    void getTripTicketsByReportFilterWithoutCompleted_ShouldReturnFilteredTickets() {
    List<TripTicket> expectedTickets = entityManager.createQuery("SELECT t FROM TripTicket t WHERE t.tripTicketInvisible=false AND (t.createdAt BETWEEN :fromDate AND :toDate)  AND (t.originProvider.providerId=:providerId) AND (t.status.statusId NOT IN(7,11))", TripTicket.class)
        .setParameter("fromDate", mockReportFilter.getFromDateTime())
        .setParameter("toDate", mockReportFilter.getToDateTime())
        .setParameter("providerId", mockReportFilter.getProviderId())
        .getResultList();
        List<TripTicket> result = reportDAO.getTripTicketsByReportFilterWithoutCompleted(mockReportFilter);
        assertNotNull(result);
        // Debug output
        System.out.println("DEBUG expectedTickets.size()=" + expectedTickets.size());
        expectedTickets.forEach(t -> System.out.println("DEBUG expected ticket id=" + t.getId()));
        System.out.println("DEBUG dao result.size()=" + result.size());
        assertEquals(expectedTickets.size(), result.size());
        assertEquals(expectedTickets.get(0), result.get(0));
    }

    @Test
    void getCompletedReportDTOList_ShouldReturnCompletedReports() {
    List<CompletedTripReportDTO> expectedReports = entityManager.createQuery("SELECT NEW com.clearinghouse.dto.CompletedTripReportDTO(t.originProvider.providerName,COUNT(t.originProvider.providerId),0) FROM TripTicket t WHERE (t.status.statusId = 7 AND (t.updatedAt >= :fromDate AND t.updatedAt <= :toDate)) GROUP BY t.originProvider.providerId", CompletedTripReportDTO.class)
        .setParameter("fromDate", mockReportFilter.getFromDateTime())
        .setParameter("toDate", mockReportFilter.getToDateTime())
        .getResultList();
        List<CompletedTripReportDTO> result = reportDAO.getCompletedReportDTOList(mockReportFilter);
        assertNotNull(result);
        assertEquals(expectedReports.size(), result.size());
        assertEquals(expectedReports.get(0).getProviderName(), result.get(0).getProviderName());
    }

    @Test
    void getTotalNoOfTicketsCompletedReportDTOList_ShouldReturnCompletedTicketsCount() {
    List<CompletedTripReportDTO> expectedReports = entityManager.createQuery("SELECT NEW com.clearinghouse.dto.CompletedTripReportDTO(t.originProvider.providerName,0,COUNT(t.originProvider.providerId)) FROM TripTicket t WHERE (t.updatedAt >= :fromDate AND t.updatedAt <= :toDate) GROUP BY t.originProvider.providerId ORDER BY t.originProvider.providerName ASC", CompletedTripReportDTO.class)
        .setParameter("fromDate", mockReportFilter.getFromDateTime())
        .setParameter("toDate", mockReportFilter.getToDateTime())
        .getResultList();
        List<CompletedTripReportDTO> result = reportDAO.getTotalNoOfTicketsCompletedReportDTOList(mockReportFilter);
        assertNotNull(result);
        assertEquals(expectedReports.size(), result.size());
        assertEquals(expectedReports.get(0).getCompletedTicketCount(), result.get(0).getCompletedTicketCount());
    }

    @Test
    void getTripTicketsByReportFilterObj_ShouldReturnTripTickets() {
    List<TripTicket> expectedTickets = entityManager.createQuery("SELECT t FROM TripTicket t WHERE t.tripTicketInvisible=false AND t.status.statusId NOT IN(11) AND (t.createdAt >= :fromDate AND t.createdAt <= :toDate)  AND (t.originProvider.providerId=:providerId)", TripTicket.class)
        .setParameter("fromDate", mockReportFilter.getFromDateTime())
        .setParameter("toDate", mockReportFilter.getToDateTime())
        .setParameter("providerId", mockReportFilter.getProviderId())
        .getResultList();
        List<TripTicket> result = reportDAO.getTripTicketsByReportFilterObj(mockReportFilter);
        assertNotNull(result);
        assertEquals(expectedTickets.size(), result.size());
        assertEquals(expectedTickets.get(0), result.get(0));
    }

    @Test
    void countOfCompletedTickets_ShouldReturnCorrectCount() {
    Long expectedCount = entityManager.createQuery("SELECT COUNT(t) FROM TripTicket t WHERE (t.originProvider.providerId=:providerId) AND (t.status.statusId=7) AND t.createdAt >= :fromDate AND t.createdAt <= :toDate", Long.class)
        .setParameter("fromDate", mockReportFilter.getFromDateTime())
        .setParameter("toDate", mockReportFilter.getToDateTime())
        .setParameter("providerId", mockReportFilter.getProviderId())
        .getSingleResult();
        int result = reportDAO.countOfCompletedTickets(mockReportFilter);
        assertEquals(expectedCount.intValue(), result);
    }

    @Test
    void countOfTotalClaimsSubmitted_ShouldReturnCorrectCount() {
    Long expectedCount = entityManager.createQuery("SELECT COUNT(tc) FROM TripClaim tc WHERE (tc.claimantProvider.providerId=:providerId) AND tc.createdAt >= :fromDate AND tc.createdAt <= :toDate", Long.class)
        .setParameter("fromDate", mockReportFilter.getFromDateTime())
        .setParameter("toDate", mockReportFilter.getToDateTime())
        .setParameter("providerId", mockReportFilter.getProviderId())
        .getSingleResult();
        int result = reportDAO.countOfTotalClaimsSubmitted(mockReportFilter);
        assertEquals(expectedCount.intValue(), result);
    }
}
