/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;

import com.clearinghouse.dao.ActivateAccountDAO;
import com.clearinghouse.dao.UserDAO;
import com.clearinghouse.dao.UsernameExistCheckingDAO;
import com.clearinghouse.dto.UserDTO;
import com.clearinghouse.entity.User;
import com.clearinghouse.entity.UserAuthority;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 *
 * @author chaitanyaP
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class ActivateAccountService implements IConvertBOToDTO, IConvertDTOToBO {


    private final ActivateAccountDAO activateAccountDAO;


    private final UserDAO userDAO;


    private final ModelMapper userModelMapper;


    private final UsernameExistCheckingDAO usernameExistCheckingDAO;
    private final BCryptPasswordEncoder passwordEncoder;


    public String activateAccount(UserDTO userObj) {

        String resultStatus = "";

        User currentUserFromDatabase = new User();
        boolean isUserPresnt = usernameExistCheckingDAO.findUserByUsername(userObj.getUsername());
        if (isUserPresnt) {
            currentUserFromDatabase = userDAO.findUserByUsername(userObj.getUsername());
        }
        if (currentUserFromDatabase != null) {
            // If credentials are expired and there's no stored password yet,
            // treat this as the 'user already activated without set password' flow
            if (currentUserFromDatabase.isCredentialsExpired()
                    && (currentUserFromDatabase.getPassword() == null || currentUserFromDatabase.getPassword().isEmpty())) {
                resultStatus = "validUser";
            } else if (passwordEncoder.matches(userObj.getPassword(), currentUserFromDatabase.getPassword())) {
                resultStatus = "validUser";
            }
            if (resultStatus.equalsIgnoreCase("validUser")) {
                if (!currentUserFromDatabase.isAccountDisabled()) {
                    if (!currentUserFromDatabase.isCredentialsExpired()) {
                        resultStatus = "userAlredyActivatedWithSetPassword";
                        return resultStatus;
                    } else {
                        resultStatus = "userAlredyActivatedWithoutSetPassword";
                        return resultStatus;
                    }

                } else if (currentUserFromDatabase.isAccountDisabled()) {
                    currentUserFromDatabase.setAccountDisabled(false);
                    currentUserFromDatabase.setTemporaryPassword("Account is enabled");
                    boolean result = activateAccountDAO.activateAccount(currentUserFromDatabase);

                    if (result) {
                        resultStatus = "accountEnabled";
                        return resultStatus;
                    }
                }
            }

        }
        resultStatus = "invalidUsernameOrPassword";

        return resultStatus;
    }

    //    status=accountEnabled-->username password matched account anbled..
//    invalidUsernameOrPassword= enterd username is not valid.
//    userAlredyActivatedWithoutSetPassword= account diabled flag is false account alredy activated. 
//    userAlredyActivatedWithSetPassword=redirect to login page
//    
    @Override
    public Object toDTO(Object bo) {
        User userBO = (User) bo;
        // Defensive: handle nulls coming from mapping or missing authorities to avoid NPEs in tests
        String userRoleWithROLEKeyWord = null;

        Set<UserAuthority> authorities = userBO.getAuthorities();
        if (authorities != null && !authorities.isEmpty()) {
            UserAuthority ua = authorities.iterator().next();
            if (ua != null) {
                userRoleWithROLEKeyWord = ua.getAuthority();
            }
        }

        UserDTO userDTO = userModelMapper.map(userBO, UserDTO.class);

        String[] userRole = userRoleWithROLEKeyWord.split("_");
        userDTO.setUserRole(userRole[1]);
        return userDTO;
    }

    @Override
    public Object toBO(Object dto) {
        UserDTO userDTO = (UserDTO) dto;

        User userBO = userModelMapper.map(userDTO, User.class);

        return userBO;
    }

    @Override
    public Object toDTOCollection(Object boCollection) {
        return null;
    }

}
