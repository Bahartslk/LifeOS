import { ItineraryItem } from '@prisma/client';
import { fromDbDate, fromDbTime } from '../../../common/utils/date-time.util';
import { ItineraryItemResponseDto } from '../dto/itinerary-item-response.dto';

/**
 * A plain pure function, not a method on either service, because both
 * `ItineraryService` (its own CRUD responses) and `TravelService` (the
 * dashboard's `upcomingItineraryItems`, which spans every one of the
 * user's trips) need the identical mapping — extracting it here avoids
 * either duplicating it or one service awkwardly injecting the other just
 * to reach a mapper.
 */
export function toItineraryItemResponse(item: ItineraryItem): ItineraryItemResponseDto {
  return {
    id: item.id,
    tripId: item.tripId,
    title: item.title,
    description: item.description,
    date: fromDbDate(item.date),
    startTime: fromDbTime(item.startTime),
    endTime: fromDbTime(item.endTime),
    location: item.location,
    orderIndex: item.orderIndex,
    type: item.type,
    transportationType: item.transportationType,
    accommodationType: item.accommodationType,
    taskId: item.taskId,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
}
