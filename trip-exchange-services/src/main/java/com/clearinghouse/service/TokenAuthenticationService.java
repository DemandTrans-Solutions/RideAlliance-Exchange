package com.clearinghouse.service;


import com.clearinghouse.dao.UserDAO;
import com.clearinghouse.entity.User;
import com.clearinghouse.entity.UserAuthentication;
import com.clearinghouse.entity.UserAuthority;
import com.clearinghouse.entity.UserToken;
import com.clearinghouse.enumentity.AuthanticationCheckStatusForLogin;
import com.clearinghouse.security.HmacTokenHandler;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public class TokenAuthenticationService {

    private static final String CSRF_HEADER_NAME = "X-AUTH-TOKEN";
    private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    private static final String JWT_COOKIE_NAME = "JWT-TOKEN";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    //    private static final long EXPIRY_DAYS_MILLISECONDS = 284012568; // One day..//sample for 90 years ..90 * 365 * 1000 * 60 * 60 * 24 * 1 ..2,840,125,680,000
//    private static final long EXPIRY_IN_DAYS = 365 * 90;//90 years
    private static final long EXPIRY_IN_DAYS = 1; // 1 day
    private static final long EXPIRY_IN_DAYS_FOR_ADAPTER = 365 * 90; // 1 day

    private final TokenHandler tokenHandler;
    private final HmacTokenHandler hmacTokenHandler;
    private final UserDAO userDAO;
    private final UserTokenService userTokenService;


    public String getUUID() {
        return UUID.randomUUID().toString();
    }

    public String addAuthentication(HttpServletResponse response, UserAuthentication authentication) {

        final User user = authentication.getDetails();
        /*add expiry*/
        user.setExpires((System.currentTimeMillis() / (1000 * 60 * 60 * 24)) + EXPIRY_IN_DAYS_FOR_ADAPTER);

        /*add csrf token*/
        /*called above method to get UUID*/
        String csrfToken = getUUID();
        user.setCsrfToken(csrfToken);

        // Configure XSRF cookie for cross-subdomain access
        Cookie csrfCookie = new Cookie(CSRF_COOKIE_NAME, csrfToken);
        csrfCookie.setPath("/");
        // Don't set domain for localhost to allow local development
        String cookieDomain = determineCookieDomain();
        if (cookieDomain != null) {
            csrfCookie.setDomain(cookieDomain);
        }
        csrfCookie.setSecure(isSecureEnvironment()); // Only secure in production
        if (isSecureEnvironment()) {
            csrfCookie.setAttribute("SameSite", "None"); // Only for cross-site in production
        }
        response.addCookie(csrfCookie);

        String jwtTokenForAdapter = tokenHandler.createTokenForUser(user);

        String hmacToken = null;
        try {
            hmacToken = hmacTokenHandler.createToken(user);
        } catch (Exception e) {
            log.error("Error creating HMAC token", e);
        }
        var existingUserToken = userTokenService.findUserTokenBiUserId(user.getId());

        //--------------------------------
        if (existingUserToken == null) {
            UserToken userToken = new UserToken();
            /*set Usertoken*/
            userToken.setUser(user);
            userToken.setHmacToken(hmacToken);
            userToken.setUserToken(jwtTokenForAdapter);
            userTokenService.createUserToken(userToken);
        } else if (existingUserToken.getHmacToken() == null && hmacToken != null) {
            //create a HMAC token for the existing user
            existingUserToken.setHmacToken(hmacToken);
            userTokenService.updateUserToken(existingUserToken);
        }
        //-----------------------------------

        user.setExpires((System.currentTimeMillis() / (1000 * 60 * 60 * 24)) + EXPIRY_IN_DAYS);
        String jwtToken = tokenHandler.createTokenForUser(user);
        Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, jwtToken);
        jwtCookie.setPath("/");
        // Don't set domain for localhost to allow local development
        if (cookieDomain != null) {
            jwtCookie.setDomain(cookieDomain);
        }
        jwtCookie.setSecure(isSecureEnvironment()); // Only secure in production
        jwtCookie.setHttpOnly(true);
        if (isSecureEnvironment()) {
            jwtCookie.setAttribute("SameSite", "None"); // Only for cross-site in production
        }
        response.addCookie(jwtCookie);

        return jwtToken;

    }

    private String determineCookieDomain() {
        // Get the current request to check the host
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String host = request.getHeader("Host");
            if (host != null) {
                // For localhost, don't set domain to allow cookie access
                if (host.contains("localhost") || host.contains("127.0.0.1")) {
                    return null; // No domain restriction for localhost
                }
                // For demandtrans-apis.com subdomains
                if (host.contains("demandtrans-apis.com")) {
                    return "demandtrans-apis.com";
                }
                // For other development domains, you can add more conditions here
            }
        }

        // Default to production domain if we can't determine
        return "demandtrans-apis.com";
    }

    private boolean isSecureEnvironment() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String host = request.getHeader("Host");
            // Only use secure settings for production domains
            return host != null && !host.contains("localhost") && !host.contains("127.0.0.1");
        }
        return true; // Default to secure
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            return ((org.springframework.web.context.request.ServletRequestAttributes)
                    org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes())
                    .getRequest();
        } catch (Exception e) {
            log.warn("Could not get current request: " + e.getMessage());
            return null;
        }
    }

    public Authentication getAuthentication(HttpServletRequest request) {
        //uncomment below to see all cookies
        //Cookie[] cokkies = request.getCookies();

        boolean isAdapterUser = false;
        String userType = AuthanticationCheckStatusForLogin.usertypeUnknown.loginUsertypeValue();
        String csrfToken = null;
        String jwtToken = null;
        String hmacToken = request.getParameter("key");

        // Check for Authorization header with Bearer token
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            jwtToken = authHeader.substring(BEARER_PREFIX.length());
            userType = AuthanticationCheckStatusForLogin.usertypeRegular.loginUsertypeValue();

            // For Bearer tokens, attempt authentication without CSRF token
            if (jwtToken != null && !jwtToken.isEmpty()) {
                User user = tokenHandler.parseUserFromAPIKeyToken(jwtToken);
                if (user != null && user.getId() > 0) {
                    try {
                        var dbUser = userDAO.findUserByUserId(user.getId());
                        if (dbUser != null && dbUser.isActive() && dbUser.getAuthorities() != null && !dbUser.getAuthorities().isEmpty()) {
                            List<UserAuthority> authorityFromDatabase = new ArrayList<>();
                            // Taking only zero'th authority to frontend.converting to list
                            for (UserAuthority userAuthority : dbUser.getAuthorities()) {
                                authorityFromDatabase.add(userAuthority);
                            }
                            Set<UserAuthority> finalSetForUserAuthority = new HashSet<>();
                            finalSetForUserAuthority.add(authorityFromDatabase.get(0));
                            user.setAuthorities(finalSetForUserAuthority);
                            return new UserAuthentication(user);
                        }
                    } catch (Exception ex) {
                        log.error("Error loading DB Claims for JWT Bearer token: " + ex.getMessage(), ex);
                    }
                }
            }
        }

        if (hmacToken != null) {
            User user = null;
            // use the API flow
            user = hmacTokenHandler.parseUserFromToken(hmacToken);
            if (user != null && user.getId() > 0) {
                userType = AuthanticationCheckStatusForLogin.usertypeAdpater.loginUsertypeValue();
                var dbUser = userDAO.findUserByUserId(user.getId());
                if (dbUser != null && dbUser.isActive() && dbUser.isAuthanticationTypeIsAdapter()) {

                    List<UserAuthority> authorityFromDatabase = new ArrayList<>();
                    // Taking only zero'th authority to frontend.converting to list
                    for (UserAuthority userAuthority : dbUser.getAuthorities()) {
                        authorityFromDatabase.add(userAuthority);
                    }
                    Set<UserAuthority> finalSetForUserAuthority = new HashSet<>();
                    finalSetForUserAuthority.add(authorityFromDatabase.get(0));
                    user.setAuthorities(finalSetForUserAuthority);


                    // Direct API access is allowed, and the user account is active
                    // Authenticate the user
                    return new UserAuthentication(user);
                }
            }
            /*
            else {
                user = tokenHandler.parseUserFromAPIKeyToken(jwtToken);
            }

            if ( user != null && user.getId() > 0 ) {

            //fetch user from db for the checking flag
            User userFromDB = userDAO.findUserByUserId(user.getId());
            if (userFromDB != null) {
                // check is user as active from token and database
                if (userFromDB.isActive()) {
                    isAdapterUser = userFromDB.isAuthanticationTypeIsAdapter();
                    // checking if adapter user is or not
                    if (isAdapterUser) {
                        userType = AuthanticationCheckStatusForLogin.usertypeAdpater.loginUsertypeValue();
                    }
                }
            } */
        } else {
            Cookie jwtCookie = WebUtils.getCookie(request, JWT_COOKIE_NAME);
            if (jwtCookie != null) {
                jwtToken = jwtCookie.getValue();
            }
            csrfToken = request.getHeader(CSRF_HEADER_NAME);
            if (jwtToken != null && csrfToken != null && !jwtToken.isEmpty() && !csrfToken.isEmpty()) {
                userType = AuthanticationCheckStatusForLogin.usertypeRegular.loginUsertypeValue();
            }
        }
        /*if requested user is not UNKNOWN */
        if (!userType.equalsIgnoreCase(AuthanticationCheckStatusForLogin.usertypeUnknown.loginUsertypeValue())) {
            // if there is no JWTTokem this is a direct API call
            if (jwtToken == null) {
                Cookie jwtCookie = WebUtils.getCookie(request, JWT_COOKIE_NAME);
                if (jwtCookie != null) {
                    jwtToken = jwtCookie.getValue();
                }
            }

            User user = tokenHandler.parseUserFromToken(jwtToken, csrfToken, userType);
            if (user != null && user.getId() > 0) {
                // setup authorities
                try {
                    var dbUser = userDAO.findUserByUserId(user.getId());
                    if (dbUser != null && dbUser.isActive() && dbUser.getAuthorities() != null && !dbUser.getAuthorities().isEmpty()) {
                        List<UserAuthority> authorityFromDatabase = new ArrayList<>();
                        // Taking only zero'th authority to frontend.converting to list
                        for (UserAuthority userAuthority : dbUser.getAuthorities()) {
                            authorityFromDatabase.add(userAuthority);
                        }
                        Set<UserAuthority> finalSetForUserAuthority = new HashSet<>();
                        finalSetForUserAuthority.add(authorityFromDatabase.get(0));
                        user.setAuthorities(finalSetForUserAuthority);
                        return new UserAuthentication(user);
                    }
                } catch (Exception ex) {
                    log.error("Error loading DB Claims for JWT token: " + ex.getMessage(), ex);
                }
            }
        }
        log.warn("User not found or token expired");
        return null;
    }

    /*newly added method for AdpaterUser to get tokens*/
    public String getNewTokenForUser(User user) {
        return tokenHandler.createTokenForUser(user);
    }

    //    /*newly added method for AdpaterUser to get tokens*/
//    public boolean checkTokenIsValid(String newJWTToken) {
//        return tokenHandler.checkToeknValid(newJWTToken);
//
//    }
//    /*newly added method for AdpaterUser to get tokens*/
//    public User parseUserFromNewToken(String token) {
//        return tokenHandler.parseUserFromAPIKeyToken(token);
//    }
    public User newMethodToParseuser(String jwtToken, String csrfToken, String userType) {
        return tokenHandler.parseUserFromToken(jwtToken, csrfToken, userType);
    }

    public String createHMACTokenForUser(User user) {
        return hmacTokenHandler.createToken(user);
    }

    public HmacTokenHandler getHmacTokenHandler() {
        return hmacTokenHandler;
    }

    public String createJWSTokenForUser(User user) {
        return tokenHandler.createTokenForUser(user);
    }
}
