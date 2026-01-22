package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.UserDTO;
import com.clearinghouse.service.ActivateAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AcivateAccountControllerTest {

    @Mock
    private ActivateAccountService activateAccountService;

    @InjectMocks
    private AcivateAccountController acivateAccountController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testActivateAccount_Success() {
        UserDTO userObj = new UserDTO();
        String serviceResponse = "Account activated successfully";

        when(activateAccountService.activateAccount(userObj)).thenReturn(serviceResponse);

        ResponseEntity<String> response = acivateAccountController.activateAccount(userObj);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"value\":\"" + serviceResponse + "\"}", response.getBody());
        verify(activateAccountService).activateAccount(userObj);
    }

    @Test
    void testActivateAccount_Failure() {
        UserDTO userObj = new UserDTO();
        String serviceResponse = "Account activation failed";

        when(activateAccountService.activateAccount(userObj)).thenReturn(serviceResponse);

        ResponseEntity<String> response = acivateAccountController.activateAccount(userObj);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"value\":\"" + serviceResponse + "\"}", response.getBody());
        verify(activateAccountService).activateAccount(userObj);
    }
}