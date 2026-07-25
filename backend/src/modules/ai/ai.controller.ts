import { Body, Controller, HttpCode, HttpStatus, Post, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiOkResponse, ApiResponse, ApiTags } from '@nestjs/swagger';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { JwtAuthGuard } from '../../common/guards/jwt-auth.guard';
import { JwtPayload } from '../../common/strategies/jwt.strategy';
import { AiCapabilityService } from './capabilities/ai-capability.service';
import {
  DAILY_BRIEF,
  PLANNER_ANALYZE,
  PLANNER_SUGGEST,
  TRAVEL_SUGGEST,
} from './capabilities/capability-definitions';
import { AiCapabilityRequestDto } from './dto/ai-capability-request.dto';
import { DailyBriefResponseDto, DailyBriefResultDto } from './dto/daily-brief-response.dto';
import {
  PlannerAnalyzeResponseDto,
  PlannerAnalyzeResultDto,
} from './dto/planner-analyze-response.dto';
import {
  PlannerSuggestResponseDto,
  PlannerSuggestResultDto,
} from './dto/planner-suggest-response.dto';
import {
  TravelSuggestResponseDto,
  TravelSuggestResultDto,
} from './dto/travel-suggest-response.dto';

/**
 * Sprint 18B's first real AI endpoints — four capability-scoped routes,
 * deliberately not a generic `/ai/chat`, per this sprint's "the objective
 * is NOT to build a generic chatbot" design rule. Thin per CLAUDE.md's
 * "business logic stays in use cases, not controllers" — every handler is
 * parse-DTO, call `AiCapabilityService.run` with the right declarative
 * `AiCapabilityDefinition`, return. Guarded the same way every other
 * controller in this codebase is (`@UseGuards(JwtAuthGuard)` at the
 * controller level; `@CurrentUser()`'s `sub` claim is the only source of
 * `userId` here, same as Planner/Travel/Users).
 */
@ApiTags('ai')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('ai')
export class AiController {
  constructor(private readonly capabilityService: AiCapabilityService) {}

  @Post('planner/analyze')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: "Analyze the caller's Planner data (patterns, overdue risk, workload balance)",
  })
  @ApiOkResponse({ type: PlannerAnalyzeResponseDto })
  @ApiResponse({
    status: 503,
    description:
      'No AI provider is currently available, or every provider in the fallback chain failed.',
  })
  @ApiResponse({
    status: 502,
    description: "The selected provider's response could not be parsed as the expected JSON shape.",
  })
  analyzePlanner(
    @CurrentUser() user: JwtPayload,
    @Body() dto: AiCapabilityRequestDto,
  ): Promise<PlannerAnalyzeResponseDto> {
    return this.capabilityService.run<PlannerAnalyzeResultDto>(
      PLANNER_ANALYZE,
      user.sub,
      dto.notes,
    );
  }

  @Post('planner/suggest')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: "Suggest 1-3 new Planner tasks based on the caller's current data" })
  @ApiOkResponse({ type: PlannerSuggestResponseDto })
  @ApiResponse({
    status: 503,
    description:
      'No AI provider is currently available, or every provider in the fallback chain failed.',
  })
  @ApiResponse({
    status: 502,
    description: "The selected provider's response could not be parsed as the expected JSON shape.",
  })
  suggestPlannerTasks(
    @CurrentUser() user: JwtPayload,
    @Body() dto: AiCapabilityRequestDto,
  ): Promise<PlannerSuggestResponseDto> {
    return this.capabilityService.run<PlannerSuggestResultDto>(
      PLANNER_SUGGEST,
      user.sub,
      dto.notes,
    );
  }

  @Post('travel/suggest')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: "Suggest 1-3 travel ideas based on the caller's current Travel data" })
  @ApiOkResponse({ type: TravelSuggestResponseDto })
  @ApiResponse({
    status: 503,
    description:
      'No AI provider is currently available, or every provider in the fallback chain failed.',
  })
  @ApiResponse({
    status: 502,
    description: "The selected provider's response could not be parsed as the expected JSON shape.",
  })
  suggestTravel(
    @CurrentUser() user: JwtPayload,
    @Body() dto: AiCapabilityRequestDto,
  ): Promise<TravelSuggestResponseDto> {
    return this.capabilityService.run<TravelSuggestResultDto>(TRAVEL_SUGGEST, user.sub, dto.notes);
  }

  @Post('daily-brief')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: "Generate a personalized daily summary spanning the caller's Planner and Travel data",
  })
  @ApiOkResponse({ type: DailyBriefResponseDto })
  @ApiResponse({
    status: 503,
    description:
      'No AI provider is currently available, or every provider in the fallback chain failed.',
  })
  @ApiResponse({
    status: 502,
    description: "The selected provider's response could not be parsed as the expected JSON shape.",
  })
  dailyBrief(
    @CurrentUser() user: JwtPayload,
    @Body() dto: AiCapabilityRequestDto,
  ): Promise<DailyBriefResponseDto> {
    return this.capabilityService.run<DailyBriefResultDto>(DAILY_BRIEF, user.sub, dto.notes);
  }
}
