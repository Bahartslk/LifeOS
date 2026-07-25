import { forwardRef, Module } from '@nestjs/common';
import { AiModule } from '../ai/ai.module';
import { PlannerModule } from '../planner/planner.module';
import { TravelAiController } from './ai/travel-ai.controller';
import { TravelAiService } from './ai/travel-ai.service';
import { TravelController } from './travel.controller';
import { ItineraryController } from './itinerary.controller';
import { TravelService } from './travel.service';
import { ItineraryService } from './itinerary.service';
import { TripsRepository } from './repositories/trips.repository';
import { ItineraryItemsRepository } from './repositories/itinerary-items.repository';

/**
 * Sprint 16.0 (Travel Backend): trip CRUD, itinerary CRUD + reorder, and
 * the dashboard, per docs/15-api-design.md#trips-travel. Owns `trips` and
 * `itinerary_items` (docs/14-database-design.md#entities) — named `travel`
 * rather than docs/14's `trips` to match the mobile client's
 * `features/travel` module name, per CLAUDE.md's "consistency with
 * existing patterns" rule applied across the whole stack.
 *
 * Imports `PlannerModule` for `ItineraryService`'s Planner-task
 * integration (`PlannerService.createIntegrationTask`/`updateTask`/
 * `deleteTask`) — the same cross-module-service-injection pattern
 * `AuthModule` established with `UsersModule`.
 *
 * `TravelService` is exported (added in Sprint 18A) so `AiModule`'s
 * `ContextBuilder` can inject it directly for `getDashboard`, the same
 * cross-module-service-injection pattern used everywhere else in this
 * codebase — a pure DI wiring addition, no behavior change to any existing
 * route or consumer.
 *
 * `PlannerModule` is imported via `forwardRef()` (Sprint 19) — this pair
 * isn't circular on its own (Travel depends on Planner, never the
 * reverse), but Sprint 19 made `PlannerModule` and `AiModule` mutually
 * circular, which closed a 3-module cycle through this exact edge
 * (`TravelModule -> PlannerModule -> AiModule -> TravelModule`).
 *
 * Sprint 20 (Intelligent Travel) added `ai/` — six Travel-owned AI
 * capability endpoints (`TravelAiController`/`TravelAiService`) built on
 * `AiModule`'s existing `AiCapabilityService`, per that sprint's "Travel
 * owns its AI capabilities" instruction. This makes `TravelModule` and
 * `AiModule` a second, direct, mutual circular module dependency
 * (`AiModule` already imports this module for `ContextBuilder`) — resolved
 * with `forwardRef()` on both sides, the same pattern Sprint 19 used for
 * the `PlannerModule` pair, and re-verified empirically by booting the app
 * (build success alone does not prove Nest's runtime DI graph resolves).
 */
@Module({
  imports: [forwardRef(() => PlannerModule), forwardRef(() => AiModule)],
  controllers: [TravelController, ItineraryController, TravelAiController],
  providers: [
    TravelService,
    ItineraryService,
    TripsRepository,
    ItineraryItemsRepository,
    TravelAiService,
  ],
  exports: [TravelService],
})
export class TravelModule {}
