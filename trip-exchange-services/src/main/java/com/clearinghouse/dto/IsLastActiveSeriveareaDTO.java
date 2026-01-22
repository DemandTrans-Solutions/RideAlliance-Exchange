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
public class IsLastActiveSeriveareaDTO {

    int serviceId;
    int providerId;


    @Override
    public String toString() {
        return "isLastActiveSeriveareaDTO{" + "serviceId=" + serviceId + ", providerId=" + providerId + '}';
    }

}
