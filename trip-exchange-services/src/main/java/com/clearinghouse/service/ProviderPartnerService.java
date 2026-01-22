/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;

import com.clearinghouse.dao.NotificationDAO;
import com.clearinghouse.dao.ProviderDAO;
import com.clearinghouse.dao.ProviderPartnerDAO;
import com.clearinghouse.dao.UserNotificationDataDAO;
import com.clearinghouse.dto.ProviderPartnerDTO;
import com.clearinghouse.entity.*;
import com.clearinghouse.enumentity.NotificationStatus;
import com.clearinghouse.enumentity.NotificationTemplateCodeValue;
import com.clearinghouse.enumentity.ProviderPartnerStatusConstants;
import com.clearinghouse.exceptions.ProviderPartnershipAlreadyExistsException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author chaitanyaP
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class ProviderPartnerService implements IConvertBOToDTO, IConvertDTOToBO {


    private final ProviderPartnerDAO providerPartnerDAO;


    private final UserNotificationDataDAO userNotificationDataDAO;


    private final ModelMapper providerPartnerModelMapper;


    private final NotificationDAO notificationDAO;


    private final ProviderDAO providerDAO;


    public List<ProviderPartnerDTO> findAllProviderPartners() {

        List<ProviderPartner> providerPartners = providerPartnerDAO.findAllProviderPartners();

        List<ProviderPartnerDTO> providerPartnerDTOList = new java.util.ArrayList<>();
        for (ProviderPartner providerPartner : providerPartners) {

            providerPartnerDTOList.add((ProviderPartnerDTO) toDTO(providerPartner));
        }

        return providerPartnerDTOList;
    }


    public ProviderPartnerDTO findProviderPartnerByProviderPartnerId(int providerPartnerId) {

        return (ProviderPartnerDTO) toDTO(providerPartnerDAO.findProviderPartnerByProviderPartnerId(providerPartnerId));
    }


    public ProviderPartnerDTO createProviderPartner(ProviderPartnerDTO providerPartnerDTO) {

        if (providerPartnerDAO.providerPartnershipCheck(providerPartnerDTO.getRequesterProviderId(), providerPartnerDTO.getCoordinatorProviderId())) {
            throw new ProviderPartnershipAlreadyExistsException(providerPartnerDTO.getCoordinatorProviderName(), "partnershipExists");
        }
        ProviderPartnerStatus newStatusForProviderPartner = new ProviderPartnerStatus();
        newStatusForProviderPartner.setProviderPartnerStatusId(ProviderPartnerStatusConstants.pending.providerPartnerStatusChek());
        ProviderPartner providerPartner = (ProviderPartner) toBO(providerPartnerDTO);
        providerPartner.setIsActive(true);
        providerPartner.setRequestStatus(newStatusForProviderPartner);

        providerPartnerDAO.createProviderPartner(providerPartner);

//        fetching the object of the provider to get details to send the notificaton to that provider
        Provider coordinatorProvider = providerDAO.findProviderByProviderId(providerPartnerDTO.getCoordinatorProviderId());
        Provider requesterProvider = providerDAO.findProviderByProviderId(providerPartnerDTO.getRequesterProviderId());

        List<User> usersOfCoordinatorProvider = userNotificationDataDAO.getUsersOfProvider(coordinatorProvider.getProviderId());

        for (User user : usersOfCoordinatorProvider) {
            //NotificationEnginePart.....
            Notification emailNotification = new Notification();
            NotificationTemplate notificationTemplate = new NotificationTemplate();
            emailNotification.setIsEMail(true);
            emailNotification.setStatusId(NotificationStatus.newStatus.status());
            notificationTemplate.setNotificationTemplateId(NotificationTemplateCodeValue.addProviderPartnerTemplateCode.templateCodeValue());
            emailNotification.setNotificationTemplate(notificationTemplate);
            emailNotification.setIsActive(true);
            emailNotification.setNumberOfAttempts(0);

            if (user.getAuthorities().iterator().next().getAuthority().trim().equalsIgnoreCase("ROLE_PROVIDERADMIN")) {

                emailNotification.setEmailTo(user.getEmail());

//        Setting parameter values in according to the template.
                Map createProviderPartnerTemplateMap = new HashMap<String, String>();
                createProviderPartnerTemplateMap.put("name", user.getName());
                createProviderPartnerTemplateMap.put("requesterProviderName", requesterProvider.getProviderName());

                String jsonValueOfTemplate = "";

                Iterator<Map.Entry<String, String>> entries = createProviderPartnerTemplateMap.entrySet().iterator();
                while (entries.hasNext()) {

                    Map.Entry<String, String> entry = entries.next();
                    jsonValueOfTemplate = jsonValueOfTemplate + "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
                    if (entries.hasNext()) {
                        jsonValueOfTemplate = jsonValueOfTemplate + ",";
                    }

                }

                String FinaljsonValueOfTemplate = "{" + jsonValueOfTemplate + "}";

                emailNotification.setParameterValues(FinaljsonValueOfTemplate);

                emailNotification.setSubject("Provider partner request ");

                notificationDAO.createNotification(emailNotification);
            }
        }

        //  ProviderPartner newProviderPartner = providerPartnerDAO.findProviderPartnerByProviderPartnerId(providerPartner.getProviderPartnerId());
        return (ProviderPartnerDTO) toDTO(providerPartner);

    }


    public ProviderPartnerDTO updateProviderPartner(ProviderPartnerDTO providerPartnerDTO) {

        if (providerPartnerDTO.getRequestStatus()
                .getProviderPartnerStatusId() == ProviderPartnerStatusConstants.breakPartnership
                .providerPartnerStatusChek()) {
            providerPartnerDTO.setActive(false);
            providerPartnerDTO.setTrustedPartnerForRequester(false);
            providerPartnerDTO.setTrustedPartnerForCoordinator(false);

            // send mail here for partnership breaked..
        }
        // following is for phase one+++++++++++
        if (providerPartnerDTO.getRequestStatus()
                .getProviderPartnerStatusId() == ProviderPartnerStatusConstants.approved.providerPartnerStatusChek()) {

            if (!providerPartnerDTO.isTrustedPartnerForCoordinator()
                    && providerPartnerDTO.isTrustedPartnerForRequester()) {
                providerPartnerDTO.setTrustedPartnerForCoordinator(true);
                providerPartnerDTO.setTrustedPartnerForRequester(true);
            } else if (providerPartnerDTO.isTrustedPartnerForCoordinator()
                    && !providerPartnerDTO.isTrustedPartnerForRequester()) {
                providerPartnerDTO.setTrustedPartnerForCoordinator(true);
                providerPartnerDTO.setTrustedPartnerForRequester(false);
            } else if (!providerPartnerDTO.isTrustedPartnerForCoordinator()
                    && !providerPartnerDTO.isTrustedPartnerForRequester()) {
                providerPartnerDTO.setTrustedPartnerForCoordinator(false);
                providerPartnerDTO.setTrustedPartnerForRequester(false);
            } else {
                providerPartnerDTO.setTrustedPartnerForCoordinator(true);
                providerPartnerDTO.setTrustedPartnerForRequester(true);
            }
        }
        //+++++++++++++++++

        if (providerPartnerDTO.getRequestStatus().getProviderPartnerStatusId() == ProviderPartnerStatusConstants.denied.providerPartnerStatusChek()) {
            providerPartnerDTO.setActive(false);

        }

        if (providerPartnerDTO.getRequestStatus().getProviderPartnerStatusId() == ProviderPartnerStatusConstants.cancelled.providerPartnerStatusChek()) {
            providerPartnerDTO.setActive(false);

        }

        //add the requset approving login here..
        ProviderPartner providerPartner = (ProviderPartner) toBO(providerPartnerDTO);
        providerPartnerDAO.updateProviderPartner(providerPartner);

        return (ProviderPartnerDTO) toDTO(providerPartner);

    }


    public boolean deleteProviderpartnerByProviderPartnerId(int providerPartnerId) {

        providerPartnerDAO.deleteProviderpartnerByProviderPartnerId(providerPartnerId);
        return true;

    }


    public List<ProviderPartnerDTO> findAllProviderPartnersByRequesterProviderId(int requesterProviderId) {

        List<ProviderPartner> providerPartners = providerPartnerDAO.findAllProviderPartnersByRequesterProviderId(requesterProviderId);

        List<ProviderPartnerDTO> providerPartnerDTOList = new java.util.ArrayList<>();
        for (ProviderPartner providerPartner : providerPartners) {

            providerPartnerDTOList.add((ProviderPartnerDTO) toDTO(providerPartner));
        }

        return providerPartnerDTOList;
    }

    public ProviderPartnerDTO deactivateProviderPartner(int providerPartnerId) {
        ProviderPartner providerPartner = providerPartnerDAO.findProviderPartnerByProviderPartnerId(providerPartnerId);
        if (providerPartner != null) {
            providerPartner.setIsActive(false);
            providerPartner = providerPartnerDAO.updateProviderPartner(providerPartner);
            return (ProviderPartnerDTO) toDTO(providerPartner);
        }
        return null;
    }

    @Override
    public Object toDTO(Object bo) {

        ProviderPartner providerPartnerBO = (ProviderPartner) bo;

        ProviderPartnerDTO providerPartnerDTO = providerPartnerModelMapper.map(providerPartnerBO, ProviderPartnerDTO.class);

        //    providerDTO.setAddressDTOObj(addressDTO);
        return providerPartnerDTO;

    }

    @Override
    public Object toBO(Object dto) {

        ProviderPartnerDTO providerPartnerDTO = (ProviderPartnerDTO) dto;

        ProviderPartner providerPartnerBO = providerPartnerModelMapper.map(providerPartnerDTO, ProviderPartner.class);

        return providerPartnerBO;

    }

    @Override
    public Object toDTOCollection(Object boCollection) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    public List<ProviderPartnerDTO> findApprovedProviderPartnersByRequesterProviderId(int requesterProviderId) {

        List<ProviderPartner> providerPartners = providerPartnerDAO.findApprovedProviderPartnersByRequesterProviderId(requesterProviderId);

        List<ProviderPartnerDTO> providerPartnerDTOList = new java.util.ArrayList<>();
        for (ProviderPartner providerPartner : providerPartners) {

            providerPartnerDTOList.add((ProviderPartnerDTO) toDTO(providerPartner));
        }

        return providerPartnerDTOList;
    }

}
