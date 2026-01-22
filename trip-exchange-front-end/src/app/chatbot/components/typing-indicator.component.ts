import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-typing-indicator',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="typing-indicator">
      <div class="typing-content">
        <div class="typing-avatar">
          <i class="pi pi-android"></i>
        </div>
        <div class="typing-bubble">
          <div class="typing-dots">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>
          <div class="typing-text">AI is typing...</div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .typing-indicator {
      display: flex;
      justify-content: flex-start;
      margin-bottom: 16px;
      animation: fadeIn 0.3s ease-out;
    }

    .typing-content {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      max-width: 85%;
    }

    .typing-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: linear-gradient(135deg, #007ad9, #0056b3);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
      flex-shrink: 0;
      margin-top: 4px;
    }

    .typing-bubble {
      background: white;
      border: 1px solid #e0e0e0;
      border-radius: 18px;
      border-bottom-left-radius: 6px;
      padding: 12px 16px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      margin-right: 40px;
      position: relative;
    }

    .typing-dots {
      display: flex;
      gap: 4px;
      margin-bottom: 4px;
      align-items: center;
      height: 16px;
    }

    .typing-dots .dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #007ad9;
      animation: typingDot 1.4s infinite ease-in-out;
    }

    .typing-dots .dot:nth-child(1) {
      animation-delay: 0s;
    }

    .typing-dots .dot:nth-child(2) {
      animation-delay: 0.2s;
    }

    .typing-dots .dot:nth-child(3) {
      animation-delay: 0.4s;
    }

    .typing-text {
      font-size: 11px;
      color: #666;
      opacity: 0.8;
      font-style: italic;
    }

    @keyframes typingDot {
      0%, 60%, 100% {
        transform: translateY(0);
        opacity: 0.4;
      }
      30% {
        transform: translateY(-8px);
        opacity: 1;
      }
    }

    @keyframes fadeIn {
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
export class TypingIndicatorComponent {

}
