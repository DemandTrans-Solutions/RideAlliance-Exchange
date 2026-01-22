/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;

import com.clearinghouse.entity.User;
import org.springframework.stereotype.Repository;


/**
 * @author chaitanyaP
 */
@Repository
public class ChangePasswordDAO extends AbstractDAO<Integer, User> {


    public boolean resetPassword(User user) {
        if (user == null) {
            return false;
        }

        boolean result = false;
        User userCheck = update(user);
        if (userCheck != null) {
            result = true;
        }

        return result;
    }


}
