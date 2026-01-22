package com.clearinghouse.dto;

/**
 * @author Shankar I
 */
public class AdapterLogDTO {

    private int logId;
    private int providerId;
    private String logType;
    private String logMessage;
    private String logDateTime;

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getProviderId() {
        return providerId;
    }

    public void setProviderId(int providerId) {
        this.providerId = providerId;
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
        return "AdapterLogDTO [logId=" + logId + ", providerId=" + providerId + ", logType=" + logType + ", logMessage="
                + logMessage + ", logDateTime=" + logDateTime + "]";
    }

}
