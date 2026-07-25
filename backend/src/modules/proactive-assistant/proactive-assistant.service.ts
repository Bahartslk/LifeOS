import { Injectable } from '@nestjs/common';
import { AiCapabilityService } from '../ai/capabilities/ai-capability.service';
import {
  OPPORTUNITY_DETECTION,
  PROACTIVE_SUGGESTIONS,
  REMINDER_RECOMMENDATIONS,
  SMART_INSIGHTS,
} from './capabilities/proactive-assistant.capabilities';
import { SuggestionDto } from './dto/suggestion.dto';
import {
  PrioritizationResult,
  prioritizeSuggestions,
} from './prioritization/suggestion-prioritizer.util';

/**
 * The thin layer between `ProactiveAssistantController` and the shared
 * `AiCapabilityService.run` executor — the same "controller stays thin,
 * nothing here duplicates AI infrastructure" split every other
 * capability-owning module in this codebase follows (`PlannerAiService`,
 * `TravelAiService`, `DailyBriefService`). `prioritize` is the one method
 * that does not call `AiCapabilityService` at all: it is a deterministic
 * algorithm over caller-supplied data, not another AI call.
 *
 * Nothing here writes to `Task`/`TaskList`/`Trip`/`ItineraryItem`, creates a
 * reminder, or sends a notification — every result is a suggestion the
 * client decides whether to act on.
 */
@Injectable()
export class ProactiveAssistantService {
  constructor(private readonly capabilityService: AiCapabilityService) {}

  getSuggestions(userId: string, notes?: string) {
    return this.capabilityService.run(PROACTIVE_SUGGESTIONS, userId, notes);
  }

  getOpportunities(userId: string, notes?: string) {
    return this.capabilityService.run(OPPORTUNITY_DETECTION, userId, notes);
  }

  getReminders(userId: string, notes?: string) {
    return this.capabilityService.run(REMINDER_RECOMMENDATIONS, userId, notes);
  }

  getInsights(userId: string, notes?: string) {
    return this.capabilityService.run(SMART_INSIGHTS, userId, notes);
  }

  prioritize(suggestions: SuggestionDto[]): PrioritizationResult {
    return prioritizeSuggestions(suggestions);
  }
}
