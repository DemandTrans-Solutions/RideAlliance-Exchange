package com.clearinghouse.service;

import com.clearinghouse.dao.ReportDAO;
import com.clearinghouse.dao.TripTicketDAO;
import com.clearinghouse.dto.*;
import com.clearinghouse.entity.Provider;
import com.clearinghouse.entity.TripTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.sql.Time;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportServiceTest {

    @Mock
    private ReportDAO reportDAO;

    @Mock
    private TripTicketDAO tripTicketDAO;

    private ModelMapper tripTicketModelMapper;

    private ModelMapper providerModelMapper;

    @org.mockito.Mock
    private com.clearinghouse.service.ProviderPartnerService providerPartnerService;

    private ReportService reportService;

    // MockitoExtension will initialize mocks; no manual setup required
    @BeforeEach
    void setUp() {
        // Use small stub ModelMapper implementations so map(...) always returns non-null DTOs
        tripTicketModelMapper = new ModelMapper() {
            @Override
            public <D> D map(Object source, Class<D> destinationType) {
                if (destinationType == DetailedTripTicketDTO.class) {
                    return destinationType.cast(new DetailedTripTicketDTO());
                }
                return null;
            }
        };

        providerModelMapper = new ModelMapper() {
            @Override
            public <D> D map(Object source, Class<D> destinationType) {
                if (destinationType == ProviderDTO.class) {
                    Provider p = (Provider) source;
                    ProviderDTO pdto = new ProviderDTO();
                    if (p != null) pdto.setProviderName(p.getProviderName());
                    return destinationType.cast(pdto);
                }
                return null;
            }
        };

        // Construct ReportService explicitly so it gets our concrete mappers
        reportService = new ReportService(reportDAO, tripTicketDAO, tripTicketModelMapper, providerModelMapper, providerPartnerService);
    }

    @Test
    void findOldestCreatedDate_ReturnsDate() {
        int providerId = 1;
        String expectedDate = "2023-01-01";
        when(reportDAO.findOldestCreatedDate(providerId)).thenReturn(expectedDate);

        String result = reportService.findOldestCreatedDate(providerId);

        assertEquals(expectedDate, result);
        verify(reportDAO).findOldestCreatedDate(providerId);
    }

    @Test
    void findSummaryReport_ReturnsSummary() {
        ReportFilterDTO reportFilterDTO = new ReportFilterDTO();
        reportFilterDTO.setFromDate("2023-01-01T00:00:00");
        reportFilterDTO.setToDate("2023-12-31T23:59:59");

        when(reportDAO.countOfTotalTickets(reportFilterDTO)).thenReturn(100);
        when(reportDAO.countOfRescindedTickets(reportFilterDTO)).thenReturn(10);
        when(reportDAO.countOfAvaialbleTickets(reportFilterDTO)).thenReturn(20);
        when(reportDAO.countOfApprovedTickets(reportFilterDTO)).thenReturn(30);
        when(reportDAO.countOfExpiredTickets(reportFilterDTO)).thenReturn(5);
        when(reportDAO.countOfCompletedTickets(reportFilterDTO)).thenReturn(35);
        when(reportDAO.countOfTotalClaimsSubmitted(reportFilterDTO)).thenReturn(50);
        when(reportDAO.countOfClaimApproved(reportFilterDTO)).thenReturn(25);
        when(reportDAO.countOfClaimPending(reportFilterDTO)).thenReturn(15);
        when(reportDAO.countOfClaimRescinded(reportFilterDTO)).thenReturn(5);
        when(reportDAO.countOfClaimDeclined(reportFilterDTO)).thenReturn(5);

    // Return mocked TripTicket instances with non-null tripClaims to avoid NPE in service
    TripTicket tt1 = mock(TripTicket.class);
    TripTicket tt2 = mock(TripTicket.class);
    when(tt1.getTripClaims()).thenReturn(new HashSet<>());
    when(tt2.getTripClaims()).thenReturn(new HashSet<>());
    List<TripTicket> tripTickets = List.of(tt1, tt2);
    when(tripTicketDAO.findAllTripTicketsByOriginatorPrividerId(reportFilterDTO.getProviderId())).thenReturn(tripTickets);

        ReportSummaryDTO result = reportService.findSummaryReport(reportFilterDTO);

        assertNotNull(result);
        assertEquals(100, result.getTotalTicketCount());
        assertEquals(10, result.getRescindedTicketCount());
        verify(reportDAO).countOfTotalTickets(reportFilterDTO);
    }

    @Test
    void findDetailedTripTicketByReportFilterOBJ_ReturnsDetailedTickets() {
        ReportFilterDTO reportFilterDTO = new ReportFilterDTO();
        reportFilterDTO.setFromDate("2023-01-01T00:00:00");
        reportFilterDTO.setToDate("2023-12-31T23:59:59");

        // Mock TripTicket objects
    TripTicket tripTicket1 = mock(TripTicket.class);
    TripTicket tripTicket2 = mock(TripTicket.class);

    // Mock provider for origin_provider
    Provider mockProvider = mock(Provider.class);

    org.mockito.Mockito.lenient().when(tripTicket1.getRequestedDropoffDate()).thenReturn(LocalDate.now());
    org.mockito.Mockito.lenient().when(tripTicket1.getRequestedDropOffTime()).thenReturn(new Time(System.currentTimeMillis()));
    org.mockito.Mockito.lenient().when(tripTicket2.getRequestedDropoffDate()).thenReturn(LocalDate.now());
    org.mockito.Mockito.lenient().when(tripTicket2.getRequestedDropOffTime()).thenReturn(new Time(System.currentTimeMillis()));

    // Ensure tripClaims and approved claim are non-null to avoid NPEs in ReportService
    org.mockito.Mockito.lenient().when(tripTicket1.getTripClaims()).thenReturn(new HashSet<>());
    org.mockito.Mockito.lenient().when(tripTicket2.getTripClaims()).thenReturn(new HashSet<>());
    org.mockito.Mockito.lenient().when(tripTicket1.getOriginProvider()).thenReturn(mockProvider);
    org.mockito.Mockito.lenient().when(tripTicket2.getOriginProvider()).thenReturn(mockProvider);

        List<TripTicket> tripTickets = List.of(tripTicket1, tripTicket2);
        when(reportDAO.getTripTicketsByReportFilterObj(reportFilterDTO)).thenReturn(tripTickets);

    // mappers are already stubbed in setUp

        List<DetailedTripTicketDTO> result = reportService.findDetailedTripTicketByReportFilterOBJ(reportFilterDTO);

        assertNotNull(result);
        assertEquals(tripTickets.size(), result.size());
        verify(reportDAO).getTripTicketsByReportFilterObj(reportFilterDTO);
    }
}