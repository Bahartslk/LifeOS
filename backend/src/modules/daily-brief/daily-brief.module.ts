import { Module } from '@nestjs/common';
import { AiModule } from '../ai/ai.module';
import { DailyBriefController } from './daily-brief.controller';
import { DailyBriefService } from './daily-brief.service';

/**
 * Sprint 21 (Daily Brief Intelligence) — the platform's first genuinely
 * cross-module AI feature, and its own brand-new top-level module: no
 * single existing business module (Planner, Travel) owns "the user's
 * whole day," so this capability set is a new module rather than being
 * bolted onto either one, mirroring the same "capability-owning module"
 * pattern Sprint 19/20 established for Planner/Travel.
 *
 * Imports only `AiModule`, for `AiCapabilityService` — no `PlannerModule`
 * or `TravelModule` import is needed, since every capability here reasons
 * purely over the caller's own aggregate context (Planner + Travel both
 * loaded via the unmodified `ContextBuilder`, internally, inside
 * `AiService.generate`) rather than fetching one specific existing
 * `Task`/`Trip` the way Sprint 19's `breakdownTask` or Sprint 20's
 * `generateItinerary`/`analyzeBudget`/etc. do. This also means this module
 * introduces **no new circular dependency** — unlike `PlannerModule`/
 * `TravelModule`, `AiModule` never needs anything from `DailyBriefModule`,
 * so the import here is one-directional, no `forwardRef()` required.
 */
@Module({
  imports: [AiModule],
  controllers: [DailyBriefController],
  providers: [DailyBriefService],
})
export class DailyBriefModule {}
