package com.clearinghouse.service;

import com.clearinghouse.entity.User;
import com.clearinghouse.entity.UserAuthentication;
import com.clearinghouse.entity.UserAuthority;
import com.clearinghouse.exceptions.SpringAppRuntimeException;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Service
public class AuthResponseService {

    private static final Logger log = LoggerFactory.getLogger(AuthResponseService.class);

    private final TokenAuthenticationService tokenAuthenticationService;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthResponseService(TokenAuthenticationService tokenAuthenticationService, CustomUserDetailsService customUserDetailsService) {
        this.tokenAuthenticationService = tokenAuthenticationService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Transactional
    public String writeAuthResponse(HttpServletResponse response, User authenticatedUser) {
        try {
            final UserAuthentication userAuthentication = new UserAuthentication(authenticatedUser);

            var jwtToken = tokenAuthenticationService.addAuthentication(response, userAuthentication);

            // Set context
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(userAuthentication);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");

            // reset failed attempts
            User userObjTobeupdated = customUserDetailsService.getUserByUsername(authenticatedUser.getUsername());
            userObjTobeupdated.setFailedAttempts(0);
            customUserDetailsService.updateUser(userObjTobeupdated);

            ObjectMapper mapper = new ObjectMapper();

            // Build a response map instead of mutating the JPA-managed entity (avoids orphanRemoval issues)
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("id", authenticatedUser.getId());
            resp.put("username", authenticatedUser.getUsername());
            resp.put("name", authenticatedUser.getName());
            // include email explicitly (entity field may be @JsonIgnore)
            try {
                resp.put("email", authenticatedUser.getEmail());
            } catch (Exception ignored) {
                // if getter not available, skip
            }
            resp.put("jobTitle", authenticatedUser.getJobTitle());
            resp.put("failedAttempts", authenticatedUser.getFailedAttempts());
            resp.put("JWTToken", jwtToken);
            resp.put("csrfToken", " ");
            resp.put("responseDataForUI", authenticatedUser.getResponseDataForUI());

            // Serialize only the first authority (preserves previous behavior) as an array of objects {"authority":"..."}
            Set<UserAuthority> authorities = authenticatedUser.getAuthorities();
            if (authorities != null && !authorities.isEmpty()) {
                UserAuthority first = authorities.iterator().next();
                Map<String, String> a = new LinkedHashMap<>();
                a.put("authority", first.getAuthority());
                List<Map<String, String>> authList = new ArrayList<>();
                authList.add(a);
                resp.put("authorities", authList);
            } else {
                resp.put("authorities", Collections.emptyList());
            }

            String jsonString = mapper.writeValueAsString(resp);

            try {
                response.getWriter().write(jsonString);
            } catch (IOException e) {
                log.error("IOException in populateWithJSON: {}", e.getMessage(), e);
                throw new SpringAppRuntimeException("IOException in populateWithJSON", e);
            }

            return jsonString;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new SpringAppRuntimeException("Failed to write auth response", e);
        }
    }
}
