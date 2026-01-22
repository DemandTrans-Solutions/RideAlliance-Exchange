/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.Set;

/**
 *
 * @author chaitanyaP
 */
@Entity
@Table(name = "user", uniqueConstraints = @UniqueConstraint(columnNames = {"username"}))
public class User extends AbstractEntity implements UserDetails {

    public User() {
    }

    public User(String username) {

        this.username = username;
    }

    public User(int id) {

        this.id = id;
    }


    public User(String username, Date expires) {
        this.username = username;
        this.expires = expires.getTime();
    }

    @Id
    @Column(name = "UserID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "UserName")
    private String username;

    @JsonIgnore
    @Column(name = "Password")
    private String password;

    //newly added fields..
    @Transient
    private long expires;

    @Transient
    private String csrfToken;

    @JsonIgnore
    @Column(name = "TemporaryPassword")
    private String temporaryPassword;

    /// /
    @NotNull
    @Column(name = "AccountLocked")
    private Boolean accountLocked = Boolean.FALSE;

    @NotNull
    @Column(name = "AccountDisabled")
    private Boolean accountDisabled = Boolean.FALSE;

    @NotNull
    @Column(name = "AccountExpired")
    private Boolean accountExpired = Boolean.FALSE;

    @NotNull
    @Column(name = "CredentialsExpired")
    private Boolean credentialsExpired = Boolean.FALSE;

    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<UserAuthority> authorities;

    //###########################################################
    @Column(name = "AuthanticationTypeIsAdapter")

    private Boolean authanticationTypeIsAdapter;

    @Column(name = "ResponseDataForUI")
    private String responseDataForUI;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "ProviderID")
    private Provider provider;

    @Column(name = "JobTitle")
    private String jobTitle;

    @JsonIgnore
    @Column(name = "Email")
    private String email;

    @Column(name = "Name")
    private String name;

    @JsonIgnore
    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @JsonIgnore
    @Column(name = "LogInConfirmationSentDate")
    private Date loginConfirmationSentDate;

    @JsonIgnore
    @Temporal(TemporalType.DATE)
    @Column(name = "LogInConfirmationDate")
    private Date loginConfirmationDate;

    @JsonIgnore
    @Column(name = "UnconfirmedEmail")
    private String unconfirmedEmail;

    @JsonIgnore
    @Column(name = "ResetPasswordToken")
    private String resetPasswordToken;

    @Column(name = "IsPasswordExpired")

    private Boolean isPasswordExpired;

    @JsonIgnore
    @Temporal(TemporalType.DATE)
    @Column(name = "ResetPasswordRequestDate")
    private Date resetPasswordRequestDate;

    @JsonIgnore
    @Temporal(TemporalType.DATE)
    @Column(name = "ResetPasswordDate")
    private Date resetPasswordDate;

    @JsonIgnore
    @Column(name = "LogInCount")
    private int loginCount;

    @Column(name = "FailedAttempts")
    private int failedAttempts;

    @Temporal(TemporalType.DATE)
    @Column(name = "LastFailedAttemptDate")
    private Date lastFailedAttemptDate;

    @JsonIgnore
    @Column(name = "CurrentLogInDate")
    private Date currentLoginDate;

    @JsonIgnore
    @Column(name = "LastLogInDate")
    private Date lastLoginDate;

    @JsonIgnore
    @Column(name = "CurrentLoginIp")
    private String currentLoginIp;

    @JsonIgnore
    @Column(name = "LastLogInIP")
    private String lastLoginIp;

    @JsonIgnore
    @Column(name = "IsNotifyPartnerCreatesTicket")

    private Boolean isNotifyPartnerCreatesTicket;

    @JsonIgnore
    @Column(name = "IsNotifyPartnerUpdateTicket")
    private Boolean isNotifyPartnerUpdateTicket;

    @JsonIgnore
    @Column(name = "IsNotifyClaimedTicketRescinded")

    private Boolean isNotifyClaimedTicketRescinded;

    @JsonIgnore
    @Column(name = "IsNotifyClaimedTicketExpired")

