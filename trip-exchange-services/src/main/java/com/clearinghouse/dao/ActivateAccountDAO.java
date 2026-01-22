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
public class ActivateAccountDAO extends AbstractDAO<Integer, User> {


    public boolean activateAccount(User user) {

        User userCheck = update(user);
        boolean status = userCheck != null;

        return status;
    }

}
