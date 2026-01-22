package com.clearinghouse.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "customerstatus")
public class CustomerStatus implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MessageId")
    private int messageId;

    @Column(name = "Message")
    private String message;

    // Default constructor
    public CustomerStatus() {
    }

    // Constructor with ID parameter
    public CustomerStatus(int messageId) {
        this.messageId = messageId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Message [MessageId=" + messageId + ", Message=" + message + "]";
    }

}