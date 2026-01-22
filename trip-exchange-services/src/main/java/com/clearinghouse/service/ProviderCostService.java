package com.clearinghouse.service;

import com.clearinghouse.dto.ProviderCostDTO;
import com.clearinghouse.entity.ProviderCost;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Shankar I
 */
@Service
@Transactional
@AllArgsConstructor
public class ProviderCostService implements IConvertBOToDTO, IConvertDTOToBO {


    private final com.clearinghouse.dao.ProviderCostDAO providerCostDAO;


    private final ModelMapper providerCostModelMapper;


    public List<ProviderCostDTO> findAllProvidersCost() {
        List<ProviderCost> providersCost = providerCostDAO.findAllProvidersCost();

        List<ProviderCostDTO> providerCostDTOList = new ArrayList<>();
        for (ProviderCost providerCost : providersCost) {

            providerCostDTOList.add((ProviderCostDTO) toDTO(providerCost));
        }

        return providerCostDTOList;

    }


    public ProviderCostDTO getCostByProviderId(int providerId) {
        ProviderCost providerCost = providerCostDAO.findCostByProviderId(providerId);

        ProviderCostDTO providerCostDTO = new ProviderCostDTO();
        if (providerCost == null) {
            return null;
        } else {
            providerCostDTO = (ProviderCostDTO) toDTO(providerCost);
            return providerCostDTO;
        }
    }

    @Override
    public Object toBO(Object dto) {
        ProviderCostDTO providerCostDTO = (ProviderCostDTO) dto;
        ProviderCost providerCostBO = providerCostModelMapper.map(providerCostDTO, ProviderCost.class);
        return providerCostBO;
    }

    @Override
    public Object toDTOCollection(Object boCollection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object toDTO(Object bo) {
        ProviderCost providerCost = (ProviderCost) bo;
        ProviderCostDTO providerCostDTO = providerCostModelMapper.map(providerCost, ProviderCostDTO.class);
        return providerCostDTO;
    }


    public ProviderCostDTO createUpdateProviderCost(ProviderCostDTO providerCostDTO) {

        // convert toBO
        ProviderCost providerCostBO = (ProviderCost) toBO(providerCostDTO);

        ProviderCost newProviderCost = new ProviderCost();

        if (providerCostBO.getProviderCostId() == 0) {
            newProviderCost = providerCostDAO.createProviderCost(providerCostBO);
        } else {
            newProviderCost = providerCostDAO.updateProviderCost(providerCostBO);
        }

        ProviderCostDTO providerCostDto = (ProviderCostDTO) toDTO(newProviderCost);

        return providerCostDto;
    }

}
