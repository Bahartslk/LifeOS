import { ApiProperty } from '@nestjs/swagger';
import { TripResponseDto } from './trip-response.dto';
import { ItineraryItemResponseDto } from './itinerary-item-response.dto';

/**
 * `GET /travel/dashboard`'s response — the exact 6 fields Sprint 16.0's
 * brief lists. Not identical to mobile's existing `TravelListData`
 * (`upcomingTrips`/`pastTrips` + `TravelStatistics`) — a new,
 * backend-computed synthesis, the same relationship Planner's
 * `TravelDashboardResponseDto` has to mobile's `PlannerDashboard`/`AiTaskContext`.
 * See `TravelService.getDashboard` for exact field derivation.
 */
export class TravelDashboardResponseDto {
  @ApiProperty({ type: [TripResponseDto], description: 'status = PLANNED, ordered by startDate.' })
  upcomingTrips!: TripResponseDto[];

  @ApiProperty({ type: [TripResponseDto], description: 'status = ONGOING, ordered by startDate.' })
  activeTrips!: TripResponseDto[];

  @ApiProperty({
    type: [TripResponseDto],
    description: 'status = COMPLETED, ordered by startDate.',
  })
  completedTrips!: TripResponseDto[];

  @ApiProperty({
    nullable: true,
    type: String,
    description: 'destination of the soonest PLANNED trip, or null if none.',
  })
  nextDestination!: string | null;

  @ApiProperty({
    type: [ItineraryItemResponseDto],
    description: 'Items with date >= today, from PLANNED/ONGOING trips only, ordered by date.',
  })
  upcomingItineraryItems!: ItineraryItemResponseDto[];

  @ApiProperty({ description: 'Every active (non-deleted) trip, any status.' })
  tripCount!: number;
}
