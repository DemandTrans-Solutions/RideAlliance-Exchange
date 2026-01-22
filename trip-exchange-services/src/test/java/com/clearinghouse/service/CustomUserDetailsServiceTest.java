package com.clearinghouse.service;

import com.clearinghouse.dao.UserDAO;
import com.clearinghouse.dao.UserRepository;
import com.clearinghouse.dao.UsernameExistCheckingDAO;
import com.clearinghouse.dto.UserDTO;
import com.clearinghouse.entity.User;
import com.clearinghouse.testutil.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDAO userDAO;

    @Mock
    private UsernameExistCheckingDAO usernameExistCheckingDAO;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper userModelMapper;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsername_UserExists() {
        // Arrange
        String username = "testUser";
        User expectedUser = TestData.user()
            .withUsername(username)
            .withRole("ROLE_ADMIN")
            .build();

        when(userRepository.findUserByUserName(username)).thenReturn(expectedUser);

        // Act
        User result = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertEquals(expectedUser, result);
        verify(userRepository).findUserByUserName(username);
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Arrange
        String username = "nonExistentUser";
        when(userRepository.findUserByUserName(username)).thenReturn(null);

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, 
            () -> customUserDetailsService.loadUserByUsername(username));
        verify(userRepository).findUserByUserName(username);
    }

    @Test
    void testUpdateUser() {
        // Arrange
        User user = TestData.user()
            .withId(1)
            .withUsername("updateUser")
            .withRole("ROLE_ADMIN")
            .build();
        UserDTO userDTO = TestData.user()
            .withId(1)
            .withUsername("updateUser")
            .buildDTO();

        when(userModelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        // Act
        User result = customUserDetailsService.updateUser(user);

        // Assert
        assertEquals(user, result);
        verify(userModelMapper).map(user, UserDTO.class);
        verify(userService).updateUser(userDTO);
    }

    @Test
    void testGetUserByUsername() {
        // Arrange
        String username = "testUser";
        User expectedUser = TestData.user()
            .withUsername(username)
            .withRole("ROLE_USER")
            .build();

        when(userDAO.findUserByUsername(username)).thenReturn(expectedUser);

        // Act
        User result = customUserDetailsService.getUserByUsername(username);

        // Assert
        assertEquals(expectedUser, result);
        verify(userDAO).findUserByUsername(username);
    }

    @Test
    void testCheckUserExist_UserExists() {
        // Arrange
        String username = "testUser";
        when(usernameExistCheckingDAO.findUserByUsername(username)).thenReturn(true);

        // Act
        boolean result = customUserDetailsService.checkUseExist(username);

        // Assert
        assertTrue(result);
        verify(usernameExistCheckingDAO).findUserByUsername(username);
    }

    @Test
    void testCheckUserExist_UserDoesNotExist() {
        // Arrange
        String username = "nonExistentUser";
        when(usernameExistCheckingDAO.findUserByUsername(username)).thenReturn(false);

        // Act
        boolean result = customUserDetailsService.checkUseExist(username);

        // Assert
        assertFalse(result);
        verify(usernameExistCheckingDAO).findUserByUsername(username);
    }

    @Test
    void testToDTO() {
        // Arrange
        User user = TestData.user()
            .withId(1)
            .withUsername("dtoUser")
            .withRole("ROLE_ADMIN")
            .build();
        UserDTO expectedDTO = TestData.user()
            .withId(1)
            .withUsername("dtoUser")
            .buildDTO();

        when(userModelMapper.map(user, UserDTO.class)).thenReturn(expectedDTO);

        // Act
        Object result = customUserDetailsService.toDTO(user);

        // Assert
        assertEquals(expectedDTO, result);
        verify(userModelMapper).map(user, UserDTO.class);
    }
}