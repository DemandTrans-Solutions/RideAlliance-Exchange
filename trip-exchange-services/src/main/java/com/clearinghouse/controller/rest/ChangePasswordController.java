/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.controller.rest;

import com.clearinghouse.dto.ChangePasswordDTO;
import com.clearinghouse.service.ChangePasswordService;
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
@Slf4j
@AllArgsConstructor
public class ChangePasswordController {


    private final ChangePasswordService changePasswordService;

    @RequestMapping(value = {"api/changePassword"}, method = RequestMethod.POST)
    public ResponseEntity<Integer> resetPassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        log.debug("Inside ChangePasswordController with DTO: " + changePasswordDTO);
        Integer result = changePasswordService.resetPassword(changePasswordDTO);
        return new ResponseEntity<>(result, HttpStatus.OK);

    }

}
