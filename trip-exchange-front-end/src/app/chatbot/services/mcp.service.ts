import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError, of } from 'rxjs';
import { catchError, retry, timeout } from 'rxjs/operators';
import {
  MCPRequest,
  MCPResponse,
  ChatMessage,
  ChatSession
} from '../models/chatbot.interfaces';
import { ChatbotConfig } from '../config/chatbot.config';

@Injectable({
  providedIn: 'root'
})
export class MCPService {
  private readonly httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json'
    })
  };

  private sessionsSubject = new BehaviorSubject<ChatSession[]>([]);
  public sessions$ = this.sessionsSubject.asObservable();

  private currentSessionSubject = new BehaviorSubject<ChatSession | null>(null);
  public currentSession$ = this.currentSessionSubject.asObservable();

  constructor(
    private http: HttpClient,
    private config: ChatbotConfig
  ) {
    this.loadSessions();
  }

  /**
   * Send a message to the MCP server
   */
  sendMessage(message: string, sessionId?: string): Observable<MCPResponse> {
    const request: MCPRequest = {
      method: 'chat.sendMessage',
      params: {
        message,
        sessionId
      },
      sessionId
    };

    return this.http.post<MCPResponse>(
      this.config.ENDPOINTS.SEND_MESSAGE,
      request,
      this.httpOptions
    ).pipe(
      timeout(this.config.CHAT_CONFIG.REQUEST_TIMEOUT_MS),
      retry(this.config.CHAT_CONFIG.MAX_RETRIES),
      catchError(this.handleError)
    );
  }

  /**
   * Get available MCP tools/capabilities
   */
  getAvailableTools(): Observable<any[]> {
    return this.http.get<any[]>(this.config.ENDPOINTS.GET_TOOLS, this.httpOptions)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Execute a specific MCP tool
   */
  executeTool(toolName: string, params: any, sessionId?: string): Observable<MCPResponse> {
    const request: MCPRequest = {
      method: `tools.${toolName}`,
      params,
      sessionId
    };

    return this.http.post<MCPResponse>(
      this.config.ENDPOINTS.EXECUTE_TOOL,
      request,
      this.httpOptions
    ).pipe(
      timeout(this.config.CHAT_CONFIG.REQUEST_TIMEOUT_MS * 2), // Double timeout for tool execution
      catchError(this.handleError)
    );
  }

  /**
   * Create a new chat session
   */
  createSession(name?: string): ChatSession {
    const session: ChatSession = {
      id: this.generateId(),
      name: name || `Chat ${new Date().toLocaleString()}`,
      messages: [],
      createdAt: new Date(),
      lastActive: new Date(),
      isActive: true
    };

    const sessions = this.sessionsSubject.value;
    sessions.push(session);
    this.sessionsSubject.next(sessions);
    this.currentSessionSubject.next(session);
    this.saveSessions();

    return session;
  }

  /**
   * Get session by ID
   */
  getSession(sessionId: string): ChatSession | undefined {
    return this.sessionsSubject.value.find(s => s.id === sessionId);
  }

  /**
   * Get current active session
   */
  getCurrentSession(): ChatSession | null {
    return this.currentSessionSubject.value;
  }

  /**
   * Set current active session by object
   */
  setCurrentSessionObject(session: ChatSession): void {
    this.currentSessionSubject.next(session);
    this.saveSessions();
  }

  /**
   * Set current active session
   */
  setCurrentSession(sessionId: string): void {
    const session = this.getSession(sessionId);
    if (session) {
      this.currentSessionSubject.next(session);
    }
  }

  /**
   * Add message to current session
   */
  addMessageToSession(sessionId: string, message: ChatMessage): void {
    const sessions = this.sessionsSubject.value;
    const sessionIndex = sessions.findIndex(s => s.id === sessionId);

    if (sessionIndex !== -1) {
      sessions[sessionIndex].messages.push(message);
      sessions[sessionIndex].lastActive = new Date();
      this.sessionsSubject.next(sessions);
      this.saveSessions();

      // Update current session if it's the active one
      const currentSession = this.currentSessionSubject.value;
      if (currentSession && currentSession.id === sessionId) {
        this.currentSessionSubject.next(sessions[sessionIndex]);
      }
    }
  }

  /**
   * Delete a session
   */
  deleteSession(sessionId: string): void {
    const sessions = this.sessionsSubject.value.filter(s => s.id !== sessionId);
    this.sessionsSubject.next(sessions);
    this.saveSessions();

    // If deleted session was current, clear it
    const currentSession = this.currentSessionSubject.value;
    if (currentSession && currentSession.id === sessionId) {
      this.currentSessionSubject.next(null);
    }
  }

  /**
   * Clear all sessions
   */
  clearAllSessions(): void {
    this.sessionsSubject.next([]);
    this.currentSessionSubject.next(null);
    this.saveSessions();
  }

  /**
   * Get chat history for a session
   */
  getChatHistory(sessionId: string): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(
      `${this.config.MCP_BASE_URL}/api/v1/sessions/${sessionId}/history`,
      this.httpOptions
    ).pipe(
      catchError(() => of([])) // Return empty array on error
    );
  }

  /**
   * Save session to backend
   */
  saveSessionToBackend(session: ChatSession): Observable<any> {
    return this.http.post(
      `${this.config.MCP_BASE_URL}/api/v1/sessions`,
      session,
      this.httpOptions
    ).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Invoke a direct HTTP endpoint (text or image response)
   * @param query The chatbot query string
   * @param userId The logged-in user's userId
   * @param queryType The type of query (e.g., 'TEXT' or 'REPORTING_CHARTING')
   * @returns Observable<Blob>
   */
  invokeEndpoint(query: string, userId: string, queryType: string): Observable<Blob> {
    // Endpoint for assistant inquire (POST)
    const url = `http://localhost:8080/api/assistant/${encodeURIComponent(userId)}/inquire`;

    // Build headers with XSRF token if present
    let headers = new HttpHeaders({ 'Accept': '*/*', 'Content-Type': 'application/json' });
    try {
      const xsrfToken = localStorage.getItem('xsrfToken');
      if (xsrfToken) {
        headers = headers.set('X-AUTH-TOKEN', xsrfToken);
      }
    } catch (e) {
      // Ignore localStorage errors
    }

    // POST the endpoint, expect blob (text or image)
    const body = { query, queryType };
    return this.http.post(url, body, {
      headers,
      withCredentials: true,
      responseType: 'blob'
    }).pipe(
      timeout(this.config.CHAT_CONFIG.REQUEST_TIMEOUT_MS),
      retry(this.config.CHAT_CONFIG.MAX_RETRIES),
      catchError(this.handleError)
    );
  }

  /**
   * Generate unique ID
   */
  private generateId(): string {
    return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * Load sessions from localStorage
   */
  private loadSessions(): void {
    try {
      const savedSessions = localStorage.getItem('chatbot-sessions');
      if (savedSessions) {
        const sessions: ChatSession[] = JSON.parse(savedSessions);
        // Convert date strings back to Date objects
        sessions.forEach(session => {
          session.createdAt = new Date(session.createdAt);
          session.lastActive = new Date(session.lastActive);
          session.messages.forEach(message => {
            message.timestamp = new Date(message.timestamp);
          });
        });
        this.sessionsSubject.next(sessions);
      }
    } catch (error) {
      console.warn('Failed to load chat sessions from localStorage:', error);
    }
  }

  /**
   * Save sessions to localStorage
   */
  private saveSessions(): void {
    try {
      const sessions = this.sessionsSubject.value;
      localStorage.setItem('chatbot-sessions', JSON.stringify(sessions));
    } catch (error) {
      console.warn('Failed to save chat sessions to localStorage:', error);
    }
  }

  /**
   * Handle HTTP errors
   */
  private handleError = (error: HttpErrorResponse): Observable<never> => {
    let errorMessage = 'An error occurred while communicating with the chatbot service.';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Client Error: ${error.error.message}`;
    } else {
      // Server-side error
      switch (error.status) {
        case 0:
          errorMessage = 'Unable to connect to the chatbot service. Please check your internet connection.';
          break;
        case 400:
          errorMessage = 'Bad request. Please check your input and try again.';
          break;
        case 401:
          errorMessage = 'You are not authorized to use this service. Please log in.';
          break;
        case 403:
          errorMessage = 'Access forbidden. You do not have permission to use this feature.';
          break;
        case 404:
          errorMessage = 'Chatbot service not found. Please contact support.';
          break;
        case 500:
          errorMessage = 'Internal server error. Please try again later.';
          break;
        case 503:
          errorMessage = 'Chatbot service is temporarily unavailable. Please try again later.';
          break;
        default:
          errorMessage = `Server Error: ${error.status} - ${error.message}`;
      }
    }

    console.error('MCP Service Error:', error);
    return throwError(() => new Error(errorMessage));
  };
}
