package com.clearinghouse.entity;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author shankarI
 */
@Entity
@Table(name = "customer")
public class Customer extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerId")
    private Integer customerId;

    @Column(name = "CustomerFirstName")
    private String customerFirstName;

    @Column(name = "CustomerMiddleName")
    private String customerMiddleName;

    @Column(name = "CustomerLastName")
    private String customerLastName;

    @Column(name = "CustomerPrimaryPhone")
    private String customerPrimaryPhone;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "DD-MM-YY HH:mm")
    @Column(name = "CustomerDateOfBirth")
    private Date customerDob;

    @OneToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "CustomerAddressID")
    private Address customerAddress;

    @Column(name = "NickName")
    private String nickName ;

    @Column(name = "CustomerMobilePhone")
    private String customerMobilePhone;

    @OneToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "MailingBillingAddressID")
    private Address mailingBillingAddress;

    @Column(name = "FundingBillingInformation")
    private String fundingBillingInformation;

    @OneToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "FundingEntityId")
    private FundingEntity fundingEntity;

    @Column(name = "Gender")
    private String gender;

    @Column(name = "LowIncome")
    private Boolean lowIncome;


    @Column(name = "Disability")
    private Boolean disability;

    @Column(name = "Vetern")
    private Boolean vetern;

    @Column(name = "Language")
    private String language;


    @Column(name = "Race")
    private String race;

    @OneToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "EthnicityID")
    private Ethnicity ethnicity;


    @Column(name = "EmailAddress")
    private String emailAddress;

    @Column(name = "CaregiverContactInfo")
    private String caregiverContactInfo;

    @Column(name = "EmergencyPhone")
    private String emergencyPhone;


    @Column(name = "EmergencyContactName")
    private String emergencyContactName;

    @Column(name = "EmergencyContactRelationship")
    private String emergencyContactRelationship;


    @Column(name = "RequiredCareComments")
    private String requiredCareComments;


    @Column(name = "NotesForDriver")
    private String notesForDriver;


    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerFirstName() {
        return customerFirstName;
    }

    public void setCustomerFirstName(String customerFirstName) {
        this.customerFirstName = customerFirstName;
    }

    public String getCustomerMiddleName() {
        return customerMiddleName;
    }

    public void setCustomerMiddleName(String customerMiddleName) {
        this.customerMiddleName = customerMiddleName;
    }

    public String getCustomerLastName() {
        return customerLastName;
    }

    public void setCustomerLastName(String customerLastName) {
        this.customerLastName = customerLastName;
    }

    public String getCustomerPrimaryPhone() {
        return customerPrimaryPhone;
    }

    public void setCustomerPrimaryPhone(String customerPrimaryPhone) {
        this.customerPrimaryPhone = customerPrimaryPhone;
    }

    public Address getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(Address customerAddress) {
        this.customerAddress = customerAddress;
    }

    public Date getCustomerDob() {
        return customerDob;
    }

    public void setCustomerDob(Date customerDob) {
        this.customerDob = customerDob;
    }

    @Override
    public String toString() {
        return "Customer [customerId=" + customerId + ", customerFirstName=" + customerFirstName
                + ", customerMiddleName=" + customerMiddleName + ", customerLastName=" + customerLastName
                + ", customerPrimaryPhone=" + customerPrimaryPhone + ", customerDob=" + customerDob
                + ", customerAddress=" + customerAddress + "]";
    }

    // Default constructor
    public Customer() {
    }

    // Constructor with ID parameter
    public Customer(Integer customerId) {
        this.customerId = customerId;
    }
}