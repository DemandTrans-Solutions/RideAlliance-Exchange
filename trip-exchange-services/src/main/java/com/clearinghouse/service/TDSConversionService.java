package com.clearinghouse.service;

import com.clearinghouse.dto.TripTicketDTO;
import com.clearinghouse.tds.generated.model.TripRequestResponseType;
import com.clearinghouse.tds.generated.model.TripRequestType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.LocalDate;
import java.util.Date;

@Service
@AllArgsConstructor
@Slf4j
public class TDSConversionService {

    private final ModelMapper tdsModelMapper;

    public TripTicketDTO convertToTripTicket(TripRequestType tripRequestType) {
        if (tripRequestType == null) {
            return null;
        }

        TripTicketDTO tripTicketDTO = tdsModelMapper.map(tripRequestType, TripTicketDTO.class);

        // Set default values or additional fields as needed
        if (tripTicketDTO.getRequestedPickupDate() == null) {
            tripTicketDTO.setRequestedPickupDate(LocalDate.now());
        }

        return tripTicketDTO;
    }

    public TripRequestResponseType convertToTripResponseType(TripTicketDTO tripTicketDTO) {
        if (tripTicketDTO == null) {
            return null;
        }

        return tdsModelMapper.map(tripTicketDTO, TripRequestResponseType.class);
    }

    // Helper method kept for backward compatibility
    public Time convertToSqlTime(com.clearinghouse.tds.generated.model.Time tdsTime) {
        if (tdsTime == null || tdsTime.getTime() == null) {
            return null;
        }
        // Convert XMLGregorianCalendar to java.util.Date
        Date utilDate = tdsTime.getTime().toGregorianCalendar().getTime();
        // Convert java.util.Date to java.sql.Time
        return new Time(utilDate.getTime());
    }
}
