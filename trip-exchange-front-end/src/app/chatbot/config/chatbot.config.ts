import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ChatbotConfig {
  // MCP Server Configuration
  public readonly MCP_BASE_URL = 'http://localhost:8081'; // Update this to match your Spring Boot server
  public readonly MCP_API_PREFIX = '/api/v1/mcp';

  // Endpoints
  public readonly ENDPOINTS = {
    SEND_MESSAGE: `${this.MCP_BASE_URL}${this.MCP_API_PREFIX}/1234/inquire`,
    EXECUTE_TOOL: `${this.MCP_BASE_URL}${this.MCP_API_PREFIX}/tools/execute`,
    GET_TOOLS: `${this.MCP_BASE_URL}${this.MCP_API_PREFIX}/tools`,
    HEALTH_CHECK: `${this.MCP_BASE_URL}${this.MCP_API_PREFIX}/health`
  };

  // Chat Configuration
  public readonly CHAT_CONFIG = {
    MAX_MESSAGE_LENGTH: 2000,
    SESSION_TIMEOUT_MINUTES: 30,
    MAX_SESSIONS: 10,
    TYPING_DELAY_MS: 1000,
    REQUEST_TIMEOUT_MS: 30000,
    MAX_RETRIES: 3
  };

  // UI Configuration
  public readonly UI_CONFIG = {
    CHATBOT_TITLE: 'AI Assistant',
    WELCOME_MESSAGE: 'Hello! I\'m your assistant. I can help you with information from the backend system. How can I assist you today?',
    ERROR_MESSAGE: 'Sorry, I encountered an error processing your request. Please try again.',
    TYPING_MESSAGE: 'AI is typing...',
    MAX_VISIBLE_MESSAGES: 100
  };
}
