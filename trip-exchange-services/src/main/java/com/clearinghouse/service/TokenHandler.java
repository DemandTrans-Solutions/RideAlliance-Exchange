package com.clearinghouse.service;

import com.clearinghouse.entity.User;
import com.clearinghouse.enumentity.AuthanticationCheckStatusForLogin;
import com.clearinghouse.exceptions.UserExpiredRuntimeException;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public final class TokenHandler {
    private static final String HMAC_ALGO = "HmacSHA256";
    private static final String SEPARATOR = ".";
    private static final String SEPARATOR_SPLITTER = "\\.";

    private final Mac hmac;
    private final Key jwtKey;
    private final ObjectMapper objectMapper;

    public TokenHandler(byte[] secretKey) {
        try {
            hmac = Mac.getInstance(HMAC_ALGO);
            hmac.init(new SecretKeySpec(secretKey, HMAC_ALGO));

            // Create a signing key for JWT
            jwtKey = Keys.hmacShaKeyFor(secretKey);

            // Initialize ObjectMapper
            objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
            objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("failed to initialize HMAC: " + e.getMessage(), e);
        }
    }

    public User parseUserFromToken(String jwtToken, String csrfToken, String userType) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtKey)
                    .parseClaimsJws(jwtToken)
                    .getBody();

            User user = new User();
            user.setId(claims.get("userId", Integer.class));
            //user.setUsername(claims.getSubject());
            user.setUsername(claims.get("email", String.class));
            user.setExpires(claims.get("expires", Long.class));
            user.setCsrfToken(claims.get("csrfToken", String.class));
            user.setAuthanticationTypeIsAdapter(claims.get("isAdapter", Boolean.class));
            user.setIsActive(claims.get("isActive", Boolean.class));
            user.setEmail(claims.get("email", String.class));

            // Check expiration
            if (System.currentTimeMillis() / (1000 * 60 * 60 * 24) < user.getExpires()) {
                if (userType.equalsIgnoreCase(AuthanticationCheckStatusForLogin.usertypeRegular.loginUsertypeValue())
                        && !csrfToken.equals(user.getCsrfToken())) {
                    log.warn("CSRF token validation failed");
                    return null;
                }
                return user;
            } else {
                throw new UserExpiredRuntimeException("User Expired");

            }
        } catch (Exception e) {
            log.error("Error parsing JWT token: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Parse a User object from a JWT token
     *
     * @param jwt The JWT token to parse
     * @return User object if token is valid, null otherwise
     */
    public User parseUserFromAPIKeyToken(String jwt) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtKey)
                    .parseClaimsJws(jwt)
                    .getBody();

            // Extract user data from claims
            User user = new User();
            user.setId(claims.get("userId", Integer.class));
            user.setUsername(claims.getSubject());
            user.setExpires(claims.get("expires", Long.class));
            user.setCsrfToken(claims.get("csrfToken", String.class));
            user.setAuthanticationTypeIsAdapter(claims.get("isAdapter", Boolean.class));
            user.setIsActive(claims.get("isActive", Boolean.class));
            user.setEmail(claims.get("email", String.class));

            // Check expiration
            if (System.currentTimeMillis() / (1000 * 60 * 60 * 24) < user.getExpires()) {
                return user;
            } else {
                throw new UserExpiredRuntimeException("User Expired");
            }

        } catch (Exception e) {
            log.error("Error parsing JWT token: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Create a JWT token for a user
     *
     * @param user The user to create token for
     * @return The JWT token
     */
    public String createTokenForUser(User user) {
        log.debug("Creating token for user: {}", user.toString());

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("expires", user.getExpires());
        claims.put("csrfToken", user.getCsrfToken());
        claims.put("isAdapter", user.isAuthanticationTypeIsAdapter());
        claims.put("isActive", user.isActive());

        // Calculate expiration time
        long expirationTimeMillis = user.getExpires() * 24 * 60 * 60 * 1000L; // Convert days to milliseconds

        JwtBuilder builder = Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(expirationTimeMillis))
                .setClaims(claims)
                .signWith(jwtKey);

        return builder.compact();
    }

    private User fromJSON(final byte[] userBytes) {
        try {
            User user = objectMapper.readValue(new ByteArrayInputStream(userBytes), User.class);
            return user;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] toJSON(User user) {
        try {
            byte[] byteArr = objectMapper.writeValueAsBytes(user);
            return byteArr;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private String toBase64(byte[] content) {
        return DatatypeConverter.printBase64Binary(content);
    }

    private byte[] fromBase64(String content) {
        return DatatypeConverter.parseBase64Binary(content);
    }

    private synchronized byte[] createHmac(byte[] content) {
        return hmac.doFinal(content);
    }
}
