/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author chaitanyaP
 */

@Getter
@Setter
@NoArgsConstructor

public class ApplicationSettingDTO {

    private int applicationSettingId;
    private String configurationEmail;
    private String fromEmail;
    private String passwordOfMail;
    private int passwordExpiredAfterDays;
    private int claimApprovalTimeInHours;

    public ApplicationSettingDTO(String mail, String invalidPassword) {
        this.configurationEmail = mail;
        this.fromEmail = mail;
        this.passwordOfMail = invalidPassword;
    }


    @Override
    public String toString() {
        return "ApplicationSettingDTO{" + "applicationSettingId=" + applicationSettingId + ", configurationEmail=" + configurationEmail + ", fromEmail=" + fromEmail + ", passwordOfMail=" + passwordOfMail + ", passwordExpiredAfterDays=" + passwordExpiredAfterDays + ", claimApprovalTimeInHours=" + claimApprovalTimeInHours + '}';
    }

}
