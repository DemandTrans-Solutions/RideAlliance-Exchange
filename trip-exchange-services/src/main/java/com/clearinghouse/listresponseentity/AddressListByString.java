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
public class AddressListByString {

    public int addressId;
    public String address;

    @Override
    public String toString() {
        return "AddressListByString{" + "addressId=" + addressId + ", address=" + address + '}';
    }

}
