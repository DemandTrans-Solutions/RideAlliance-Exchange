package com.clearinghouse.service;

import com.clearinghouse.dao.TripTicketDAO;
import com.clearinghouse.dao.WorkingHoursDAO;
import com.clearinghouse.dto.CheckWorkingHoursDTO;
import com.clearinghouse.dto.WorkingHoursDTO;
import com.clearinghouse.entity.TripTicket;
import com.clearinghouse.entity.WorkingHours;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
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
public class WorkingHoursService implements IConvertBOToDTO, IConvertDTOToBO {


    private final WorkingHoursDAO workingHoursDAO;


    private final TripTicketDAO tripTicketDAO;


    private final ModelMapper workingHoursModelMapper;


    public List<WorkingHoursDTO> getAllWorkingHours() {

        List<WorkingHours> workingHourList = workingHoursDAO.getAllWorkingHours();
        List<WorkingHoursDTO> workingHoursDTOList = new ArrayList<>();

        for (WorkingHours workingHours : workingHourList) {
            workingHoursDTOList.add((WorkingHoursDTO) toDTO(workingHours));
        }
        return workingHoursDTOList;
    }

    @Override
    public Object toBO(Object dto) {
        WorkingHoursDTO workingHoursDTO = (WorkingHoursDTO) dto;
        WorkingHours workingHours = workingHoursModelMapper.map(workingHoursDTO, WorkingHours.class);
        return workingHours;
    }

