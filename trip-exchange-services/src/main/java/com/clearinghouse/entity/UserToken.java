/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.entity;

import jakarta.persistence.*;

/**
 *
 * @author ChaitanyaP
 */
@Entity
@Table(name = "usertoken")
public class UserToken extends AbstractEntity {

    @Id
    @Column(name = "UserTokenID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userTokenId;

    @OneToOne
    @JoinColumn(name = "UserID")
    private User user;


    @Column(name = "UserToken")
    private String userToken;

    @Column(name = "HMACToken")
    private String hmacToken;


    public String getHmacToken() {
        return hmacToken;
    }

    public void setHmacToken(String hmacToken) {
        this.hmacToken = hmacToken;
    }

    public int getUserTokenId() {
        return userTokenId;
    }

    public void setUserTokenId(int userTokenId) {
        this.userTokenId = userTokenId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    // Default constructor
    public UserToken() {
    }

    // Constructor with ID parameter
    public UserToken(int userTokenId) {
        this.userTokenId = userTokenId;
    }

    @Override
    public String toString() {
        return "UserToken{" + "userTokenId=" + userTokenId + ", user=" + user + ", userToken=" + userToken + " hmacToken=" + hmacToken + '}';
    }

}
