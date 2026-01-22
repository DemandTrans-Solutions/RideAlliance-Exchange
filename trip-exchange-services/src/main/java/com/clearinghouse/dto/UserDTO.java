/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 *
 * @author chaitanyaP
 */

@Getter
@Setter
@NoArgsConstructor
public class UserDTO {
    private int id;
    private String username;
    private String password;
    private boolean accountExpired;
//    private boolean accountLocked;
//    private boolean credentialsExpired;
//    private boolean accountDisabled;

    //##################
//Skipping role type..
//#######################
    private int providerId;

    private String providerName;

    private String jobTitle;

    @NotNull
    private String email;
    private String name;
    private String userRole;
    private String phoneNumber;

    //    private Date loginConfirmationSentDate;
//    private Date loginConfirmationDate;
//    private String unconfirmedEmail;
//    private String resetPasswordToken;
//    private Date resetPasswordRequestDate;
    @JsonProperty("isPasswordExpired")
    private boolean isPasswordExpired;
    private Date resetPasswordDate;
    //    private int loginCount;
    private int failedAttempts;
    //    private Date CurrentLoginDate;
    private Date lastFailedAttemptDate;

    //    private Date lastLoginDate;
//
    private String currentLoginIp;
    private String lastLoginIp;
    @JsonProperty("isNotifyPartnerCreatesTicket")
    private boolean isNotifyPartnerCreatesTicket;
    @JsonProperty("isNotifyPartnerUpdateTicket")
    private boolean isNotifyPartnerUpdateTicket;
    @JsonProperty("isNotifyClaimedTicketRescinded")
    private boolean isNotifyClaimedTicketRescinded;
    @JsonProperty("isNotifyClaimedTicketExpired")
    private boolean isNotifyClaimedTicketExpired;
    @JsonProperty("isNotifyNewTripClaimAwaitingApproval")
    private boolean isNotifyNewTripClaimAwaitingApproval;
    @JsonProperty("isNotifyNewTripClaimAutoApproved")
    private boolean isNotifyNewTripClaimAutoApproved;
    @JsonProperty("isNotifyTripClaimApproved")
    private boolean isNotifyTripClaimApproved;
    @JsonProperty("isNotifyTripClaimDeclined")
    private boolean isNotifyTripClaimDeclined;
    @JsonProperty("isNotifyTripClaimRescinded")
    private boolean isNotifyTripClaimRescinded;
    @JsonProperty("isNotifyTripCommentAdded")
    private boolean isNotifyTripCommentAdded;
    @JsonProperty("isNotifyTripResultSubmitted")
    private boolean isNotifyTripResultSubmitted;
    @JsonProperty("isNotifyTripReceived")
    private boolean isNotifyTripReceived;
    @JsonProperty("isNotifyTripCancelled")
    private boolean isNotifyTripCancelled;
    @JsonProperty("isNotifyTripExpired")
    private boolean isNotifyTripExpired;
    @JsonProperty("isNotifyTripWeeklyReport")
    private boolean isNotifyTripWeeklyReport;
    @JsonProperty("isNotifyTripClaimCancelled")
    private boolean isNotifyTripClaimCancelled;
    @JsonProperty("isNotifyTripPriceMismatched")
    private boolean isNotifyTripPriceMismatched;
    @JsonProperty("accountLocked")
    private boolean accountLocked;
    @JsonProperty("accountDisabled")
    private boolean accountDisabled;
    @JsonProperty("authanticationTypeIsAdapter")
    private boolean authanticationTypeIsAdapter;
    @JsonProperty("isActive")
    private boolean isActive;


    @Override
    public String toString() {
        return "UserDTO{" + "id=" + id + ", username=" + username + ", password=" + password + ", accountExpired=" + accountExpired + ", providerId=" + providerId + ", providerName=" + providerName + ", jobTitle=" + jobTitle + ", email=" + email + ", name=" + name + ", userRole=" + userRole + ", phoneNumber=" + phoneNumber + ", isPasswordExpired=" + isPasswordExpired + ", resetPasswordDate=" + resetPasswordDate + ", failedAttempts=" + failedAttempts + ", lastFailedAttemptDate=" + lastFailedAttemptDate + ", currentLoginIp=" + currentLoginIp + ", lastLoginIp=" + lastLoginIp + ", isNotifyPartnerCreatesTicket=" + isNotifyPartnerCreatesTicket + ", isNotifyPartnerUpdateTicket=" + isNotifyPartnerUpdateTicket + ", isNotifyClaimedTicketRescinded=" + isNotifyClaimedTicketRescinded + ", isNotifyClaimedTicketExpired=" + isNotifyClaimedTicketExpired + ", isNotifyNewTripClaimAwaitingApproval=" + isNotifyNewTripClaimAwaitingApproval + ", isNotifyNewTripClaimAutoApproved=" + isNotifyNewTripClaimAutoApproved + ", isNotifyTripClaimApproved=" + isNotifyTripClaimApproved + ", isNotifyTripClaimDeclined=" + isNotifyTripClaimDeclined + ", isNotifyTripClaimRescinded=" + isNotifyTripClaimRescinded + ", isNotifyTripCommentAdded=" + isNotifyTripCommentAdded + ", isNotifyTripResultSubmitted=" + isNotifyTripResultSubmitted + ", isActive=" + isActive + '}';
    }

}
