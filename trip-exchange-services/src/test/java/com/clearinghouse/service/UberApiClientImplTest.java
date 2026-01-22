package com.clearinghouse.service;

import com.clearinghouse.config.UberProperties;
// ...existing code...
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
// ...existing code...
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class UberApiClientImplTest {

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper = new ObjectMapper();
    private UberProperties props;

    @BeforeEach
    void setUp() {
    restTemplate = new RestTemplate();
    // separate RestTemplate for token fetch to avoid interceptor recursion in production; tests will mock it
    // so token fetches can be verified independently
    // note: we do not register the interceptor on tokenRestTemplate
        
        props = new UberProperties();
        props.setBaseUrl("https://example.com");
        props.setClientId("cid");
        props.setApiKey("key");
    }

    @Test
    void getGuestTripEstimates_fetchesToken_thenCallsEndpoint() {
        // Use MockRestServiceServer to simulate remote service responses
    RestTemplate tokenRestTemplate = new RestTemplate();
    org.springframework.test.web.client.MockRestServiceServer serverForToken = org.springframework.test.web.client.MockRestServiceServer.createServer(tokenRestTemplate);
    org.springframework.test.web.client.MockRestServiceServer server = org.springframework.test.web.client.MockRestServiceServer.createServer(restTemplate);

        // token response JSON
        String tokenJson = "{\"access_token\": \"tk\", \"expires_in\": 3600}";
    serverForToken.expect(org.springframework.test.web.client.ExpectedCount.once(),
            org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo("https://example.com/auth/apiKeyToken"))
        .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.method(org.springframework.http.HttpMethod.POST))
        .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess(tokenJson, org.springframework.http.MediaType.APPLICATION_JSON));

        // estimate response JSON (simple shape matching the record fields)
        String estimatesJson = "{ \"etasUnavailable\": false, \"faresUnavailable\": false, \"productEstimates\": [] }";
    server.expect(org.springframework.test.web.client.ExpectedCount.once(),
            org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo("https://example.com/api/v1/uber/getOnDemandTripEstimates"))
        .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.method(org.springframework.http.HttpMethod.POST))
        .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess(estimatesJson, org.springframework.http.MediaType.APPLICATION_JSON));

    UberApiClientImpl client = new UberApiClientImpl(restTemplate, props, tokenRestTemplate, objectMapper);

        com.clearinghouse.dto.UberOnDemandEstimateRequest req = com.clearinghouse.dto.UberOnDemandEstimateRequest.builder().pickup(null).dropoff(null).build();
    com.clearinghouse.dto.UberTripEstimatesResponse resp = client.getOnDemandTripEstimates(req);
        assertNotNull(resp);

    serverForToken.verify();
    server.verify();
    }
}
