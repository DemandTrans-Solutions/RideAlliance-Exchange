/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;


import com.clearinghouse.dao.UserDAO;
import com.clearinghouse.dao.UserRepository;
import com.clearinghouse.dao.UsernameExistCheckingDAO;
import com.clearinghouse.dto.UserDTO;
import com.clearinghouse.entity.User;
import com.clearinghouse.entity.UserAuthority;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author manisha
 */
@Service
@AllArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService, IConvertBOToDTO {


    private final UserRepository userDao;


    private final UserDAO userDaoForDatabase;


    private final UsernameExistCheckingDAO usernameExistCheckingDAO;


    private final UserService userService;


    private final ModelMapper userModelMapper;

    private final AccountStatusUserDetailsChecker detailsChecker = new AccountStatusUserDetailsChecker();

    @Override
    public final User loadUserByUsername(String username) {
        final User user = userDao.findUserByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("user not found");
        }
        detailsChecker.check(user);
        return user;

    }
//writing own methods

    public final User updateUser(User user) {
//        return userDaoForDatabase.updateUser(user);
        UserDTO userDTO = (UserDTO) toDTO(user);
        userService.updateUser(userDTO);
        return user;

    }

    public final User getUserByUsername(String username) {
        return userDaoForDatabase.findUserByUsername(username);
    }

    public final boolean checkUseExist(String username) {
        return usernameExistCheckingDAO.findUserByUsername(username);
    }

    @Override
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

        String finalRole = roles.get(0);
        userDTO.setUserRole(finalRole);
        return userDTO;
    }
}
