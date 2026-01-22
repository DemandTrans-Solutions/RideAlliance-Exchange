package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author shankarI
 */

@Getter
@Setter
@NoArgsConstructor
public class WorkingHoursDTO {

    private int workingHoursId;
    private int providerId;
    private String day;
    private String startTime;
    private String endTime;

    @JsonProperty("isHoliday")
    private boolean isHoliday;

    @JsonProperty("isActive")
    private boolean isActive;


    @Override
    public String toString() {
        return "WorkingHoursDTO [workingHoursId=" + workingHoursId + ", providerId=" + providerId + ", day=" + day
                + ", startTime=" + startTime + ", endTime=" + endTime + ", isHoliday=" + isHoliday + ", isActive="
                + isActive + "]";
    }

}
