package com.clearinghouse.service;

import com.clearinghouse.dao.TripTicketDAO;
import com.clearinghouse.dao.TripTicketDistanceDAO;
import com.clearinghouse.dto.TripTicketDTO;
import com.clearinghouse.dto.TripTicketDistanceDTO;
import com.clearinghouse.entity.TripTicketDistance;
import com.clearinghouse.entity.TripTicket;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author shankarI
 */

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class TripTicketDistanceService implements IConvertDTOToBO, IConvertBOToDTO {


    private final TripTicketDistanceDAO tripTicketDistanceDAO;


    private final ModelMapper tripTicketDistanceModelMapper;


    private final TripTicketDAO tripTicketDAO;


    public TripTicketDistanceDTO createUpdateTripTicketDistance(TripTicketDistanceDTO tripTicketDistanceDTO) {
        // convert toBO
        TripTicketDistance tripTicketDistanceBO = (TripTicketDistance) toBO(tripTicketDistanceDTO);

        TripTicketDistance newTripTicketDistance = new TripTicketDistance();

        if (tripTicketDistanceBO.getTripTicketDistanceId() == 0) {
            newTripTicketDistance = tripTicketDistanceDAO.createTripTicketDistance(tripTicketDistanceBO);
        } else {
            newTripTicketDistance = tripTicketDistanceDAO.updateTripTicketDistance(tripTicketDistanceBO);
        }
        //convert toDTO
        TripTicketDistanceDTO tripTicketDistanceDto = (TripTicketDistanceDTO) toDTO(newTripTicketDistance);

        return tripTicketDistanceDto;
    }


    @Override
    public Object toDTO(Object bo) {
        if (bo == null) return null;
        TripTicketDistance tripTicketDistance = (TripTicketDistance) bo;

        // Manual field-by-field mapping to ensure values are correctly copied
        TripTicketDistanceDTO tripTicketDistanceDTO = new TripTicketDistanceDTO();
        tripTicketDistanceDTO.setTripTicketDistanceId(tripTicketDistance.getTripTicketDistanceId());
        tripTicketDistanceDTO.setTripTicketDistance(tripTicketDistance.getTripTicketDistance());
        tripTicketDistanceDTO.setTripTicketTime(tripTicketDistance.getTripTicketTime());
        tripTicketDistanceDTO.setTimeInString(tripTicketDistance.getTimeInString());

        try {
            if (tripTicketDistance.getTripTicket() != null) {
                tripTicketDistanceDTO.setTripTicketId(tripTicketDistance.getTripTicket().getId());
            }
        } catch (Exception e) {
            log.warn("Failed to map TripTicket association to DTO: {}", e.getMessage());
        }

        return tripTicketDistanceDTO;
    }

    @Override
    public Object toBO(Object dto) {
        // Manual field-by-field mapping to ensure values are copied correctly
        if (dto == null) return null;
        TripTicketDistanceDTO tripTicketDistanceDTO = (TripTicketDistanceDTO) dto;

        TripTicketDistance tripTicketDistance = new TripTicketDistance();

        // Map primitive/standard fields
        tripTicketDistance.setTripTicketDistanceId(tripTicketDistanceDTO.getTripTicketDistanceId());
        tripTicketDistance.setTripTicketDistance(tripTicketDistanceDTO.getTripTicketDistance());
        tripTicketDistance.setTripTicketTime(tripTicketDistanceDTO.getTripTicketTime());
        tripTicketDistance.setTimeInString(tripTicketDistanceDTO.getTimeInString());

        // Map association (TripTicket) if provided
        try {
            if (tripTicketDistanceDTO.getTripTicketId() > 0) {
                tripTicketDistance.setTripTicket(new TripTicket(tripTicketDistanceDTO.getTripTicketId()));
            }
        } catch (Exception e) {
            log.warn("Failed to map TripTicket association from DTO: {}", e.getMessage());
        }

        return tripTicketDistance;
    }

    @Override
    public Object toDTOCollection(Object boCollection) {
        if (boCollection == null) return null;

        List<?> rawList;
        try {
            rawList = (List<?>) boCollection;
        } catch (ClassCastException e) {
            log.warn("toDTOCollection expected a List but got: {}", boCollection.getClass());
            return null;
        }

        List<TripTicketDistanceDTO> dtoList = new ArrayList<>();
        for (Object item : rawList) {
            if (item == null) continue;
            TripTicketDistanceDTO dto = (TripTicketDistanceDTO) toDTO(item);
            if (dto != null) dtoList.add(dto);
        }
        return dtoList;
    }


    public List<TripTicketDistanceDTO> findAllTripTicketDistance() {

        List<TripTicketDistance> tripTicketDistanceList = tripTicketDistanceDAO.findAllTripTicketDistance();
        List<TripTicketDistanceDTO> tripTicketDistanceDtoList = new ArrayList<TripTicketDistanceDTO>();

        for (TripTicketDistance tripTicketDistance : tripTicketDistanceList) {
            TripTicketDistanceDTO tripTicketDistanceDTO = (TripTicketDistanceDTO) toDTO(tripTicketDistance);
            tripTicketDistanceDtoList.add(tripTicketDistanceDTO);
        }
        return tripTicketDistanceDtoList;
    }


    public TripTicketDistanceDTO getDistanceByTripTicketId(int tripTicketId) {
        TripTicketDistance tripTicketDistance = tripTicketDistanceDAO.getDistanceByTripTicketId(tripTicketId);

        TripTicketDistanceDTO tripTicketDistanceDTO = new TripTicketDistanceDTO();
        if (tripTicketDistance == null) {
            return null;
        } else {
            tripTicketDistanceDTO = (TripTicketDistanceDTO) toDTO(tripTicketDistance);
            return tripTicketDistanceDTO;
        }
    }

    // newly added to save distance and time of each tripticket

    public TripTicketDistanceDTO saveDistanceTime(TripTicketDTO newTripTicket, TripTicketDistanceDTO timeAndDistanceDTO) {
        //TripTicketDistanceDTO timeAndDistanceDTO = tripTicketDAO.convertLatLongToGetTimeAndDistance(newTripTicket);

        TripTicketDistanceDTO tripTicketDistanceDto = new TripTicketDistanceDTO();

        tripTicketDistanceDto.setTripTicketDistance(timeAndDistanceDTO.getTripTicketDistance());
        tripTicketDistanceDto.setTripTicketTime(timeAndDistanceDTO.getTripTicketTime());
        tripTicketDistanceDto.setTimeInString(timeAndDistanceDTO.getTimeInString());
        tripTicketDistanceDto.setTripTicketId(newTripTicket.getId());
        TripTicketDistance tripTicketDistance = (TripTicketDistance) toBO(tripTicketDistanceDto);
        tripTicketDistanceDAO.saveDistanceTime(tripTicketDistance);
        return (TripTicketDistanceDTO) toDTO(tripTicketDistance);
    }

    // newly added to update distance and time of tripticket

    public TripTicketDistanceDTO updateDistanceTime(int tripTicketId, TripTicketDTO tripTicketDTO, TripTicketDistanceDTO timeAndDistanceDTO) {
        //TripTicketDistanceDTO timeAndDistanceDTO = tripTicketDAO.convertLatLongToGetTimeAndDistance(tripTicketDTO);

        TripTicketDistance tripTicketDistance = tripTicketDistanceDAO.getDistanceByTripTicketId(tripTicketId);
        log.info("existing tripTicketDistance: {}", tripTicketDistance);
        TripTicketDistanceDTO tripTicketDistanceDto = new TripTicketDistanceDTO();

        tripTicketDistanceDto.setTripTicketDistance(timeAndDistanceDTO.getTripTicketDistance());
        tripTicketDistanceDto.setTripTicketTime(timeAndDistanceDTO.getTripTicketTime());
        tripTicketDistanceDto.setTimeInString(timeAndDistanceDTO.getTimeInString());
        tripTicketDistanceDto.setTripTicketId(tripTicketId);

        tripTicketDistanceDto.setTripTicketDistanceId(tripTicketDistance.getTripTicketDistanceId());
        TripTicketDistance tripTicketDistanceBO = (TripTicketDistance) toBO(tripTicketDistanceDto);
        tripTicketDistanceBO = tripTicketDistanceDAO.updateTripTicketDistance(tripTicketDistanceBO);
        log.debug("updated tripTicketDistance: {}", tripTicketDistanceBO);
        return (TripTicketDistanceDTO) toDTO(tripTicketDistanceBO);
    }


    public TripTicketDistanceDTO checkDistanceTime(TripTicketDTO tripTicketDTO) {
        TripTicketDistanceDTO timeAndDistanceDTO = tripTicketDAO.convertLatLongToGetTimeAndDistance(tripTicketDTO);
        return timeAndDistanceDTO;
    }


}
