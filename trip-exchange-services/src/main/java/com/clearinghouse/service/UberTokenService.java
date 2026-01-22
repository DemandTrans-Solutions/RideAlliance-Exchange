package com.clearinghouse.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for fetching and caching Uber access tokens.
 */
@Component
@Slf4j
public class UberTokenService {

    private final RestTemplate tokenRestTemplate;
    private final String baseUrl;
    private final String clientId;
    private final String apiKey;

    private volatile Token token = null;

    public UberTokenService(RestTemplate tokenRestTemplate, com.clearinghouse.config.UberProperties props) {
        this.tokenRestTemplate = tokenRestTemplate;
        this.baseUrl = props.getBaseUrl();
        this.clientId = props.getClientId();
        this.apiKey = props.getApiKey();
    }

    public synchronized String getAccessToken() {
        if (token != null && !token.isExpired()) {
            return token.accessToken;
        }

        String uri = org.springframework.web.util.UriComponentsBuilder.fromUriString(baseUrl).path("/auth/apiKeyToken").toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Client-Id", clientId);
        headers.set("X-API-Key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        final int maxAttempts = 3;
        long backoffMs = 200L;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                if (attempt > 1) {
                    log.debug("Retry attempt {} to fetch token (backoff {}ms)", attempt, backoffMs);
                } else {
                    log.debug("Fetching new access token from {}", uri);
                }

                ResponseEntity<TokenResponse> resp = tokenRestTemplate.postForEntity(uri, entity, TokenResponse.class);
                TokenResponse tr = resp != null ? resp.getBody() : null;
                if (resp != null && resp.getStatusCode().is2xxSuccessful() && tr != null && tr.accessToken != null) {
                    long expiresIn = tr.expiresIn != null ? tr.expiresIn : 300L;
                    token = new Token(tr.accessToken, Instant.now().plusSeconds(expiresIn - 10));
                    log.info("Fetched access token, expires in {}s", expiresIn);
                    return token.accessToken;
                } else {
                    log.warn("Unexpected token response (status={} body={}) on attempt {}/{}",
                            resp != null ? resp.getStatusCode() : null, tr, attempt, maxAttempts);
                }
            } catch (RestClientException e) {
                log.warn("Failed to fetch token on attempt {}/{}: {}", attempt, maxAttempts, e.toString());
            }

            if (attempt < maxAttempts) {
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("Token fetch sleep interrupted");
                    break;
                }
                backoffMs *= 2;
            }
        }

        log.error("Failed to obtain access token after {} attempts", maxAttempts);
        return null;
    }

    private static class Token {
        final String accessToken;
        final Instant expiresAt;

        Token(String accessToken, Instant expiresAt) {
            this.accessToken = accessToken;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
