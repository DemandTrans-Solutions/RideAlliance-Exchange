/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.UserDTO;
import com.clearinghouse.entity.UserToken;
import com.clearinghouse.service.TokenAuthenticationService;
import com.clearinghouse.service.UserService;
import com.clearinghouse.service.UserTokenService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * @author chaitanyaP
 */
@RestController
@Slf4j
@RequestMapping(value = {"api/users"})
@AllArgsConstructor
public class UserController {


    private final TokenAuthenticationService tokenAuthenticationService;
    private final UserTokenService userTokenService;


    private final UserService userService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<UserDTO>> listAllUsers() {
        List<UserDTO> userDTO = userService.findAllUsers();
        if (userDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{userId}"}, method = RequestMethod.GET)
    public ResponseEntity<UserDTO> getUserById(@PathVariable("userId") int userId) {
        UserDTO userDTO = userService.findUserByUserId(userId);
        if (userDTO == null) {
            log.error("#getUserById User ID not found [" + userId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        UserDTO newUserDto = userService.createUser(userDTO);
        return new ResponseEntity<>(newUserDto, HttpStatus.CREATED);

    }

    @RequestMapping(value = {"/{userId}"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable("userId") int userId,
            @Valid @RequestBody UserDTO userDTO) {
        UserDTO currentUserDTO = userService.findUserByUserId(userId);
        if (currentUserDTO == null) {
            log.error("#updateUser User ID not found [" + userId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UserDTO updatedUserDTO = userService.updateUser(userDTO);
        return new ResponseEntity<>(updatedUserDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{userId}"}, method = RequestMethod.DELETE)
    public ResponseEntity<Boolean> deleteUser(
            @PathVariable("userId") int userId) {
        if (userService.findUserByUserId(userId) == null) {
            log.error("#deleteUser user ID not found [" + userId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            boolean UserDeleteStatus = userService.deleteuserByUserId(userId);
            return new ResponseEntity<>(UserDeleteStatus, HttpStatus.OK);
        }
    }

    @RequestMapping(value = {"/{userId}/activate"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> updateUserForEnableUserAccount(@PathVariable("userId") int userId) {
        UserDTO currentUserDTO = userService.findUserByUserId(userId);
        if (currentUserDTO == null) {
            log.error("#updateUser User ID not found [" + userId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UserDTO updatedUserDTO = userService.updateUserForAccountActivation(userId);
        return new ResponseEntity<>(updatedUserDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{userId}/deactivate"}, method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> updateUserForDisableUserAccount(
            @PathVariable("userId") int userId) {
        UserDTO currentUserDTO = userService.findUserByUserId(userId);
        if (currentUserDTO == null) {
            log.error("#updateUser User ID not found [" + userId + "]");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UserDTO updatedUserDTO = userService.updateUserForAccountDeactivation(userId);
        return new ResponseEntity<>(updatedUserDTO, HttpStatus.OK);
    }

    @RequestMapping(value = {"/usersByProviderId/{providerId}"}, method = RequestMethod.GET)
    public ResponseEntity<List<UserDTO>> getUserByProviderId(@PathVariable("providerId") int providerId) {
        List<UserDTO> userDTOList = userService.findUserByUserProviderId(providerId);

        return new ResponseEntity<>(userDTOList, HttpStatus.OK);
    }

    @RequestMapping(value = {"/userIdByProviderId/{providerId}"}, method = RequestMethod.GET)
    public ResponseEntity<Integer> getUserIdByProviderId(@PathVariable("providerId") int providerId) {
        int userId = userService.getUserIdByProviderId(providerId);

        return new ResponseEntity<>(userId, HttpStatus.OK);
    }

    @RequestMapping(value = {"/getTokenForAdapter/{userId}"}, method = RequestMethod.GET)
    public ResponseEntity<String> getTokenByUserId(@PathVariable("userId") int userId) {
        var userDTO = userService.findUserByUserId(userId);
        if (userDTO == null && userId > 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            String tokenForAdapter = userService.getTokenByUserId(userId);
            String token = "";
            token = token + "{\"value\":\"" + tokenForAdapter + "\"}";

            return new ResponseEntity<>(token, HttpStatus.OK);
        }
    }

    @RequestMapping(value = {"/checkTokenPresent/{userId}"}, method = RequestMethod.GET)
    public ResponseEntity<UserToken> checkIsTokenExistsByUserId(@PathVariable("userId") int userId) {
        var user = userService.findUserByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            UserToken tokenForAdapter = userService.findUserTokenByUserId(userId);
            if (tokenForAdapter == null) {
                return new ResponseEntity<>(null, HttpStatus.NOT_IMPLEMENTED);
            }

            return new ResponseEntity<>(tokenForAdapter, HttpStatus.OK);
        }
    }

    @RequestMapping(value = {"/createToken/{userId}"}, method = RequestMethod.GET)
    public ResponseEntity<UserToken> createTokenForUser(@PathVariable("userId") int userId) {
        var user = userService.findUserEntityByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            UserToken tokenForAdapter = userService.findUserTokenByUserId(userId);
            if (tokenForAdapter == null || tokenForAdapter.getHmacToken() == null) {
                tokenAuthenticationService.createHMACTokenForUser(user);
                if (tokenForAdapter != null) {
                    tokenForAdapter.setHmacToken(tokenAuthenticationService.createHMACTokenForUser(user));
                    userTokenService.updateUserToken(tokenForAdapter);
                } else {
                    tokenForAdapter = new UserToken();
                    tokenForAdapter.setUser(user);
                    tokenForAdapter.setHmacToken(tokenAuthenticationService.createHMACTokenForUser(user));
                    tokenForAdapter.setUserToken(tokenAuthenticationService.createJWSTokenForUser(user));
                    userTokenService.createUserToken(tokenForAdapter);
                }
            }
            return new ResponseEntity<>(tokenForAdapter, HttpStatus.OK);
        }
    }


}
