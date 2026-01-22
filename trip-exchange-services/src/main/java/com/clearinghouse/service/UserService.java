/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;

import com.clearinghouse.configuration.PasswordRuleBean;
import com.clearinghouse.dao.*;
import com.clearinghouse.dto.UserDTO;
import com.clearinghouse.entity.*;
import com.clearinghouse.enumentity.NotificationStatus;
import com.clearinghouse.enumentity.NotificationTemplateCodeValue;
import com.clearinghouse.exceptions.UsernameExistException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author chaitanyaP
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class UserService implements IConvertBOToDTO, IConvertDTOToBO {


    private final UserDAO userDAO;


    private final ProviderDAO providerDAO;


    private final UserNotificationDataDAO userNotificationDataDAO;


    private final NotificationDAO notificationDAO;


    private final UsernameExistCheckingDAO usernameExistCheckingDAO;


    private final ModelMapper userModelMapper;

    private final UserTokenDAO userTokenDAO;

    private final PasswordRuleBean passwordRuleBean;

    private final String hostedSiteUrl;

    private final UserContextService userContextService;

//    public UserServiceImpl(TokenAuthenticationService authenticationService) {
//        this.authenticationService = authenticationService;
//    }


    /*creating token for adpater user 90 years validity*/
//    private static final long EXPIRY_IN_DAYS = 365 * 90;//90 years

    public User findUserEntityByUserId(int userId) {
        return userDAO.findUserByUserId(userId);
    }


    public String getCurrentUserName()
    {
        var context = userContextService.extractUserContext();
        if ( context != null ) {
            var user = findUserEntityByUserId(context.userId());
            if ( user != null ) {
                return user.getName();
            }
        }
        return "N/A";
    }

    public int getCurrentUserId()
    {
        var context = userContextService.extractUserContext();
        if ( context != null ) {
            return context.userId();
        }
        return 1;
    }


    public List<UserDTO> findAllUsers() {
        List<User> users = userDAO.findAllUsers();

        List<UserDTO> userDTOList = new ArrayList<>();
        for (User user : users) {
            userDTOList.add((UserDTO) toDTO(user));
        }

        var list = userDTOList.stream()
                .filter(UserDTO::isActive)
                .collect(Collectors.toList());

        return userDTOList;
    }


    public UserDTO findUserByUserId(int userId) {
        return (UserDTO) toDTO(userDAO.findUserByUserId(userId));
    }


    public UserDTO createUser(UserDTO userDTO) {

        if (usernameExistCheckingDAO.findUserByUsername(userDTO.getEmail())) {
            throw new UsernameExistException(userDTO.getEmail(), "Email already exists!!!");
        }

        User user = (User) toBO(userDTO);
        //newly added ByDefault set true user adapter val
        user.setAuthanticationTypeIsAdapter(true);
        user.setAccountDisabled(false);

        user.setIsActive(true);
        UserAuthority authority = new UserAuthority();

        authority.setAuthority("ROLE_" + userDTO.getUserRole().toUpperCase());
        authority.setUser(user);//checking...
        Set<UserAuthority> authorities = new HashSet<>();
        authorities.add(authority);
//        this feild is for to send data to UI
        user.setResponseDataForUI(Integer.toString(userDTO.getProviderId()));
        user.setAuthorities(authorities);

        //temporary password generation logic..
        char[] tempPWD = RandomPasswordGenerator.generatePswd(passwordRuleBean.getMinLen(), passwordRuleBean.getMaxLen(), passwordRuleBean.getNoOfCAPSAlpha(), passwordRuleBean.getNoOfDigits(), passwordRuleBean.getNoOfSplChars());
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String tempPassword = new String(tempPWD);
        String password = passwordEncoder.encode(tempPassword);

        user.setPassword(password);
//        save the temp pass for devolopment purpose in db..
        user.setTemporaryPassword(tempPassword);

        userDAO.createUser(user);

        //NotificationEnginePart.....
        Notification emailNotification = new Notification();
        NotificationTemplate notificationTemplate = new NotificationTemplate();
        emailNotification.setEmailTo(user.getEmail());
        emailNotification.setIsEMail(true);
        emailNotification.setStatusId(NotificationStatus.newStatus.status());
        notificationTemplate.setNotificationTemplateId(NotificationTemplateCodeValue.createUserTemplateCode.templateCodeValue());
        emailNotification.setNotificationTemplate(notificationTemplate);
        emailNotification.setNumberOfAttempts(0);
        emailNotification.setIsActive(true);

//        Setting parameter values in according to the template.
        Map createUserTemplateMap = new HashMap<String, String>();
        createUserTemplateMap.put("name", user.getName());
        createUserTemplateMap.put("username", user.getUsername());
        createUserTemplateMap.put("password", tempPassword);
        createUserTemplateMap.put("year", Year.now().toString());

        /*localtestinglink*/

        //  createUserTemplateMap.put("verificationLink", "http://localhost:4200/#/activateAccount");

        /*zconserver UAT link*/
        //createUserTemplateMap.put("verificationLink", "http://3.133.117.8:8090/tripexchange/#/activateAccount");


        /*prod server link*/
        createUserTemplateMap.put("verificationLink", hostedSiteUrl + "/#/activateAccount");
        String jsonValueOfTemplate = "";

        Iterator<Map.Entry<String, String>> entries = createUserTemplateMap.entrySet().iterator();
        while (entries.hasNext()) {

            Map.Entry<String, String> entry = entries.next();
            jsonValueOfTemplate = jsonValueOfTemplate + "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
            if (entries.hasNext()) {
                jsonValueOfTemplate = jsonValueOfTemplate + ",";
            }

        }

        String FinaljsonValueOfTemplate = "{" + jsonValueOfTemplate + "}";

        emailNotification.setParameterValues(FinaljsonValueOfTemplate);

        emailNotification.setSubject("Activate account");

        notificationDAO.createNotification(emailNotification);

        return (UserDTO) toDTO(user);
    }


    @Transactional
    public UserDTO updateUser(UserDTO userDTO) {
        User currentUserFromDatabase = userDAO.findUserByUserId(userDTO.getId());

        currentUserFromDatabase.setEmail(userDTO.getEmail());
        currentUserFromDatabase.setJobTitle(userDTO.getJobTitle());
        currentUserFromDatabase.setName(userDTO.getName());
        currentUserFromDatabase.setPhoneNumber(userDTO.getPhoneNumber());
        currentUserFromDatabase.setIsNotifyPartnerCreatesTicket(userDTO.isNotifyPartnerCreatesTicket());
        currentUserFromDatabase.setIsNotifyPartnerUpdateTicket(userDTO.isNotifyPartnerUpdateTicket());
        currentUserFromDatabase.setIsNotifyClaimedTicketRescinded(userDTO.isNotifyClaimedTicketRescinded());
        currentUserFromDatabase.setIsNotifyClaimedTicketExpired(userDTO.isNotifyClaimedTicketExpired());
        currentUserFromDatabase.setIsNotifyNewTripClaimAutoApproved(userDTO.isNotifyNewTripClaimAutoApproved());
        currentUserFromDatabase.setIsNotifyTripClaimApproved(userDTO.isNotifyTripClaimApproved());
        currentUserFromDatabase.setIsNotifyTripClaimDeclined(userDTO.isNotifyTripClaimDeclined());
        currentUserFromDatabase.setIsNotifyTripCommentAdded(userDTO.isNotifyTripCommentAdded());
        currentUserFromDatabase.setIsNotifyTripResultSubmitted(userDTO.isNotifyTripResultSubmitted());
        currentUserFromDatabase.setIsActive(userDTO.isActive());
        currentUserFromDatabase.setIsNotifyTripClaimRescinded(userDTO.isNotifyTripClaimRescinded());
        currentUserFromDatabase.setIsNotifyNewTripClaimAwaitingApproval(userDTO.isNotifyNewTripClaimAwaitingApproval());
        currentUserFromDatabase.setIsNotifyTripClaimCancelled(userDTO.isNotifyTripClaimCancelled());
        currentUserFromDatabase.setIsNotifyTripReceived(userDTO.isNotifyTripReceived());
        currentUserFromDatabase.setIsNotifyTripExpired(userDTO.isNotifyTripExpired());
        currentUserFromDatabase.setIsNotifyTripCancelled(userDTO.isNotifyTripCancelled());
        currentUserFromDatabase.setIsNotifyTripPriceMismatched(userDTO.isNotifyTripPriceMismatched());
        currentUserFromDatabase.setIsNotifyTripWeeklyReport(userDTO.isNotifyTripWeeklyReport());
        //adding new feildds for failed attempts login
        if (userDTO.getLastFailedAttemptDate() != null) {
            currentUserFromDatabase.setFailedAttempts(userDTO.getFailedAttempts());
            currentUserFromDatabase.setLastFailedAttemptDate(userDTO.getLastFailedAttemptDate());
            currentUserFromDatabase.setAccountExpired(userDTO.isAccountExpired());
        }
        Provider provider = providerDAO.findProviderByProviderId(userDTO.getProviderId());
        currentUserFromDatabase.setProvider(provider);

        Set<UserAuthority> setOfauthorityFromDatabase = currentUserFromDatabase.getAuthorities();
        UserAuthority oldUserAuthority = null;
        if (setOfauthorityFromDatabase != null && !setOfauthorityFromDatabase.isEmpty()) {
            List<UserAuthority> newListOfauthAuthoritys = new ArrayList<>();
            newListOfauthAuthoritys.addAll(setOfauthorityFromDatabase);
            oldUserAuthority = newListOfauthAuthoritys.get(0);
        }

        //updating the database values
        User newUser = userDAO.updateUser(currentUserFromDatabase);
        int resultBit = 0;
        if (oldUserAuthority != null) {
            resultBit = updateUserPermnently(currentUserFromDatabase.getId(), oldUserAuthority.getAuthority(), "ROLE_" + userDTO.getUserRole().toUpperCase());
        }

        //this is the twick for userorle to get the updated role to DTO
        UserDTO userDTOUpdated = (UserDTO) toDTO(newUser);
        userDTOUpdated.setUserRole(userDTO.getUserRole().toUpperCase());
        return userDTOUpdated;
    }


    public boolean deleteuserByUserId(int userId) {
        userDAO.deleteUserByUserId(userId);
        return true;
    }


    public UserDTO updateUserForAccountActivation(int userId) {
        User user = userDAO.findUserByUserId(userId);
        user.setAccountLocked(false);
        user.setIsActive(true);
        User updateduser = userDAO.updateUser(user);

        //send mail to user for account activated succefully by admin
        sendMailToUserForAccActivation(updateduser);

        return (UserDTO) toDTO(updateduser);
    }


    public UserDTO updateUserForAccountDeactivation(int userId) {

        User user = userDAO.findUserByUserId(userId);
        user.setAccountLocked(true);
        user.setIsActive(false);
        User updateduser = userDAO.updateUser(user);

        //send mail to user for account deactivated succefully by admin
        sendMailToUserForAccDeActivation(updateduser);

        return (UserDTO) toDTO(updateduser);
    }


    public List<UserDTO> findUserByUserProviderId(int providerId) {
        List<User> userByProviderIdList = userNotificationDataDAO.getUsersOfProvider(providerId);
        List<UserDTO> userDTOByProviderIdList = new ArrayList<>();
        for (User user : userByProviderIdList) {
            userDTOByProviderIdList.add((UserDTO) toDTO(user));
        }
        return userDTOByProviderIdList;
    }


    public int getUserIdByProviderId(int providerId) {
        return userDAO.getUserIdbyProviderId(providerId);
    }

    private void sendMailToUserForAccActivation(User updateduser) {
        try {
            Notification emailNotification = new Notification();
            NotificationTemplate notificationTemplate = new NotificationTemplate();
            emailNotification.setIsEMail(true);
            emailNotification.setStatusId(NotificationStatus.newStatus.status());
            notificationTemplate.setNotificationTemplateId(NotificationTemplateCodeValue.userActivationByAdmin.templateCodeValue());
            emailNotification.setNotificationTemplate(notificationTemplate);
            emailNotification.setIsActive(true);
            emailNotification.setNumberOfAttempts(0);

            emailNotification.setEmailTo(updateduser.getEmail());

            //        Setting parameter values in according to the template.
            Map userActivationByAdminTemplateMap = new HashMap<String, String>();
            userActivationByAdminTemplateMap.put("name", updateduser.getName());
            userActivationByAdminTemplateMap.put("year", Year.now().toString());


            String jsonValueOfTemplate = "";

            Iterator<Map.Entry<String, String>> entries = userActivationByAdminTemplateMap.entrySet().iterator();
            while (entries.hasNext()) {

                Map.Entry<String, String> entry = entries.next();
                jsonValueOfTemplate = jsonValueOfTemplate + "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
                if (entries.hasNext()) {
                    jsonValueOfTemplate = jsonValueOfTemplate + ",";
                }

            }

            String FinaljsonValueOfTemplate = "{" + jsonValueOfTemplate + "}";

            emailNotification.setParameterValues(FinaljsonValueOfTemplate);

            emailNotification.setSubject("User account activation");

            notificationDAO.createNotification(emailNotification);
        } catch (Exception ex) {
            log.error("Error in sending mail to user for account activation", ex);
        }
    }

    private void sendMailToUserForAccDeActivation(User updateduser) {
        try {
            //NotificationEnginePart.....
            Notification emailNotification = new Notification();
            NotificationTemplate notificationTemplate = new NotificationTemplate();
            emailNotification.setIsEMail(true);
            emailNotification.setStatusId(NotificationStatus.newStatus.status());
            notificationTemplate.setNotificationTemplateId(NotificationTemplateCodeValue.userDeactivationByAdmin.templateCodeValue());
            emailNotification.setNotificationTemplate(notificationTemplate);
            emailNotification.setIsActive(true);
            emailNotification.setNumberOfAttempts(0);

            emailNotification.setEmailTo(updateduser.getEmail());

//        Setting parameter values in according to the template.
            Map userDeactivationByAdminTemplateMap = new HashMap<String, String>();
            userDeactivationByAdminTemplateMap.put("name", updateduser.getName());
            userDeactivationByAdminTemplateMap.put("year", Year.now().toString());


            String jsonValueOfTemplate = "";

            Iterator<Map.Entry<String, String>> entries = userDeactivationByAdminTemplateMap.entrySet().iterator();
            while (entries.hasNext()) {

                Map.Entry<String, String> entry = entries.next();
                jsonValueOfTemplate = jsonValueOfTemplate + "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
                if (entries.hasNext()) {
                    jsonValueOfTemplate = jsonValueOfTemplate + ",";
                }

            }

            String FinaljsonValueOfTemplate = "{" + jsonValueOfTemplate + "}";

            emailNotification.setParameterValues(FinaljsonValueOfTemplate);

            emailNotification.setSubject("User account deactivation");

            notificationDAO.createNotification(emailNotification);
        } catch (Exception ex) {
            log.error("Error in sending mail to user for account deactivation", ex);
        }
    }

    /*update userRole for keeping only one role at a time*/
    public int updateUserPermnently(int id, String oldUserrole, String newUserrole) {
        int newUserAuthorityResult = userDAO.updateUserRole(id, oldUserrole, newUserrole);
        return newUserAuthorityResult;
    }


    public int findProviderIdByUsername(String username) {
        return userDAO.findProviderIdByUsername(username);
    }



    /*create token for the provider*/

    public String getTokenByUserId(int userId) {
        User newUser = userDAO.findUserByUserId(userId);
        /*new codeimpl*/

//        HttpServletResponse response = new HttpServletResponseWrapper(null);
        UserAuthentication authentication = new UserAuthentication(newUser);

//        String jwtTokenTesting = authenticationService.addAuthenticationToGetToken(null, authentication);
//        String[] splittedToken = jwtTokenTesting.split("\\.");
//        byte[] hash = DatatypeConverter.parseBase64Binary(splittedToken[1]);
//
//        User user = authenticationService.newMethodToParseuser(jwtTokenTesting, " ", AuthanticationCheckStatusForLogin.usertypeAdpater.loginUsertypeValue());
//        if (user != null) {
//            if (hash.length == 32) {
//                UserToken userToken = new UserToken();
//                /*set Usertoken*/
//                userToken.setUser(user);
//                userToken.setUserToken(jwtTokenTesting);
//                userTokenDAO.CreateUserToken(userToken);
//
//                return jwtTokenTesting;
//            }
//        } else {
//
//            getTokenByUserId(userId);
//        }

//======================================================================================
//        UserToken userToken = new UserToken();
//
//        newUser.setExpires((System.currentTimeMillis() / (1000 * 60 * 60 * 24)) + EXPIRY_IN_DAYS);
//        /*add csrf token*/
//        String csrfToken = authenticationService.getUUID();
//
//        newUser.setCsrfToken(csrfToken);
//
//        String jwtToken = authenticationService.getNewTokenForUser(newUser);
//        log.debug("OLDToken-----" + jwtToken);
//
////            User testUser = authenticationService.parseUserFromNewToken(jwtToken);
//        User user = authenticationService.newMethodToParseuser(jwtToken, csrfToken, AuthanticationCheckStatusForLogin.usertypeAdpater.loginUsertypeValue());
//        if (user != null) {
//            /*code for calling UI To test api */
//
////                body.add("api_key", jwtToken);
////            String URL = "http://localhost:8081/tripexchange/users/" + user.getId() + "?api_key=" + jwtToken;
////            ResponseEntity<User> testUser = callTestMethodForuser(URL);
////            log.debug("URL------------" + URL);
////            log.debug("NewToken------------" + jwtToken);
////            log.debug("testuser------------" + testUser);
////            if (testUser.getStatusCode() == HttpStatus.OK) {
////                /*set Usertoken*/
////                userToken.setUser(user);
////                userToken.setUserToken(jwtToken);
////                userTokenDAO.CreateUserToken(userToken);
////                return jwtToken;
////            } else {
////                log.debug("RepeatMethod---------------");
////                getTokenByUserId(userId);
////            }
//            /*set Usertoken*/
//            userToken.setUser(user);
//            userToken.setUserToken(jwtToken);
//            userTokenDAO.CreateUserToken(userToken);
//            return jwtToken;
//        } else {
//            log.debug("RepeatMethod---------------");
//            getTokenByUserId(userId);
//        }
        return "";
    }

//    public ResponseEntity<User> callTestMethodForuser(String URL) {
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders requestHeaders = new HttpHeaders();
//        requestHeaders.add("Content-Type", "application/json");
//        HttpEntity<?> httpEntity = new HttpEntity<Object>(body, requestHeaders);
//
//        ResponseEntity<User> testuser = restTemplate.exchange(URL, HttpMethod.GET, httpEntity, User.class);
//        return testuser;
//    }

    public UserToken findUserTokenByUserId(int userId) {
        return userTokenDAO.findUserTokenByUserId(userId);
    }


    public Object toDTO(Object bo) {
        User userBO = (User) bo;
        //this is for testing purpose of the null condition..
        String userRoleWithROLEKeyWord = null;

        Set<UserAuthority> authorities = new HashSet<>();
        authorities = userBO.getAuthorities();

        UserDTO userDTO = userModelMapper.map(userBO, UserDTO.class);

        List<String> roles = new ArrayList<>();
        for (UserAuthority userAuthority : authorities) {
            String[] userRoleTemporaryArray = userAuthority.getAuthority().split("_");
            String userRoleWithoutUnderscore = userRoleTemporaryArray[1];

            roles.add(userRoleWithoutUnderscore);

        }
        if (roles != null && !roles.isEmpty()) {
            String finalRole = roles.get(0);
            userDTO.setUserRole(finalRole);
        } else {
            log.warn("User role for {} is null or empty", userBO);
        }
        return userDTO;
    }

    @Override
    public Object toBO(Object dto) {
        UserDTO userDTO = (UserDTO) dto;
        User userBO = userModelMapper.map(userDTO, User.class);
        setNullBooleanFieldsToFalse(userBO);
        return userBO;
    }


    /**
     * Sets all null Boolean fields in a User object to false
     * to prevent ConstraintViolationException during persistence
     */
    private void setNullBooleanFieldsToFalse(User user) {
        try {
            for (java.lang.reflect.Field field : User.class.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getType() == Boolean.class && field.get(user) == null) {
                    field.set(user, false);
                    log.debug("Set null Boolean field {} to false", field.getName());
                }
            }
        } catch (IllegalAccessException e) {
            log.error("Error setting null Boolean fields to false", e);
        }
    }


    @Override
    public Object toDTOCollection(Object boCollection) {

//        if (boCollection != null) {
//            List<User> users = (List<User>) boCollection;
//            java.lang.reflect.Type targetListType = new TypeToken<List<UserDTO>>() {
//            }.getType();
//            List<UserDTO> userDTOs = modelMapper.map(users, targetListType);
//            return userDTOs;
//        }
        return null;

    }

}
