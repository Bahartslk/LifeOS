import {
  Body,
  Controller,
  HttpCode,
  HttpStatus,
  Param,
  ParseUUIDPipe,
  Post,
  UseGuards,
} from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiNotFoundResponse,
  ApiOkResponse,
  ApiOperation,
  ApiTags,
} from '@nestjs/swagger';
import { CurrentUser } from '../../../common/decorators/current-user.decorator';
import { NaturalLanguageInputDto } from '../../../common/dto/natural-language-input.dto';
import { JwtAuthGuard } from '../../../common/guards/jwt-auth.guard';
import { JwtPayload } from '../../../common/strategies/jwt.strategy';
import { AiCapabilityRequestDto } from '../../ai/dto/ai-capability-request.dto';
import { ApiAiProviderErrorResponses } from '../../ai/swagger/ai-provider-error-responses.decorator';
import { DeadlineExtractionResponseDto } from './dto/deadline-extraction-response.dto';
import { PrioritySuggestionInputDto } from './dto/priority-suggestion-input.dto';
import { PrioritySuggestionResponseDto } from './dto/priority-suggestion-response.dto';
import { ScheduleAnalysisResponseDto } from './dto/schedule-analysis-response.dto';
import { SmartTagsResponseDto } from './dto/smart-tags-response.dto';
import { TaskBreakdownResponseDto } from './dto/task-breakdown-response.dto';
import { TaskContentInputDto } from './dto/task-content-input.dto';
import { TaskDraftResponseDto } from './dto/task-draft-response.dto';
import { PlannerAiService } from './planner-ai.service';

/**
 * Sprint 19 (Intelligent Planner) — six Planner-owned AI capability
 * endpoints under `/planner/ai/...`, distinct from `AiModule`'s own
 * `/ai/planner/analyze`/`/ai/planner/suggest` (Sprint 18B) — this
 * controller lives in, and is registered by, `PlannerModule`, per this
 * sprint's explicit "Planner owns these capabilities" instruction; it
 * reuses `AiCapabilityService` (via `PlannerAiService`) rather than
 * duplicating any AI infrastructure. Thin per CLAUDE.md's "business logic
 * stays in use cases, not controllers" — every handler is parse-DTO, call
 * `PlannerAiService`, return. Guarded the same way every other controller
 * in this codebase is.
 *
 * None of these routes ever writes to `Task`/`TaskList` — every response
 * is a draft or suggestion; the client decides whether to turn one into a
 * real `POST`/`PATCH /planner/tasks` call.
 */
@ApiTags('planner')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('planner/ai')
export class PlannerAiController {
  constructor(private readonly plannerAiService: PlannerAiService) {}

  @Post('tasks/draft')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Turn natural language into a structured task draft (not saved)' })
  @ApiOkResponse({ type: TaskDraftResponseDto })
  @ApiAiProviderErrorResponses()
  createTaskDraft(
    @CurrentUser() user: JwtPayload,
    @Body() dto: NaturalLanguageInputDto,
  ): Promise<TaskDraftResponseDto> {
    return this.plannerAiService.createTaskDraft(user.sub, dto.text);
  }

  @Post('tasks/:id/breakdown')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Suggest subtasks for an existing task (not saved)' })
  @ApiOkResponse({ type: TaskBreakdownResponseDto })
  @ApiNotFoundResponse({ description: "Task does not exist or isn't the caller's." })
  @ApiAiProviderErrorResponses()
  breakdownTask(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseUUIDPipe) id: string,
    @Body() dto: AiCapabilityRequestDto,
  ): Promise<TaskBreakdownResponseDto> {
    return this.plannerAiService.breakdownTask(user.sub, id, dto.notes);
  }

  @Post('schedule/analysis')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary:
      "Analyze the caller's Planner schedule: overloaded/free days, conflicts, workload balance",
  })
  @ApiOkResponse({ type: ScheduleAnalysisResponseDto })
  @ApiAiProviderErrorResponses()
  analyzeSchedule(
    @CurrentUser() user: JwtPayload,
    @Body() dto: AiCapabilityRequestDto,
  ): Promise<ScheduleAnalysisResponseDto> {
    return this.plannerAiService.analyzeSchedule(user.sub, dto.notes);
  }

  @Post('priority-suggestion')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary:
      "Suggest a priority for a task, based on its due date, wording, and the caller's current workload",
  })
  @ApiOkResponse({ type: PrioritySuggestionResponseDto })
  @ApiAiProviderErrorResponses()
  suggestPriority(
    @CurrentUser() user: JwtPayload,
    @Body() dto: PrioritySuggestionInputDto,
  ): Promise<PrioritySuggestionResponseDto> {
    return this.plannerAiService.suggestPriority(user.sub, dto);
  }

  @Post('tags')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Generate suggested category tags for a task' })
  @ApiOkResponse({ type: SmartTagsResponseDto })
  @ApiAiProviderErrorResponses()
  suggestTags(
    @CurrentUser() user: JwtPayload,
    @Body() dto: TaskContentInputDto,
  ): Promise<SmartTagsResponseDto> {
    return this.plannerAiService.suggestTags(user.sub, dto);
  }

  @Post('deadline-extraction')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Extract a normalized date/time from natural language, including relative expressions',
  })
  @ApiOkResponse({ type: DeadlineExtractionResponseDto })
  @ApiAiProviderErrorResponses()
  extractDeadline(
    @CurrentUser() user: JwtPayload,
    @Body() dto: NaturalLanguageInputDto,
  ): Promise<DeadlineExtractionResponseDto> {
    return this.plannerAiService.extractDeadline(user.sub, dto.text);
  }
}
