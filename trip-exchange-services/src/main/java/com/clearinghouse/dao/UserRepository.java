/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;

import com.clearinghouse.entity.User;
import org.springframework.stereotype.Repository;

/**
 * @author zcon
 */
@Repository
public class UserRepository extends AbstractDAO<Long, User> {


    public User findUserByUserName(String username) {
        User user = (User) getEntityManager()
                .createQuery("SELECT u  FROM User u WHERE u.username = :username")
                .setParameter("username", username)
                .getSingleResult();
        return user;

    }


}
