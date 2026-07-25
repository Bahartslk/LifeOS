import { Injectable } from '@nestjs/common';
import { AiCapabilityService } from '../../ai/capabilities/ai-capability.service';
import { ItineraryService } from '../itinerary.service';
import { TravelService } from '../travel.service';
import { TripResponseDto } from '../dto/trip-response.dto';
import { ItineraryItemResponseDto } from '../dto/itinerary-item-response.dto';
import {
  BUDGET_ANALYSIS,
  ITINERARY_GENERATION,
  PACKING_LIST,
  RISK_ANALYSIS,
  TRAVEL_TAGS,
  TRIP_DRAFT,
} from './capabilities/travel-ai.capabilities';
import { TravelTagsInputDto } from './dto/travel-tags-input.dto';

/**
 * The ownership-check + prompt-composition layer between `TravelAiController`
 * and the shared `AiCapabilityService.run` executor — the same split
 * `modules/planner/ai/planner-ai.service.ts` established in Sprint 19. Two
 * kinds of capability here:
 *
 * - Standalone (`createTripDraft`, `suggestTags`): operates on text/fields
 *   the caller supplies for *this* call — no existing `Trip` involved.
 * - Trip-driven (`generateItinerary`, `analyzeBudget`, `suggestPacking`,
 *   `analyzeRisk`): reads an existing trip (and, for risk analysis, its
 *   itinerary) via `TravelService.getTrip`/`ItineraryService.getItinerary`
 *   — both already ownership-checked (404, never 403, for another user's
 *   trip) — before that data is ever placed in a prompt.
 *
 * Nothing here writes to `Trip`/`ItineraryItem`; every result is a draft or
 * suggestion the client reviews before calling the existing
 * `POST`/`PATCH /travel/trips`/`.../itinerary` routes itself.
 */
@Injectable()
export class TravelAiService {
  constructor(
    private readonly capabilityService: AiCapabilityService,
    private readonly travelService: TravelService,
    private readonly itineraryService: ItineraryService,
  ) {}

  createTripDraft(userId: string, text: string) {
    return this.capabilityService.run(TRIP_DRAFT, userId, undefined, text);
  }

  async generateItinerary(userId: string, tripId: string, notes?: string) {
    const trip = await this.travelService.getTrip(userId, tripId);
    return this.capabilityService.run(
      ITINERARY_GENERATION,
      userId,
      notes,
      this.describeTripForPrompt(trip),
    );
  }

  async analyzeBudget(userId: string, tripId: string, notes?: string) {
    const trip = await this.travelService.getTrip(userId, tripId);
    return this.capabilityService.run(
      BUDGET_ANALYSIS,
      userId,
      notes,
      this.describeTripForPrompt(trip),
    );
  }

  async suggestPacking(userId: string, tripId: string, notes?: string) {
    const trip = await this.travelService.getTrip(userId, tripId);
    return this.capabilityService.run(
      PACKING_LIST,
      userId,
      notes,
      this.describeTripForPrompt(trip),
    );
  }

  async analyzeRisk(userId: string, tripId: string, notes?: string) {
    const [trip, items] = await Promise.all([
      this.travelService.getTrip(userId, tripId),
      this.itineraryService.getItinerary(userId, tripId),
    ]);
    const primaryInput = [
      this.describeTripForPrompt(trip),
      this.describeItineraryForPrompt(items),
    ].join('\n\n');
    return this.capabilityService.run(RISK_ANALYSIS, userId, notes, primaryInput);
  }

  suggestTags(userId: string, input: TravelTagsInputDto) {
    const lines = [
      `Destination: ${input.destination}`,
      input.description ? `Description: ${input.description}` : undefined,
    ];
    const primaryInput = lines.filter((line): line is string => Boolean(line)).join('\n');
    return this.capabilityService.run(TRAVEL_TAGS, userId, undefined, primaryInput);
  }

  private describeTripForPrompt(trip: TripResponseDto): string {
    const lines = [
      `Title: ${trip.title}`,
      trip.description ? `Description: ${trip.description}` : undefined,
      `Destination: ${trip.destination}, ${trip.country}`,
      `Dates: ${trip.startDate} to ${trip.endDate}`,
      `Status: ${trip.status}`,
    ];
    return lines.filter((line): line is string => Boolean(line)).join('\n');
  }

  private describeItineraryForPrompt(items: ItineraryItemResponseDto[]): string {
    if (items.length === 0) {
      return 'Itinerary: (no itinerary items exist for this trip yet)';
    }
    const lines = items.map((item) => {
      const time = item.startTime
        ? ` ${item.startTime}${item.endTime ? `-${item.endTime}` : ''}`
        : '';
      const location = item.location ? ` @ ${item.location}` : '';
      return `- [${item.type}] ${item.title} (${item.date}${time}${location})`;
    });
    return ['Itinerary:', ...lines].join('\n');
  }
}
