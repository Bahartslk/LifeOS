import { Injectable } from '@nestjs/common';
import { PlannerService } from '../../planner/planner.service';
import { TravelService } from '../../travel/travel.service';
import { UsersService } from '../../users/users.service';
import {
  toPlannerContext,
  toTravelContext,
  toUserContext,
  toUserPreferences,
} from '../mapper/context.mapper';
import { AiContext } from '../types/ai-context.types';

export interface BuildContextOptions {
  /** Default `true`. Set `false` for a request that has nothing to do with Planner, to skip the dashboard query entirely (this module's "avoid unnecessary context loading" / "prepare lazy context assembly" rule). */
  includePlanner?: boolean;
  /** Default `true`. Same as `includePlanner`, for Travel. */
  includeTravel?: boolean;
}

/**
 * Assembles a provider-independent `AiContext` from existing business
 * services only — never a repository directly, per this sprint's "AI may
 * depend on UsersService, PlannerService, TravelService. Nothing else."
 * Reuses each service's own dashboard/profile method rather than a raw
 * list query: those are already bounded to "today + upcoming" / "current
 * profile", never a user's full Planner or Travel history, so this class
 * adds no new query shape — it only re-shapes and further trims that
 * output for prompt injection (see `mapper/context.mapper.ts`'s
 * `MAX_CONTEXT_*` truncation).
 */
@Injectable()
export class ContextBuilder {
  constructor(
    private readonly usersService: UsersService,
    private readonly plannerService: PlannerService,
    private readonly travelService: TravelService,
  ) {}

  async build(userId: string, options: BuildContextOptions = {}): Promise<AiContext> {
    const includePlanner = options.includePlanner ?? true;
    const includeTravel = options.includeTravel ?? true;

    const [profile, plannerDashboard, travelDashboard] = await Promise.all([
      this.usersService.getProfile(userId),
      includePlanner ? this.plannerService.getDashboard(userId) : Promise.resolve(null),
      includeTravel ? this.travelService.getDashboard(userId) : Promise.resolve(null),
    ]);

    return {
      user: toUserContext(profile),
      preferences: toUserPreferences(profile),
      planner: plannerDashboard ? toPlannerContext(plannerDashboard) : null,
      travel: travelDashboard ? toTravelContext(travelDashboard) : null,
      metadata: { userId, generatedAt: new Date().toISOString() },
      conversation: [],
    };
  }
}
