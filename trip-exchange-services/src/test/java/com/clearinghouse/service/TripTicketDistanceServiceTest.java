package com.clearinghouse.service;

import com.clearinghouse.dto.TripTicketDistanceDTO;
import com.clearinghouse.entity.TripTicket;
import com.clearinghouse.entity.TripTicketDistance;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TripTicketDistanceServiceTest {

    @Test
    public void toBO_manualMapping_populatesFields() {
        // Arrange
        ModelMapper mapper = new ModelMapper();
        TripTicketDistanceService service = new TripTicketDistanceService(null, mapper, null);

        TripTicketDistanceDTO dto = new TripTicketDistanceDTO();
        dto.setTripTicketDistanceId(123);
        dto.setTripTicketId(77);
        dto.setTripTicketDistance(12.5f);
        dto.setTripTicketTime(42.0f);
        dto.setTimeInString("0h42m");

        // Act
        Object bo = service.toBO(dto);

        // Assert
        assertNotNull(bo);
        assertTrue(bo instanceof TripTicketDistance);
        TripTicketDistance ent = (TripTicketDistance) bo;

        assertEquals(123, ent.getTripTicketDistanceId());
        assertEquals(12.5f, ent.getTripTicketDistance(), 0.0001f);
        assertEquals(42.0f, ent.getTripTicketTime(), 0.0001f);
        assertEquals("0h42m", ent.getTimeInString());
        assertNotNull(ent.getTripTicket());
        assertEquals(77, ent.getTripTicket().getId());
    }

    @Test
    public void toDTO_manualMapping_populatesFieldsAndCollection() {
        ModelMapper mapper = new ModelMapper();
        TripTicketDistanceService service = new TripTicketDistanceService(null, mapper, null);

        // Create entity
        TripTicketDistance ent = new TripTicketDistance();
        ent.setTripTicketDistanceId(222);
        ent.setTripTicketDistance(33.3f);
        ent.setTripTicketTime(11.1f);
        ent.setTimeInString("0h11m");
        ent.setTripTicket(new TripTicket(88));

        // Act: single
        Object dtoObj = service.toDTO(ent);

        // Assert single
        assertNotNull(dtoObj);
        assertTrue(dtoObj instanceof TripTicketDistanceDTO);
        TripTicketDistanceDTO dto = (TripTicketDistanceDTO) dtoObj;
        assertEquals(222, dto.getTripTicketDistanceId());
        assertEquals(33.3f, dto.getTripTicketDistance(), 0.0001f);
        assertEquals(11.1f, dto.getTripTicketTime(), 0.0001f);
        assertEquals("0h11m", dto.getTimeInString());
        assertEquals(88, dto.getTripTicketId());

        // Act: collection
        List<TripTicketDistanceDTO> dtoList = (List<TripTicketDistanceDTO>) service.toDTOCollection(List.of(ent));

        // Assert collection
        assertNotNull(dtoList);
        assertEquals(1, dtoList.size());
        TripTicketDistanceDTO dtoFromList = dtoList.get(0);
        assertEquals(dto.getTripTicketDistanceId(), dtoFromList.getTripTicketDistanceId());
    }
}
