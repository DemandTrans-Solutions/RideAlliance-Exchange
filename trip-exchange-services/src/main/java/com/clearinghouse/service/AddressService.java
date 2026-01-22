package com.clearinghouse.service;

import com.clearinghouse.dao.AddressDAO;
import com.clearinghouse.dto.AddressDTO;
import com.clearinghouse.entity.Address;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AddressService implements IConvertDTOToBO, IConvertBOToDTO {


    private final ModelMapper addressModelMapper;


    private final AddressDAO addressDao;


    public Address cloneAddressNewCoords(Address address, float latitude, float longitude) {
        var copy  = new Address();
        copy.setLatitude(latitude);
        copy.setLongitude(longitude);
        copy.setStreet1(address.getStreet1());
        copy.setStreet2(address.getStreet2());
        copy.setCity(address.getCity());
        copy.setState(address.getState());
        copy.setZipcode(address.getZipcode());
        copy.setAddressType(address.getAddressType());
        copy.setCommonName(address.getCommonName());
        copy.setPhoneExtension(address.getPhoneExtension());
        copy.setPhoneNumber(address.getPhoneNumber());
        return addressDao.createNewAddress(copy);
    }

    public Address convertIntoBO(AddressDTO customerAddressDTO) {
        Address address = (Address) toBO(customerAddressDTO);
        return address;
    }


    public AddressDTO convertIntoDTo(Address customerAddress) {
        AddressDTO addressDto = (AddressDTO) toDTO(customerAddress);
        return addressDto;
    }

    @Override
    public Object toBO(Object dto) {
        AddressDTO addressDTO = (AddressDTO) dto;
//	        stringtoLocalDateConversion
        Address addressBO = addressModelMapper.map(addressDTO, Address.class);
        return addressBO;
    }

    @Override
    public Object toDTO(Object bo) {
        Address addressBO = (Address) bo;
//        stringtoLocalDateConversion
        AddressDTO addressDTO = addressModelMapper.map(addressBO, AddressDTO.class);
        return addressDTO;
    }

    @Override
    public Object toDTOCollection(Object boCollection) {
        // TODO Auto-generated method stub
        return null;
    }


    public Address createAddress(Address dropOffAddressBO) {
        return addressDao.createNewAddress(dropOffAddressBO);

    }


}
