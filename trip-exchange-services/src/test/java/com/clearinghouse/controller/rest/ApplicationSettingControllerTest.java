package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.ApplicationSettingDTO;
import com.clearinghouse.service.ApplicationSettingService;
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
import static org.mockito.Mockito.*;

class ApplicationSettingControllerTest {

    @Mock
    private ApplicationSettingService applicationSettingService;

    @InjectMocks
    private ApplicationSettingController applicationSettingController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListAllApplicationSettings() {
        List<ApplicationSettingDTO> settings = new ArrayList<>();
        settings.add(new ApplicationSettingDTO());
        when(applicationSettingService.findAllApplicationSettings()).thenReturn(settings);

        ResponseEntity<List<ApplicationSettingDTO>> response = applicationSettingController.listAllApplicationSettings();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(settings, response.getBody());
        verify(applicationSettingService).findAllApplicationSettings();
    }

    @Test
    void testListAllApplicationSettings_NoContent() {
        when(applicationSettingService.findAllApplicationSettings()).thenReturn(new ArrayList<>());

        ResponseEntity<List<ApplicationSettingDTO>> response = applicationSettingController.listAllApplicationSettings();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(applicationSettingService).findAllApplicationSettings();
    }

    @Test
    void testGetApplicationSettingById() {
        int applicationSettingId = 1;
        ApplicationSettingDTO setting = new ApplicationSettingDTO();
        when(applicationSettingService.findApplicationSettingByApplicationId(applicationSettingId)).thenReturn(setting);

        ResponseEntity<ApplicationSettingDTO> response = applicationSettingController.getApplicationSettingById(applicationSettingId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(setting, response.getBody());
        verify(applicationSettingService).findApplicationSettingByApplicationId(applicationSettingId);
    }

    @Test
    void testGetApplicationSettingById_NotFound() {
        int applicationSettingId = 1;
        when(applicationSettingService.findApplicationSettingByApplicationId(applicationSettingId)).thenReturn(null);

        ResponseEntity<ApplicationSettingDTO> response = applicationSettingController.getApplicationSettingById(applicationSettingId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(applicationSettingService).findApplicationSettingByApplicationId(applicationSettingId);
    }

    @Test
    void testUpdateApplicationSetting() {
        int applicationSettingId = 1;
        ApplicationSettingDTO currentSetting = new ApplicationSettingDTO();
        ApplicationSettingDTO updatedSetting = new ApplicationSettingDTO();
        when(applicationSettingService.findApplicationSettingByApplicationId(applicationSettingId)).thenReturn(currentSetting);
        when(applicationSettingService.updateApplicationSetting(currentSetting)).thenReturn(updatedSetting);

        ResponseEntity<ApplicationSettingDTO> response = applicationSettingController.updateApplicationSetting(applicationSettingId, currentSetting);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedSetting, response.getBody());
        verify(applicationSettingService).findApplicationSettingByApplicationId(applicationSettingId);
        verify(applicationSettingService).updateApplicationSetting(currentSetting);
    }

    @Test
    void testUpdateApplicationSetting_NotFound() {
        int applicationSettingId = 1;
        ApplicationSettingDTO setting = new ApplicationSettingDTO();
        when(applicationSettingService.findApplicationSettingByApplicationId(applicationSettingId)).thenReturn(null);

        ResponseEntity<ApplicationSettingDTO> response = applicationSettingController.updateApplicationSetting(applicationSettingId, setting);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(applicationSettingService).findApplicationSettingByApplicationId(applicationSettingId);
        verify(applicationSettingService, never()).updateApplicationSetting(setting);
    }
}