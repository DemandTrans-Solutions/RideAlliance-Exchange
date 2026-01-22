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
public class ProviderPartnerList {

    public int providerId;
    public String providerName;

    public ProviderPartnerList(int providerId, String providerName) {
        this.providerId = providerId;
        this.providerName = providerName;
    }


}
