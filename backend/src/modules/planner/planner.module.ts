import { forwardRef, Module } from '@nestjs/common';
import { AiModule } from '../ai/ai.module';
import { UsersModule } from '../users/users.module';
import { PlannerAiController } from './ai/planner-ai.controller';
import { PlannerAiService } from './ai/planner-ai.service';
import { PlannerController } from './planner.controller';
import { PlannerService } from './planner.service';
import { TasksRepository } from './repositories/tasks.repository';
import { TaskListsRepository } from './repositories/task-lists.repository';

/**
 * Sprint 15.0 (Planner Backend): the full task CRUD + dashboard API, per
 * docs/15-api-design.md#tasks-planner. Owns `task_lists` and `tasks`
 * (docs/14-database-design.md#entities) — named `planner` rather than
 * docs/14's `tasks` to match the mobile client's `features/planner` module
 * name, per CLAUDE.md's "consistency with existing patterns" rule applied
 * across the whole stack.
 *
 * `PlannerService` is exported so Sprint 16.0's `TravelModule` (and any
 * future feature module) can inject it directly — the same
 * cross-module-service-injection pattern `AuthModule` already established
 * with `UsersModule`/`UsersService`. See `PlannerService.createIntegrationTask`.
 *
 * Sprint 19 (Intelligent Planner) added `ai/` — six Planner-owned AI
 * capability endpoints (`PlannerAiController`/`PlannerAiService`) built on
 * `AiModule`'s existing `AiCapabilityService`, per that sprint's "Planner
 * owns these capabilities" instruction. This makes `PlannerModule` and
 * `AiModule` a genuine, mutual circular module dependency (`AiModule`
 * already imports `PlannerModule` for `ContextBuilder`'s
 * `PlannerService.getDashboard` access) — resolved with `forwardRef()` on
 * both sides rather than restructured away, since the dependency itself is
 * real in both directions. See `AiModule`'s own doc comment for the full
 * reasoning.
 *
 * Imports `UsersModule` (plain, no `forwardRef`) so `PlannerService.getDashboard`
 * can resolve "today" in the caller's `User.timezone`. `UsersModule` imports
 * nothing, so this adds no new cycle.
 */
@Module({
  imports: [forwardRef(() => AiModule), UsersModule],
  controllers: [PlannerController, PlannerAiController],
  providers: [PlannerService, TasksRepository, TaskListsRepository, PlannerAiService],
  exports: [PlannerService],
})
export class PlannerModule {}
