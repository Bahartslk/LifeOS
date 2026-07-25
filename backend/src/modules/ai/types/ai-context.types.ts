/**
 * The provider-independent context model every prompt template renders
 * from (see `templates/`). Field set matches this sprint's brief exactly:
 * User, Preferences, Planner, Travel, Metadata (incl. Timestamp),
 * Conversation.
 */

export interface AiUserContext {
  displayName: string;
}

export interface AiUserPreferences {
  language: string;
  timezone: string;
  themePreference: string;
}

export interface AiPlannerTaskSummary {
  title: string;
  dueDate: string;
  dueTime: string | null;
  priority: string;
  status: string;
}

export interface AiPlannerContext {
  todayTasks: AiPlannerTaskSummary[];
  upcomingTasks: AiPlannerTaskSummary[];
  completedCount: number;
  pendingCount: number;
  progressPercentage: number;
}

export interface AiTravelTripSummary {
  title: string;
  destination: string;
  country: string;
  startDate: string;
  endDate: string;
  status: string;
}

export interface AiItineraryItemSummary {
  title: string;
  date: string;
  startTime: string | null;
  location: string | null;
  type: string;
}

export interface AiTravelContext {
  upcomingTrips: AiTravelTripSummary[];
  activeTrips: AiTravelTripSummary[];
  nextDestination: string | null;
  upcomingItineraryItems: AiItineraryItemSummary[];
  tripCount: number;
}

export interface AiContextMetadata {
  userId: string;
  /** ISO-8601 — this model's "Timestamp" field, folded into metadata rather than a top-level sibling. */
  generatedAt: string;
}

export interface AiConversationTurn {
  role: 'user' | 'assistant';
  content: string;
}

/**
 * `planner`/`travel` are `null` when `ContextBuilder.build` was called with
 * that section excluded (see `BuildContextOptions`) — never an empty
 * object standing in for "no data", so a template can tell "the user asked
 * to skip this" apart from "the user genuinely has zero tasks/trips".
 *
 * `conversation` is reserved and always `[]` in Sprint 18A — no chat
 * endpoint or persistence exists yet (explicitly out of scope this
 * sprint), but the field is part of the model now so a future
 * conversational feature only has to populate it, not add it.
 */
export interface AiContext {
  user: AiUserContext;
  preferences: AiUserPreferences;
  planner: AiPlannerContext | null;
  travel: AiTravelContext | null;
  metadata: AiContextMetadata;
  conversation: AiConversationTurn[];
}
