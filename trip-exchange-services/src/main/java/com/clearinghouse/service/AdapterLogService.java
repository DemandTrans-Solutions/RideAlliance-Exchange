package com.clearinghouse.service;

import com.clearinghouse.dao.AdapterLogDAO;
import com.clearinghouse.dao.UserDAO;
import com.clearinghouse.dto.AdapterLogDTO;
import com.clearinghouse.entity.AdapterLog;
import com.clearinghouse.entity.User;
import com.clearinghouse.exceptions.InvalidInputException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Shankar I
 */

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class AdapterLogService implements IConvertBOToDTO, IConvertDTOToBO {


    private final AdapterLogDAO adapterLogDAO;


    private final ModelMapper adapterLogModelMapper;


    private final UserDAO userDAO;


    public AdapterLogDTO createAdapterLog(AdapterLogDTO adapterLogDTO) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = ((User) auth.getDetails());
        log.debug("user&&&&&==" + user.toString());
        User originalUser = userDAO.findUserByUserId(user.getId());
        int providerId;
        try {
            providerId = originalUser.getProvider().getProviderId();
        } catch (Exception e) {
            log.error("Error getting providerId in createAdapterLog: {}", e.getMessage(), e);
            throw new InvalidInputException("api_key: Invalid api key");
        }
        adapterLogDTO.setProviderId(providerId);
        AdapterLog adapterLogBO = (AdapterLog) toBO(adapterLogDTO);
        adapterLogBO = adapterLogDAO.createAdapterLog(adapterLogBO);
        return (AdapterLogDTO) toDTO(adapterLogBO);
    }

    @Override
    public Object toBO(Object dto) {
        AdapterLogDTO adapterLogDTO = (AdapterLogDTO) dto;
        AdapterLog adapterLogBO = adapterLogModelMapper.map(adapterLogDTO, AdapterLog.class);
        return adapterLogBO;
    }

    @Override
    public Object toDTOCollection(Object boCollection) {
        throw new UnsupportedOperationException("Not supported yet.");
        //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object toDTO(Object bo) {
        AdapterLog adapterLogBO = (AdapterLog) bo;
        AdapterLogDTO adapterLogDTO = adapterLogModelMapper.map(adapterLogBO, AdapterLogDTO.class);
        return adapterLogDTO;
    }


    public List<AdapterLogDTO> findAllAdapterLogs() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = ((User) auth.getDetails());
        log.debug("user&&&&&==" + user.toString());


        List<AdapterLog> adapterLogBoList = adapterLogDAO.findAllAdapterLogs();
        List<AdapterLogDTO> adapterLogDTOList = new ArrayList<>();
        for (AdapterLog adapterLog : adapterLogBoList) {
            adapterLogDTOList.add((AdapterLogDTO) toDTO(adapterLog));
        }
        return adapterLogDTOList;
    }


    public List<AdapterLogDTO> findAdapterLogsByProviderId(int providerId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = ((User) auth.getDetails());
        log.debug("user&&&&&==" + user.toString());

        List<AdapterLog> adapterLogBoList = adapterLogDAO.findAdapterLogsByProviderId(providerId);
        List<AdapterLogDTO> adapterLogDTOList = new ArrayList<>();
        for (AdapterLog adapterLog : adapterLogBoList) {
            adapterLogDTOList.add((AdapterLogDTO) toDTO(adapterLog));
        }
        return adapterLogDTOList;
    }

}
