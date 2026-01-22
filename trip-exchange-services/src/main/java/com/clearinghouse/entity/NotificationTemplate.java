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
@Table(name = "notificationtemplate")
public class NotificationTemplate extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationTemplateID")
    private int notificationTemplateId;

    // Default constructor
    public NotificationTemplate() {
    }

    // Constructor with ID parameter
    public NotificationTemplate(int notificationTemplateId) {
        this.notificationTemplateId = notificationTemplateId;
    }

    @Column(name = "TemplateName")
    private String templateName;

    @Column(name = "TemplatePath")
    private String templatePath;

    @Column(name = "TemplateCode")
    private int templateCode;

    @Column(name = "ParameterList")
    private String parameterList;

    public int getNotificationTemplateId() {
        return notificationTemplateId;
    }

    public void setNotificationTemplateId(int notificationTemplateId) {
        this.notificationTemplateId = notificationTemplateId;
    }


    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public int getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(int templateCode) {
        this.templateCode = templateCode;
    }

    public String getParameterList() {
        return parameterList;
    }

    public void setParameterList(String parameterList) {
        this.parameterList = parameterList;
    }

    @Override
    public String toString() {
        return "NotificationTemplate{" + "notificationTemplateId=" + notificationTemplateId + ", templateName=" + templateName + ", templatePath=" + templatePath + ", templateCode=" + templateCode + ", parameterList=" + parameterList + '}';
    }


}
