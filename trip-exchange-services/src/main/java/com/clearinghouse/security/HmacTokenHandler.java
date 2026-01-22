package com.clearinghouse.security;

import com.clearinghouse.entity.User;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


@Slf4j
public class HmacTokenHandler {
    private final SecretKeySpec secretKey;
    private static final String HMAC_SHA256 = "HmacSHA256";

    public HmacTokenHandler(byte[] secret) {
        this.secretKey = new SecretKeySpec(secret, HMAC_SHA256);
    }

    public String createToken(User user) {
        try {
            String userData = user.getId() + ":" + user.getUsername() + ":" + user.getExpires();
            String tokenData = userData + ":" + System.currentTimeMillis();

            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(secretKey);

            byte[] hmacData = mac.doFinal(tokenData.getBytes(StandardCharsets.UTF_8));
            String hmac = Base64.getEncoder().encodeToString(hmacData);

            return Base64.getEncoder().encodeToString((tokenData + ":" + hmac).getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error creating HMAC token", e);
            throw new RuntimeException("Error creating HMAC token", e);
        }
    }

    public User parseUserFromToken(String token) {
        try {
            String decodedToken = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decodedToken.split(":");

            if (parts.length != 5) { // userId, username, expires, timestamp, hmac
                log.error("Invalid token format: {}", decodedToken);
                throw new RuntimeException("Invalid token format");
            }

            // Verify HMAC
            String tokenData = String.join(":", parts[0], parts[1], parts[2], parts[3]);
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(secretKey);
            String expectedHmac = Base64.getEncoder().encodeToString(
                    mac.doFinal(tokenData.getBytes(StandardCharsets.UTF_8))
            );

            if (!expectedHmac.equals(parts[4])) {
                log.error("Invalid token signature for token: {}", token);
                throw new RuntimeException("Invalid token signature");
            }

            User user = new User();
            user.setId((int) Long.parseLong(parts[0]));
            user.setUsername(parts[1]);
            user.setExpires(Long.parseLong(parts[2]));

            return user;
        } catch (Exception e) {
            log.error("Error parsing HMAC token", e);
            throw new RuntimeException("Error parsing HMAC token", e);
        }
    }
}