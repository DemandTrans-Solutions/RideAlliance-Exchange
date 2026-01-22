/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;

import com.clearinghouse.dao.ListDAO;
import com.clearinghouse.listresponseentity.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
//import com.clearinghouse.entity.TitleList;

/**
 *
 * @author chaitanyaP
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class ListService {


    private final ListDAO listDao;

    public List<ProviderList> getAllProviders() {
        return listDao.getAllProviders();

    }


    public List<ProviderList> getAllProvidersByProviderLogin(int providerId) {
        return listDao.getAllProvidersByProviderLogin(providerId);
    }


    public List<RoleList> getAllListRoles() {
        return listDao.getAllListRoles();

    }

//    @Override
//    public List<TitleList> getAllListTitles() {
//
//        return listDao.getAllListTitles();
//
//    }

    public List<ServiceAreaList> getAllListServiceAreas() {

        return listDao.getAllListServiceAreas();
    }


    public List<ProviderPartnerList> getAllListProviderPartners(int providerId) {

        return listDao.getAllListProviderPartners(providerId);
    }


    public List<StatusList> getAllListStatus() {
        return listDao.getAllListStatus();
    }

//    @Override
//    public List<ProviderPartnerStatus> getAllListProviderPartnerStatusList() {
//       return listDao.getAllListProviderPartnerStatus();
//    }

    public List<TicketFilterList> getAllListTicketFilters(int userId) {
        return listDao.getAllListTicketFilters(userId);
    }


    public List<AddressListByString> getAllListAddress(String addressWord) {

        List<AddressListBO> addressListBOs = listDao.getAllListAddress(addressWord);
        List<AddressListByString> addressListByStrings = new ArrayList<>();
        for (AddressListBO addressListBO : addressListBOs) {
            addressListByStrings.add(toAddressListByStringDTO(addressListBO));
        }
        return addressListByStrings;
    }

    public AddressListByString toAddressListByStringDTO(AddressListBO addressListBO) {
        AddressListByString addressListByString = new AddressListByString();
        addressListByString.setAddressId(addressListBO.getAddressId());
        // Build address string by skipping null or empty fields
        List<String> parts = new ArrayList<>();
        addIfPresent(parts, addressListBO.getStreet1());
        addIfPresent(parts, addressListBO.getStreet2());
        addIfPresent(parts, addressListBO.getCity());
        addIfPresent(parts, addressListBO.getCounty());
        addIfPresent(parts, addressListBO.getState());
        addIfPresent(parts, addressListBO.getZipcode());
        addIfPresent(parts, addressListBO.getPhoneNumber());
        addressListByString.setAddress(String.join(",", parts));
        return addressListByString;
    }

    private void addIfPresent(List<String> parts, String value) {
        if (value == null) {
            return;
        }
        String trimmed = value.trim();
        if (!trimmed.isEmpty()) {
            parts.add(trimmed);
        }
    }


    public List<ProviderList> getAllListOriginatingProvidersByProviderId(int providerId) {
        return listDao.getOriginatorProviderListByProviderId(providerId);
    }


    public List<ProviderList> getAllListClaimingProvidersByProviderId(int providerId) {
        return listDao.getClaimantProviderListByProviderId(providerId);
    }

}
