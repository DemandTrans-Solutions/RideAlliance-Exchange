package com.clearinghouse.service;

import com.clearinghouse.dao.ActivateAccountDAO;
import com.clearinghouse.dao.UserDAO;
import com.clearinghouse.dao.UsernameExistCheckingDAO;
import com.clearinghouse.dto.UserDTO;
import com.clearinghouse.entity.User;
import com.clearinghouse.entity.UserAuthority;
import com.clearinghouse.testutil.UserBuilder;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ActivateAccountServiceTest {

    @Mock
    private ActivateAccountDAO activateAccountDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private UsernameExistCheckingDAO usernameExistCheckingDAO;

    @Mock
    private ModelMapper userModelMapper;
    
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private ActivateAccountService activateAccountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    }

    @Test
    void testActivateAccount_ValidUser_AccountEnabled() {
    User user = new UserBuilder()
        .withUsername("testUser")
        .withPassword("encodedPassword")
        .withAccountDisabled(true)
        .build();
        UserDTO userDTO = new UserBuilder()
                .withUsername("testUser")
                .withPassword("password")
                .buildDTO();

    when(usernameExistCheckingDAO.findUserByUsername("testUser")).thenReturn(true);
    when(userDAO.findUserByUsername("testUser")).thenReturn(user);
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    // set properties on the real user object instead of stubbing its methods
    user.setAccountDisabled(true);
    when(activateAccountDAO.activateAccount(user)).thenReturn(true);

        String result = activateAccountService.activateAccount(userDTO);

        assertEquals("accountEnabled", result);
        verify(usernameExistCheckingDAO).findUserByUsername("testUser");
        verify(userDAO).findUserByUsername("testUser");
        verify(activateAccountDAO).activateAccount(user);
    }

    @Test
    void testActivateAccount_InvalidUsernameOrPassword() {
        UserDTO userDTO = new UserBuilder()
                .withUsername("invalidUser")
                .withPassword("password")
                .buildDTO();

        when(usernameExistCheckingDAO.findUserByUsername("invalidUser")).thenReturn(false);

        String result = activateAccountService.activateAccount(userDTO);

        assertEquals("invalidUsernameOrPassword", result);
        verify(usernameExistCheckingDAO).findUserByUsername("invalidUser");
        verify(userDAO, never()).findUserByUsername(anyString());
    }

    @Test
    public void testActivateAccount_UserAlreadyActivatedWithSetPassword() {
        UserDTO userDTO = new UserBuilder()
                .withUsername("testUser")
                .withPassword("password")
                .buildDTO();

    User user = new UserBuilder()
        .withUsername("testUser")
        .withPassword(passwordEncoder.encode("password"))
        .withAccountDisabled(false)
        .withCredentialsExpired(false)
        .build();

    when(usernameExistCheckingDAO.findUserByUsername("testUser")).thenReturn(true);
    when(userDAO.findUserByUsername("testUser")).thenReturn(user);
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    // set properties on real user object instead of stubbing
    user.setAccountDisabled(false);
    user.setIsActive(true);
    user.setPassword("existingPassword");

        String result = activateAccountService.activateAccount(userDTO);

        assertEquals("userAlredyActivatedWithSetPassword", result);
        verify(usernameExistCheckingDAO).findUserByUsername("testUser");
        verify(userDAO).findUserByUsername("testUser");
    }

    @Test
    public void testActivateAccount_UserAlreadyActivatedWithoutSetPassword() {
        UserDTO userDTO = new UserBuilder()
                .withUsername("testUser")
                .withPassword("password")
                .buildDTO();

        User user = new UserBuilder()
                .withUsername("testUser")
                .withPassword(passwordEncoder.encode("password"))
                .withAccountDisabled(false)
                .withCredentialsExpired(true)
                .build();

    when(usernameExistCheckingDAO.findUserByUsername("testUser")).thenReturn(true);
    when(userDAO.findUserByUsername("testUser")).thenReturn(user);
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    // set properties on real user object instead of stubbing
    user.setIsActive(true);
    user.setPassword(null);

        String result = activateAccountService.activateAccount(userDTO);

        assertEquals("userAlredyActivatedWithoutSetPassword", result);
        verify(usernameExistCheckingDAO).findUserByUsername("testUser");
        verify(userDAO).findUserByUsername("testUser");
    }

    @Test
    void testToDTO() {
        UserAuthority authority = new UserAuthority();
        authority.setAuthority("ROLE_ADMIN");
        Set<UserAuthority> authorities = new HashSet<>();
        authorities.add(authority);

        User user = new UserBuilder()
                .withRole("ROLE_ADMIN")
                .build();
    UserDTO userDTO = new UserBuilder()
        .withRole("ROLE_ADMIN")
        .buildDTO();
        
        when(userModelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        Object result = activateAccountService.toDTO(user);

        assertEquals(userDTO, result);
        verify(userModelMapper).map(user, UserDTO.class);
    }

    @Test
    void testToBO() {
        UserDTO userDTO = new UserBuilder().buildDTO();
        User user = new UserBuilder().build();

        when(userModelMapper.map(userDTO, User.class)).thenReturn(user);

        Object result = activateAccountService.toBO(userDTO);

        assertEquals(user, result);
        verify(userModelMapper).map(userDTO, User.class);
    }

    @Test
    void testToDTOCollection() {
        Object result = activateAccountService.toDTOCollection(null);
        assertEquals(null, result);
    }
}
