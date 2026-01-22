export interface ChatMessage {
  id: string;
  content: string;
  timestamp: Date;
  sender: 'user' | 'bot' | 'system';
  type?: 'text' | 'image' | 'file' | 'error' | 'image-prompt';
  isTyping?: boolean;
  metadata?: {
    [key: string]: any;
  };
}

export interface ChatSession {
  id: string;
  name: string; // Changed from title to name to match component usage
  messages: ChatMessage[];
  createdAt: Date;
  lastActive: Date; // Changed from lastActivity to lastActive to match component usage
  isActive?: boolean;
}

export interface MCPRequest {
  method: string;
  params?: {
    [key: string]: any;
  };
  sessionId?: string;
}

export interface MCPResponse {
  success: boolean;
  content?: string; // Add content field that the component expects
  data?: any;
  error?: string;
  sessionId?: string;
  messageId?: string;
  metadata?: {
    [key: string]: any;
  };
}

export interface ChatbotConfig {
  apiEndpoint: string;
  maxMessages?: number;
  enableFileUpload?: boolean;
  enableImageUpload?: boolean;
  placeholder?: string;
  welcomeMessage?: string;
  errorMessage?: string;
  typingIndicatorDelay?: number;
}

export interface TypingIndicator {
  isTyping: boolean;
  duration?: number;
}

export interface HTTPRequest {
  method: string;
  params?: {
    [key: string]: any;
  };
  sessionId?: string;
}
