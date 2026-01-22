package com.clearinghouse.controller.rest;


import com.clearinghouse.entity.User;
import com.clearinghouse.service.AuthResponseService;
import com.clearinghouse.service.CustomUserDetailsService;
import com.clearinghouse.service.SharedEncryptionService;
import com.clearinghouse.web.dto.LoginValidateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping(value = {"api/login/validateUser"})
@AllArgsConstructor
@Slf4j
public class LoginValidateController {



    private final SharedEncryptionService sharedEncryptionService;
    private final CustomUserDetailsService customUserDetailsService;
    private final AuthResponseService authResponseService;

    private final ObjectMapper mapper = new ObjectMapper();


    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> validate(@RequestBody LoginValidateRequest req, HttpServletResponse response) {
        if (req == null || !StringUtils.hasText(req.encodedEmailAddress())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("encodedEmailAddress is required");
        }

        String plaintext;
        try {
            plaintext = sharedEncryptionService.decryptToString(req.encodedEmailAddress());
        } catch (IllegalArgumentException e) {
            log.warn("Failed to decrypt encodedEmailAddress: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Invalid encodedEmailAddress");
        }

        String email = null;
        try {
            // Try parse as JSON first {"email":"...","ts":...}
            if (plaintext.trim().startsWith("{") ) {
                JsonNode node = mapper.readTree(plaintext);
                if (node.has("email")) {
                    email = node.get("email").asText();
                }
                if (node.has("ts")) {
                    long ts = node.get("ts").asLong();
                    long now = Instant.now().getEpochSecond();
                    long ttl = sharedEncryptionService.getTtlSeconds();
                    if (Math.abs(now - ts) > ttl) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token expired");
                    }
                }
            } else {
                email = plaintext.trim();
            }
        } catch (Exception e) {
            log.warn("Failed to parse decrypted payload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid decrypted payload");
        }

        if (email == null || !isValidEmail(email)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Decoded value not a valid email");
        }

        // Check user exists
        if (!customUserDetailsService.checkUseExist(email)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unknown user");
        }

        // Load user and delegate to AuthResponseService which writes token & body into response
        try {
            User user = customUserDetailsService.getUserByUsername(email);
            var providerId = user.getProvider() != null ? Integer.toString(user.getProvider().getProviderId()) : null;
            user.setResponseDataForUI(providerId);
            authResponseService.writeAuthResponse(response, user);
            // AuthResponseService already wrote the full JSON into the HttpServletResponse writer.
            // Returning the body as well causes the payload to be written twice. Return an empty 200.
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error while creating auth response: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create auth response");
        }
    }

    private boolean isValidEmail(String email) {
        if (email == null) return false;
        email = email.trim();
        // Simple validation: contains @ and a dot after @
        int at = email.indexOf('@');
        if (at <= 0) return false;
        int dot = email.indexOf('.', at);
        return dot > at + 1 && dot < email.length() - 1;
    }

}
