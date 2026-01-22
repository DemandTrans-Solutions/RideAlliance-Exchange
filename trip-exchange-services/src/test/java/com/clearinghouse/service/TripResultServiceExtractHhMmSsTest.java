package com.clearinghouse.service;

import com.clearinghouse.dao.*;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.Mockito;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TripResultServiceExtractHhMmSsTest {

    @Test
    public void testExtractHhMmSs_variousFormats() throws Exception {
        // Create mocks for constructor dependencies
        FileGenerateService fileGenerateService = Mockito.mock(FileGenerateService.class);
        UserDAO userDAO = Mockito.mock(UserDAO.class);
        TripResultDAO tripResultDAO = Mockito.mock(TripResultDAO.class);
        TripClaimService tripClaimService = Mockito.mock(TripClaimService.class);
        UserNotificationDataDAO userNotificationDataDAO = Mockito.mock(UserNotificationDataDAO.class);
        NotificationDAO notificationDAO = Mockito.mock(NotificationDAO.class);
        TripTicketDAO tripTicketDAO = Mockito.mock(TripTicketDAO.class);
        ModelMapper modelMapper = Mockito.mock(ModelMapper.class);

        TripResultService svc = new TripResultService(
                fileGenerateService,
                userDAO,
                tripResultDAO,
                tripClaimService,
                userNotificationDataDAO,
                notificationDAO,
                tripTicketDAO,
                modelMapper
        );

        Method m = TripResultService.class.getDeclaredMethod("extractHhMmSs", String.class);
        m.setAccessible(true);

        // ISO datetime with 'T'
        assertEquals("08:40:00", m.invoke(svc, "2025-10-18T08:40:00"));
        // ISO datetime with Z
        assertEquals("08:40:00", m.invoke(svc, "2025-10-18T08:40:00Z"));
        // Simple HH:mm
        assertEquals("08:40:00", m.invoke(svc, "08:40"));
        // Single-digit hour and minute
        assertEquals("08:04:00", m.invoke(svc, "8:4"));
        // Embedded time
        assertEquals("08:40:30", m.invoke(svc, "some text 08:40:30 end"));
        // Date and time separated by space (no T)
        assertEquals("08:40:00", m.invoke(svc, "2025-10-18 08:40:00"));
    }
}

