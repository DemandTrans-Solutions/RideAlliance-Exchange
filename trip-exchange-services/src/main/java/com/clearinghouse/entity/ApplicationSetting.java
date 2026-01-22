/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.entity;

import jakarta.persistence.*;

import java.io.Serializable;

/**
 *
 * @author chaitanyaP
 */
@Entity
@Table(name = "applicationsetting")
public class ApplicationSetting implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ApplicationSettingID")
    private int applicationSettingId;

    @Column(name = "ConfigurationEmail")
    private String configurationEmail;

    @Column(name = "FromEmail")
    private String fromEmail;

    @Column(name = "PasswordOfMail")
    private String passwordOfMail;

    @Column(name = "PasswrodExpiredAfterDays")
    private int passwrodExpiredAfterDays;

    @Column(name = "ClaimApprovalTimeInHours")
    private int claimApprovalTimeInHours;

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public int getApplicationSettingId() {
        return applicationSettingId;
    }

    public void setApplicationSettingId(int applicationSettingId) {
        this.applicationSettingId = applicationSettingId;
    }

    public String getConfigurationEmail() {
        return configurationEmail;
    }

    public void setConfigurationEmail(String configurationEmail) {
        this.configurationEmail = configurationEmail;
    }

    public String getPasswordOfMail() {
        return passwordOfMail;
    }

    public void setPasswordOfMail(String passwordOfMail) {
        this.passwordOfMail = passwordOfMail;
    }

    public int getPasswrodExpiredAfterDays() {
        return passwrodExpiredAfterDays;
    }

    public void setPasswrodExpiredAfterDays(int passwrodExpiredAfterDays) {
        this.passwrodExpiredAfterDays = passwrodExpiredAfterDays;
    }

    public int getClaimApprovalTimeInHours() {
        return claimApprovalTimeInHours;
    }

    public void setClaimApprovalTimeInHours(int claimApprovalTimeInHours) {
        this.claimApprovalTimeInHours = claimApprovalTimeInHours;
    }

    @Override
    public String toString() {
        return "ApplicationSetting{" + "applicationSettingId=" + applicationSettingId + ", configurationEmail=" + configurationEmail + ", fromEmail=" + fromEmail + ", passwordOfMail=" + passwordOfMail + ", passwrodExpiredAfterDays=" + passwrodExpiredAfterDays + ", claimApprovalTimeInHours=" + claimApprovalTimeInHours + '}';
    }

}
