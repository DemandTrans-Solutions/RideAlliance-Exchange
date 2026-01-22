/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;

import com.clearinghouse.dao.ActivityDAO;
import com.clearinghouse.dto.ActivityDTO;
import com.clearinghouse.entity.Activity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author chaitanyaP
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class ActivityService implements com.clearinghouse.service.IConvertBOToDTO, com.clearinghouse.service.IConvertDTOToBO {


    private final ActivityDAO activityDAO;


    private final ModelMapper activityModelMapper;


    public List<ActivityDTO> findAllActivites() {
        List<Activity> activities = activityDAO.findAllActivities();
        List<ActivityDTO> activityDTOList = new ArrayList<>();
        for (Activity activity : activities) {
            activityDTOList.add((ActivityDTO) toDTO(activity));
        }
        return activityDTOList;

    }


    public ActivityDTO findActivityByActivityId(int activityId) {

        return (ActivityDTO) toDTO(activityDAO.findActivityByActivityId(activityId));
    }


    public ActivityDTO createActivity(ActivityDTO activityDTO) {
        Activity activityNew = activityDAO.createActivity((Activity) toBO(activityDTO));
        return (ActivityDTO) toDTO(activityNew);
    }


    public List<ActivityDTO> findAllActivitesByTripTicketId(int tripTicketId) {

        List<Activity> activitiesByTripTicketId = activityDAO.findAllActivitesByTripTicketId(tripTicketId);
        List<ActivityDTO> activitiesByTripTicketIdDTOList = new ArrayList<>();
        for (Activity activity : activitiesByTripTicketId) {
            activitiesByTripTicketIdDTOList.add((ActivityDTO) toDTO(activity));
        }
        return activitiesByTripTicketIdDTOList;

    }

    @Override
    public Object toDTO(Object bo) {
        Activity activityBO = (Activity) bo;

        ActivityDTO activityDTO = activityModelMapper.map(activityBO, ActivityDTO.class);
        activityDTO.setCreatedAt(activityBO.getCreatedAt().withZoneSameInstant(ZoneId.of("GMT-7")).toString());
        return activityDTO;
    }

    @Override
    public Object toBO(Object dto) {
        ActivityDTO activityDTO = (ActivityDTO) dto;

        Activity activity = activityModelMapper.map(activityDTO, Activity.class);

        return activity;
    }

    @Override
    public Object toDTOCollection(Object boCollection) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
