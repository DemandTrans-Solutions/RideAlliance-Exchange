package com.clearinghouse.service;

import com.clearinghouse.dto.FundingSourceDTO;
import com.clearinghouse.entity.FundingSource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class FundingSourceService implements IConvertBOToDTO, IConvertDTOToBO {


    private final com.clearinghouse.dao.FundingSourceDAO fundindSourceDAO;


    private final ModelMapper fundingSourceModelMapper;


    public List<FundingSourceDTO> findAllFundingSources() {
        List<FundingSource> fundingSources = fundindSourceDAO.getAllFundingSources();

        List<FundingSourceDTO> fundingSourceDTOs = new ArrayList<FundingSourceDTO>();

        for (FundingSource fundungSource : fundingSources) {
            FundingSourceDTO fundingSourceDTO = (FundingSourceDTO) toDTO(fundungSource);
            fundingSourceDTOs.add(fundingSourceDTO);
        }
        return fundingSourceDTOs;
    }


    public FundingSourceDTO createFundingSource(FundingSourceDTO fundingSourceDTO) {

        FundingSource fundingSource = (FundingSource) toBO(fundingSourceDTO);
        fundingSource.setStatus(true);
        FundingSource fundingSourceBO = fundindSourceDAO.createFundingSource(fundingSource);
        return (FundingSourceDTO) toDTO(fundingSourceBO);
    }


    public Object toBO(Object dto) {
        FundingSourceDTO fundingSourceDTO = (FundingSourceDTO) dto;
        FundingSource fundingSourceBO = fundingSourceModelMapper.map(fundingSourceDTO, FundingSource.class);
        return fundingSourceBO;
    }

    @Override
    public Object toDTOCollection(Object boCollection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object toDTO(Object bo) {
        FundingSource fundingSource = (FundingSource) bo;
        FundingSourceDTO fundingSourceDTO = fundingSourceModelMapper.map(fundingSource, FundingSourceDTO.class);
        return fundingSourceDTO;
    }


    public boolean findFundingSourceByName(String name) {
        return fundindSourceDAO.findFundingSourceByName(name);
    }


    public FundingSourceDTO findFundingSourceById(int fundingSourceId) {
        FundingSource fundingSource = fundindSourceDAO.findFundingSourceById(fundingSourceId);
        if (fundingSource != null) {
            return (FundingSourceDTO) toDTO(fundingSource);
        } else {
            return null;
        }
    }


    public FundingSourceDTO updateFundingSource(FundingSourceDTO fundingSourceDTO) {
        FundingSource fundingSource = (FundingSource) toBO(fundingSourceDTO);
        fundindSourceDAO.updateFundingSource(fundingSource);

        return (FundingSourceDTO) toDTO(fundingSource);
    }


    public FundingSourceDTO activateFundingSource(int fundingSourceId) {

        FundingSource fundingSource = fundindSourceDAO.activateFundingSource(fundingSourceId);
        fundingSource.setStatus(true);
        //update status to activate
        fundindSourceDAO.updateFundingSource(fundingSource);

        FundingSourceDTO fundingSourceDTO = (FundingSourceDTO) toDTO(fundingSource);
        return fundingSourceDTO;
    }


    public FundingSourceDTO deactivateFundingSource(int fundingSourceId) {

        FundingSource fundingSource = fundindSourceDAO.deactivateFundingSource(fundingSourceId);
        fundingSource.setStatus(false);
        //update status to deactivate
        fundindSourceDAO.updateFundingSource(fundingSource);

        FundingSourceDTO fundingSourceDTO = (FundingSourceDTO) toDTO(fundingSource);
        return fundingSourceDTO;
    }

}
