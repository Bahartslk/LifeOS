import { ApiPropertyOptional, PartialType } from '@nestjs/swagger';
import { TaskStatus } from '@prisma/client';
import { IsEnum, IsOptional } from 'class-validator';
import { CreateTaskDto } from './create-task.dto';

/**
 * Every `CreateTaskDto` field, but optional (PATCH semantics — only supplied
 * fields change) — via `PartialType` rather than a hand-duplicated DTO,
 * per CLAUDE.md's "avoid duplicated code" rule. Adds `status`, which
 * `CreateTaskDto` deliberately omits (every task is created `TODO`; see
 * `PlannerService.createTask`).
 *
 * No `dueTime`-clearing support: once set, `dueTime` can be changed to
 * another time but not explicitly cleared back to "all-day" through this
 * endpoint — not requested this sprint, and PATCH's "omit = unchanged"
 * convention has no natural way to also mean "clear" without a second
 * signal (e.g. an explicit `null`), which would need its own validation
 * story. A real requirement, not a decision to make speculatively now.
 */
export class UpdateTaskDto extends PartialType(CreateTaskDto) {
  @ApiPropertyOptional({ enum: TaskStatus })
  @IsOptional()
  @IsEnum(TaskStatus)
  status?: TaskStatus;
}
