import { ApiProperty } from '@nestjs/swagger';
import { TaskResponseDto } from './task-response.dto';

/**
 * `GET /planner/dashboard`'s response — the exact 7 fields Sprint 15.0's
 * brief lists. Not identical to either of mobile's two existing
 * dashboard-shaped models (`PlannerDashboard`, AI Assistant's
 * `AiTaskContext`) — it's a new, backend-computed synthesis of both (see
 * `PlannerService.getDashboard`'s doc comment for the exact derivation of
 * each field, matching `BuildAiTaskContextUseCase`'s classification logic
 * exactly). The mobile client will need a small adapter to consume this
 * once it switches off `FakePlannerRepository` — out of this sprint's
 * "do not modify mobile code" scope.
 */
export class PlannerDashboardResponseDto {
  @ApiProperty({ type: [TaskResponseDto] })
  todayTasks!: TaskResponseDto[];

  @ApiProperty({ type: [TaskResponseDto] })
  upcomingTasks!: TaskResponseDto[];

  @ApiProperty({
    description:
      "Count of ALL the caller's active (non-deleted) tasks with status DONE — not scoped to today/upcoming.",
  })
  completedCount!: number;

  @ApiProperty({
    description: 'totalCount - completedCount, over the same all-time active task set.',
  })
  pendingCount!: number;

  @ApiProperty({
    description:
      '0-100, round(completedCount / totalCount * 100); 0 if the caller has no tasks yet.',
  })
  progressPercentage!: number;

  @ApiProperty({
    type: [TaskResponseDto],
    description:
      'Incomplete (status != DONE), priority HIGH, drawn from todayTasks + upcomingTasks.',
  })
  highPriorityTasks!: TaskResponseDto[];

  @ApiProperty({
    type: [TaskResponseDto],
    description: 'source = TRAVEL, drawn from todayTasks + upcomingTasks, any status.',
  })
  travelTasks!: TaskResponseDto[];
}
