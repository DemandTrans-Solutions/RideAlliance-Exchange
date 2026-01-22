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
public class PaginationDTO {

    private long totalCount = 0;
    private int currentPageNumber = 0;
    private int pageSize = 10;
    private String sortField;
    private int sortOrder;


}
