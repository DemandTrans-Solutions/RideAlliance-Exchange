/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 *
 * @author manisha
 */

@MappedSuperclass
@Configurable
public abstract class AbstractEntity {

    @Column(name = "AddedBy", insertable = true, updatable = false)
    @JsonIgnore
    private Integer createdBy; // was created_by

    @Column(name = "AddedOn", insertable = true, updatable = false)
    @JsonIgnore
    private ZonedDateTime createdAt; // was created_at

    @Column(name = "UpdatedBy", insertable = true, updatable = true)
    @JsonIgnore
    private Integer updatedBy; // was updated_by

    @Column(name = "UpdatedOn", insertable = true, updatable = true)
    @JsonIgnore
    private ZonedDateTime updatedAt; // was updated_at

    @Transient
    private static String timezoneBean;


    // Add this static setter to AbstractEntity
    public static void setTimezone(String timezone) {
        AbstractEntity.timezoneBean = timezone;
    }


    //    @Column(name = "IsActive")
//    private boolean isActive;
    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(int updatedBy) {
        this.updatedBy = updatedBy;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    //    public boolean isActive() {
//        return isActive;
//    }
//
//    public void setIsActive(boolean isActive) {
//        this.isActive = isActive;
//    }
    @PrePersist
    public void onPrePersist() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String principalName = "";
        if (auth != null) {
            principalName = (String) auth.getPrincipal();
        }
        createdAt = ZonedDateTime.now(ZoneId.of(timezoneBean));
//        createdDateTime = new DateTime();
//        for cronjob it will give error for context
        if (auth != null && principalName != null && (!principalName.equalsIgnoreCase("anonymousUser"))) {
            createdBy = ((User) auth.getDetails()).getId();
        } else {
            createdBy = 1;
        }
        updatedAt = ZonedDateTime.now(ZoneId.of(timezoneBean));
        if (auth != null && principalName != null && (!principalName.equalsIgnoreCase("anonymousUser"))) {
            updatedBy = ((User) auth.getDetails()).getId();
        } else {
            updatedBy = 1;

        }
    }

    @PreUpdate
    public void onPreUpdate() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String principalName = "";
        if (auth != null) {
            principalName = (String) auth.getPrincipal();
        }
        updatedAt = ZonedDateTime.now(ZoneId.of(timezoneBean));
//        for cronjob it will give error for context
        if (auth != null && principalName != null && !principalName.equalsIgnoreCase("anonymousUser")) {
            updatedBy = ((User) auth.getDetails()).getId();
        } else {

            updatedBy = 1;
        }
    }

}