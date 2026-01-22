package com.clearinghouse.service;

import com.clearinghouse.dao.CustomerDAO;
import com.clearinghouse.dto.TripTicketDTO;
import com.clearinghouse.entity.Customer;
import com.clearinghouse.enumentity.CustomerStatusConstants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;


@Service
@AllArgsConstructor
@Slf4j
public class CustomerService {


    private final CustomerDAO customerDAO;


    public TripTicketDTO createCustomerByTripTicketDTO(TripTicketDTO newTripTicketDTO) {

        List<Customer> getcustomerList = customerDAO.findAllCustomers();

        String checkDuplicate = null;
        String checkNotDuplicate = null;

        for (Customer customerBO : getcustomerList) {
            //String duplicateString="";
            int i = 0;

            if ((newTripTicketDTO.getCustomerFirstName() != null) && (newTripTicketDTO.getCustomerLastName() != null)) {
                if (newTripTicketDTO.getCustomerFirstName().equals(customerBO.getCustomerFirstName()) && newTripTicketDTO.getCustomerLastName().equals(customerBO.getCustomerLastName())) {
                    ++i;
                    //duplicateString= duplicateString + " Customer Name ";
                }
            }
            if (newTripTicketDTO.getCustomerAddress() != null) {
                if (newTripTicketDTO.getCustomerAddress().getAddressId() == (customerBO.getCustomerAddress().getAddressId())) {
                    //		++i;
                    //	duplicateString= duplicateString + ", Address ";
                    //}
                    //if(!(newTripTicketDTO.getCustomerAddress().getZipcode().isEmpty())) {
		/*if(newTripTicketDTO.getCustomerAddress().getStreet1().equalsIgnoreCase(customerBO.getCustomerAddress().getStreet1())
			    && newTripTicketDTO.getCustomerAddress().getZipcode().equalsIgnoreCase(customerBO.getCustomerAddress().getZipcode())
				&& newTripTicketDTO.getCustomerAddress().getLatitude().equalsIgnoreCase(customerBO.getCustomerAddress().getLatitude())
			    && newTripTicketDTO.getCustomerAddress().getLongitude().equalsIgnoreCase(customerBO.getCustomerAddress().getLongitude()) 
			    && newTripTicketDTO.getCustomerAddress().getCity().equalsIgnoreCase(customerBO.getCustomerAddress().getCity())
			    && newTripTicketDTO.getCustomerAddress().getState().equalsIgnoreCase(customerBO.getCustomerAddress().getState())){*/
                    ++i;
                    //	duplicateString= duplicateString + ", Address ";
                }
            }
            if (newTripTicketDTO.getCustomerDob() != null && customerBO.getCustomerDob() != null) {
                LocalDate existingDob = convertToLocalDate(customerBO.getCustomerDob());
                if (newTripTicketDTO.getCustomerDob().equals(existingDob)) {
                    ++i;
                    //duplicateString= duplicateString + ", customer_DOB ";
                }
            }
            if (newTripTicketDTO.getCustomerHomePhone() != null) {
                if (newTripTicketDTO.getCustomerHomePhone().equals(customerBO.getCustomerPrimaryPhone())) {
                    ++i;
                    //	duplicateString= duplicateString + ", customer_Phone ";
                }
            }
//		log.debug(duplicateString + "are Duplicate values********************");
            if (i == 4) {
                checkDuplicate = null;
                //checkDuplicate="Trip ticket is created. This client is already Registered in the Hub.";
                checkDuplicate = customerDAO.getCustomerStatusByMsgId(CustomerStatusConstants.existingCustomer.getCustomerStatus());
            } else {
                checkNotDuplicate = null;
                //	checkNotDuplicate="Trip ticket is created. New client is Registered.";
                checkNotDuplicate = customerDAO.getCustomerStatusByMsgId(CustomerStatusConstants.newCustomer.getCustomerStatus());
                Customer customer = new Customer();
                customer.setCustomerFirstName(newTripTicketDTO.getCustomerFirstName());
                customer.setCustomerMiddleName(newTripTicketDTO.getCustomerMiddleName());
                customer.setCustomerLastName(newTripTicketDTO.getCustomerLastName());
                customer.setCustomerPrimaryPhone(newTripTicketDTO.getCustomerHomePhone());
                if (newTripTicketDTO.getCustomerDob() != null) {
                    customer.setCustomerDob(java.sql.Date.valueOf(newTripTicketDTO.getCustomerDob()));
                }
                customer.getCustomerAddress().setAddressId(newTripTicketDTO.getCustomerAddress().getAddressId());
                //customer.getCustomerAddress().setAddressId(newTripTicketDTO.getCustomer_addressId().getAddressId());

                customerDAO.createCustomer(customer);
            }
        }
        if (checkDuplicate != null) {
            newTripTicketDTO.setCustomerStatusForDuplication(checkDuplicate);
            log.debug("***************************############" + "This client is already Registered in the Hub." +
                    "*************** Trip ticket is created." + "*************************************************");
        } else {
            newTripTicketDTO.setCustomerStatusForDuplication(checkNotDuplicate);
            log.debug("***************************############" + "New Customer is Registered." +
                    "*********************** Trip ticket is created." + "*************************************************");
        }

        return newTripTicketDTO;
    }

    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}