    private Boolean isNotifyClaimedTicketExpired;

    @JsonIgnore
    @Column(name = "IsNotifyNewTripClaimAwaitingApproval")

    private Boolean isNotifyNewTripClaimAwaitingApproval;

    @JsonIgnore
    @Column(name = "IsNotifyNewTripClaimAutoApproved")

    private Boolean isNotifyNewTripClaimAutoApproved;

    @JsonIgnore
    @Column(name = "IsNotifyTripClaimApproved")

    private Boolean isNotifyTripClaimApproved;

    @JsonIgnore
    @Column(name = "IsNotifyTripClaimDeclined")

    private Boolean isNotifyTripClaimDeclined;

    @JsonIgnore
    @Column(name = "IsNotifyTripClaimRescinded")

    private Boolean isNotifyTripClaimRescinded;

    @JsonIgnore
    @Column(name = "IsNotifyTripCommentAdded")

    private Boolean isNotifyTripCommentAdded;

    @JsonIgnore
    @Column(name = "IsNotifyTripResultSubmitted")

    private Boolean isNotifyTripResultSubmitted;

    @JsonIgnore
    @Column(name = "IsNotifyTripReceived")

    private Boolean isNotifyTripReceived;

    @JsonIgnore
    @Column(name = "IsNotifyTripCancelled")

    private Boolean isNotifyTripCancelled;

    @JsonIgnore
    @Column(name = "IsNotifyTripExpired")

    private Boolean isNotifyTripExpired;

    @JsonIgnore
    @Column(name = "IsNotifyTripWeeklyReport")

    private Boolean isNotifyTripWeeklyReport;

    @JsonIgnore
    @Column(name = "IsNotifyTripClaimCancelled")

    private Boolean isNotifyTripClaimCancelled;

    @JsonIgnore
    @Column(name = "IsNotifyTripPriceMismatched")

    private Boolean isNotifyTripPriceMismatched;

    @JsonIgnore
    @Column(name = "AccountLockedDate")
    private Date AccountLockedDate;

    @JsonIgnore
    @Column(name = "PrePassword1")
    private String oldPassword1;

    @JsonIgnore
    @Column(name = "PrePassword2")
    private String oldPassword2;

    @JsonIgnore
    @Column(name = "PrePassword3")
    private String oldPassword3;

    @JsonIgnore
    @Column(name = "PrePassword4")
    private String oldPassword4;

    @JsonIgnore
    @Column(name = "IsActive")
    private Boolean isActive;

    @Transient
    private String JWTToken;

    public String getJWTToken() {
        return JWTToken;
    }

    public void setJWTToken(String JWTToken) {
        this.JWTToken = JWTToken;
    }

    public boolean isIsPasswordExpired() {
        return isPasswordExpired;
    }

    public void setIsPasswordExpired(boolean isPasswordExpired) {
        this.isPasswordExpired = isPasswordExpired;
    }

    public Date getLastFailedAttemptDate() {
        return lastFailedAttemptDate;
    }

    public void setLastFailedAttemptDate(Date lastFailedAttemptDate) {
        this.lastFailedAttemptDate = lastFailedAttemptDate;
    }

    public String getResponseDataForUI() {
        return responseDataForUI;
    }

    public void setResponseDataForUI(String responseDataForUI) {
        this.responseDataForUI = responseDataForUI;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<UserAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<UserAuthority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !Boolean.TRUE.equals(accountExpired);
    }

    @Override
    public boolean isAccountNonLocked() {
        return !Boolean.TRUE.equals(accountLocked);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !Boolean.TRUE.equals(credentialsExpired);
    }

