package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.ChangePasswordDTO;
import com.clearinghouse.service.ChangePasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ChangePasswordControllerTest {

    @Mock
    private ChangePasswordService changePasswordService;

    @InjectMocks
    private ChangePasswordController changePasswordController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testResetPassword_Success() {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        Integer serviceResponse = 1; // Assuming 1 indicates success

        when(changePasswordService.resetPassword(changePasswordDTO)).thenReturn(serviceResponse);

        ResponseEntity<Integer> response = changePasswordController.resetPassword(changePasswordDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody());
        verify(changePasswordService).resetPassword(changePasswordDTO);
    }

    @Test
    void testResetPassword_Failure() {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        Integer serviceResponse = 0; // Assuming 0 indicates failure

        when(changePasswordService.resetPassword(changePasswordDTO)).thenReturn(serviceResponse);

        ResponseEntity<Integer> response = changePasswordController.resetPassword(changePasswordDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody());
        verify(changePasswordService).resetPassword(changePasswordDTO);
    }
}