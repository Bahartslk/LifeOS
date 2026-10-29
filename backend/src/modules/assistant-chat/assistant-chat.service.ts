import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { AppConfig } from '../../config/configuration';
import { todayInTimeZone } from '../../common/utils/date-time.util';
import { AiCapabilityService } from '../ai/capabilities/ai-capability.service';
import { UsersService } from '../users/users.service';
import { ASSISTANT_CHAT, buildChatInput } from './capabilities/assistant-chat.capability';
import { ChatRequestDto } from './dto/chat-request.dto';
import { ChatResponseDto } from './dto/chat-response.dto';
import { ChatDailyLimitReachedException } from './exceptions/chat-daily-limit-reached.exception';
import { ChatUsageRepository } from './repositories/chat-usage.repository';

/** Used when a user somehow has no time zone; matches the `users.timezone` default. */
const DEFAULT_TIME_ZONE = 'Europe/Istanbul';

/**
 * One stateless chat turn: reserve one of today's messages, ask the AI, and
 * return the reply with how many messages are left. Nothing about the
 * conversation is stored; only the daily count is.
 *
 * The message is reserved before the AI call (so concurrent requests can't
 * exceed the limit) and given back if the call fails, so a user never loses
 * a message for an answer they didn't get.
 */
@Injectable()
export class AssistantChatService {
  constructor(
    private readonly capabilityService: AiCapabilityService,
    private readonly usersService: UsersService,
    private readonly chatUsage: ChatUsageRepository,
    private readonly configService: ConfigService<AppConfig, true>,
  ) {}

  async sendMessage(userId: string, dto: ChatRequestDto): Promise<ChatResponseDto> {
    const dailyLimit = this.configService.get('ai.chatDailyLimit', { infer: true });
    const day = await this.todayForUser(userId);

    const used = await this.chatUsage.reserve(userId, day, dailyLimit);
    if (used === null) {
      throw new ChatDailyLimitReachedException(dailyLimit);
    }

    try {
      const response = await this.capabilityService.run(
        ASSISTANT_CHAT,
        userId,
        undefined,
        buildChatInput(dto.message, dto.history ?? []),
      );
      return { reply: response.result.reply, remainingMessages: Math.max(dailyLimit - used, 0) };
    } catch (error) {
      await this.chatUsage.release(userId, day);
      throw error;
    }
  }

  /** "Today" in the user's own time zone, so the limit resets at their midnight, not UTC's. */
  private async todayForUser(userId: string): Promise<string> {
    const user = await this.usersService.findById(userId);
    return todayInTimeZone(user?.timezone || DEFAULT_TIME_ZONE);
  }
}
