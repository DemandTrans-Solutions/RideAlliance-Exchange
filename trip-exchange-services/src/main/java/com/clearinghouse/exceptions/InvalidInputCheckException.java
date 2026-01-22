/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.exceptions;

import java.util.List;

/**
 *
 * @author chaitanyaP
 */
public class InvalidInputCheckException extends RuntimeException {

    List<String> messageList;
    int providerId;

    public InvalidInputCheckException(List<String> messageList, int providerId) {
        this.messageList = messageList;
        this.providerId = providerId;
    }

    public List<String> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<String> messageList) {
        this.messageList = messageList;
    }

    public int getProviderId() {
        return providerId;
    }

    public void setProviderId(int providerId) {
        this.providerId = providerId;
    }

}
