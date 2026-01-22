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
public class TicketFilterList {

    int filterId;
    String filterName;

    public TicketFilterList(int filterId, String filterName) {
        this.filterId = filterId;
        this.filterName = filterName;
    }


}
