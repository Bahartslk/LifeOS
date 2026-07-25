import { Module } from '@nestjs/common';
import { AiModule } from '../ai/ai.module';
import { ProactiveAssistantController } from './proactive-assistant.controller';
import { ProactiveAssistantService } from './proactive-assistant.service';

/**
 * Sprint 22 (Proactive Assistant Engine) — the platform's first module
 * built specifically to generate suggestions without being asked, sitting
 * above Planner, Travel, and Daily Brief rather than owned by any one of
 * them, the same "new top-level module, not bolted onto an existing one"
 * call Sprint 21 made for `DailyBriefModule`.
 *
 * Imports only `AiModule`, for `AiCapabilityService` — no `PlannerModule`
 * or `TravelModule` import is needed, since every AI-backed capability here
 * reasons purely over the caller's own aggregate context (Planner + Travel
 * both loaded via the unmodified `ContextBuilder`, internally, inside
 * `AiService.generate`) rather than fetching one specific existing
 * `Task`/`Trip`. This also means this module introduces **no new circular
 * dependency** — the same reasoning `DailyBriefModule`'s own doc comment
 * gives: `AiModule` never needs anything from this module, so the import
 * here is one-directional, no `forwardRef()` required.
 */
@Module({
  imports: [AiModule],
  controllers: [ProactiveAssistantController],
  providers: [ProactiveAssistantService],
})
export class ProactiveAssistantModule {}
