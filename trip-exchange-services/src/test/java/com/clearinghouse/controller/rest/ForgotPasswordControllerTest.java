package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.ForgotPasswordDTO;
import com.clearinghouse.service.ForgotPasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ForgotPasswordControllerTest {

    @Mock
    private ForgotPasswordService forgotPasswordService;

    @InjectMocks
    private ForgotPasswordController forgotPasswordController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendMail_Success() {
        ForgotPasswordDTO forgotPasswordDTO = new ForgotPasswordDTO();
        when(forgotPasswordService.sendTempPassword(forgotPasswordDTO)).thenReturn(true);

        ResponseEntity<Boolean> response = forgotPasswordController.sendMail(forgotPasswordDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
        verify(forgotPasswordService).sendTempPassword(forgotPasswordDTO);
    }

    @Test
    void testSendMail_Failure() {
        ForgotPasswordDTO forgotPasswordDTO = new ForgotPasswordDTO();
        when(forgotPasswordService.sendTempPassword(forgotPasswordDTO)).thenReturn(false);

        ResponseEntity<Boolean> response = forgotPasswordController.sendMail(forgotPasswordDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(false, response.getBody());
        verify(forgotPasswordService).sendTempPassword(forgotPasswordDTO);
    }

    @Test
    void testResetForgotPassword_Success() {
        ForgotPasswordDTO forgotPasswordDTO = new ForgotPasswordDTO();
        Integer serviceResponse = 1; // Assuming 1 indicates success
        when(forgotPasswordService.resetForgotPassword(forgotPasswordDTO)).thenReturn(serviceResponse);

        ResponseEntity<Integer> response = forgotPasswordController.resetForgotPassword(forgotPasswordDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody());
        verify(forgotPasswordService).resetForgotPassword(forgotPasswordDTO);
    }

    @Test
    void testResetForgotPassword_Failure() {
        ForgotPasswordDTO forgotPasswordDTO = new ForgotPasswordDTO();
        Integer serviceResponse = 0; // Assuming 0 indicates failure
        when(forgotPasswordService.resetForgotPassword(forgotPasswordDTO)).thenReturn(serviceResponse);

        ResponseEntity<Integer> response = forgotPasswordController.resetForgotPassword(forgotPasswordDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody());
        verify(forgotPasswordService).resetForgotPassword(forgotPasswordDTO);
    }
}