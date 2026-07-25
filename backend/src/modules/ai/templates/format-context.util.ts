import {
  AiItineraryItemSummary,
  AiPlannerContext,
  AiPlannerTaskSummary,
  AiTravelContext,
  AiTravelTripSummary,
} from '../types/ai-context.types';

/**
 * Pure, deterministic text formatting for prompt injection — no
 * provider-specific formatting tricks, just a compact, readable block a
 * template's `renderSystem` can drop into the system prompt. Kept separate
 * from `templates/*.template.ts` since every template that includes
 * Planner or Travel context needs the identical formatting.
 */

function formatTask(task: AiPlannerTaskSummary): string {
  const time = task.dueTime ? ` ${task.dueTime}` : '';
  return `- [${task.status}/${task.priority}] ${task.title} (${task.dueDate}${time})`;
}

export function formatPlannerContext(planner: AiPlannerContext): string {
  const today = planner.todayTasks.map(formatTask).join('\n') || '(none)';
  const upcoming = planner.upcomingTasks.map(formatTask).join('\n') || '(none)';
  return [
    `Today's tasks:\n${today}`,
    `Upcoming tasks:\n${upcoming}`,
    `Progress: ${planner.completedCount} completed, ${planner.pendingCount} pending (${planner.progressPercentage}%).`,
  ].join('\n\n');
}

function formatTrip(trip: AiTravelTripSummary): string {
  return `- [${trip.status}] ${trip.title} — ${trip.destination}, ${trip.country} (${trip.startDate} to ${trip.endDate})`;
}

function formatItineraryItem(item: AiItineraryItemSummary): string {
  const time = item.startTime ? ` ${item.startTime}` : '';
  const location = item.location ? ` @ ${item.location}` : '';
  return `- [${item.type}] ${item.title} (${item.date}${time}${location})`;
}

export function formatTravelContext(travel: AiTravelContext): string {
  const upcoming = travel.upcomingTrips.map(formatTrip).join('\n') || '(none)';
  const active = travel.activeTrips.map(formatTrip).join('\n') || '(none)';
  const itinerary = travel.upcomingItineraryItems.map(formatItineraryItem).join('\n') || '(none)';
  return [
    `Upcoming trips:\n${upcoming}`,
    `Active trips:\n${active}`,
    `Next destination: ${travel.nextDestination ?? '(none)'}`,
    `Upcoming itinerary items:\n${itinerary}`,
    `Total trip count: ${travel.tripCount}.`,
  ].join('\n\n');
}
