package com.clearinghouse.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Interceptor that adds Authorization: Bearer <token> header to outgoing requests.
 * It relies on a TokenProvider to supply a valid token.
 */

@Slf4j
public class UberAuthInterceptor implements ClientHttpRequestInterceptor {

    public interface TokenProvider {
        String getToken();
    }

    private final TokenProvider tokenProvider;

    public UberAuthInterceptor(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String token = tokenProvider.getToken();
        if (token != null) {
            request.getHeaders().setBearerAuth(token);
        }

        // Log request
        log.info("=== Uber API Request ===");
        log.info("URI: {}", request.getURI());
        log.info("Method: {}", request.getMethod());
        log.info("Headers: {}", request.getHeaders());
        if (body != null && body.length > 0) {
            String requestBody = new String(body, StandardCharsets.UTF_8);
            log.info("Request Body: {}", requestBody);
        }

        ClientHttpResponse response = execution.execute(request, body);

        // Log response
        log.info("=== Uber API Response ===");
        log.info("Status Code: {}", response.getStatusCode());
        log.info("Headers: {}", response.getHeaders());

        // Read and log response body
        byte[] responseBody = StreamUtils.copyToByteArray(response.getBody());
        if (responseBody.length > 0) {
            String responseBodyString = new String(responseBody, StandardCharsets.UTF_8);
            log.info("Response Body: {}", responseBodyString);
        }

        // Return wrapped response with buffered body
        return new BufferedClientHttpResponse(response, responseBody);
    }

    private static class BufferedClientHttpResponse implements ClientHttpResponse {
        private final ClientHttpResponse response;
        private final byte[] body;

        public BufferedClientHttpResponse(ClientHttpResponse response, byte[] body) {
            this.response = response;
            this.body = body;
        }

        @Override
        public InputStream getBody() throws IOException {
            return new ByteArrayInputStream(body);
        }

        @Override
        public org.springframework.http.HttpStatusCode getStatusCode() throws IOException {
            return response.getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return response.getStatusText();
        }

        @Override
        public void close() {
            response.close();
        }

        @Override
        public org.springframework.http.HttpHeaders getHeaders() {
            return response.getHeaders();
        }
    }
}
