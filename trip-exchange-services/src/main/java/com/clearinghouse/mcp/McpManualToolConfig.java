package com.clearinghouse.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpManualToolConfig {
    private static final Logger logger = LoggerFactory.getLogger(McpManualToolConfig.class);
/*
    @Bean
    public ToolCallback assistantInquireToolCallback(AssistantInquireTool assistantInquireTool) {
        logger.info("Registering AssistantInquireTool as MCP ToolCallback via MethodToolCallback");
        return new MethodToolCallback(assistantInquireTool);
    }*/
}
