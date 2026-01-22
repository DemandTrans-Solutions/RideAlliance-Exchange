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
public class ChangePasswordDTO {

    private String username;
    private String password;

    @Override
    public String toString() {
        return "ChangePasswordDTO{" + "username=" + username + ", password=" + password + '}';
    }


}
