/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;


import com.clearinghouse.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chaitanyaP
 */
@Repository
public class UserDAO extends AbstractDAO<Integer, User> {

    @PersistenceContext
    EntityManager entityManagerNew;


    public List<User> findAllUsers() {

        List<User> users = getEntityManager()
                .createQuery("SELECT s FROM User s WHERE s.provider.providerId!=1 order by name ")
                .getResultList();
        return users;
    }


    public User findUserByUserId(int userId) {

        return getByKey(userId);

    }


    public User createUser(User user) {

        // Only default username to email when username wasn't provided by caller.
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            user.setUsername(user.getEmail());
        }
        add(user);
        return user;
    }


    public User updateUser(User user) {
//     String username=user.getEmail();
//        user.setUsername(username);
        return update(user);
    }


    public void deleteUserByUserId(int userId) {

        User user = (User) getEntityManager()
                .createQuery("SELECT s FROM User s WHERE s.id = :userId")
                .setParameter("userId", userId)
                .getSingleResult();
        user.setIsActive(false);

    }


    public User findUserByUsername(String username) {

        try {
            User user = (User) getEntityManager()
                    .createQuery("SELECT s FROM User s WHERE s.username = :username")
                    .setParameter("username", username)
                    .getSingleResult();
            return user;
        } catch (NoResultException e) {
            return null;
        }
    }


    public int updateUserRole(int userId, String oldUserRoleString, String newUserRoleString) {
//        UserAuthority userTempAuthority = (UserAuthority) getEntityManager()
        int result = entityManagerNew.createQuery("UPDATE UserAuthority ua SET authority =:newUserRoleString  WHERE ua.user.id =:userId AND ua.authority=:oldUserRoleString ")
                .setParameter("oldUserRoleString", oldUserRoleString)
                .setParameter("newUserRoleString", newUserRoleString)
                .setParameter("userId", userId)
                .executeUpdate();
        return result;

    }


    public int getUserIdbyProviderId(int providerId) {

        int userId = 1;
        List<User> listOfUsers = getEntityManager()
                .createQuery("SELECT s FROM User s WHERE s.provider.providerId=:providerId")
                .setParameter("providerId", providerId)
                .getResultList();

        for (User user : listOfUsers) {

            if ((user.getAuthorities().iterator().next().getAuthority().equalsIgnoreCase("ROLE_PROVIDERADMIN") || user.getAuthorities().iterator().next().getAuthority().equalsIgnoreCase("ROLE_PROVIDERUSER")) && user.isActive()) {
                userId = user.getId();
                break;
            }
        }

        return userId;

    }


    public int findProviderIdByUsername(String username) {
        try {
            int providerId = (int) entityManager.createQuery("select u.provider.providerId from User u where u.username=:username")
                    .setParameter("username", username)
                    .getSingleResult();
            return providerId;
        } catch (NoResultException e) {
            //  e.printStackTrace();
            return 0;
        }

    }

}
