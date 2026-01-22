package com.clearinghouse.mcp;

import com.clearinghouse.mcp.dto.ModelInvokeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(McpController.class)
@TestPropertySource(properties = {
        "aws.bedrock.allowedModels[0].modelId=amazon.titan-text-express-v1",
        "mcp.quota.maxRequestsPerMinute=64",
        "mcp.quota.maxTokensPerRequest=2048"
})
class McpControllerTest {

    /*
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BedrockService bedrockService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        Mockito.when(bedrockService.invokeModel(Mockito.anyString(), Mockito.anyString())).thenReturn("mocked output");
    }

    @Test
    void testInvokeModel_Success() throws Exception {
        ModelInvokeRequest req = new ModelInvokeRequest();
        req.setModelId("amazon.titan-text-express-v1");
        req.setInputPayload("{\"prompt\":\"Hello\"}");
        mockMvc.perform(post("/api/mcp/invoke")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.output").value("mocked output"));
    }

    @Test
    void testInvokeModel_ModelNotAllowed() throws Exception {
        ModelInvokeRequest req = new ModelInvokeRequest();
        req.setModelId("not.allowed.model");
        req.setInputPayload("{\"prompt\":\"Hello\"}");
        mockMvc.perform(post("/api/mcp/invoke")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error").value("Model not allowed"));
    }

    @Test
    void testInvokeModel_InputPayloadTooLarge() throws Exception {
        ModelInvokeRequest req = new ModelInvokeRequest();
        req.setModelId("amazon.titan-text-express-v1");
        req.setInputPayload("a".repeat(3000));
        mockMvc.perform(post("/api/mcp/invoke")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error").value("Input payload too large"));
    }

    @Test
    void testInvokeModel_RateLimitExceeded() throws Exception {
        ModelInvokeRequest req = new ModelInvokeRequest();
        req.setModelId("amazon.titan-text-express-v1");
        req.setInputPayload("{\"prompt\":\"Hello\"}");
        for (int i = 0; i < 65; i++) {
            mockMvc.perform(post("/api/mcp/invoke")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)));
        }
        ResultActions result = mockMvc.perform(post("/api/mcp/invoke")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));
        result.andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error").value("Rate limit exceeded"));
    }

    @Test
    void testInvokeModel_MissingModelId() throws Exception {
        ModelInvokeRequest req = new ModelInvokeRequest();
        req.setInputPayload("{\"prompt\":\"Hello\"}");
        mockMvc.perform(post("/api/mcp/invoke")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void testInvokeModel_EmptyInputPayload() throws Exception {
        ModelInvokeRequest req = new ModelInvokeRequest();
        req.setModelId("amazon.titan-text-express-v1");
        req.setInputPayload("");
        mockMvc.perform(post("/api/mcp/invoke")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }*/
}
