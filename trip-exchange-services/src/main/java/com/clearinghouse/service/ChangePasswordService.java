/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;


import com.clearinghouse.dao.ChangePasswordDAO;
import com.clearinghouse.dao.UserDAO;
import com.clearinghouse.dto.ChangePasswordDTO;
import com.clearinghouse.entity.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;

/**
 *
 * @author chaitanyaP
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class ChangePasswordService {


    private final ChangePasswordDAO changePasswordDAO;


    private final UserDAO userDAO;


    public Integer resetPassword(ChangePasswordDTO changePasswordDTO) {

        User currentUserDatabaseObj = userDAO.findUserByUsername(changePasswordDTO.getUsername());

        int statusCode = 0;

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean passwordCheckWithAllThereeOldPasswords;
        //checking the new password with current password in database..
        boolean currentpaswwordmatch = passwordEncoder.matches(changePasswordDTO.getPassword(), currentUserDatabaseObj.getPassword());
        if (currentpaswwordmatch) {
            statusCode = 1;
        }

        //checking for the previous passwords if exists...
        if (!currentpaswwordmatch) {

            if (currentUserDatabaseObj.getOldPassword1() != null) {
                if (passwordEncoder.matches(changePasswordDTO.getPassword(), currentUserDatabaseObj.getOldPassword1())) {
                    passwordCheckWithAllThereeOldPasswords = true;
                    statusCode = 2;
                }
            } else if (currentUserDatabaseObj.getOldPassword2() != null) {

                if (passwordEncoder.matches(changePasswordDTO.getPassword(), currentUserDatabaseObj.getOldPassword2())) {
                    passwordCheckWithAllThereeOldPasswords = true;
                    statusCode = 2;
                }
            } else if (currentUserDatabaseObj.getOldPassword3() != null) {
                if (passwordEncoder.matches(changePasswordDTO.getPassword(), currentUserDatabaseObj.getOldPassword3())) {
                    passwordCheckWithAllThereeOldPasswords = true;
                    statusCode = 2;
                }
            } else if (currentUserDatabaseObj.getOldPassword4() != null) {

                if (passwordEncoder.matches(changePasswordDTO.getPassword(), currentUserDatabaseObj.getOldPassword4())) {
                    passwordCheckWithAllThereeOldPasswords = true;
                    statusCode = 2;
                }
            }

        }

        //if the current password is not matched with new password or any old password set 
        if (statusCode == 0) {

            // Maintain a stack sytructure of change password
            String temporaryStoredCurrentPassword = currentUserDatabaseObj.getPassword();
            String temptemporayStoredOldPassword1 = currentUserDatabaseObj.getOldPassword1();
            String temptemporayStoredOldPassword2 = currentUserDatabaseObj.getOldPassword2();
            String temptemporayStoredOldPassword3 = currentUserDatabaseObj.getOldPassword3();

            String tempOldPassword1 = null;
            String tempOldPassword2 = null;
            String tempOldPassword3 = null;
            String tempOldPassword4 = null;

            if (temporaryStoredCurrentPassword != null) {
                tempOldPassword1 = temporaryStoredCurrentPassword;

            }
            if (temptemporayStoredOldPassword1 != null) {
                tempOldPassword2 = temptemporayStoredOldPassword1;
            }
            if (temptemporayStoredOldPassword2 != null) {
                tempOldPassword3 = temptemporayStoredOldPassword2;
            }
            if (temptemporayStoredOldPassword3 != null) {
                tempOldPassword4 = temptemporayStoredOldPassword3;
            }

            //setting all values to the database..
            currentUserDatabaseObj.setPassword(passwordEncoder.encode(changePasswordDTO.getPassword()));
            currentUserDatabaseObj.setOldPassword1(tempOldPassword1);
            currentUserDatabaseObj.setOldPassword2(tempOldPassword2);
            currentUserDatabaseObj.setOldPassword3(tempOldPassword3);
            currentUserDatabaseObj.setOldPassword4(tempOldPassword4);
            //setting credntials expired flag to false..
            currentUserDatabaseObj.setIsActive(true);
            currentUserDatabaseObj.setCredentialsExpired(false);
            currentUserDatabaseObj.setTemporaryPassword("password is changed");

            /*set current date as password reset date*/
            LocalDate localDate = LocalDate.now();
            Date currentDate = java.sql.Date.valueOf(localDate);

            currentUserDatabaseObj.setResetPasswordDate(currentDate);
            currentUserDatabaseObj.setIsPasswordExpired(false);

            boolean checkStatus = changePasswordDAO.resetPassword(currentUserDatabaseObj);
            if (checkStatus) {
                statusCode = 3;
            }
        }
// statusCode=0 if current password and new password  not matched matched 
//        statusCode=1 if current password and new password matched
//        statusCode =2 if new password matched with any OLd password
//        StatusCode= 3 if password changed successfully..
        return statusCode;

    }
}
