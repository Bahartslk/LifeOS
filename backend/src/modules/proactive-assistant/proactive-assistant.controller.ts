import { Body, Controller, HttpCode, HttpStatus, Post, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOkResponse, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { JwtPayload } from '../../common/strategies/jwt.strategy';
import { AiCapabilityRequestDto } from '../ai/dto/ai-capability-request.dto';
import { ApiAiProviderErrorResponses } from '../ai/swagger/ai-provider-error-responses.decorator';
import { InsightsResponseDto } from './dto/insight.dto';
import { OpportunitiesResponseDto } from './dto/opportunity.dto';
import { PrioritizeResponseDto } from './dto/prioritize-response.dto';
import { RemindersResponseDto } from './dto/reminder-recommendation.dto';
import { PrioritizeRequestDto } from './dto/suggestion.dto';
import { SuggestionsResponseDto } from './dto/suggestions-response.dto';
import { ProactiveAssistantService } from './proactive-assistant.service';

/**
 * Sprint 22 (Proactive Assistant Engine) — five endpoints under
 * `/proactive-assistant/...`. Four (`suggestions`, `opportunities`,
 * `reminders`, `insights`) reuse `AiCapabilityService` (via
 * `ProactiveAssistantService`) exactly as `PlannerAiController`/
 * `TravelAiController`/`DailyBriefController` do; `prioritize` runs a
 * deterministic algorithm over a caller-supplied suggestion list and never
 * calls a provider, so it has no `ApiAiProviderErrorResponses()` and its
 * response has no `provider`/`model`/`usage`/`latencyMs` envelope.
 *
 * The assistant is suggestion-only: no route here ever creates or modifies
 * a `Task`/`TaskList`/`Trip`/`ItineraryItem`, creates a reminder, or sends a
 * notification. Guarded the same way every other controller in this
 * codebase is.
 */
@ApiTags('proactive-assistant')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('proactive-assistant')
export class ProactiveAssistantController {
  constructor(private readonly proactiveAssistantService: ProactiveAssistantService) {}

  @Post('suggestions')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary:
      "Proactively suggest ways to help based on the caller's Planner and Travel situation, without a specific question",
  })
  @ApiOkResponse({ type: SuggestionsResponseDto })
  @ApiAiProviderErrorResponses()
  getSuggestions(
    @CurrentUser() user: JwtPayload,
    @Body() dto: AiCapabilityRequestDto,
  ): Promise<SuggestionsResponseDto> {
    return this.proactiveAssistantService.getSuggestions(user.sub, dto.notes);
  }

  @Post('opportunities')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: "Detect positive opportunities in the caller's current Planner and Travel situation",
  })
  @ApiOkResponse({ type: OpportunitiesResponseDto })
  @ApiAiProviderErrorResponses()
  getOpportunities(
    @CurrentUser() user: JwtPayload,
    @Body() dto: AiCapabilityRequestDto,
  ): Promise<OpportunitiesResponseDto> {
    return this.proactiveAssistantService.getOpportunities(user.sub, dto.notes);
  }

  @Post('reminders')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Recommend reminders the caller might want to set (never created or scheduled here)',
  })
  @ApiOkResponse({ type: RemindersResponseDto })
  @ApiAiProviderErrorResponses()
  getReminders(
    @CurrentUser() user: JwtPayload,
    @Body() dto: AiCapabilityRequestDto,
  ): Promise<RemindersResponseDto> {
    return this.proactiveAssistantService.getReminders(user.sub, dto.notes);
  }

  @Post('insights')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: "Generate short, actionable insights about the caller's own situation" })
  @ApiOkResponse({ type: InsightsResponseDto })
  @ApiAiProviderErrorResponses()
  getInsights(
    @CurrentUser() user: JwtPayload,
    @Body() dto: AiCapabilityRequestDto,
  ): Promise<InsightsResponseDto> {
    return this.proactiveAssistantService.getInsights(user.sub, dto.notes);
  }

  @Post('prioritize')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary:
      'Rank, deduplicate, and remove contradictions from a list of suggestions (a deterministic algorithm, not an AI call)',
  })
  @ApiOkResponse({ type: PrioritizeResponseDto })
  prioritize(@Body() dto: PrioritizeRequestDto): PrioritizeResponseDto {
    return this.proactiveAssistantService.prioritize(dto.suggestions);
  }
}
