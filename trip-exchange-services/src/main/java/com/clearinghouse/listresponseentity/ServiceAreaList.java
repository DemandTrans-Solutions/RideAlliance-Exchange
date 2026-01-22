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
public class ServiceAreaList {

    public long serviceAreaId;
    public String description;

    public ServiceAreaList(long serviceAreaId, String description) {
        this.serviceAreaId = serviceAreaId;
        this.description = description;
    }


}
