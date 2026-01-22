/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;

import com.clearinghouse.dao.ApplicationSettingDAO;
import com.clearinghouse.dao.UserDAO;
import com.clearinghouse.entity.ApplicationSetting;
import com.clearinghouse.entity.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 *
 * @author chaitanyaP
 */
@Service
@Slf4j
@AllArgsConstructor
public class PasswordAutoExpirationSchedularService {


    private final UserDAO userDAO;


    private final ApplicationSettingDAO applicationSettingDAO;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<User> getAllUsers() {
        return userDAO.findAllUsers();
    }


    @Transactional(propagation = Propagation.REQUIRED)
    @Scheduled(initialDelay = 200000, fixedRate = 12000000)//this is for 199.99 min or 3.3 hr 
    public void passwordExpirationAfterDays() {

        List<ApplicationSetting> applicationSettings = applicationSettingDAO.findAllApplicationSettings();

        int passwordExpirationAfterDays = applicationSettings.get(0).getPasswrodExpiredAfterDays();

        if (passwordExpirationAfterDays != 0) {

            LocalDate localDate = LocalDate.now();
            Date currentDate = java.sql.Date.valueOf(localDate);

            List<User> usersList = getAllUsers();
            for (User user : usersList) {

                /*if is password expired flag is false then only proceed*/
                if ((!user.isIsPasswordExpired()) && (user.getResetPasswordDate() != null)) {

                    /*getting resetPasswordDate  and add days in it and comare with curentdate of user*/
                    Date restepasswordDate = user.getResetPasswordDate();
                    LocalDate oldResetPasswordLocalDate = LocalDate.parse(restepasswordDate.toString());
                    oldResetPasswordLocalDate = oldResetPasswordLocalDate.plusDays(passwordExpirationAfterDays);
                    Date newPasswordResetDate = java.sql.Date.valueOf(oldResetPasswordLocalDate);
                    int result = newPasswordResetDate.compareTo(currentDate);

                    if (result < 0) {
                        user.setIsPasswordExpired(true);
                        userDAO.updateUser(user);

                    }
                }

            }

        }

    }

}
