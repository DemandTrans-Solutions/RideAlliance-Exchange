/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;


import com.clearinghouse.exceptionentity.EmailExistCheckingEntity;
import com.clearinghouse.exceptionentity.UsernameExistCheckingEntity;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

/**
 * @author chaitanyaP
 */
@Repository
public class UsernameExistCheckingDAO extends AbstractDAO<Serializable, Object> {


    public boolean findUserByUsername(String username) {
        if (username == null) {
            return false;
        }

        TypedQuery<UsernameExistCheckingEntity> query = getEntityManager().createQuery("SELECT NEW com.clearinghouse.exceptionentity.UsernameExistCheckingEntity(u.username) FROM User AS u", UsernameExistCheckingEntity.class);
        List<UsernameExistCheckingEntity> listOfusernames = query.getResultList();

        boolean result = false;

        for (UsernameExistCheckingEntity usernameExistCheckingEntity : listOfusernames) {
            if (usernameExistCheckingEntity == null || usernameExistCheckingEntity.getUsername() == null) {
                continue;
            }

            result = usernameExistCheckingEntity.getUsername().equalsIgnoreCase(username);

            if (result) {
                break;
            }
        }

        return result;
    }


    public boolean findUserByEmail(String email) {
        if (email == null) {
            return false;
        }

        TypedQuery<EmailExistCheckingEntity> query = getEntityManager().createQuery("SELECT NEW com.clearinghouse.exceptionentity.EmailExistCheckingEntity(u.email) FROM User AS u", EmailExistCheckingEntity.class);
        List<EmailExistCheckingEntity> listOfusernames = query.getResultList();

        boolean result = false;

        for (EmailExistCheckingEntity usernameExistCheckingEntity : listOfusernames) {
            if (usernameExistCheckingEntity == null || usernameExistCheckingEntity.getEmail() == null) {
                continue;
            }

            result = usernameExistCheckingEntity.getEmail().equalsIgnoreCase(email);

            if (result) {
                break;
            }
        }

        return result;

    }

}
