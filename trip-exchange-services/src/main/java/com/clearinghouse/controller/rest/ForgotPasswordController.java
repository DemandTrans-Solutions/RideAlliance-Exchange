/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.ForgotPasswordDTO;
import com.clearinghouse.service.ForgotPasswordService;
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
 * @author ChaitanyaP
 */
@RestController
@RequestMapping(value = {"api/forgotCrendential"})
@Slf4j
@AllArgsConstructor
public class ForgotPasswordController {


    private final ForgotPasswordService forgotPasswordService;


    @RequestMapping(value = {"/sendMail"}, method = RequestMethod.POST)//GET)
    public ResponseEntity<Boolean> sendMail(@RequestBody ForgotPasswordDTO forgotPasswordDTOObj) {
        log.debug(String.valueOf(forgotPasswordDTOObj));
        boolean result = forgotPasswordService.sendTempPassword(forgotPasswordDTOObj);
        return new ResponseEntity<>(result, HttpStatus.OK);

    }

    @RequestMapping(value = {"/resetForgotPassword"}, method = RequestMethod.POST)
    public ResponseEntity<Integer> resetForgotPassword(@RequestBody ForgotPasswordDTO forgotPasswordDTOObj) {
        log.debug(String.valueOf(forgotPasswordDTOObj));
        Integer result = forgotPasswordService.resetForgotPassword(forgotPasswordDTOObj);
        return new ResponseEntity<>(result, HttpStatus.OK);
        // return new ResponseEntity<> (setPasswordService.updatePassword(userObj), HttpStatus.OK);
    }

}
