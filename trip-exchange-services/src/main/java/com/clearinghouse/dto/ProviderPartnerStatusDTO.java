/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderPartnerStatusDTO {

    private int providerPartnerStatusId;
    private String status;
    private String description;

    @Override
    public String toString() {
        return "ProviderPartnerStatusDTO{" + "providerPartnerStatusId=" + providerPartnerStatusId + ", status=" + status + ", description=" + description + '}';
    }

}
