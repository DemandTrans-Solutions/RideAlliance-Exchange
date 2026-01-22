import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChatMessage } from '../models/chatbot.interfaces';

@Component({
  selector: 'app-chat-message',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="message-wrapper" [class.user-message]="isUser" [class.bot-message]="isBot" [class.error-message]="isError">
      <div class="message-content">
        <div class="message-avatar" *ngIf="isBot">
          <i class="pi pi-android"></i>
        </div>

        <div class="message-bubble">
          <!-- Collapsible image prompt -->
          <ng-container *ngIf="message.type === 'image-prompt'; else defaultContent">
            <div class="message-text">{{ message.content }}</div>
            <button class="collapse-btn" (click)="collapsed = !collapsed" type="button">
              <span *ngIf="collapsed">Show Image ▼</span>
              <span *ngIf="!collapsed">Hide Image ▲</span>
            </button>
            <div *ngIf="!collapsed" class="collapsible-image-container">
              <img [src]="message.metadata?.['imageUrl']" alt="AI Image" style="max-width:100%; max-height:300px; display:block; margin:8px 0; border-radius:8px; box-shadow:0 2px 8px rgba(0,0,0,0.08);" />
            </div>
            <div class="message-time">{{ messageTime }}</div>
          </ng-container>
          <ng-template #defaultContent>
            <div class="message-text" [innerHTML]="formattedContent"></div>
            <div class="message-time">{{ messageTime }}</div>
          </ng-template>

          <!-- Metadata display for bot messages -->
          <div class="message-metadata" *ngIf="message.metadata && isBot && message.type !== 'image-prompt'">
            <div class="metadata-item" *ngFor="let item of message.metadata | keyvalue">
              <span class="metadata-key">{{ item.key }}:</span>
              <span class="metadata-value">{{ item.value }}</span>
            </div>
          </div>
        </div>

        <div class="message-avatar" *ngIf="isUser">
          <i class="pi pi-user"></i>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .message-wrapper {
      display: flex;
      margin-bottom: 16px;
      animation: fadeInUp 0.3s ease-out;
    }

    .user-message {
      justify-content: flex-end;
    }

    .user-message .message-content {
      flex-direction: row-reverse;
    }

    .user-message .message-bubble {
      background: linear-gradient(135deg, #007ad9, #0056b3);
      color: white;
      margin-left: 40px;
      border-bottom-right-radius: 6px;
    }

    .user-message .message-time {
      color: rgba(255, 255, 255, 0.8);
    }

    .bot-message {
      justify-content: flex-start;
    }

    .bot-message .message-bubble {
      background: white;
      color: #333;
      border: 1px solid #e0e0e0;
      margin-right: 40px;
      border-bottom-left-radius: 6px;
    }

    .error-message .message-bubble {
      background: #fff5f5;
      border-color: #fed7d7;
      color: #c53030;
    }

    .message-content {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      max-width: 85%;
    }

    .message-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
      flex-shrink: 0;
      margin-top: 4px;
    }

    .bot-message .message-avatar {
      background: linear-gradient(135deg, #007ad9, #0056b3);
      color: white;
    }

    .user-message .message-avatar {
      background: #f0f0f0;
      color: #666;
    }

    .message-bubble {
      border-radius: 18px;
      padding: 12px 16px;
      position: relative;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      word-wrap: break-word;
      line-height: 1.4;
    }

    .message-text {
      font-size: 14px;
      margin-bottom: 4px;
    }

    .message-time {
      font-size: 11px;
      color: #999;
      text-align: right;
      margin-top: 4px;
      opacity: 0.7;
    }

    .message-metadata {
      margin-top: 8px;
      padding-top: 8px;
      border-top: 1px solid rgba(0, 0, 0, 0.1);
      font-size: 12px;
    }

    .user-message .message-metadata {
      border-color: rgba(255, 255, 255, 0.2);
    }

    .metadata-item {
      display: flex;
      justify-content: space-between;
      margin-bottom: 4px;
    }

    .metadata-item:last-child {
      margin-bottom: 0;
    }

    .metadata-key {
      font-weight: 500;
      opacity: 0.8;
    }

    .metadata-value {
      font-weight: 400;
      text-align: right;
      word-break: break-word;
      max-width: 60%;
    }

    .collapse-btn {
      background: none;
      border: none;
      color: #007ad9;
      cursor: pointer;
      font-size: 13px;
      margin: 4px 0 0 0;
      padding: 0;
      outline: none;
      text-decoration: underline;
    }
    .collapse-btn:hover {
      color: #0056b3;
      text-decoration: none;
    }
    .collapsible-image-container {
      text-align: center;
      margin-top: 8px;
    }

    @keyframes fadeInUp {
      from {
        opacity: 0;
        transform: translateY(10px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }
  `]
})
export class ChatMessageComponent {
  @Input() message!: ChatMessage;
  collapsed = true;

  get isUser(): boolean {
    return this.message.sender === 'user';
  }

  get isBot(): boolean {
    return this.message.sender === 'bot';
  }

  get isError(): boolean {
    return this.message.type === 'error';
  }

  get messageTime(): string {
    return this.message.timestamp.toLocaleTimeString([], {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  get formattedContent(): string {
    if (!this.message.content) return '';

    // For image messages, return the raw HTML (do not format)
    if (this.message.type === 'image') {
      return this.message.content;
    }

    // Simple formatting for links, bold, etc. for text messages
    return this.message.content
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/(https?:\/\/[^\s]+)/g, '<a href="$1" target="_blank" rel="noopener noreferrer">$1</a>');
  }
}
