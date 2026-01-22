package com.clearinghouse.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.sql.Time;

/**
 *
 * @author shankarI
 */

@Entity
@Table(name = "workinghours")
public class WorkingHours extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WorkingHourId")
    private int workingHoursId;

    @OneToOne//(cascade = { CascadeType.ALL })
    @JoinColumn(name = "ProviderId")
    private Provider provider;

    @Column(name = "DayOfWeek")
    private String day;

    @Column(name = "StartTime")
    private Time startTime;

    @Column(name = "EndTime")
    private Time endTime;

    @Column(name = "IsHoliday")
    private Boolean isHoliday;

    @Column(name = "IsActive")

    private Boolean isActive;

    public int getWorkingHoursId() {
        return workingHoursId;
    }

    public void setWorkingHoursId(int workingHoursId) {
        this.workingHoursId = workingHoursId;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }


    public boolean getIsHoliday() {
        return isHoliday;
    }

    public void setIsHoliday(boolean isHoliday) {
        this.isHoliday = isHoliday;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "WorkingHours [workingHoursId=" + workingHoursId + ", provider=" + provider + ", day=" + day
                + ", startTime=" + startTime + ", endTime=" + endTime + ", isHoliday=" + isHoliday + ", isActive="
                + isActive + "]";
    }


}
