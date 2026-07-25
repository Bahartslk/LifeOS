import { Injectable } from '@nestjs/common';
import { AiCapabilityService } from '../../ai/capabilities/ai-capability.service';
import { PlannerService } from '../planner.service';
import { TaskResponseDto } from '../dto/task-response.dto';
import {
  DEADLINE_EXTRACTION,
  PRIORITY_SUGGESTION,
  SCHEDULE_ANALYSIS,
  SMART_TAGS,
  TASK_BREAKDOWN,
  TASK_DRAFT,
} from './capabilities/planner-ai.capabilities';
import { PrioritySuggestionInputDto } from './dto/priority-suggestion-input.dto';
import { TaskContentInputDto } from './dto/task-content-input.dto';

/**
 * The ownership-check + prompt-composition layer between `PlannerAiController`
 * and the shared `AiCapabilityService.run` executor — the same split every
 * other Planner concern already uses (controllers stay thin; this class
 * holds the actual logic). Two kinds of capability here:
 *
 * - Context-only (`analyzeSchedule`): the caller's own Planner data, loaded
 *   automatically by `ContextBuilder` — no per-call input needed beyond an
 *   optional `notes` steer, identical to Sprint 18B's `planner-analyze`.
 * - Input-driven (everything else): operates on specific text/fields the
 *   caller supplies for *this* call, passed through as `AiCapabilityService
 *   .run`'s `primaryInput` (Sprint 19's small, additive extension to that
 *   method) — never part of any user's stored context.
 *
 * `breakdownTask` is the one method that touches persisted data at all,
 * and only to READ it: `PlannerService.getTask` enforces the same
 * ownership check (404, never 403, for another user's task) every other
 * Planner route already uses, before that task's fields are ever placed in
 * a prompt. Nothing here writes to `Task`/`TaskList` — every result is a
 * draft or suggestion the client reviews before calling the existing
 * `POST`/`PATCH /planner/tasks` routes itself.
 */
@Injectable()
export class PlannerAiService {
  constructor(
    private readonly capabilityService: AiCapabilityService,
    private readonly plannerService: PlannerService,
  ) {}

  createTaskDraft(userId: string, text: string) {
    return this.capabilityService.run(TASK_DRAFT, userId, undefined, text);
  }

  async breakdownTask(userId: string, taskId: string, notes?: string) {
    const task = await this.plannerService.getTask(userId, taskId);
    return this.capabilityService.run(
      TASK_BREAKDOWN,
      userId,
      notes,
      this.describeTaskForPrompt(task),
    );
  }

  analyzeSchedule(userId: string, notes?: string) {
    return this.capabilityService.run(SCHEDULE_ANALYSIS, userId, notes);
  }

  suggestPriority(userId: string, input: PrioritySuggestionInputDto) {
    return this.capabilityService.run(
      PRIORITY_SUGGESTION,
      userId,
      undefined,
      this.describeDraftForPrompt(input),
    );
  }

  suggestTags(userId: string, input: TaskContentInputDto) {
    return this.capabilityService.run(
      SMART_TAGS,
      userId,
      undefined,
      this.describeDraftForPrompt(input),
    );
  }

  extractDeadline(userId: string, text: string) {
    return this.capabilityService.run(DEADLINE_EXTRACTION, userId, undefined, text);
  }

  private describeTaskForPrompt(task: TaskResponseDto): string {
    const lines = [
      `Title: ${task.title}`,
      task.description ? `Description: ${task.description}` : undefined,
      `Due: ${task.dueDate}${task.dueTime ? ` ${task.dueTime}` : ''}`,
      `Priority: ${task.priority}`,
    ];
    return lines.filter((line): line is string => Boolean(line)).join('\n');
  }

  private describeDraftForPrompt(input: TaskContentInputDto | PrioritySuggestionInputDto): string {
    const lines = [
      `Title: ${input.title}`,
      input.description ? `Description: ${input.description}` : undefined,
      'dueDate' in input
        ? `Due: ${input.dueDate}${input.dueTime ? ` ${input.dueTime}` : ''}`
        : undefined,
    ];
    return lines.filter((line): line is string => Boolean(line)).join('\n');
  }
}
