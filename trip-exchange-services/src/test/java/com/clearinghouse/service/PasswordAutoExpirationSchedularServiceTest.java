package com.clearinghouse.service;

import com.clearinghouse.dao.ApplicationSettingDAO;
import com.clearinghouse.dao.UserDAO;
import com.clearinghouse.entity.ApplicationSetting;
import com.clearinghouse.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PasswordAutoExpirationSchedularServiceTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private ApplicationSettingDAO applicationSettingDAO;

    @InjectMocks
    private PasswordAutoExpirationSchedularService passwordAutoExpirationSchedularService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    void passwordExpirationAfterDays_UsersWithExpiredPasswords() {
        List<ApplicationSetting> applicationSettings = new ArrayList<>();
        ApplicationSetting setting = new ApplicationSetting();
        setting.setPasswrodExpiredAfterDays(30);
        applicationSettings.add(setting);

        List<User> users = new ArrayList<>();
        User user = new User();
        user.setIsPasswordExpired(false);
        user.setResetPasswordDate(Date.valueOf(LocalDate.now().minusDays(31)));
        users.add(user);

        when(applicationSettingDAO.findAllApplicationSettings()).thenReturn(applicationSettings);
        when(userDAO.findAllUsers()).thenReturn(users);

        passwordAutoExpirationSchedularService.passwordExpirationAfterDays();

        assertTrue(user.isIsPasswordExpired());
        verify(userDAO).updateUser(user);
    }

    void passwordExpirationAfterDays_NoUsers() {
        List<ApplicationSetting> applicationSettings = new ArrayList<>();
        ApplicationSetting setting = new ApplicationSetting();
        setting.setPasswrodExpiredAfterDays(30);
        applicationSettings.add(setting);

        when(applicationSettingDAO.findAllApplicationSettings()).thenReturn(applicationSettings);
        when(userDAO.findAllUsers()).thenReturn(new ArrayList<>());

        passwordAutoExpirationSchedularService.passwordExpirationAfterDays();

        verify(userDAO, never()).updateUser(any());
    }

    void passwordExpirationAfterDays_NoExpirationSetting() {
        List<ApplicationSetting> applicationSettings = new ArrayList<>();
        ApplicationSetting setting = new ApplicationSetting();
        setting.setPasswrodExpiredAfterDays(0);
        applicationSettings.add(setting);

        when(applicationSettingDAO.findAllApplicationSettings()).thenReturn(applicationSettings);

        passwordAutoExpirationSchedularService.passwordExpirationAfterDays();

        verify(userDAO, never()).findAllUsers();
    }

    void passwordExpirationAfterDays_UserWithNoResetPasswordDate() {
        List<ApplicationSetting> applicationSettings = new ArrayList<>();
        ApplicationSetting setting = new ApplicationSetting();
        setting.setPasswrodExpiredAfterDays(30);
        applicationSettings.add(setting);

        List<User> users = new ArrayList<>();
        User user = new User();
        user.setIsPasswordExpired(false);
        user.setResetPasswordDate(null);
        users.add(user);

        when(applicationSettingDAO.findAllApplicationSettings()).thenReturn(applicationSettings);
        when(userDAO.findAllUsers()).thenReturn(users);

        passwordAutoExpirationSchedularService.passwordExpirationAfterDays();

        verify(userDAO, never()).updateUser(user);
    }
}