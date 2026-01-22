package com.clearinghouse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;


/**
 * @author Shankar I
 */

@Entity
@Table(name = "adapterlog")
public class AdapterLog extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AdapterLogId")
    private int logId;

    @OneToOne
    @JoinColumn(name = "ProviderId")
    private Provider provider;

    @NotNull
    @Column(name = "LogType")
    private String logType;

    @NotNull
    @Column(name = "Message")
    private String logMessage;

    @Column(name = "LogsDateTime")
    private String logDateTime;

    // Default constructor
    public AdapterLog() {
    }

    // Constructor with ID parameter
    public AdapterLog(int logId) {
        this.logId = logId;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public String getLogDateTime() {
        return logDateTime;
    }

    public void setLogDateTime(String logDateTime) {
        this.logDateTime = logDateTime;
    }

    @Override
    public String toString() {
        return "AdapterLog [logId=" + logId + ", provider=" + provider + ", logType=" + logType + ", logMessage="
                + logMessage + ", logDateTime=" + logDateTime + "]";
    }
}
