/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;

import com.clearinghouse.dao.ProviderDAO;
import com.clearinghouse.dao.UserNotificationDataDAO;
import com.clearinghouse.dto.ProviderDTO;
import com.clearinghouse.entity.Provider;
import com.clearinghouse.entity.ProviderType;
import com.clearinghouse.entity.User;
import com.clearinghouse.exceptions.ProviderExistsException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 * @author chaitanyaP
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class ProviderService implements IConvertBOToDTO, IConvertDTOToBO {


    private final ProviderDAO providerDAO;


    private final UserNotificationDataDAO userNotificationDataDAO;


    private final UserService userService;


    private final ModelMapper providerModelMapper;


    public Provider findById(int providerId) {
        return providerDAO.findProviderByProviderId(providerId);
    }

    public Provider findByName(String providerName) { return  providerDAO.findProviderByName(providerName);}


    public List<ProviderDTO> findAllProviders() {

        List<Provider> providers = providerDAO.findAllProviders();
        List<ProviderDTO> providerDTOList = new java.util.ArrayList<>();
        for (Provider provider : providers) {

            providerDTOList.add((ProviderDTO) toDTO(provider));
        }

        return providerDTOList;
    }



    public Provider findUberProvider() {
        return findByName("Uber");
    }

    public ProviderDTO findProviderByProviderId(int providerId) {
        Provider provider = providerDAO.findProviderByProviderId(providerId);
        if (provider != null) {
            return (ProviderDTO) toDTO(provider);
        } else {
            return null;
        }
    }


    public ProviderDTO activateProvider(int providerId) {

        //set is active true to that provider..
        Provider oldProvider = providerDAO.findProviderByProviderId(providerId);
        oldProvider.setIsActive(true);
        providerDAO.updateProvider(oldProvider);
        //fetch all users of that provider.
        List<User> usersOfProvider = userNotificationDataDAO.getUsersOfProvider(providerId);
        //deactivating users
        for (User user : usersOfProvider) {
            userService.updateUserForAccountActivation(user.getId());
        }

        return (ProviderDTO) toDTO(oldProvider);

    }


    public ProviderDTO deactivateProvider(int providerId) {
        //set is active false to that provider..
        Provider oldProvider = providerDAO.findProviderByProviderId(providerId);
        oldProvider.setIsActive(false);
        providerDAO.updateProvider(oldProvider);
        //fetch all users of that provider.
        List<User> usersOfProvider = userNotificationDataDAO.getUsersOfProvider(providerId);
        //deactivating users
        for (User user : usersOfProvider) {
            userService.updateUserForAccountDeactivation(user.getId());
        }

        return (ProviderDTO) toDTO(oldProvider);

    }


    public ProviderDTO createProvider(ProviderDTO providerDTO) {

        if (providerDAO.findProviderByEmail(providerDTO.getContactEmail())) {
            throw new ProviderExistsException(providerDTO.getContactEmail(), "Email already Exists");

        }

        Provider provider = (Provider) toBO(providerDTO);
        provider.setIsActive(true);
        provider.setProviderType(new ProviderType(1));
        providerDAO.createProvider(provider);
        return (ProviderDTO) toDTO(provider);
    }


    public ProviderDTO updateProvider(ProviderDTO providerDTO) {
        Provider provider = (Provider) toBO(providerDTO);
        providerDAO.updateProvider(provider);

        return (ProviderDTO) toDTO(provider);
    }


    public boolean deleteProviderByProviderId(int providerId) {

        providerDAO.deleteProviderByProviderId(providerId);
        return true;
    }

    @Override
    public Object toDTO(Object bo) {

        Provider providerBO = (Provider) bo;

        ProviderDTO providerDTO = providerModelMapper.map(providerBO, ProviderDTO.class);

        return providerDTO;
    }

    @Override
    public Object toBO(Object dto) {

        ProviderDTO providerDTO = (ProviderDTO) dto;

        Provider providerBO = providerModelMapper.map(providerDTO, Provider.class);
        return providerBO;
    }

    @Override
    public Object toDTOCollection(Object boCollection) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

//    private static class ArrayList {
//
//        public ArrayList() {
//        }
//    }
}
