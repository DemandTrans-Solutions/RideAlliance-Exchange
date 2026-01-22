package com.clearinghouse.service;

import com.clearinghouse.dao.ChangePasswordDAO;
import com.clearinghouse.dao.UserDAO;
import com.clearinghouse.dto.ChangePasswordDTO;
import com.clearinghouse.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ChangePasswordServiceTest {

    @Mock
    private ChangePasswordDAO changePasswordDAO;

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private ChangePasswordService changePasswordService;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    void testResetPassword_CurrentPasswordMatch() {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setUsername("testUser");
        changePasswordDTO.setPassword("currentPassword");

        User user = new User();
        user.setPassword(passwordEncoder.encode("currentPassword"));

        when(userDAO.findUserByUsername("testUser")).thenReturn(user);

        int result = changePasswordService.resetPassword(changePasswordDTO);

        assertEquals(1, result);
        verify(userDAO).findUserByUsername("testUser");
        verify(changePasswordDAO, never()).resetPassword(any(User.class));
    }

    @Test
    void testResetPassword_MatchWithOldPassword() {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setUsername("testUser");
        changePasswordDTO.setPassword("oldPassword1");

        User user = new User();
        user.setPassword(passwordEncoder.encode("currentPassword"));
        user.setOldPassword1(passwordEncoder.encode("oldPassword1"));

        when(userDAO.findUserByUsername("testUser")).thenReturn(user);

        int result = changePasswordService.resetPassword(changePasswordDTO);

        assertEquals(2, result);
        verify(userDAO).findUserByUsername("testUser");
        verify(changePasswordDAO, never()).resetPassword(any(User.class));
    }

    @Test
    void testResetPassword_SuccessfulChange() {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setUsername("testUser");
        changePasswordDTO.setPassword("newPassword");

        User user = new User();
        user.setPassword(passwordEncoder.encode("currentPassword"));
        user.setOldPassword1(passwordEncoder.encode("oldPassword1"));
        user.setOldPassword2(passwordEncoder.encode("oldPassword2"));
        user.setOldPassword3(passwordEncoder.encode("oldPassword3"));

        when(userDAO.findUserByUsername("testUser")).thenReturn(user);
        when(changePasswordDAO.resetPassword(any(User.class))).thenReturn(true);

        int result = changePasswordService.resetPassword(changePasswordDTO);

        assertEquals(3, result);
        verify(userDAO).findUserByUsername("testUser");
        verify(changePasswordDAO).resetPassword(any(User.class));
    }

    @Test
    void testResetPassword_FailureToChange() {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setUsername("testUser");
        changePasswordDTO.setPassword("newPassword");

        User user = new User();
        user.setPassword(passwordEncoder.encode("currentPassword"));

        when(userDAO.findUserByUsername("testUser")).thenReturn(user);
        when(changePasswordDAO.resetPassword(any(User.class))).thenReturn(false);

        int result = changePasswordService.resetPassword(changePasswordDTO);

        assertEquals(0, result);
        verify(userDAO).findUserByUsername("testUser");
        verify(changePasswordDAO).resetPassword(any(User.class));
    }
}