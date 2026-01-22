/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;


import com.clearinghouse.dao.ApplicationSettingDAO;
import com.clearinghouse.dto.ApplicationSettingDTO;
import com.clearinghouse.entity.ApplicationSetting;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;

/**
 *
 * @author chaitanyaP
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class ApplicationSettingService implements IConvertBOToDTO, IConvertDTOToBO {


    private final ApplicationSettingDAO applicationSettingDAO;

//    @Autowired
//    ModelMapper applicationSettingModelMapper;

    private final ModelMapper modelMapper;


    public List<ApplicationSettingDTO> findAllApplicationSettings() {

        List<ApplicationSetting> applicationSettings = applicationSettingDAO.findAllApplicationSettings();

        List<ApplicationSettingDTO> applicationSettingDTOList = new java.util.ArrayList<>();
        for (ApplicationSetting applicationSetting : applicationSettings) {

            applicationSettingDTOList.add((ApplicationSettingDTO) toDTO(applicationSetting));
        }

        return applicationSettingDTOList;
    }


    public ApplicationSettingDTO findApplicationSettingByApplicationId(int applicationSettingId) {
        return (ApplicationSettingDTO) toDTO(applicationSettingDAO.findApplicationSettingById(applicationSettingId));
    }


    public ApplicationSettingDTO updateApplicationSetting(ApplicationSettingDTO applicationSettingDTO) {

        ApplicationSetting applicationSetting = (ApplicationSetting) toBO(applicationSettingDTO);

//        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        String encodedPassword = passwordEncoder.encode(applicationSetting.getPasswordOfMail());
//        applicationSetting.setPasswordOfMail(encodedPassword);
        applicationSettingDAO.updateApplicationSetting(applicationSetting);

        return (ApplicationSettingDTO) toDTO(applicationSetting);
    }

    @Override
    public Object toDTO(Object bo) {

        ApplicationSetting applicationSetting = (ApplicationSetting) bo;
        ApplicationSettingDTO applicationSettingDTO = modelMapper.map(applicationSetting, ApplicationSettingDTO.class);

        // Decode data on other side, by processing encoded data
        if (applicationSetting.getPasswordOfMail() != null) {
            byte[] valueDecoded = Base64.getDecoder().decode(applicationSetting.getPasswordOfMail().getBytes());
            String decodedPassword = new String(valueDecoded);
            applicationSettingDTO.setPasswordOfMail(decodedPassword);
        } else {
            applicationSettingDTO.setPasswordOfMail("");
        }

        return applicationSettingDTO;
    }

    @Override
    public Object toBO(Object dto) {

        ApplicationSettingDTO applicationSettingDTO = (ApplicationSettingDTO) dto;
        ApplicationSetting applicationSettingBO = modelMapper.map(applicationSettingDTO, ApplicationSetting.class);

        // Encode data on your side using BASE64
        if (applicationSettingDTO.getPasswordOfMail() != null) {
            byte[] bytesEncoded = Base64.getEncoder().encode(applicationSettingDTO.getPasswordOfMail().getBytes());
            String encodedPassword = new String(bytesEncoded);
            applicationSettingBO.setPasswordOfMail(encodedPassword);
        } else {
            applicationSettingBO.setPasswordOfMail("");
        }

        return applicationSettingBO;
    }

    @Override
    public Object toDTOCollection(Object boCollection) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
