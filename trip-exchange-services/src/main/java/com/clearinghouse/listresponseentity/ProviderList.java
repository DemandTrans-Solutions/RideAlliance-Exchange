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
//this class is cretaetd  for the projection list..
@Getter
@Setter
@NoArgsConstructor
public class ProviderList {

    public int providerId;
    public String providerName;

    public ProviderList(int providerId, String providerName) {
        this.providerId = providerId;
        this.providerName = providerName;
    }


}