    @Override
    public Object toDTOCollection(Object boCollection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object toDTO(Object bo) {
        WorkingHours workingHoursBO = (WorkingHours) bo;
        WorkingHoursDTO workingHoursDTO = workingHoursModelMapper.map(workingHoursBO, WorkingHoursDTO.class);
        String startTime = workingHoursDTO.getStartTime();
        workingHoursDTO.setStartTime(startTime.substring(0, startTime.length() - 3));
        String endTime = workingHoursDTO.getEndTime();
        workingHoursDTO.setEndTime(endTime.substring(0, endTime.length() - 3));
        return workingHoursDTO;
    }


    public List<WorkingHoursDTO> findWorkingHoursByProviderId(int providerId) {
        List<WorkingHours> workingHoursByProviderIdList = workingHoursDAO.findWorkingHoursByProviderId(providerId);
        List<WorkingHoursDTO> workingHoursDTOByProviderIdList = new ArrayList<WorkingHoursDTO>();
        for (WorkingHours workingHourBO : workingHoursByProviderIdList) {
            WorkingHoursDTO workingHoursDTO = (WorkingHoursDTO) toDTO(workingHourBO);
            workingHoursDTOByProviderIdList.add(workingHoursDTO);
        }
        return workingHoursDTOByProviderIdList;
    }


    public List<WorkingHoursDTO> createWorkingHours(List<WorkingHoursDTO> workingHoursDTO) {

        List<WorkingHours> workingHoursList = new ArrayList<WorkingHours>();
        for (WorkingHoursDTO workingHoursDto : workingHoursDTO) {
            WorkingHours workingHourObj = (WorkingHours) toBO(workingHoursDto);
            workingHoursList.add(workingHourObj);
        }

        List<WorkingHours> createWorkingHoursList = workingHoursDAO.createWorkingHoursList(workingHoursList);

        List<WorkingHoursDTO> workingHoursDTOList = new ArrayList<>();
        for (WorkingHours workingHoursBo : createWorkingHoursList) {

            WorkingHoursDTO workingHoursDtoObj = (WorkingHoursDTO) toDTO(workingHoursBo);
            workingHoursDTOList.add(workingHoursDtoObj);
        }
        return workingHoursDTOList;
    }


    public List<WorkingHoursDTO> updateWorkingHours(List<WorkingHoursDTO> updatedWorkingHoursList) {

        List<WorkingHours> newWorkingHoursList = new ArrayList<WorkingHours>();
        for (WorkingHoursDTO workingHoursDto : updatedWorkingHoursList) {
            WorkingHours workingHourObj = (WorkingHours) toBO(workingHoursDto);
            newWorkingHoursList.add(workingHourObj);
        }

        List<WorkingHours> createWorkingHoursList = workingHoursDAO.updateWorkingHoursList(newWorkingHoursList);

        List<WorkingHoursDTO> workingHoursDTOList = new ArrayList<>();
        for (WorkingHours workingHoursBo : createWorkingHoursList) {

            WorkingHoursDTO workingHoursDtoObj = (WorkingHoursDTO) toDTO(workingHoursBo);
            workingHoursDTOList.add(workingHoursDtoObj);
        }
        return workingHoursDTOList;
    }


    public List<WorkingHoursDTO> createUpdateWorkingHours(List<WorkingHoursDTO> updatedWorkingHoursList) {

        List<WorkingHours> createWorkingHoursList = new ArrayList<WorkingHours>();
        List<WorkingHours> updateWorkingHoursList = new ArrayList<WorkingHours>();

        for (WorkingHoursDTO workingHoursDto : updatedWorkingHoursList) {
            // convert toBO
            WorkingHours workingHourObj = (WorkingHours) toBO(workingHoursDto);

            // separate workingHours into create and update list
            if (workingHourObj.getWorkingHoursId() == 0) {
                createWorkingHoursList.add(workingHourObj);
            } else
                updateWorkingHoursList.add(workingHourObj);
        }

        List<WorkingHours> combineCreateUpdateList = new ArrayList<WorkingHours>();
        // to create new workingHours
        List<WorkingHours> createNewWorkingHoursList = workingHoursDAO.createWorkingHoursList(createWorkingHoursList);
        // to update existing workingHours
        List<WorkingHours> updateNewWorkingHoursList = workingHoursDAO.updateWorkingHoursList(updateWorkingHoursList);

        combineCreateUpdateList.addAll(createNewWorkingHoursList);
        combineCreateUpdateList.addAll(updateNewWorkingHoursList);

        List<WorkingHoursDTO> workingHoursDTOList = new ArrayList<>();
        for (WorkingHours workingHoursBo : combineCreateUpdateList) {

            WorkingHoursDTO workingHoursDtoObj = (WorkingHoursDTO) toDTO(workingHoursBo);
            workingHoursDTOList.add(workingHoursDtoObj);
        }
        return workingHoursDTOList;
    }


    public CheckWorkingHoursDTO checkWorkingHours(CheckWorkingHoursDTO checkWorkingHoursDTO) {

        // CheckWorkingHoursDTO checkWorkingHoursDtoObj=new CheckWorkingHoursDTO();
        boolean checkWorkingHours = false;
        int workingHoursId = 0;
        String msg = null;
        /*
         * fetch the workingHours of that claimant provider and check wheather trip
         * ticket pickUp time are in that
         */
        String getDayName = null;
        TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(checkWorkingHoursDTO.getTripTicketId());
        if (tripTicket.getRequestedPickupDate() != null) {
            getDayName = workingHoursDAO.getDayName(Date.valueOf(tripTicket.getRequestedPickupDate()));
        } else if (tripTicket.getRequestedDropoffDate() != null) {
            getDayName = workingHoursDAO.getDayName(Date.valueOf(tripTicket.getRequestedDropoffDate()));
        } else {
            getDayName = null;
        }

        if (getDayName == null) {
            checkWorkingHoursDTO.setIsEligibleForCreateClaim(false);
            checkWorkingHoursDTO.setMsg("Trip ticket is missing requested pickup/dropoff date");
            return checkWorkingHoursDTO;
        }

        WorkingHours workingHoursForDay = workingHoursDAO.getWorkingHourForDay(getDayName, checkWorkingHoursDTO.getClaimantProviderId());


        if (workingHoursForDay == null) {
            msg = "No working hours found for the claimant provider";
        } else {

            // check is workingHours is active
            if (workingHoursForDay.getIsActive()) {
                boolean result = false;
                // check for PU Time is in workingHours or not
                if (tripTicket.getRequestedPickupTime() != null) {
                    result = workingHoursDAO.checkPickupTimeInWorkingHours(tripTicket.getId(),
                            workingHoursForDay.getStartTime(), workingHoursForDay.getEndTime());
                } else {
                    result = workingHoursDAO.checkDropOffTimeInWorkingHours(tripTicket.getId(),
                            workingHoursForDay.getStartTime(), workingHoursForDay.getEndTime());
                }
                if (result) {
                    checkWorkingHours = true;
                    workingHoursId = workingHoursForDay.getWorkingHoursId();
                    msg = "TripTicket pickup or dropOff time is in working hours";
                } else {
                    msg = "TripTicket pickup or dropOff time is not in working hours";
                }
            } else {
                checkWorkingHours = false;
                msg = "Working hours is inactive";
            }
        }

        checkWorkingHoursDTO.setIsEligibleForCreateClaim(checkWorkingHours);
        checkWorkingHoursDTO.setWorkingHoursId(workingHoursId);
        checkWorkingHoursDTO.setMsg(msg);

        return checkWorkingHoursDTO;
    }


}