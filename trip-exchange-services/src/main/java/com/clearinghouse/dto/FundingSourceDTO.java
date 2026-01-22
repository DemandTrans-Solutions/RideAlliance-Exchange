package com.clearinghouse.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Shankar I
 */
@Getter
@Setter
@NoArgsConstructor
public class FundingSourceDTO {

    private int fundingSourceId;
    private String name;
    private String description;
    private boolean status;


    @Override
    public String toString() {
        return "FundingSourceDTO [fundingSourceId=" + fundingSourceId + ", name=" + name + ", description="
                + description + ", status=" + status + "]";
    }

}
