package com.clearinghouse.service;

import com.clearinghouse.configuration.PasswordRuleBean;
import com.clearinghouse.dao.*;
import com.clearinghouse.dto.UserDTO;
import com.clearinghouse.entity.*;
import com.clearinghouse.exceptions.UsernameExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private PasswordRuleBean passwordRuleBean;


    @Mock
    private UserDAO userDAO;

    @Mock
    private ProviderDAO providerDAO;

    @Mock
    private UserNotificationDataDAO userNotificationDataDAO;

    @Mock
    private NotificationDAO notificationDAO;

    @Mock
    private UsernameExistCheckingDAO usernameExistCheckingDAO;

    @Mock
    private ModelMapper userModelMapper;

    @Mock
    private UserTokenDAO userTokenDAO;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAllUsers_ReturnsAllUsers() {
        // Create User objects with non-null authorities
        User user1 = new User();
        User user2 = new User();
        user1.setAuthorities(new HashSet<>());
        user2.setAuthorities(new HashSet<>());
        List<User> users = List.of(user1, user2);

        var userDTO = new UserDTO();
        userDTO.setUserRole("TestRole");

        when(userDAO.findAllUsers()).thenReturn(users);
        when(userModelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(userDTO);

        List<UserDTO> result = userService.findAllUsers();

        assertEquals(users.size(), result.size());
        verify(userDAO).findAllUsers();
    }

    @Test
    void findUserByUserId_ReturnsUser() {
        int userId = 1;
        User user = new User();
        user.setAuthorities(new HashSet<>());  // Initialize authorities collection

        var userDTO = new UserDTO();
        userDTO.setUserRole("TestRole");
        when(userDAO.findUserByUserId(userId)).thenReturn(user);
        when(userModelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        UserDTO result = userService.findUserByUserId(userId);

        assertNotNull(result);
        verify(userDAO).findUserByUserId(userId);
    }

    @Test
    void createUser_ThrowsExceptionIfUsernameExists() {
        var userDTO = new UserDTO();
        userDTO.setUserRole("TestRole");

        userDTO.setEmail("test@example.com");
        when(usernameExistCheckingDAO.findUserByUsername(userDTO.getEmail())).thenReturn(true);

        assertThrows(UsernameExistException.class, () -> userService.createUser(userDTO));
    }

    @Test
    void createUser_CreatesNewUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test@example.com");
        userDTO.setUserRole("ADMIN");
        when(usernameExistCheckingDAO.findUserByUsername(userDTO.getEmail())).thenReturn(false);
        when(userModelMapper.map(userDTO, User.class)).thenReturn(new User());
        when(userDAO.createUser(any(User.class))).thenReturn(new User());
        when(userModelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(userDTO);

        UserDTO result = userService.createUser(userDTO);

        assertNotNull(result);
        verify(userDAO).createUser(any(User.class));
    }

    @Test
    void updateUser_UpdatesExistingUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserRole("TestRole");
        userDTO.setId(1);

        User user = new User();
        user.setAuthorities(new HashSet<>());  // Initialize authorities collection

        when(userDAO.findUserByUserId(userDTO.getId())).thenReturn(user);
        when(userDAO.updateUser(user)).thenReturn(user);
        when(userModelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        UserDTO result = userService.updateUser(userDTO);

        assertNotNull(result);
        verify(userDAO).updateUser(user);
    }

    @Test
    void deleteUserByUserId_DeletesUser() {
        int userId = 1;

        boolean result = userService.deleteuserByUserId(userId);

        assertTrue(result);
        verify(userDAO).deleteUserByUserId(userId);
    }

    @Test
    void updateUserForAccountActivation_ActivatesAccount() {
        int userId = 1;
        User user = new User();
        var userDTO = new UserDTO();
        userDTO.setUserRole("TestRole");
        user.setAuthorities(new HashSet<>());
        when(userDAO.findUserByUserId(userId)).thenReturn(user);
        when(userDAO.updateUser(user)).thenReturn(user);
        when(userModelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        UserDTO result = userService.updateUserForAccountActivation(userId);

        assertNotNull(result);
        verify(userDAO).updateUser(user);
    }
    @Test
    void updateUserForAccountDeactivation_DeactivatesAccount() {
        int userId = 1;
        User user = new User();
        var userDTO = new UserDTO();
        userDTO.setUserRole("TestRole");

        user.setAuthorities(new HashSet<>());
        when(userDAO.findUserByUserId(userId)).thenReturn(user);
        when(userDAO.updateUser(user)).thenReturn(user);
        when(userModelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        UserDTO result = userService.updateUserForAccountDeactivation(userId);

        assertNotNull(result);
        verify(userDAO).updateUser(user);
    }

    @Test
    void findUserByUserProviderId_ReturnsUsersByProviderId() {
        int providerId = 1;
        var userDTO = new UserDTO();
        userDTO.setUserRole("TestRole");

        var user1 = new User();
        var user2 = new User();
        user1.setAuthorities(new HashSet<>());
        user2.setAuthorities(new HashSet<>());


        List<User> users = List.of(user1, user2);
        when(userNotificationDataDAO.getUsersOfProvider(providerId)).thenReturn(users);
        when(userModelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(userDTO);

        List<UserDTO> result = userService.findUserByUserProviderId(providerId);

        assertEquals(users.size(), result.size());
        verify(userNotificationDataDAO).getUsersOfProvider(providerId);
    }

    @Test
    void getUserIdByProviderId_ReturnsUserId() {
        int providerId = 1;
        int userId = 123;
        when(userDAO.getUserIdbyProviderId(providerId)).thenReturn(userId);

        int result = userService.getUserIdByProviderId(providerId);

        assertEquals(userId, result);
        verify(userDAO).getUserIdbyProviderId(providerId);
    }
}