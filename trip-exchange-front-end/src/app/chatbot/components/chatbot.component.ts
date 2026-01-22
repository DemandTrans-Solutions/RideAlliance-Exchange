import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

// PrimeNG imports
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ScrollPanelModule } from 'primeng/scrollpanel';
import { CardModule } from 'primeng/card';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TooltipModule } from 'primeng/tooltip';
import { MessageModule } from 'primeng/message';

import { MCPService } from '../services/mcp.service';
import { ChatMessage, ChatSession, MCPResponse } from '../models/chatbot.interfaces';
import { ChatMessageComponent } from './chat-message.component';
import { TypingIndicatorComponent } from './typing-indicator.component';

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    InputTextModule,
    ScrollPanelModule,
    CardModule,
    ProgressSpinnerModule,
    TooltipModule,
    MessageModule,
    ChatMessageComponent,
    TypingIndicatorComponent
  ],
  template: `
    <div class="chatbot-container" [class.chatbot-open]="isChatbotOpen">
      <!-- Chatbot Toggle Button -->
      <button
        *ngIf="!isChatbotOpen"
        class="chatbot-toggle"
        (click)="toggleChatbot()"
        pTooltip="Open Chat Assistant"
        tooltipPosition="left"
        type="button">
        <i class="pi pi-comments"></i>
      </button>

      <!-- Chatbot Window -->
      <div class="chatbot-window" *ngIf="isChatbotOpen" [class.minimized]="isMinimized">
        <!-- Header -->
        <div class="chatbot-header">
          <div class="header-content">
            <i class="pi pi-android"></i>
            <span class="title">AI Assistant</span>
            <div class="status-indicator" [class.online]="!isLoading"></div>
          </div>
          <div class="header-actions">
            <button
              class="header-btn"
              (click)="minimizeChat()"
              pTooltip="Minimize"
              type="button">
              <i class="pi" [class.pi-window-minimize]="!isMinimized" [class.pi-window-maximize]="isMinimized"></i>
            </button>
            <button
              class="header-btn"
              (click)="newChat()"
              pTooltip="New Chat"
              type="button">
              <i class="pi pi-plus"></i>
            </button>
            <button
              class="header-btn"
              (click)="clearChat()"
              pTooltip="Clear Chat"
              type="button">
              <i class="pi pi-trash"></i>
            </button>
            <button
              class="header-btn close-btn"
              (click)="closeChat()"
              pTooltip="Close"
              type="button">
              <i class="pi pi-times"></i>
            </button>
          </div>
        </div>

        <!-- Error Message -->
        <div class="error-container" *ngIf="error">
          <p-message
            severity="error"
            [text]="error"
            closable="true"
            (onClose)="dismissError()">
          </p-message>
        </div>

        <!-- Chat Body -->
        <div class="chatbot-body" *ngIf="!isMinimized">
          <!-- Messages Container -->
          <div class="messages-container" #messageContainer>
            <div class="messages-list">
              <app-chat-message
                *ngFor="let message of messages; trackBy: trackByMessageId"
                [message]="message">
              </app-chat-message>

              <!-- Typing Indicator -->
              <app-typing-indicator *ngIf="isTyping"></app-typing-indicator>
            </div>
          </div>

          <!-- Input Area -->
          <div class="input-container">
            <div class="input-wrapper">
              <input
                #messageInput
                type="text"
                pInputText
                [(ngModel)]="newMessage"
                (keypress)="onKeyPress($event)"
                [disabled]="isLoading"
                placeholder="Type your message here..."
                class="message-input"
                autocomplete="off">

              <button
                class="send-button"
                (click)="sendMessage()"
                [disabled]="!newMessage.trim() || isLoading"
                type="button">
                <i class="pi pi-send" *ngIf="!isLoading"></i>
                <i class="pi pi-spin pi-spinner" *ngIf="isLoading"></i>
              </button>
            </div>

            <!-- Toggle for response type -->
            <div class="toggle-container">
              <label>
                <input type="radio" name="queryType" [(ngModel)]="selectedQueryType" value="TEXT"> Text
              </label>
              <label>
                <input type="radio" name="queryType" [(ngModel)]="selectedQueryType" value="REPORTING_CHARTING"> Report/Chart
              </label>
            </div>

            <div class="input-footer">
              <small class="input-hint">Press Enter to send, Shift+Enter for new line</small>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styleUrls: ['./chatbot.component.scss']
})
export class ChatbotComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('messageContainer') private messageContainer!: ElementRef;
  @ViewChild('messageInput') private messageInput!: ElementRef;

  private destroy$ = new Subject<void>();

  currentSession: ChatSession | null = null;
  messages: ChatMessage[] = [];
  newMessage = '';
  isLoading = false;
  isTyping = false;
  error: string | null = null;
  isMinimized = false;
  isChatbotOpen = false;

  private pendingImageBlob: Blob | null = null;
  private pendingImageFilename: string | null = null;
  private awaitingDownloadResponse = false;

  selectedQueryType: string = 'TEXT';

  constructor(private mcpService: MCPService) {}

  ngOnInit(): void {
    this.initializeChat();
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeChat(): void {
    // Try to get existing session or create new one
    this.currentSession = this.mcpService.getCurrentSession();

    if (this.currentSession) {
      this.loadSessionMessages();
    } else {
      this.createNewSession();
    }
  }

  private createNewSession(): void {
    const sessionId = this.generateSessionId();
    this.currentSession = {
      id: sessionId,
      name: `Chat ${new Date().toLocaleTimeString()}`,
      createdAt: new Date(),
      lastActive: new Date(),
      messages: []
    };

    this.mcpService.setCurrentSessionObject(this.currentSession);
    this.addWelcomeMessage();
  }

  private loadSessionMessages(): void {
    if (this.currentSession) {
      this.messages = [...this.currentSession.messages];
    }
  }

  private addWelcomeMessage(): void {
    const welcomeMessage: ChatMessage = {
      id: this.generateMessageId(),
      content: 'Hello! I am your assistant. I can help you with information from the backend system. How can I assist you today?',
      timestamp: new Date(),
      sender: 'bot',
      type: 'text'
    };

    this.addMessage(welcomeMessage);
  }

  toggleChatbot(): void {
    this.isChatbotOpen = !this.isChatbotOpen;
    if (this.isChatbotOpen && !this.isMinimized) {
      setTimeout(() => this.focusInput(), 100);
    }
  }

  minimizeChat(): void {
    this.isMinimized = !this.isMinimized;
    if (!this.isMinimized) {
      setTimeout(() => this.focusInput(), 100);
    }
  }

  closeChat(): void {
    this.isChatbotOpen = false;
    this.isMinimized = false;
  }

  sendMessage(): void {
    if (!this.newMessage.trim() || this.isLoading) {
      return;
    }

    // Handle download prompt response
    if (this.awaitingDownloadResponse && this.pendingImageBlob) {
      const userResponse = this.newMessage.trim().toLowerCase();
      if (userResponse === 'yes' || userResponse === 'y') {
        this.downloadPendingImage();
        this.addMessage({
          id: this.generateMessageId(),
          content: 'Downloading image...',
          timestamp: new Date(),
          sender: 'system',
          type: 'text'
        });
      } else {
        this.addMessage({
          id: this.generateMessageId(),
          content: 'Image download canceled.',
          timestamp: new Date(),
          sender: 'system',
          type: 'text'
        });
      }
      this.awaitingDownloadResponse = false;
      this.pendingImageBlob = null;
      this.pendingImageFilename = null;
      this.newMessage = '';
      return;
    }

    if (!this.currentSession) {
      this.createNewSession();
    }

    const userMessage: ChatMessage = {
      id: this.generateMessageId(),
      content: this.newMessage.trim(),
      timestamp: new Date(),
      sender: 'user',
      type: 'text'
    };

    this.addMessage(userMessage);
    const messageContent = this.newMessage.trim();
    const queryType = this.selectedQueryType;
    this.newMessage = '';
    this.error = null;

    this.sendToBot(messageContent, queryType);
  }

  private sendToBot(message: string, queryType: string): void {
    this.isLoading = true;
    this.isTyping = true;

    const useDirectEndpoint = true; // Set to true to use invokeEndpoint
    const userId = this.getCurrentUserId();

    if (useDirectEndpoint) {
      this.mcpService.invokeEndpoint(message, userId, queryType)
        .pipe(takeUntil(this.destroy$))
        .subscribe((response: Blob) => {
          this.isLoading = false;
          this.isTyping = false;

          if (response.type.startsWith('text')) {
            const reader = new FileReader();
            reader.onload = () => {
              const botMessage: ChatMessage = {
                id: this.generateMessageId(),
                content: reader.result as string,
                timestamp: new Date(),
                sender: 'bot',
                type: 'text'
              };
              this.addMessage(botMessage);
            };
            reader.readAsText(response);
          } else if (response.type === 'image/jpeg' || response.type === 'image/png') {
            // Prompt user to download, with collapsible image
            this.pendingImageBlob = response;
            this.pendingImageFilename = `assistant-image.${response.type === 'image/png' ? 'png' : 'jpg'}`;
            this.awaitingDownloadResponse = true;
            const imageUrl = URL.createObjectURL(response);
            const botMessage: ChatMessage = {
              id: this.generateMessageId(),
              content: 'I received an image file. Would you like to download it? (yes/no)',
              timestamp: new Date(),
              sender: 'bot',
              type: 'image-prompt',
              metadata: { imageUrl }
            };
            this.addMessage(botMessage);
          } else if (response.type.startsWith('image')) {
            // Show image inline (for other image types)
            const imageUrl = URL.createObjectURL(response);
            const botMessage: ChatMessage = {
              id: this.generateMessageId(),
              content: `<img src='${imageUrl}' alt='AI Image' style='max-width:100%;'/>`,
              timestamp: new Date(),
              sender: 'bot',
              type: 'image'
            };
            this.addMessage(botMessage);
          } else {
            const botMessage: ChatMessage = {
              id: this.generateMessageId(),
              content: 'Received a file or unknown response type.',
              timestamp: new Date(),
              sender: 'bot',
              type: 'file'
            };
            this.addMessage(botMessage);
          }
        }, (error: any) => {
          this.isLoading = false;
          this.isTyping = false;
          this.error = error.message || 'Failed to send message. Please try again.';
          const errorMessage: ChatMessage = {
            id: this.generateMessageId(),
            content: 'Sorry, I encountered an error processing your request. Please try again.',
            timestamp: new Date(),
            sender: 'bot',
            type: 'error'
          };
          this.addMessage(errorMessage);
        });
    } else {
      this.mcpService.sendMessage(message)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response: MCPResponse) => {
            this.isLoading = false;
            this.isTyping = false;

            const botMessage: ChatMessage = {
              id: this.generateMessageId(),
              content: response.content || 'I received your message but could not generate a response.',
              timestamp: new Date(),
              sender: 'bot',
              type: 'text',
              metadata: response.metadata
            };

            this.addMessage(botMessage);
          },
          error: (error: any) => {
            this.isLoading = false;
            this.isTyping = false;
            this.error = error.message || 'Failed to send message. Please try again.';
            const errorMessage: ChatMessage = {
              id: this.generateMessageId(),
              content: 'Sorry, I encountered an error processing your request. Please try again.',
              timestamp: new Date(),
              sender: 'bot',
              type: 'error'
            };
            this.addMessage(errorMessage);
          }
        });
    }
  }

  private downloadPendingImage(): void {
    if (this.pendingImageBlob && this.pendingImageFilename) {
      const url = URL.createObjectURL(this.pendingImageBlob);
      const a = document.createElement('a');
      a.href = url;
      a.download = this.pendingImageFilename;
      document.body.appendChild(a);
      a.click();
      setTimeout(() => {
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
      }, 100);
    }
  }

  // Add this helper method to get the current userId (replace with your actual logic)
  private getCurrentUserId(): string {
    // Example: return from localStorage or authentication service
    return localStorage.getItem('userId') || 'unknown-user';
  }

  private addMessage(message: ChatMessage): void {
    this.messages.push(message);

    if (this.currentSession) {
      this.currentSession.messages.push(message);
      this.currentSession.lastActive = new Date();
      this.mcpService.setCurrentSessionObject(this.currentSession);
    }
  }

  clearChat(): void {
    if (this.currentSession) {
      this.messages = [];
      this.currentSession.messages = [];
      this.mcpService.setCurrentSessionObject(this.currentSession);
      this.addWelcomeMessage();
    }
  }

  newChat(): void {
    this.createNewSession();
    this.messages = [];
    this.addWelcomeMessage();
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  private scrollToBottom(): void {
    if (this.messageContainer) {
      try {
        const element = this.messageContainer.nativeElement;
        element.scrollTop = element.scrollHeight;
      } catch (err) {
        console.warn('Could not scroll to bottom:', err);
      }
    }
  }

  private focusInput(): void {
    if (this.messageInput) {
      this.messageInput.nativeElement.focus();
    }
  }

  private generateSessionId(): string {
    return `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  private generateMessageId(): string {
    return `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  dismissError(): void {
    this.error = null;
  }

  trackByMessageId(index: number, message: ChatMessage): string {
    return message.id;
  }
}
