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
@Table(name = "status")
public class Status extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StatusID")
    private int statusId;

    @Column(name = "Type")
    private String type;

    @Column(name = "Description")
    private String description;

    // Default constructor
    public Status() {
    }

    // Constructor with ID parameter
    public Status(int statusId) {
        this.statusId = statusId;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Status{" + "statusId=" + statusId + ", type=" + type + ", description=" + description + '}';
    }

}
