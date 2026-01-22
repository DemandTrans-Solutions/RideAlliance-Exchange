package com.clearinghouse.dao;

import com.clearinghouse.entity.UserToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @author chaitanyaP
 */
@Repository
@Slf4j
public class UserTokenDAO extends AbstractDAO<Integer, UserToken> {


    public UserToken createUserToken(UserToken userToken) {

        add(userToken);
        return userToken;
    }

    public UserToken updateUserToken(UserToken userToken) {
        update(userToken);
        return userToken;
    }


    public UserToken findUserTokenByUserId(int userId) {
        try {
            UserToken userToken = (UserToken) getEntityManager()
                    .createQuery("SELECT ut FROM UserToken ut WHERE ut.user.id = :userId")
                    .setParameter("userId", userId)
                    .setMaxResults(1)
                    .getSingleResult();
            return userToken;
        } catch (Exception e) {
            log.error("Error finding UserToken for userId {}", userId, e);
            return null;
        }
    }

}
