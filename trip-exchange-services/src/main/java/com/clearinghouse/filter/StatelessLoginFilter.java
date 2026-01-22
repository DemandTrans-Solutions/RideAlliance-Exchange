package com.clearinghouse.filter;

import com.clearinghouse.dao.UserDAO;
import com.clearinghouse.entity.User;
import com.clearinghouse.entity.UserAuthentication;
import com.clearinghouse.service.CustomUserDetailsService;
import com.clearinghouse.service.TokenAuthenticationService;
import com.clearinghouse.service.AuthResponseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class StatelessLoginFilter extends AbstractAuthenticationProcessingFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StatelessLoginFilter.class);
//this is for taking login details of the attempted login

    LinkedHashMap<String, String> userForMaximumLoginAttemptsCheck = new LinkedHashMap<>();

    private final TokenAuthenticationService tokenAuthenticationService;
    private final CustomUserDetailsService customUserDetailsService;
    private final AuthResponseService authResponseService;
    private final UserDAO userDAO;

    public StatelessLoginFilter(String urlMapping, TokenAuthenticationService tokenAuthenticationService,
                                CustomUserDetailsService customUserDetailsService, AuthResponseService authResponseService, 
                                UserDAO userDAO, AuthenticationManager authManager) {
        // AntPathRequestMatcher has been deprecated; pass an inline RequestMatcher to the superclass
        super((request) -> {
            AntPathMatcher antPathMatcher = new AntPathMatcher();
            String path = request.getServletPath();
            if (path == null || path.isEmpty()) {
                path = request.getRequestURI();
            }
            return antPathMatcher.match(urlMapping, path);
        });
        this.customUserDetailsService = customUserDetailsService;
        this.tokenAuthenticationService = tokenAuthenticationService;
        this.authResponseService = authResponseService;
        this.userDAO = userDAO;
        setAuthenticationManager(authManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        // Check if this is HMAC key authentication
        String hmacKey = request.getParameter("key");
        if (hmacKey != null && !hmacKey.trim().isEmpty()) {
            return attemptHmacAuthentication(hmacKey);
        }

        // Default to username/password authentication
        return attemptUsernamePasswordAuthentication(request);
    }

    private Authentication attemptUsernamePasswordAuthentication(HttpServletRequest request) 
            throws IOException, AuthenticationException {
        //follwing LinkedHashMap is used because of @jsonIgnore annotaion of password in user model. 
//        if(request.getInputStream()!=null){
        @SuppressWarnings("unchecked") final LinkedHashMap<String, String> user = new ObjectMapper().readValue(request.getInputStream(), LinkedHashMap.class);

        userForMaximumLoginAttemptsCheck = user;
        final UsernamePasswordAuthenticationToken loginToken = new UsernamePasswordAuthenticationToken(
                user.get("username"), user.get("password"));

        //code for the cheking user if exists and lastFiledattemptdate !=currentdate
        //if yes update user eexpired flag to false
        LocalDate localDate = LocalDate.now();
        Date currentDate = java.sql.Date.valueOf(localDate);
        String username = userForMaximumLoginAttemptsCheck.get("username");

        if (customUserDetailsService.checkUseExist(username)) {
            User attemptedUser = customUserDetailsService.getUserByUsername(username);
            if (attemptedUser.getLastFailedAttemptDate() != null) {
                int result = attemptedUser.getLastFailedAttemptDate().compareTo(currentDate);
                if (result != 0) {
                    attemptedUser.setAccountExpired(false);
                    customUserDetailsService.updateUser(attemptedUser);
                }
            }

        }

        return getAuthenticationManager().authenticate(loginToken);
    }

    private Authentication attemptHmacAuthentication(String hmacKey) throws AuthenticationException {
        try {
            // Use the existing HMAC token handler from TokenAuthenticationService
            User user = tokenAuthenticationService.getHmacTokenHandler().parseUserFromToken(hmacKey);
            
            if (user != null && user.getId() > 0) {
                // Load the full user details from database
                User dbUser = userDAO.findUserByUserId(user.getId());
                
                if (dbUser == null) {
                    throw new BadCredentialsException("User not found");
                }
                
                if (!dbUser.isActive()) {
                    throw new DisabledException("User is disabled");
                }
                
                if (!dbUser.isAuthanticationTypeIsAdapter()) {
                    throw new BadCredentialsException("User is not authorized for HMAC authentication");
                }
                
                // Create authentication token for the validated user
                // Return a UserAuthentication with the database user (which has full authority details)
                return new UserAuthentication(dbUser);
            } else {
                throw new BadCredentialsException("Invalid HMAC token");
            }
        } catch (Exception e) {
            log.error("HMAC authentication failed: {}", e.getMessage());
            throw new BadCredentialsException("Invalid HMAC token", e);
        }

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authentication) throws IOException, ServletException {

        // Check if this was HMAC authentication
        boolean isHmacAuthentication = request.getParameter("key") != null;
        
        // For HMAC authentication, we need to get the user details differently
        final User authenticatedUser;
        if (isHmacAuthentication) {
            // For HMAC authentication, we already have the full user details from the database
            authenticatedUser = ((UserAuthentication) authentication).getDetails();
        } else {
            // For username/password authentication, lookup the complete User object from the database
            authenticatedUser = customUserDetailsService.loadUserByUsername(authentication.getName());
        }
        
        final UserAuthentication userAuthentication = new UserAuthentication(authenticatedUser);

        // Add the authentication to the Security context
        SecurityContextHolder.getContext().setAuthentication(userAuthentication);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        if (isHmacAuthentication) {
            // For HMAC authentication, provide a simplified response indicating HMAC-per-request usage
            writeHmacAuthResponse(response, authenticatedUser);
        } else {
            // For username/password authentication, provide full JWT token response for browser clients
            tokenAuthenticationService.addAuthentication(response, userAuthentication);
            
            //set failed attempts to 0 and lastFailed attempt to null
            User userObjTobeupdated = customUserDetailsService.getUserByUsername(authenticatedUser.getUsername());
            userObjTobeupdated.setFailedAttempts(0);
            customUserDetailsService.updateUser(userObjTobeupdated);
            
            // Delegate to AuthResponseService which will build the response without mutating the entity
            authResponseService.writeAuthResponse(response, authenticatedUser);
        }
    }

    private void writeHmacAuthResponse(HttpServletResponse response, User authenticatedUser) throws IOException {
        response.getWriter().write(String.format(
            "{\n" +
            "  \"id\": %d,\n" +
            "  \"username\": \"%s\",\n" +
            "  \"name\": \"%s\",\n" +
            "  \"email\": \"%s\",\n" +
            "  \"authType\": \"HMAC\",\n" +
            "  \"message\": \"Authentication successful. Use your HMAC key as '?key=YOUR_KEY' parameter for all subsequent API requests.\",\n" +
            "  \"authorities\": %s\n" +
            "}",
            authenticatedUser.getId(),
            authenticatedUser.getUsername(),
            authenticatedUser.getName() != null ? authenticatedUser.getName() : "",
            authenticatedUser.getEmail() != null ? authenticatedUser.getEmail() : "",
            authenticatedUser.getAuthorities().toString()
        ));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        //There are total 4 exceptions related to 4 flags and InternalAuthenticationServiceException is thrown with different message
        //Order of exception is as follows
        //Locked - Disabled - account expired - credentials expired
        //Also one more exception which can be handled BadCredentialsException & UsernameNotFoundException
        //below one example is given incase different status code needs to be sent for exception
        String str = exception.getMessage(); // this gives different msg for each exception
//        if (exception instanceof InternalAuthenticationServiceException) {
//            response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
//        } else {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        }

        if (str.equalsIgnoreCase("User account is locked")) {
            response.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);//EXPECTATION_FAILED);
        } else if (str.equalsIgnoreCase("User is disabled")) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);//TEMPORARY_REDIRECT);
        } else if (str.equalsIgnoreCase("User account has expired")) {
            response.setStatus(HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED);//NOT_ACCEPTABLE);
        } else if (str.equalsIgnoreCase("User credentials have expired")) {
            response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);//MODIFIED);
        } else if (str.equalsIgnoreCase("Bad credentials")) {

            //take user name and check if it exists 
            String user = userForMaximumLoginAttemptsCheck.get("username");

//            User newuser = customUserDetailsService.getUserByUsername(user);
//            boolean b = usernameExistCheckingDAO.FindUserByUsername(user);
            if (customUserDetailsService.checkUseExist(user)) {
                //fetch that and update failed attempts count and add date for that
                User attemptedUser = customUserDetailsService.getUserByUsername(user);

                //check if count>4 and date is = current date
//                update the count and date
                int failedAttempts = attemptedUser.getFailedAttempts();
                failedAttempts = failedAttempts + 1;
                attemptedUser.setFailedAttempts(failedAttempts);

                LocalDate localDate = LocalDate.now();
                Date currentDate = java.sql.Date.valueOf(localDate);
                if (failedAttempts == 1) {

                    attemptedUser.setLastFailedAttemptDate(currentDate);
                }

                if ((attemptedUser.getFailedAttempts() > 3)) {
                    int result = attemptedUser.getLastFailedAttemptDate().compareTo(currentDate);
                    if (result == 0) {
                        //add code for account expiry flag
                        attemptedUser.setAccountExpired(true);
                        //send mail to user
                        customUserDetailsService.updateUser(attemptedUser);

                    } else {
                        attemptedUser.setFailedAttempts(1);
                        attemptedUser.setLastFailedAttemptDate(currentDate);
//                        attemptedUser.setAccountExpired(false);
                        customUserDetailsService.updateUser(attemptedUser);
                    }

                } else {
                    customUserDetailsService.updateUser(attemptedUser);
                }
            }

//send mail to  that  uesr account is locked duet TO MAXIMUM attempts contact to admin
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);//if username or password or pin is wrong

        } else {
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);//NOT_FOUND);
        }

        log.debug(
                "------------------------" + str);

    }
//    SC_GATEWAY_TIMEOUT=User account is locked-->504
//            SC_SERVICE_UNAVAILABLE=User is disabled--->503
//                    SC_HTTP_VERSION_NOT_SUPPORTED=User account has expired--->505
//                            SC_NOT_IMPLEMENTED=User credentials have expired--->501
//                                 SC_BAD_GATEWAY  = Bad credentials--->502
//                                         SC_NOT_ACCEPTABLE=not found--->406

}
