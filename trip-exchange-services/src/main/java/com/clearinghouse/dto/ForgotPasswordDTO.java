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
public class ForgotPasswordDTO {

    String email;
    String username;
    String tempPassword;
    String oldPassword;
    String newPassword;
//   String password;


    @Override
    public String toString() {
        return "ForgotPasswordDTO{" + "email=" + email + ", username=" + username + ", tempPassword=" + tempPassword + ", oldPassword=" + oldPassword + ", newPassword=" + newPassword + '}';
    }

}