    @Override
    public boolean isEnabled() {
        return !Boolean.TRUE.equals(accountDisabled);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isAccountExpired() {
        return Boolean.TRUE.equals(accountExpired);
    }

    public void setAccountExpired(boolean accountExpired) {
        this.accountExpired = accountExpired;
    }

    public boolean isAccountLocked() {
        return Boolean.TRUE.equals(accountLocked);
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public boolean isCredentialsExpired() {
        return Boolean.TRUE.equals(credentialsExpired);
    }

    public void setCredentialsExpired(boolean credentialsExpired) {
        this.credentialsExpired = credentialsExpired;
    }

    public boolean isAccountDisabled() {
        return Boolean.TRUE.equals(accountDisabled);
    }

    public void setAccountDisabled(boolean accountDisabled) {
        this.accountDisabled = accountDisabled;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    //    public Titles getTitle() {
//        return title;
//    }
//
//    public void setTitle(Titles title) {
//        this.title = title;
//    }
    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Date getLoginConfirmationSentDate() {
        return loginConfirmationSentDate;
    }

    public void setLoginConfirmationSentDate(Date loginConfirmationSentDate) {
        this.loginConfirmationSentDate = loginConfirmationSentDate;
    }

    public Date getLoginConfirmationDate() {
        return loginConfirmationDate;
    }

    public void setLoginConfirmationDate(Date loginConfirmationDate) {
        this.loginConfirmationDate = loginConfirmationDate;
    }

    public String getUnconfirmedEmail() {
        return unconfirmedEmail;
    }

    public void setUnconfirmedEmail(String unconfirmedEmail) {
        this.unconfirmedEmail = unconfirmedEmail;
    }

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public Date getResetPasswordRequestDate() {
        return resetPasswordRequestDate;
    }

    public void setResetPasswordRequestDate(Date resetPasswordRequestDate) {
        this.resetPasswordRequestDate = resetPasswordRequestDate;
    }

    public Date getResetPasswordDate() {
        return resetPasswordDate;
    }

    public void setResetPasswordDate(Date resetPasswordDate) {
        this.resetPasswordDate = resetPasswordDate;
    }

    public int getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(int loginCount) {
        this.loginCount = loginCount;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public Date getCurrentLoginDate() {
        return currentLoginDate;
    }

    public void setCurrentLoginDate(Date currentLoginDate) {
        this.currentLoginDate = currentLoginDate;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public String getCurrentLoginIp() {
        return currentLoginIp;
    }

    public void setCurrentLoginIp(String currentLoginIp) {
        this.currentLoginIp = currentLoginIp;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public boolean isIsNotifyPartnerCreatesTicket() {
        return isNotifyPartnerCreatesTicket;
    }

    public void setIsNotifyPartnerCreatesTicket(boolean isNotifyPartnerCreatesTicket) {
        this.isNotifyPartnerCreatesTicket = isNotifyPartnerCreatesTicket;
    }

    public boolean isIsNotifyPartnerUpdateTicket() {
        return isNotifyPartnerUpdateTicket;
    }

    public void setIsNotifyPartnerUpdateTicket(boolean isNotifyPartnerUpdateTicket) {
        this.isNotifyPartnerUpdateTicket = isNotifyPartnerUpdateTicket;
    }

    public boolean isIsNotifyClaimedTicketRescinded() {
        return isNotifyClaimedTicketRescinded;
    }

    public void setIsNotifyClaimedTicketRescinded(boolean isNotifyClaimedTicketRescinded) {
        this.isNotifyClaimedTicketRescinded = isNotifyClaimedTicketRescinded;
    }

    public boolean isIsNotifyClaimedTicketExpired() {
        return isNotifyClaimedTicketExpired;
    }

    public void setIsNotifyClaimedTicketExpired(boolean isNotifyClaimedTicketExpired) {
        this.isNotifyClaimedTicketExpired = isNotifyClaimedTicketExpired;
    }

    public String getOldPassword1() {
        return oldPassword1;
    }

    public void setOldPassword1(String oldPassword1) {
        this.oldPassword1 = oldPassword1;
    }

    public String getOldPassword2() {
        return oldPassword2;
    }

    public void setOldPassword2(String oldPassword2) {
        this.oldPassword2 = oldPassword2;
    }

    public String getOldPassword3() {
        return oldPassword3;
    }

    public void setOldPassword3(String oldPassword3) {
        this.oldPassword3 = oldPassword3;
    }

    public String getOldPassword4() {
        return oldPassword4;
    }

    public void setOldPassword4(String oldPassword4) {
        this.oldPassword4 = oldPassword4;
    }

    public Date getAccountLockedDate() {
        return AccountLockedDate;
    }

    public void setAccountLockedDate(Date AccountLockedDate) {
        this.AccountLockedDate = AccountLockedDate;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public String getCsrfToken() {
        return csrfToken;
    }

    public void setCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }

    public void setTemporaryPassword(String temporaryPassword) {
        this.temporaryPassword = temporaryPassword;
    }

    public boolean isActive() {
        return isActive;
    }


    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isAuthanticationTypeIsAdapter() {
        return authanticationTypeIsAdapter;
    }

    public void setAuthanticationTypeIsAdapter(boolean authanticationTypeIsAdapter) {
        this.authanticationTypeIsAdapter = authanticationTypeIsAdapter;
    }

    public boolean isIsNotifyNewTripClaimAwaitingApproval() {
        return isNotifyNewTripClaimAwaitingApproval;
    }

    public void setIsNotifyNewTripClaimAwaitingApproval(boolean isNotifyNewTripClaimAwaitingApproval) {
        this.isNotifyNewTripClaimAwaitingApproval = isNotifyNewTripClaimAwaitingApproval;
    }

    public boolean isIsNotifyNewTripClaimAutoApproved() {
        return isNotifyNewTripClaimAutoApproved;
    }

    public void setIsNotifyNewTripClaimAutoApproved(boolean isNotifyNewTripClaimAutoApproved) {
        this.isNotifyNewTripClaimAutoApproved = isNotifyNewTripClaimAutoApproved;
    }

    public boolean isIsNotifyTripClaimApproved() {
        return isNotifyTripClaimApproved;
    }

    public void setIsNotifyTripClaimApproved(boolean isNotifyTripClaimApproved) {
        this.isNotifyTripClaimApproved = isNotifyTripClaimApproved;
    }

    public boolean isIsNotifyTripClaimDeclined() {
        return isNotifyTripClaimDeclined;
    }

    public void setIsNotifyTripClaimDeclined(boolean isNotifyTripClaimDeclined) {
        this.isNotifyTripClaimDeclined = isNotifyTripClaimDeclined;
    }

    public boolean isIsNotifyTripClaimRescinded() {
        return isNotifyTripClaimRescinded;
    }

    public void setIsNotifyTripClaimRescinded(boolean isNotifyTripClaimRescinded) {
        this.isNotifyTripClaimRescinded = isNotifyTripClaimRescinded;
    }

    public boolean isIsNotifyTripCommentAdded() {
        return isNotifyTripCommentAdded;
    }

    public void setIsNotifyTripCommentAdded(boolean isNotifyTripCommentAdded) {
        this.isNotifyTripCommentAdded = isNotifyTripCommentAdded;
    }

    public boolean isIsNotifyTripResultSubmitted() {
        return isNotifyTripResultSubmitted;
    }

    public void setIsNotifyTripResultSubmitted(boolean isNotifyTripResultSubmitted) {
        this.isNotifyTripResultSubmitted = isNotifyTripResultSubmitted;
    }

    public boolean isIsNotifyTripReceived() {
        return isNotifyTripReceived;
    }

    public void setIsNotifyTripReceived(boolean isNotifyTripReceived) {
        this.isNotifyTripReceived = isNotifyTripReceived;
    }

    public boolean isIsNotifyTripCancelled() {
        return isNotifyTripCancelled;
    }

    public void setIsNotifyTripCancelled(boolean isNotifyTripCancelled) {
        this.isNotifyTripCancelled = isNotifyTripCancelled;
    }

    public boolean isIsNotifyTripExpired() {
        return isNotifyTripExpired;
    }

    public void setIsNotifyTripExpired(boolean isNotifyTripExpired) {
        this.isNotifyTripExpired = isNotifyTripExpired;
    }

    public boolean isIsNotifyTripWeeklyReport() {
        return isNotifyTripWeeklyReport;
    }

    public void setIsNotifyTripWeeklyReport(boolean isNotifyTripWeeklyReport) {
        this.isNotifyTripWeeklyReport = isNotifyTripWeeklyReport;
    }

    public boolean isIsNotifyTripClaimCancelled() {
        return isNotifyTripClaimCancelled;
    }

    public void setIsNotifyTripClaimCancelled(boolean isNotifyTripClaimCancelled) {
        this.isNotifyTripClaimCancelled = isNotifyTripClaimCancelled;
    }

    public boolean isIsNotifyTripPriceMismatched() {
        return isNotifyTripPriceMismatched;
    }

    public void setIsNotifyTripPriceMismatched(boolean isNotifyTripPriceMismatched) {
        this.isNotifyTripPriceMismatched = isNotifyTripPriceMismatched;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", username=" + username + ", password=" + password + ", expires=" + expires
                + ", csrfToken=" + csrfToken + ", temporaryPassword=" + temporaryPassword + ", accountLocked="
                + accountLocked + ", accountDisabled=" + accountDisabled + ", accountExpired=" + accountExpired
                + ", credentialsExpired=" + credentialsExpired + ", authorities=" + authorities
                + ", authanticationTypeIsAdapter=" + authanticationTypeIsAdapter + ", responseDataForUI="
                + responseDataForUI + ", provider=" + provider + ", jobTitle=" + jobTitle + ", email=" + email
                + ", name=" + name + ", phoneNumber=" + phoneNumber + ", loginConfirmationSentDate="
                + loginConfirmationSentDate + ", loginConfirmationDate=" + loginConfirmationDate + ", unconfirmedEmail="
                + unconfirmedEmail + ", resetPasswordToken=" + resetPasswordToken + ", isPasswordExpired="
                + isPasswordExpired + ", resetPasswordRequestDate=" + resetPasswordRequestDate + ", resetPasswordDate="
                + resetPasswordDate + ", loginCount=" + loginCount + ", failedAttempts=" + failedAttempts
                + ", lastFailedAttemptDate=" + lastFailedAttemptDate + ", currentLoginDate=" + currentLoginDate
                + ", lastLoginDate=" + lastLoginDate + ", currentLoginIp=" + currentLoginIp + ", lastLoginIp="
                + lastLoginIp + ", isNotifyPartnerCreatesTicket=" + isNotifyPartnerCreatesTicket
                + ", isNotifyPartnerUpdateTicket=" + isNotifyPartnerUpdateTicket + ", isNotifyClaimedTicketRescinded="
                + isNotifyClaimedTicketRescinded + ", isNotifyClaimedTicketExpired=" + isNotifyClaimedTicketExpired
                + ", isNotifyNewTripClaimAwaitingApproval=" + isNotifyNewTripClaimAwaitingApproval
                + ", isNotifyNewTripClaimAutoApproved=" + isNotifyNewTripClaimAutoApproved
                + ", isNotifyTripClaimApproved=" + isNotifyTripClaimApproved + ", isNotifyTripClaimDeclined="
                + isNotifyTripClaimDeclined + ", isNotifyTripClaimRescinded=" + isNotifyTripClaimRescinded
                + ", isNotifyTripCommentAdded=" + isNotifyTripCommentAdded + ", isNotifyTripResultSubmitted="
                + isNotifyTripResultSubmitted + ", isNotifyTripReceived=" + isNotifyTripReceived
                + ", isNotifyTripCancelled=" + isNotifyTripCancelled + ", isNotifyTripExpired=" + isNotifyTripExpired
                + ", isNotifyTripWeeklyReport=" + isNotifyTripWeeklyReport + ", isNotifyTripClaimCancelled="
                + isNotifyTripClaimCancelled + ", isNotifyTripPriceMismatched=" + isNotifyTripPriceMismatched
                + ", AccountLockedDate=" + AccountLockedDate + ", oldPassword1=" + oldPassword1 + ", oldPassword2="
                + oldPassword2 + ", oldPassword3=" + oldPassword3 + ", oldPassword4=" + oldPassword4 + ", isActive="
                + isActive + "]";
    }

}
