package com.clearinghouse.dto;

import com.clearinghouse.entity.Address;
import com.clearinghouse.entity.Ethnicity;
import com.clearinghouse.entity.FundingEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class CustomerDTO {

    private int CustomerId;

    @NotBlank
    @Size(min = 1, max = 150)
    @JsonProperty("customer_first_name")
    private String customerFirstName;
    @JsonProperty("customer_middle_name")
    private String customerMiddleName;

    @NotBlank
    @Size(min = 1, max = 150)
    @JsonProperty("customer_last_name")
    private String customerLastName;

    @NotBlank
    @JsonProperty("customer_primary_phone")
    private String customerPrimaryPhone;

    @NotBlank
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("customer_dob")
    private Date customerDob;


    @JsonProperty("customer_address")
    private AddressDTO customerAddress;

    private String nickName ;

    private String customerMobilePhone;

    private AddressDTO mailingBillingAddress;

    private String fundingBillingInformation;

    private FundingEntityDTO fundingEntity;

    private String gender;

    private Boolean lowIncome;

    private Boolean disability;

    private Boolean vetern;

    private String language;

    private String race;

    private EthnicityDTO ethnicity;

    private String emailAddress;

    private String caregiverContactInfo;

    private String emergencyPhone;

    private String emergencyContactName;

    private String emergencyContactRelationship;

    private String requiredCareComments;

    private String notesForDriver;


    @Override
    public String toString() {
        return "CustomerDTO [CustomerId=" + CustomerId + ", customerFirstName=" + customerFirstName
                + ", customerMiddleName=" + customerMiddleName + ", customerLastName=" + customerLastName
                + ", customerPrimaryPhone=" + customerPrimaryPhone + ", customerDob=" + customerDob
                + ", customerAddress=" + customerAddress + "]";
    }

}
