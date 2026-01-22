/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.UserDTO;
import com.clearinghouse.service.ActivateAccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author chaitanyaP
 */
@RestController
@RequestMapping(value = {"api/activateAccount"})
@Slf4j
@AllArgsConstructor
public class AcivateAccountController {


    private final ActivateAccountService activateAccountService;


    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> activateAccount(@RequestBody UserDTO userObj) {
        log.debug(String.valueOf(userObj));
//        boolean result = activateAccountService.activateAccount(userObj);
        String result = activateAccountService.activateAccount(userObj);

        String finalResult = "{\"value\":\"" + result + "\"}";
        return new ResponseEntity<>(finalResult, HttpStatus.OK);

        //log.debug("inside controller ");
        //return new ResponseEntity<> (true, HttpStatus.OK);
    }

}
