import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

// PrimeNG imports
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ScrollPanelModule } from 'primeng/scrollpanel';
import { CardModule } from 'primeng/card';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TooltipModule } from 'primeng/tooltip';
import { MessageModule } from 'primeng/message';

// Chatbot components
import { ChatbotComponent } from './components/chatbot.component';
import { ChatMessageComponent } from './components/chat-message.component';
import { TypingIndicatorComponent } from './components/typing-indicator.component';

// Services
import { MCPService } from './services/mcp.service';

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    ButtonModule,
    InputTextModule,
    ScrollPanelModule,
    CardModule,
    ProgressSpinnerModule,
    TooltipModule,
    MessageModule,
    ChatbotComponent,
    ChatMessageComponent,
    TypingIndicatorComponent
  ],
  providers: [
    MCPService
  ],
  exports: [
    ChatbotComponent
  ]
})
export class ChatbotModule { }
