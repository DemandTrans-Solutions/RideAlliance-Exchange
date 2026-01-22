package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.UserDTO;
import com.clearinghouse.entity.UserToken;
import com.clearinghouse.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListAllUsers() {
        List<UserDTO> users = new ArrayList<>();
        users.add(new UserDTO());
        when(userService.findAllUsers()).thenReturn(users);

        ResponseEntity<List<UserDTO>> response = userController.listAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
        verify(userService).findAllUsers();
    }

    @Test
    void testListAllUsers_NoContent() {
        when(userService.findAllUsers()).thenReturn(new ArrayList<>());

        ResponseEntity<List<UserDTO>> response = userController.listAllUsers();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).findAllUsers();
    }

    @Test
    void testGetUserById() {
        int userId = 1;
        UserDTO user = new UserDTO();
        when(userService.findUserByUserId(userId)).thenReturn(user);

        ResponseEntity<UserDTO> response = userController.getUserById(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
        verify(userService).findUserByUserId(userId);
    }

    @Test
    void testGetUserById_NotFound() {
        int userId = 1;
        when(userService.findUserByUserId(userId)).thenReturn(null);

        ResponseEntity<UserDTO> response = userController.getUserById(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).findUserByUserId(userId);
    }

    @Test
    void testCreateUser() {
        UserDTO user = new UserDTO();
        UserDTO createdUser = new UserDTO();
        when(userService.createUser(user)).thenReturn(createdUser);

        ResponseEntity<UserDTO> response = userController.createUser(user);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdUser, response.getBody());
        verify(userService).createUser(user);
    }

    @Test
    void testUpdateUser() {
        int userId = 1;
        UserDTO user = new UserDTO();
        UserDTO currentUser = new UserDTO();
        UserDTO updatedUser = new UserDTO();
        when(userService.findUserByUserId(userId)).thenReturn(currentUser);
        when(userService.updateUser(user)).thenReturn(updatedUser);

        ResponseEntity<UserDTO> response = userController.updateUser(userId, user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedUser, response.getBody());
        verify(userService).findUserByUserId(userId);
        verify(userService).updateUser(user);
    }

    @Test
    void testUpdateUser_NotFound() {
        int userId = 1;
        UserDTO user = new UserDTO();
        when(userService.findUserByUserId(userId)).thenReturn(null);

        ResponseEntity<UserDTO> response = userController.updateUser(userId, user);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).findUserByUserId(userId);
        verify(userService, never()).updateUser(user);
    }

    @Test
    void testDeleteUser() {
        int userId = 1;
        when(userService.findUserByUserId(userId)).thenReturn(new UserDTO());
        when(userService.deleteuserByUserId(userId)).thenReturn(true);

        ResponseEntity<Boolean> response = userController.deleteUser(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
        verify(userService).findUserByUserId(userId);
        verify(userService).deleteuserByUserId(userId);
    }

    @Test
    void testDeleteUser_NotFound() {
        int userId = 1;
        when(userService.findUserByUserId(userId)).thenReturn(null);

        ResponseEntity<Boolean> response = userController.deleteUser(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).findUserByUserId(userId);
        verify(userService, never()).deleteuserByUserId(userId);
    }

    @Test
    void testUpdateUserForEnableUserAccount() {
        int userId = 1;
        UserDTO currentUser = new UserDTO();
        UserDTO updatedUser = new UserDTO();
        when(userService.findUserByUserId(userId)).thenReturn(currentUser);
        when(userService.updateUserForAccountActivation(userId)).thenReturn(updatedUser);

        ResponseEntity<UserDTO> response = userController.updateUserForEnableUserAccount(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedUser, response.getBody());
        verify(userService).findUserByUserId(userId);
        verify(userService).updateUserForAccountActivation(userId);
    }

    @Test
    void testUpdateUserForEnableUserAccount_NotFound() {
        int userId = 1;
        when(userService.findUserByUserId(userId)).thenReturn(null);

        ResponseEntity<UserDTO> response = userController.updateUserForEnableUserAccount(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).findUserByUserId(userId);
        verify(userService, never()).updateUserForAccountActivation(userId);
    }

    @Test
    void testUpdateUserForDisableUserAccount() {
        int userId = 1;
        UserDTO currentUser = new UserDTO();
        UserDTO updatedUser = new UserDTO();
        when(userService.findUserByUserId(userId)).thenReturn(currentUser);
        when(userService.updateUserForAccountDeactivation(userId)).thenReturn(updatedUser);

        ResponseEntity<UserDTO> response = userController.updateUserForDisableUserAccount(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedUser, response.getBody());
        verify(userService).findUserByUserId(userId);
        verify(userService).updateUserForAccountDeactivation(userId);
    }

    @Test
    void testUpdateUserForDisableUserAccount_NotFound() {
        int userId = 1;
        when(userService.findUserByUserId(userId)).thenReturn(null);

        ResponseEntity<UserDTO> response = userController.updateUserForDisableUserAccount(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).findUserByUserId(userId);
        verify(userService, never()).updateUserForAccountDeactivation(userId);
    }

    @Test
    void testGetUserByProviderId() {
        int providerId = 1;
        List<UserDTO> users = new ArrayList<>();
        when(userService.findUserByUserProviderId(providerId)).thenReturn(users);

        ResponseEntity<List<UserDTO>> response = userController.getUserByProviderId(providerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
        verify(userService).findUserByUserProviderId(providerId);
    }

    @Test
    void testGetUserIdByProviderId() {
        int providerId = 1;
        int userId = 1;
        when(userService.getUserIdByProviderId(providerId)).thenReturn(userId);

        ResponseEntity<Integer> response = userController.getUserIdByProviderId(providerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userId, response.getBody());
        verify(userService).getUserIdByProviderId(providerId);
    }

    @Test
    void testGetTokenByUserId() {
        int userId = -10000;
        String token = "sampleToken";
        when(userService.getTokenByUserId(userId)).thenReturn(token);

        ResponseEntity<String> response = userController.getTokenByUserId(userId);

        var status = response.getStatusCode();
        assertTrue(status == HttpStatus.OK || status == HttpStatus.NOT_FOUND);
        assertEquals("{\"value\":\"sampleToken\"}", response.getBody());
        verify(userService).getTokenByUserId(userId);
    }

    @Test
    void testGetTokenByUserId_NotFound() {
        int userId = 1;
        when(userService.findUserByUserId(userId)).thenReturn(null);

        ResponseEntity<String> response = userController.getTokenByUserId(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService, never()).getTokenByUserId(userId);
    }

    @Test
    void testCheckIsTokenExistsByUserId() {
        int userId = 1;
        UserDTO user = new UserDTO(); // Mock user exists
        UserToken token = new UserToken(); // Mock token exists

        when(userService.findUserByUserId(userId)).thenReturn(user);
        when(userService.findUserTokenByUserId(userId)).thenReturn(token);

        ResponseEntity<UserToken> response = userController.checkIsTokenExistsByUserId(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(token, response.getBody());
        verify(userService).findUserByUserId(userId);
        verify(userService).findUserTokenByUserId(userId);
    }

    @Test
    void testCheckIsTokenExistsByUserId_NotFound() {
        int userId = -1000;
        when(userService.findUserByUserId(userId)).thenReturn(null);

        ResponseEntity<UserToken> response = userController.checkIsTokenExistsByUserId(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService, never()).findUserTokenByUserId(userId);
    }


}