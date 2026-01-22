/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author chaitanyaP
 */

@Getter
@Setter
@NoArgsConstructor
public class StatusDTO {

    private int statusId;
    private String type;
    private String description;

    @Override
    public String toString() {
        return "StatusDTO{" + "statusId=" + statusId + ", type=" + type + ", description=" + description + '}';
    }

}
