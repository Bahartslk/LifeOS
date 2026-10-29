import { Module } from '@nestjs/common';
import { AiModule } from '../ai/ai.module';
import { UsersModule } from '../users/users.module';
import { AssistantChatController } from './assistant-chat.controller';
import { AssistantChatService } from './assistant-chat.service';
import { ChatUsageRepository } from './repositories/chat-usage.repository';

/**
 * AI chat (`POST /ai/chat`): a one-directional consumer of `AiModule`, like
 * `DailyBriefModule`, plus `UsersModule` for the user's time zone. Owns the
 * `ai_chat_usage` daily counter; stores no conversation content.
 */
@Module({
  imports: [AiModule, UsersModule],
  controllers: [AssistantChatController],
  providers: [AssistantChatService, ChatUsageRepository],
})
export class AssistantChatModule {}
