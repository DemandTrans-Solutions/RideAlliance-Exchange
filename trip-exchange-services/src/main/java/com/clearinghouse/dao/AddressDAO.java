package com.clearinghouse.dao;

import com.clearinghouse.dto.AddressDTO;
import com.clearinghouse.entity.Address;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class AddressDAO extends AbstractDAO<Integer, Address> {

    @PersistenceContext
    EntityManager entityManagerNew;


    public List<Address> findCustomerAdddress() {
        List<Address> addresses = getEntityManager()
                .createQuery("SELECT a FROM Address a ")
                .getResultList();
        return addresses;
    }

    public Integer findExistingAddressId(AddressDTO customerAddressDTO) {
        int addrsId = (int) getEntityManager()
                .createQuery("SELECT a.addressId FROM Address a WHERE a.street1=:street1 AND a.city=:city AND a.state=:state AND a.zipcode=:zip ")
                .setParameter("street1", customerAddressDTO.getStreet1()).setParameter("city", customerAddressDTO.getCity()).setParameter("state", customerAddressDTO.getState())
                .setParameter("zip", customerAddressDTO.getZipcode()).getSingleResult();
        return addrsId;
    }


    public Address updateAddressById(AddressDTO customerAddressDTO) {
        Address updatedAddress = (Address) getEntityManager()
                .createQuery("UPDATE Address a SET a.street2=:street2 WHERE a.addressId=:addressId")
                .setParameter("street2", customerAddressDTO.getStreet2()).setParameter("addressId", customerAddressDTO.getAddressId())
                .getSingleResult();
        return updatedAddress;
    }


    public Address createNewAddress(Address address) {
        add(address);
        return address;
    }


    public boolean checkForAddrsDuplication(AddressDTO customerAddressDTO) {
        boolean flag = false;
        long count = (long) getEntityManager().createQuery(
                        "SELECT count(a.addressId) FROM Address a WHERE a.street1=:street1 AND a.city=:city AND a.state=:state AND a.zipcode=:zip ")
                .setParameter("street1", customerAddressDTO.getStreet1())
                .setParameter("city", customerAddressDTO.getCity()).setParameter("state", customerAddressDTO.getState())
                .setParameter("zip", customerAddressDTO.getZipcode()).getSingleResult();
        if (count > 0) {
            flag = true;
        }
        return flag;
    }

    private TypedQuery<Address> createAddressLocationQuery(AddressDTO addressDTO) {
        return getEntityManager().createQuery("SELECT a FROM Address a WHERE a.street1=:street1 AND a.city=:city AND a.state=:state AND a.zipcode=:zip", Address.class)
                .setParameter("street1", addressDTO.getStreet1())
                .setParameter("city", addressDTO.getCity())
                .setParameter("state", addressDTO.getState())
                .setParameter("zip", addressDTO.getZipcode());
    }

    public Address findExistingAddress(AddressDTO addressDTO) {
        List<Address> addresses = createAddressLocationQuery(addressDTO).getResultList();

        try {
            // Make sure the lat / long coords are a match, if they are not, update to the latest
            addresses.forEach(address -> {
                if (addressDTO.getLatitude() != 0 && addressDTO.getLongitude() != 0 &&
                        (addressDTO.getLatitude() != address.getLatitude() || addressDTO.getLongitude() != address.getLongitude())) {
                    log.debug("Address " + addressDTO + " has updated lat long coords. Changing from " + address.getLatitude() +
                            "," + address.getLongitude() + " to " + addressDTO.getLatitude() + "," + addressDTO.getLongitude());
                    address.setLatitude(addressDTO.getLatitude());
                    address.setLongitude(addressDTO.getLongitude());
                    update(address);
                }
            });
        } catch (Exception e) {
            log.error("Error updating address lat long coords: " + e.getMessage());
        }
        return addresses.isEmpty() ? null : addresses.get(0);
    }


    public Address updateAddress(Address address) {
        update(address);
        return address;
    }
}

