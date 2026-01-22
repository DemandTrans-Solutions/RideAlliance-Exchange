package com.clearinghouse.service;

import com.clearinghouse.configuration.PasswordRuleBean;
import com.clearinghouse.dao.*;
import com.clearinghouse.dto.ForgotPasswordDTO;
import com.clearinghouse.entity.User;
import com.clearinghouse.exceptions.UsernameNotExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ForgotPasswordServiceTest {

    @Mock
    private ForgotPasswordDAO forgotPasswordDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private NotificationDAO notificationDAO;

    @Mock
    private UsernameExistCheckingDAO usernameExistCheckingDAO;

    @Mock
    private Environment env;

    @Mock
    private PasswordRuleBean passwordRuleBean;

    @InjectMocks
    private ForgotPasswordService forgotPasswordService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendTempPassword_EmailNotExists() {
        ForgotPasswordDTO forgotPasswordDTO = new ForgotPasswordDTO();
        forgotPasswordDTO.setEmail("nonexistent@example.com");

        when(usernameExistCheckingDAO.findUserByEmail(forgotPasswordDTO.getEmail())).thenReturn(false);

        assertThrows(UsernameNotExistException.class, () -> forgotPasswordService.sendTempPassword(forgotPasswordDTO));
        verify(usernameExistCheckingDAO).findUserByEmail(forgotPasswordDTO.getEmail());
    }

    @Test
    void testSendTempPassword_Success() {
        ForgotPasswordDTO forgotPasswordDTO = new ForgotPasswordDTO();
        forgotPasswordDTO.setEmail("test@example.com");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("oldPassword");

        when(usernameExistCheckingDAO.findUserByEmail(forgotPasswordDTO.getEmail())).thenReturn(true);
        when(userDAO.findUserByUsername(forgotPasswordDTO.getEmail())).thenReturn(user);
        when(forgotPasswordDAO.storeTempPassword(user)).thenReturn(true);

        boolean result = forgotPasswordService.sendTempPassword(forgotPasswordDTO);

        assertTrue(result);
        verify(usernameExistCheckingDAO).findUserByEmail(forgotPasswordDTO.getEmail());
        verify(userDAO).findUserByUsername(forgotPasswordDTO.getEmail());
        verify(forgotPasswordDAO).storeTempPassword(user);
    }

    @Test
    void testResetForgotPassword_TempPasswordMatch() {
        ForgotPasswordDTO forgotPasswordDTO = new ForgotPasswordDTO();
        forgotPasswordDTO.setUsername("testUser");
        forgotPasswordDTO.setTempPassword("tempPassword");
        forgotPasswordDTO.setNewPassword("newPassword");
        forgotPasswordDTO.setOldPassword("oldPassword");

        User user = new User();
        user.setPassword(new BCryptPasswordEncoder().encode("tempPassword"));

        when(userDAO.findUserByUsername(forgotPasswordDTO.getUsername())).thenReturn(user);
        when(forgotPasswordDAO.resetForgotPassword(user)).thenReturn(true);

        int result = forgotPasswordService.resetForgotPassword(forgotPasswordDTO);

        assertEquals(3, result);
        verify(userDAO).findUserByUsername(forgotPasswordDTO.getUsername());
        verify(forgotPasswordDAO).resetForgotPassword(user);
    }

    @Test
    void testResetForgotPassword_NewPasswordMatchesOld() {
        ForgotPasswordDTO forgotPasswordDTO = new ForgotPasswordDTO();
        forgotPasswordDTO.setUsername("testUser");
        forgotPasswordDTO.setTempPassword("currentPassword");
        forgotPasswordDTO.setNewPassword("oldPassword");
        forgotPasswordDTO.setOldPassword("oldPassword");

        User user = new User();
        user.setPassword(new BCryptPasswordEncoder().encode("currentPassword"));
        user.setOldPassword1(new BCryptPasswordEncoder().encode("oldPassword"));

        when(userDAO.findUserByUsername(forgotPasswordDTO.getUsername())).thenReturn(user);

        int result = forgotPasswordService.resetForgotPassword(forgotPasswordDTO);

        assertEquals(2, result);
        verify(userDAO).findUserByUsername(forgotPasswordDTO.getUsername());
        verify(forgotPasswordDAO, never()).resetForgotPassword(user);
    }

    @Test
    void testResetForgotPassword_CurrentPasswordMatchesNew() {
        ForgotPasswordDTO forgotPasswordDTO = new ForgotPasswordDTO();
        forgotPasswordDTO.setUsername("testUser");
        forgotPasswordDTO.setTempPassword("tempPassword");
        forgotPasswordDTO.setNewPassword("currentPassword");
        forgotPasswordDTO.setOldPassword("currentPassword");

        User user = new User();
        user.setPassword(new BCryptPasswordEncoder().encode("currentPassword"));

        when(userDAO.findUserByUsername(forgotPasswordDTO.getUsername())).thenReturn(user);

        int result = forgotPasswordService.resetForgotPassword(forgotPasswordDTO);

        assertEquals(1, result);
        verify(userDAO).findUserByUsername(forgotPasswordDTO.getUsername());
        verify(forgotPasswordDAO, never()).resetForgotPassword(user);
    }

    @Test
    void testResetForgotPassword_Failure() {
        ForgotPasswordDTO forgotPasswordDTO = new ForgotPasswordDTO();
        forgotPasswordDTO.setUsername("testUser");
        forgotPasswordDTO.setTempPassword("tempPassword");
        forgotPasswordDTO.setNewPassword("newPassword");
        forgotPasswordDTO.setOldPassword("oldPassword");

        User user = new User();
        user.setPassword(new BCryptPasswordEncoder().encode("tempPassword"));

        when(userDAO.findUserByUsername(forgotPasswordDTO.getUsername())).thenReturn(user);
        when(forgotPasswordDAO.resetForgotPassword(user)).thenReturn(false);

        int result = forgotPasswordService.resetForgotPassword(forgotPasswordDTO);

        assertEquals(0, result);
        verify(userDAO).findUserByUsername(forgotPasswordDTO.getUsername());
        verify(forgotPasswordDAO).resetForgotPassword(user);
    }
}