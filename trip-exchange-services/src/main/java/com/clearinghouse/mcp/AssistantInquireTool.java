package com.clearinghouse.mcp;

import com.clearinghouse.controller.rest.AssistantController;
import com.clearinghouse.dto.ChatQueryRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AssistantInquireTool {
    @Autowired
    private AssistantController assistantController;

    public AssistantInquireTool() {
        log.info("AssistantInquireTool bean has been created");
    }

    @Tool(name = "tripexchange_inquire", description = "Allows the user to ask open-ended questions to the assistant. " +
            "The assistant will respond with relevant information based on the user's query. " +
            "This tool is designed to provide quick and accurate responses to user inquiries. " +
            "The assistant expects a user ID and a query string as input. " +
            "The user ID is used to identify the user making the inquiry, and the query string is the question or request for information. ")
    public Object inquire(String userId, ChatQueryRecord chatQuery) {
        log.info("tripexchange_inquire tool method invoked for userId={}, query={}", userId, chatQuery);
        // Delegate to AssistantController's inquire logic
        ResponseEntity<?> response = assistantController.inquire(userId, chatQuery);
        return response.getBody();
    }
}
