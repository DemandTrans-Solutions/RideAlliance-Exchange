package com.clearinghouse.dao;
/**
 * @author ShankarI
 */

import com.clearinghouse.dto.TripTicketDTO;
import com.clearinghouse.entity.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class CustomerDAO extends AbstractDAO<Integer, Customer> {

    @PersistenceContext
    EntityManager entityManagerNew;


    public List<Customer> findAllCustomers() {
        List<Customer> customers = getEntityManager()
                .createQuery("SELECT s FROM Customer s ")
                .getResultList();
        return customers;
    }


    public Customer createCustomer(Customer customer) {
        add(customer);
        return customer;
    }


    public Integer findLatestCustomerIdForTripticket() {
        Query query = getEntityManager()
                .createQuery("SELECT MAX(c.customerId) from Customer c");
        Integer custId = (Integer) query.getSingleResult();
        return custId;
    }

    //new added by shankar I
    public String getCustomerStatusByMsgId(Integer customerStatusId) {
        Query query = getEntityManager()
                .createQuery("SELECT c.message FROM CustomerStatus c WHERE c.messageId = :msgId ");
        String msg = (String) query.setParameter("msgId", customerStatusId).getSingleResult();
        return msg;
    }


    public boolean checkForCustomerDuplication(TripTicketDTO tripticketDTO) {
        boolean flag = false;
        long count = (long) getEntityManager().createQuery(
                        "SELECT count(c.customerId) FROM Customer c WHERE c.customerFirstName=:firstName AND c.customerLastName=:lastName "
                                + "AND c.customerPrimaryPhone=:phone AND c.customerAddress.addressId=:custAddrsId ")
                .setParameter("firstName", tripticketDTO.getCustomerFirstName())
                .setParameter("lastName", tripticketDTO.getCustomerLastName())
                .setParameter("phone", tripticketDTO.getCustomerHomePhone())
                .setParameter("custAddrsId", tripticketDTO.getCustomerAddress().getAddressId()).getSingleResult();
        if (count > 0) {
            flag = true;
        }
        return flag;
    }
}