import { Body, Controller, HttpCode, HttpStatus, Post, UseGuards } from '@nestjs/common';
import {
  ApiBadRequestResponse,
  ApiBearerAuth,
  ApiOkResponse,
  ApiOperation,
  ApiResponse,
  ApiTags,
} from '@nestjs/swagger';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { JwtPayload } from '../../common/strategies/jwt.strategy';
import { ApiAiProviderErrorResponses } from '../ai/swagger/ai-provider-error-responses.decorator';
import { AssistantChatService } from './assistant-chat.service';
import { ChatRequestDto } from './dto/chat-request.dto';
import { ChatResponseDto } from './dto/chat-response.dto';
import { CHAT_DAILY_LIMIT_REACHED_CODE } from './exceptions/chat-daily-limit-reached.exception';

/** Short, stateless chat with the LifeOS assistant (docs/09-ai-features.md#ai-chat). */
@ApiTags('ai')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('ai/chat')
export class AssistantChatController {
  constructor(private readonly assistantChatService: AssistantChatService) {}

  @Post()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary:
      "Send one chat message; the assistant answers in Turkish using the caller's Planner and Travel data. Suggestions only, nothing is stored or changed.",
  })
  @ApiOkResponse({ type: ChatResponseDto })
  @ApiBadRequestResponse({
    description: 'Empty message, message over 500 characters, or more than 8 history turns.',
  })
  @ApiResponse({
    status: 429,
    description: `The user's daily chat limit (AI_CHAT_DAILY_LIMIT, default 5) is used up: the error body has \`code: "${CHAT_DAILY_LIMIT_REACHED_CODE}"\`. A 429 without that code means the AI provider's own quota is used up.`,
  })
  @ApiAiProviderErrorResponses()
  sendMessage(
    @CurrentUser() user: JwtPayload,
    @Body() dto: ChatRequestDto,
  ): Promise<ChatResponseDto> {
    return this.assistantChatService.sendMessage(user.sub, dto);
  }
}
