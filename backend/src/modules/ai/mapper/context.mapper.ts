import { PlannerDashboardResponseDto } from '../../planner/dto/dashboard-response.dto';
import { TaskResponseDto } from '../../planner/dto/task-response.dto';
import { TravelDashboardResponseDto } from '../../travel/dto/dashboard-response.dto';
import { ItineraryItemResponseDto } from '../../travel/dto/itinerary-item-response.dto';
import { TripResponseDto } from '../../travel/dto/trip-response.dto';
import { UserProfileResponseDto } from '../../users/dto/user-profile-response.dto';
import {
  MAX_CONTEXT_ITINERARY_ITEMS,
  MAX_CONTEXT_TASKS_PER_SECTION,
  MAX_CONTEXT_TRIPS_PER_SECTION,
} from '../constants/ai.constants';
import {
  AiItineraryItemSummary,
  AiPlannerContext,
  AiPlannerTaskSummary,
  AiTravelContext,
  AiTravelTripSummary,
  AiUserContext,
  AiUserPreferences,
} from '../types/ai-context.types';

/**
 * Pure shape-transformation functions — `ContextBuilder` owns orchestration
 * (which services to call, in what order), this file owns turning their
 * existing response DTOs into the smaller, provider-independent `AiContext`
 * sub-shapes. Every list is truncated to this module's `MAX_CONTEXT_*`
 * constants here, in one place, rather than relying on each template to
 * remember to slice.
 */

export function toUserContext(profile: UserProfileResponseDto): AiUserContext {
  return { displayName: profile.displayName };
}

export function toUserPreferences(profile: UserProfileResponseDto): AiUserPreferences {
  return {
    language: profile.language,
    timezone: profile.timezone,
    themePreference: profile.themePreference,
  };
}

function toTaskSummary(task: TaskResponseDto): AiPlannerTaskSummary {
  return {
    title: task.title,
    dueDate: task.dueDate,
    dueTime: task.dueTime,
    priority: task.priority,
    status: task.status,
  };
}

export function toPlannerContext(dashboard: PlannerDashboardResponseDto): AiPlannerContext {
  return {
    todayTasks: dashboard.todayTasks.slice(0, MAX_CONTEXT_TASKS_PER_SECTION).map(toTaskSummary),
    upcomingTasks: dashboard.upcomingTasks
      .slice(0, MAX_CONTEXT_TASKS_PER_SECTION)
      .map(toTaskSummary),
    completedCount: dashboard.completedCount,
    pendingCount: dashboard.pendingCount,
    progressPercentage: dashboard.progressPercentage,
  };
}

function toTripSummary(trip: TripResponseDto): AiTravelTripSummary {
  return {
    title: trip.title,
    destination: trip.destination,
    country: trip.country,
    startDate: trip.startDate,
    endDate: trip.endDate,
    status: trip.status,
  };
}

function toItineraryItemSummary(item: ItineraryItemResponseDto): AiItineraryItemSummary {
  return {
    title: item.title,
    date: item.date,
    startTime: item.startTime,
    location: item.location,
    type: item.type,
  };
}

export function toTravelContext(dashboard: TravelDashboardResponseDto): AiTravelContext {
  return {
    upcomingTrips: dashboard.upcomingTrips
      .slice(0, MAX_CONTEXT_TRIPS_PER_SECTION)
      .map(toTripSummary),
    activeTrips: dashboard.activeTrips.slice(0, MAX_CONTEXT_TRIPS_PER_SECTION).map(toTripSummary),
    nextDestination: dashboard.nextDestination,
    upcomingItineraryItems: dashboard.upcomingItineraryItems
      .slice(0, MAX_CONTEXT_ITINERARY_ITEMS)
      .map(toItineraryItemSummary),
    tripCount: dashboard.tripCount,
  };
}
