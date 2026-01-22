/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.listresponseentity;

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
public class StatusList {
    private int statusId;
    private String type;

    public StatusList(int statusId, String type) {
        this.statusId = statusId;
        this.type = type;
    }


}
