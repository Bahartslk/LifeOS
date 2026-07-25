import { Injectable } from '@nestjs/common';
import { AiCapabilityService } from '../ai/capabilities/ai-capability.service';
import {
  DAILY_MOTIVATION,
  DAILY_PRIORITIES,
  DAILY_SUMMARY,
  PREPARATION_SUGGESTIONS,
  SCHEDULE_CONFLICTS,
  UPCOMING_RISK_ANALYSIS,
} from './capabilities/daily-brief.capabilities';

/**
 * The thin layer between `DailyBriefController` and the shared
 * `AiCapabilityService.run` executor — simpler than
 * `modules/planner/ai/planner-ai.service.ts`/`modules/travel/ai/travel-ai.service.ts`
 * because every Daily Brief capability reasons purely over the caller's
 * own aggregate context (already assembled by the unmodified
 * `ContextBuilder` inside `AiService.generate`) — none of them operate on
 * one specific existing `Task`/`Trip`, so there is no ownership check or
 * entity fetch to perform here, and no `primaryInput` to compose. This
 * class exists at all only to keep `DailyBriefController` free of direct
 * `AiCapabilityService` calls, the same "controller stays thin" split
 * every other module in this codebase already follows.
 *
 * Nothing here writes to `Task`/`TaskList`/`Trip`/`ItineraryItem` — every
 * result is a suggestion the client decides whether to act on.
 */
@Injectable()
export class DailyBriefService {
  constructor(private readonly capabilityService: AiCapabilityService) {}

  getDailySummary(userId: string, notes?: string) {
    return this.capabilityService.run(DAILY_SUMMARY, userId, notes);
  }

  detectScheduleConflicts(userId: string, notes?: string) {
    return this.capabilityService.run(SCHEDULE_CONFLICTS, userId, notes);
  }

  suggestPreparation(userId: string, notes?: string) {
    return this.capabilityService.run(PREPARATION_SUGGESTIONS, userId, notes);
  }

  analyzeUpcomingRisks(userId: string, notes?: string) {
    return this.capabilityService.run(UPCOMING_RISK_ANALYSIS, userId, notes);
  }

  suggestDailyPriorities(userId: string, notes?: string) {
    return this.capabilityService.run(DAILY_PRIORITIES, userId, notes);
  }

  generateMotivation(userId: string, notes?: string) {
    return this.capabilityService.run(DAILY_MOTIVATION, userId, notes);
  }
}
