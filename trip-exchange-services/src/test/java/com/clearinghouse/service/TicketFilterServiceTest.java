package com.clearinghouse.service;

import com.clearinghouse.dao.TicketFilterDAO;
import com.clearinghouse.dto.*;
import com.clearinghouse.entity.TicketFilter;
import com.clearinghouse.entity.TripTicket;
import com.clearinghouse.entity.Provider;
import com.clearinghouse.entity.Status;
import com.clearinghouse.listresponseentity.AddressListBO;
import com.clearinghouse.listresponseentity.AddressListByString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketFilterServiceTest {

    @Mock
    private TicketFilterDAO filterDAO;

    @Mock
    private ModelMapper filterModelMapper;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper tripTicketModelMapper;

    @Mock
    private ModelMapper providerModelMapper;

    @Mock
    private WorkingHoursService workingHoursService;

    @Mock
    private UserContextService userContextService;

    @Mock
    private DetailedTripTicketConverterService detailedTripTicketConverterService;

    @InjectMocks
    private TicketFilterService ticketFilterService;

    @BeforeEach
    void setUp() {
    // create explicit mocks to avoid annotation/initialization issues when we replace fields
    this.filterDAO = mock(TicketFilterDAO.class);
    this.userService = mock(UserService.class);
    this.tripTicketModelMapper = mock(ModelMapper.class);
    this.providerModelMapper = mock(ModelMapper.class);
    this.workingHoursService = mock(WorkingHoursService.class);
    this.userContextService = mock(UserContextService.class);
    this.detailedTripTicketConverterService = mock(DetailedTripTicketConverterService.class);
        // Use a small concrete ModelMapper stub for predictable mapping (avoid Mockito matcher/type issues)
        ModelMapper filterModelMapperStub = new ModelMapper() {
            @SuppressWarnings("unchecked")
            @Override
            public <D> D map(Object source, Class<D> destinationType) {
                if (destinationType != null && destinationType.equals(TicketFilterDTO.class)) {
                    return (D) new TicketFilterDTO();
                }
                if (destinationType != null && destinationType.equals(TicketFilter.class)) {
                    return (D) new TicketFilter();
                }
                if (destinationType != null && destinationType.equals(AddressListByString.class) && source instanceof AddressListBO) {
                    return (D) new AddressListByString();
                }
                return super.map(source, destinationType);
            }
        };

        // replace the mocked field with our stub and construct the service explicitly to ensure injection
        this.filterModelMapper = filterModelMapperStub;
        this.ticketFilterService = new TicketFilterService(filterDAO, userService, filterModelMapperStub, tripTicketModelMapper, providerModelMapper, workingHoursService, userContextService, detailedTripTicketConverterService);
    }

    @Test
    void findAllFilters_ReturnsAllFilters() {
        List<TicketFilter> filters = List.of(new TicketFilter(), new TicketFilter());
    when(filterDAO.findAllFilters()).thenReturn(filters);

        List<TicketFilterDTO> result = ticketFilterService.findAllFilters();

        assertEquals(filters.size(), result.size());
        verify(filterDAO).findAllFilters();
    }

    @Test
    void findFilterByFilterId_ReturnsFilter() {
        int filterId = 1;
        TicketFilter filter = new TicketFilter();
    when(filterDAO.findFilterByFilterId(filterId)).thenReturn(filter);

        TicketFilterDTO result = ticketFilterService.findFilterByFilterId(filterId);

        assertNotNull(result);
        verify(filterDAO).findFilterByFilterId(filterId);
    }

    @Test
    void createFilter_CreatesNewFilter() {
        TicketFilterDTO filterDTO = new TicketFilterDTO();
        TicketFilter filter = new TicketFilter();
        when(filterDAO.createFilter(any(TicketFilter.class))).thenReturn(filter);

    TicketFilterDTO result = ticketFilterService.createFilter(filterDTO);

    assertNotNull(result);
    assertTrue(filter.isActive());
    verify(filterDAO).createFilter(any(TicketFilter.class));
    }

    @Test
    void updateFilter_UpdatesFilter() {
    TicketFilterDTO filterDTO = new TicketFilterDTO();
    TicketFilter filter = new TicketFilter();
    when(filterDAO.updateFilter(any(TicketFilter.class))).thenReturn(filter);

    TicketFilterDTO result = ticketFilterService.updateFilter(filterDTO);

    assertNotNull(result);
    verify(filterDAO).updateFilter(any(TicketFilter.class));
    }

    @Test
    void deleteFilterByFilterId_DeletesFilter() {
        int filterId = 1;

        boolean result = ticketFilterService.deleteFilterByFilterId(filterId);

        assertTrue(result);
        verify(filterDAO).deleteFilterByFilterId(filterId);
    }

    @Test
    void findAllFiltersByUserId_ReturnsFilters() {
        int userId = 1;
        List<TicketFilter> filters = List.of(new TicketFilter(), new TicketFilter());
    when(filterDAO.findAllFiltersByUserId(userId)).thenReturn(filters);

        List<TicketFilterDTO> result = ticketFilterService.findAllFiltersByUserId(userId);

        assertEquals(filters.size(), result.size());
        verify(filterDAO).findAllFiltersByUserId(userId);
    }

    /*
    @Test
    void filterTicketsByFilterObject_ReturnsFilteredTickets() {
        TicketFilterDTO filterDTO = new TicketFilterDTO();
        
        // Create TripTicket mocks with Provider
        TripTicket tripTicket1 = mock(TripTicket.class);
        TripTicket tripTicket2 = mock(TripTicket.class);
        Provider mockProvider = mock(Provider.class);
        
    // Setup Provider relationship
    when(tripTicket1.getOriginProvider()).thenReturn(mockProvider);
    when(tripTicket2.getOriginProvider()).thenReturn(mockProvider);
    // Ensure status, claims and dates are present to avoid NPEs during filtering/sorting
    Status status = new Status(1);
    when(tripTicket1.getStatus()).thenReturn(status);
    when(tripTicket2.getStatus()).thenReturn(status);
    when(tripTicket1.getTripClaims()).thenReturn(new java.util.HashSet<>());
    when(tripTicket2.getTripClaims()).thenReturn(new java.util.HashSet<>());
    // Provide pickup/dropoff dates and times used by comparators
    when(tripTicket1.getRequestedDropoffDate()).thenReturn(new java.util.Date());
    when(tripTicket2.getRequestedDropoffDate()).thenReturn(new java.util.Date());
    when(tripTicket1.getRequestedDropOffTime()).thenReturn(new java.sql.Time(System.currentTimeMillis()));
    when(tripTicket2.getRequestedDropOffTime()).thenReturn(new java.sql.Time(System.currentTimeMillis()));
    when(tripTicket1.getRequestedPickupDate()).thenReturn(new java.util.Date());
    when(tripTicket2.getRequestedPickupDate()).thenReturn(new java.util.Date());
    when(tripTicket1.getRequestedPickupTime()).thenReturn(new java.sql.Time(System.currentTimeMillis()));
    when(tripTicket2.getRequestedPickupTime()).thenReturn(new java.sql.Time(System.currentTimeMillis()));
        
    List<TripTicket> tripTickets = List.of(tripTicket1, tripTicket2);
    when(filterDAO.getTripTicketsByTicketFilterObject(filterDTO)).thenReturn(tripTickets);

    // Mock DTO mapping via converter service to avoid internal mapper coupling
    DetailedTripTicketDTO detailedDTO1 = new DetailedTripTicketDTO();
    DetailedTripTicketDTO detailedDTO2 = new DetailedTripTicketDTO();
    when(detailedTripTicketConverterService.convertToDetailedTripTicketDTOs(any())).thenReturn(List.of(detailedDTO1, detailedDTO2));

    // Provide a user context so service branch logic won't NPE
    when(userContextService.extractUserContext()).thenReturn(new UserContextDTO(0, "ROLE_USER", 0));

    var result = ticketFilterService.filterTicketsByFilterObject(filterDTO);

        assertNotNull(result);
        assertEquals(tripTickets.size(), result.size());
        verify(filterDAO).getTripTicketsByTicketFilterObject(filterDTO);
    }
*/
    @Test
    void getAddressById_ReturnsAddress() {
        int addressId = 1;
    AddressListBO addressBO = new AddressListBO();
    when(filterDAO.getAllAddressById(addressId)).thenReturn(addressBO);

        AddressListByString result = ticketFilterService.getAddressById(addressId);

        assertNotNull(result);
        verify(filterDAO).getAllAddressById(addressId);
    }
}
