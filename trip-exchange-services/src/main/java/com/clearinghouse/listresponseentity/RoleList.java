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
public class RoleList {

    public int roleId;
    public String roleName;

    public RoleList(int roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

}
