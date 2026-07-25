import { Body, Controller, HttpCode, HttpStatus, Post, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOkResponse, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { JwtPayload } from '../../common/strategies/jwt.strategy';
import { AiCapabilityRequestDto } from '../ai/dto/ai-capability-request.dto';
import { ApiAiProviderErrorResponses } from '../ai/swagger/ai-provider-error-responses.decorator';
import { DailyBriefService } from './daily-brief.service';
import { DailyMotivationResponseDto } from './dto/daily-motivation-response.dto';
import { DailyPrioritiesResponseDto } from './dto/daily-priorities-response.dto';
import { DailySummaryResponseDto } from './dto/daily-summary-response.dto';
import { PreparationResponseDto } from './dto/preparation-response.dto';
import { ScheduleConflictsResponseDto } from './dto/schedule-conflicts-response.dto';
import { UpcomingRiskAnalysisResponseDto } from './dto/upcoming-risks-response.dto';

/**
 * Sprint 21 (Daily Brief Intelligence) — the platform's first genuinely
 * cross-module AI feature: six capability-scoped endpoints under
 * `/daily-brief/...`, each combining Planner and Travel context together
 * (via the unmodified `ContextBuilder`/`assistant` prompt template) rather
 * than reasoning over a single module, per this sprint's "Daily Brief
 * becomes the first feature that understands the user's overall situation"
 * goal. A brand-new `DailyBriefModule` — not owned by Planner or Travel,
 * since no single one of them owns "the user's whole day." Reuses
 * `AiCapabilityService` (via `DailyBriefService`) exactly as Sprint 19/20
 * did; no AI infrastructure was duplicated or modified to build this.
 *
 * None of these routes ever writes to `Task`/`TaskList`/`Trip`/
 * `ItineraryItem` — every response is a suggestion; the client decides
 * whether to act on any of it.
 */
@ApiTags('daily-brief')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('daily-brief')
export class DailyBriefController {
  constructor(private readonly dailyBriefService: DailyBriefService) {}

  @Post('summary')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: "Summarize today's agenda across Planner tasks, upcoming trips, and deadlines",
  })
  @ApiOkResponse({ type: DailySummaryResponseDto })
  @ApiAiProviderErrorResponses()
  getSummary(
    @CurrentUser() user: JwtPayload,
    @Body() dto: AiCapabilityRequestDto,
  ): Promise<DailySummaryResponseDto> {
    return this.dailyBriefService.getDailySummary(user.sub, dto.notes);
  }

  @Post('conflicts')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary:
      'Detect scheduling conflicts across Planner and Travel: overlaps, impossible schedules, insufficient travel time, overloaded days',
  })
  @ApiOkResponse({ type: ScheduleConflictsResponseDto })
  @ApiAiProviderErrorResponses()
  getConflicts(
    @CurrentUser() user: JwtPayload,
    @Body() dto: AiCapabilityRequestDto,
  ): Promise<ScheduleConflictsResponseDto> {
    return this.dailyBriefService.detectScheduleConflicts(user.sub, dto.notes);
  }

  @Post('preparation')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Generate prioritized preparation recommendations from upcoming tasks and trips',
  })
  @ApiOkResponse({ type: PreparationResponseDto })
  @ApiAiProviderErrorResponses()
  getPreparation(
    @CurrentUser() user: JwtPayload,
    @Body() dto: AiCapabilityRequestDto,
  ): Promise<PreparationResponseDto> {
    return this.dailyBriefService.suggestPreparation(user.sub, dto.notes);
  }

  @Post('risks')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary:
      'Analyze the next 7 days across Planner and Travel for deadline, workload, travel, and preparation risk',
  })
  @ApiOkResponse({ type: UpcomingRiskAnalysisResponseDto })
  @ApiAiProviderErrorResponses()
  getUpcomingRisks(
    @CurrentUser() user: JwtPayload,
    @Body() dto: AiCapabilityRequestDto,
  ): Promise<UpcomingRiskAnalysisResponseDto> {
    return this.dailyBriefService.analyzeUpcomingRisks(user.sub, dto.notes);
  }

  @Post('priorities')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: "Recommend a ranking for today's existing tasks (never modifies stored priorities)",
  })
  @ApiOkResponse({ type: DailyPrioritiesResponseDto })
  @ApiAiProviderErrorResponses()
  getPriorities(
    @CurrentUser() user: JwtPayload,
    @Body() dto: AiCapabilityRequestDto,
  ): Promise<DailyPrioritiesResponseDto> {
    return this.dailyBriefService.suggestDailyPriorities(user.sub, dto.notes);
  }

  @Post('motivation')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: "Generate one short, specific motivational message based on today's actual workload",
  })
  @ApiOkResponse({ type: DailyMotivationResponseDto })
  @ApiAiProviderErrorResponses()
  getMotivation(
    @CurrentUser() user: JwtPayload,
    @Body() dto: AiCapabilityRequestDto,
  ): Promise<DailyMotivationResponseDto> {
    return this.dailyBriefService.generateMotivation(user.sub, dto.notes);
  }
}
