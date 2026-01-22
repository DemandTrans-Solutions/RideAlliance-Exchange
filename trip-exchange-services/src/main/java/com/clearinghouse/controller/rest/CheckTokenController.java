package com.clearinghouse.controller.rest;


import com.clearinghouse.dao.UserDAO;
import com.clearinghouse.dto.UserDTO;
import com.clearinghouse.entity.UserToken;
import com.clearinghouse.service.UserTokenService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = {"/api/checkToken"})
@Slf4j
@AllArgsConstructor
public class CheckTokenController {

    private final UserTokenService userTokenService;

    private final UserDAO userDAO;




    @PostMapping("/{userId}")
    public ResponseEntity<UserToken> checkToken(@PathVariable("userId") int userId) {
        /*
        var user = userDAO.findUserByUserId(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        var userToken = userTokenService.findUserToken(user);
        return ResponseEntity.ok(userToken);
         */
        return null;
    }



}
