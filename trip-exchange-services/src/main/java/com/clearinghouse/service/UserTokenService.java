/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;

import com.clearinghouse.dao.UserTokenDAO;
import com.clearinghouse.entity.User;
import com.clearinghouse.entity.UserToken;
import com.clearinghouse.security.HmacTokenHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author chaitanyaP
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class UserTokenService {


    private final UserTokenDAO userTokenDAO;

    private final TokenHandler tokenHandler;

    private final HmacTokenHandler hmacTokenHandler;



    public UserToken findUserToken(User user) {
        var userToken = userTokenDAO.findUserTokenByUserId(user.getId());

        boolean modified = false;
        if ( userToken == null ) {
            userToken = new UserToken();
            userToken.setUser(user);
            modified = true;
        }

        if ( userToken.getHmacToken() == null ) {
            modified = true;
            userToken.setHmacToken(hmacTokenHandler.createToken(user));
        }

        if ( userToken.getUserToken() == null ) {
            modified = true;
            userToken.setUserToken(tokenHandler.createTokenForUser(user));
        }

        if ( modified ) {
            userTokenDAO.createUserToken(userToken);
        }
        return userToken;
    }


    public UserToken updateUserToken(UserToken userToken) {
        return userTokenDAO.updateUserToken(userToken);
    }

    public void createUserToken(UserToken userToken) {
        userTokenDAO.createUserToken(userToken);
    }


    public UserToken findUserTokenBiUserId(int userId) {
        return userTokenDAO.findUserTokenByUserId(userId);
    }

}
