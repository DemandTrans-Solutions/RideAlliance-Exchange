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
public class ForgotPasswordDAO extends AbstractDAO<Integer, User> {


    public boolean storeTempPassword(User user) {

        boolean result = false;
        User userCheck = update(user);
        if (userCheck != null) {
            result = true;
        }

        return result;

    }


    public boolean resetForgotPassword(User user) {
        boolean result = false;
        User userCheck = update(user);
        if (userCheck != null) {
            result = true;
        }

        return result;

    }

}
