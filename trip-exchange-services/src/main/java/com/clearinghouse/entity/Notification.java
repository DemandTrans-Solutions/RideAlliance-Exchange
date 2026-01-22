/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author chaitanyaP
 */
@Entity
@Table(name = "notification")
public class Notification extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationID")
    private int notificationId;

    // Default constructor
    public Notification() {
    }

    // Constructor with ID parameter
    public Notification(int notificationId) {
        this.notificationId = notificationId;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "NotificationTemplateID")
    private NotificationTemplate notificationTemplate;

    //here not going to add the phone no..which was in db  structure..
    @Column(name = "IsEMail")

    private Boolean isEmail;

    @Column(name = "IsSMS")

    private Boolean isSMS;

    @Column(name = "EmailTo")
    private String emailTo;

    @Column(name = "EmailCC")
    private String emailCC;

    @Column(name = "EmailBCC")
    private String emailBCC;

    @Column(name = "Subject")
    private String subject;

    @Column(name = "RedirectURL")
    private String redirectURL;

    @Column(name = "NumberOfAttempts")
    private int numberOfAttempts;

    @Column(name = "ParameterValues")
    private String parameterValues;

    @Column(name = "StatusID")
    private int statusId;

    @Column(name = "IsActive")
    private Boolean isActive;

    //new added
    @Column(name = "  IsEmailAttachment", columnDefinition = "bit(1) default false")
    private boolean isEmailAttachment;

    @Column(name = "FilePath")
    @Convert(converter = StringListConverter.class)
    private List<String> filePathList;

    @Column(name = "FileName")
    @Convert(converter = StringListConverter.class)
    private List<String> fileNameList;

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public NotificationTemplate getNotificationTemplate() {
        return notificationTemplate;
    }

    public void setNotificationTemplate(NotificationTemplate notificationTemplate) {
        this.notificationTemplate = notificationTemplate;
    }

    public boolean isIsEmail() {
        return isEmail;
    }

    public void setIsEMail(boolean isEmail) {
        this.isEmail = isEmail;
    }

    public boolean isIsSMS() {
        return isSMS;
    }

    public void setIsSMS(boolean isSMS) {
        this.isSMS = isSMS;
    }

    public String getEmailTo() {
        return emailTo;
    }

    public void setEmailTo(String emailTo) {
        this.emailTo = emailTo;
    }

    public String getEmailCC() {
        return emailCC;
    }

    public void setEmailCC(String emailCC) {
        this.emailCC = emailCC;
    }

    public String getEmailBCC() {
        return emailBCC;
    }

    public void setEmailBCC(String emailBCC) {
        this.emailBCC = emailBCC;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public int getNumberOfAttempts() {
        return numberOfAttempts;
    }

    public void setNumberOfAttempts(int numberOfAttempts) {
        this.numberOfAttempts = numberOfAttempts;
    }

    public String getParameterValues() {
        return parameterValues;
    }

    public void setParameterValues(String parameterValues) {
        this.parameterValues = parameterValues;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isEmailAttachment() {
        return isEmailAttachment;
    }

    public void setEmailAttachment(boolean isEmailAttachment) {
        this.isEmailAttachment = isEmailAttachment;
    }


    public List<String> getFilePathList() {
        return filePathList;
    }

    public void setFilePathList(List<String> filePathList) {
        this.filePathList = filePathList;
    }

    public List<String> getFileNameList() {
        return fileNameList;
    }

    public void setFileNameList(List<String> fileNameList) {
        this.fileNameList = fileNameList;
    }

    @Override
    public String toString() {
        return "Notification [notificationId=" + notificationId + ", notificationTemplate=" + notificationTemplate
                + ", isEMail=" + isEmail + ", isSMS=" + isSMS + ", emailTo=" + emailTo + ", emailCC=" + emailCC
                + ", emailBCC=" + emailBCC + ", subject=" + subject + ", redirectURL=" + redirectURL
                + ", numberOfAttempts=" + numberOfAttempts + ", parameterValues=" + parameterValues + ", statusId="
                + statusId + ", isActive=" + isActive + ", isEmailAttachment=" + isEmailAttachment + ", filePathList="
                + filePathList + ", fileNameList=" + fileNameList + "]";
    }


}
