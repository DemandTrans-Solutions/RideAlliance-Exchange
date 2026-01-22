/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;

import com.clearinghouse.configuration.PasswordRuleBean;
import com.clearinghouse.dao.*;
import com.clearinghouse.dto.ForgotPasswordDTO;
import com.clearinghouse.entity.Notification;
import com.clearinghouse.entity.NotificationTemplate;
import com.clearinghouse.entity.User;
import com.clearinghouse.enumentity.NotificationStatus;
import com.clearinghouse.enumentity.NotificationTemplateCodeValue;
import com.clearinghouse.exceptions.UsernameNotExistException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author chaitanyaP
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class ForgotPasswordService {


    private final ForgotPasswordDAO forgotPasswordDAO;


    private final UserDAO userDAO;


    private final NotificationDAO notificationDAO;


    private final UsernameExistCheckingDAO usernameExistCheckingDAO;


    private final String hostedSiteUrl;

    private final Environment env;

    private final PasswordRuleBean passwordRuleBean;

    public boolean sendTempPassword(ForgotPasswordDTO forgotPasswordDTOObj) {

        boolean resultOfTheEmailSending = false;
        boolean isEmailpresent = usernameExistCheckingDAO.findUserByEmail(forgotPasswordDTOObj.getEmail());

        if (!isEmailpresent) {
            throw new UsernameNotExistException(forgotPasswordDTOObj.getEmail(), "Entered emailid is not exists!!");
        } else {

            User userObjFromDatabase = userDAO.findUserByUsername(forgotPasswordDTOObj.getEmail());
//if account axpired flag is set to one because of maximun failed attempts then set here ti true and no of attempts to 0
            if (userObjFromDatabase.isAccountExpired()) {
                userObjFromDatabase.setAccountExpired(false);
                userObjFromDatabase.setFailedAttempts(0);

            }

            // Maintain a stack sytructure of change password
            String temporaryStoredCurrentPassword = userObjFromDatabase.getPassword();
            String temptemporayStoredOldPassword1 = userObjFromDatabase.getOldPassword1();
            String temptemporayStoredOldPassword2 = userObjFromDatabase.getOldPassword2();
            String temptemporayStoredOldPassword3 = userObjFromDatabase.getOldPassword3();

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
//                currentUserDatabaseObj.setPassword(passwordEncoder.encode(forgotPasswordDTOObj.getNewPassword()));
            userObjFromDatabase.setOldPassword1(tempOldPassword1);
            userObjFromDatabase.setOldPassword2(tempOldPassword2);
            userObjFromDatabase.setOldPassword3(tempOldPassword3);
            userObjFromDatabase.setOldPassword4(tempOldPassword4);

            //temp password generation logic..
            char[] tempPWD = RandomPasswordGenerator.generatePswd(passwordRuleBean.getMinLen(), passwordRuleBean.getMaxLen(), passwordRuleBean.getNoOfCAPSAlpha(), passwordRuleBean.getNoOfDigits(), passwordRuleBean.getNoOfSplChars());
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String tempPassword = new String(tempPWD);
            String password = passwordEncoder.encode(tempPassword);

            userObjFromDatabase.setPassword(password);
//        save the temp pass for devolopment purpose in db..
            userObjFromDatabase.setTemporaryPassword(tempPassword);
            //setting credntilas expired flag true..
            userObjFromDatabase.setCredentialsExpired(false);

            //NotificationEnginePart.....
            Notification emailNotification = new Notification();
            NotificationTemplate notificationTemplate = new NotificationTemplate();
            emailNotification.setEmailTo(userObjFromDatabase.getEmail());
            emailNotification.setIsEMail(true);
            emailNotification.setStatusId(NotificationStatus.newStatus.status());
            notificationTemplate.setNotificationTemplateId(NotificationTemplateCodeValue.forgotPasswordTemplateCode.templateCodeValue());
            emailNotification.setNotificationTemplate(notificationTemplate);
            emailNotification.setNumberOfAttempts(0);

//        Setting parameter values in according to the template.
            Map changePasswordTemplateMap = new HashMap<String, String>();
            changePasswordTemplateMap.put("name", userObjFromDatabase.getName());
            changePasswordTemplateMap.put("username", userObjFromDatabase.getUsername());
            changePasswordTemplateMap.put("password", tempPassword);
            //please add the emailID in  the End of follwing part..
            String extraAddedPArtForURL = "?username=" + userObjFromDatabase.getUsername();
            /*local testing link*/
//            changePasswordTemplateMap.put("verificationLink", "http://10.235.4.31:3000/changePassword" + extraAddedPArtForURL);

            /*zcon server link*/
//            changePasswordTemplateMap.put("verificationLink", "http://125.99.44.122:9079/tripexchangeClient/#/changePassword" + extraAddedPArtForURL);

            /*usserver link*/
            changePasswordTemplateMap.put("verificationLink", hostedSiteUrl + "/#/changePassword" + extraAddedPArtForURL);
            String jsonValueOfTemplate = "";

            Iterator<Map.Entry<String, String>> entries = changePasswordTemplateMap.entrySet().iterator();
            while (entries.hasNext()) {

                Map.Entry<String, String> entry = entries.next();
                jsonValueOfTemplate = jsonValueOfTemplate + "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
                if (entries.hasNext()) {
                    jsonValueOfTemplate = jsonValueOfTemplate + ",";
                }

            }

            String FinaljsonValueOfTemplate = "{" + jsonValueOfTemplate + "}";

            emailNotification.setParameterValues(FinaljsonValueOfTemplate);
            emailNotification.setIsActive(true);

            emailNotification.setSubject("Forgot password details");

            notificationDAO.createNotification(emailNotification);

            boolean resultCheck = forgotPasswordDAO.storeTempPassword(userObjFromDatabase);
            if (resultCheck) {

                resultOfTheEmailSending = true;
            }
        }

        return resultOfTheEmailSending;
    }

    public Integer resetForgotPassword(ForgotPasswordDTO forgotPasswordDTOObj) {

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        int statusCode = 0;
        User currentUserDatabaseObj = userDAO.findUserByUsername(forgotPasswordDTOObj.getUsername());
//getting tempPassword here
        boolean checkForTemporaryPasswordMatch = passwordEncoder.matches(forgotPasswordDTOObj.getTempPassword(), currentUserDatabaseObj.getPassword());
        boolean checkForOldPasswordMatch = passwordEncoder.matches(forgotPasswordDTOObj.getOldPassword(), currentUserDatabaseObj.getPassword());

        if (checkForOldPasswordMatch || checkForTemporaryPasswordMatch) {

            boolean passwordCheckWithAllThereeOldPasswords;
            //checking the new password with current password in database..
            boolean currentpaswwordmatch = passwordEncoder.matches(forgotPasswordDTOObj.getNewPassword(), currentUserDatabaseObj.getPassword());
            if (currentpaswwordmatch) {
                statusCode = 1;
            }

            //checking for the previous passwords if exists...
            if (!currentpaswwordmatch) {

                if (currentUserDatabaseObj.getOldPassword1() != null) {
                    if (passwordEncoder.matches(forgotPasswordDTOObj.getNewPassword(), currentUserDatabaseObj.getOldPassword1())) {
                        passwordCheckWithAllThereeOldPasswords = true;
                        statusCode = 2;
                    }
                } else if (currentUserDatabaseObj.getOldPassword2() != null) {

                    if (passwordEncoder.matches(forgotPasswordDTOObj.getNewPassword(), currentUserDatabaseObj.getOldPassword2())) {
                        passwordCheckWithAllThereeOldPasswords = true;
                        statusCode = 2;
                    }
                } else if (currentUserDatabaseObj.getOldPassword3() != null) {
                    if (passwordEncoder.matches(forgotPasswordDTOObj.getNewPassword(), currentUserDatabaseObj.getOldPassword3())) {
                        passwordCheckWithAllThereeOldPasswords = true;
                        statusCode = 2;
                    }
                } else if (currentUserDatabaseObj.getOldPassword4() != null) {

                    if (passwordEncoder.matches(forgotPasswordDTOObj.getNewPassword(), currentUserDatabaseObj.getOldPassword4())) {
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
                currentUserDatabaseObj.setPassword(passwordEncoder.encode(forgotPasswordDTOObj.getNewPassword()));
                currentUserDatabaseObj.setOldPassword1(tempOldPassword1);
                currentUserDatabaseObj.setOldPassword2(tempOldPassword2);
                currentUserDatabaseObj.setOldPassword3(tempOldPassword3);
                currentUserDatabaseObj.setOldPassword4(tempOldPassword4);
                //setting credntials expired flag to false..
                currentUserDatabaseObj.setCredentialsExpired(false);
                currentUserDatabaseObj.setTemporaryPassword("password is changed");

                /*set current date as password reset date*/
                LocalDate localDate = LocalDate.now();
                Date currentDate = java.sql.Date.valueOf(localDate);

                currentUserDatabaseObj.setResetPasswordDate(currentDate);
                currentUserDatabaseObj.setIsPasswordExpired(false);

                boolean checkStatus = forgotPasswordDAO.resetForgotPassword(currentUserDatabaseObj);
                if (checkStatus) {
                    statusCode = 3;
                }
            }
        }
// statusCode=0 if temppassword or old password not matches with db current password. OR current password and new password  not matched matched 
//        statusCode=1 if current password and new password matched
//        statusCode =2 if new password matched with any OLd password
//        StatusCode= 3 if password changed successfully..
        return statusCode;

    }

}
