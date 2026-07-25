import { Injectable } from '@nestjs/common';
import {
  AccommodationType,
  ItineraryItem,
  ItineraryItemType,
  TransportationType,
  TripStatus,
} from '@prisma/client';
import { PrismaService } from '../../../prisma/prisma.service';

export interface CreateItineraryItemData {
  tripId: string;
  title: string;
  description: string | null;
  date: Date;
  startTime: Date | null;
  endTime: Date | null;
  location: string | null;
  orderIndex: number;
  type: ItineraryItemType;
  transportationType: TransportationType | null;
  accommodationType: AccommodationType | null;
  taskId: string | null;
}

export type UpdateItineraryItemData = Partial<
  Pick<
    CreateItineraryItemData,
    | 'title'
    | 'description'
    | 'date'
    | 'startTime'
    | 'endTime'
    | 'location'
    | 'type'
    | 'transportationType'
    | 'accommodationType'
    | 'taskId'
  >
>;

/**
 * Sole data-access point for `itinerary_items`. Ownership is always
 * enforced through a join to the parent `trip` (`trip: { userId, deletedAt:
 * null }`) rather than a `userId` column of its own — `itinerary_items` has
 * no such column (it's never accessed independently of its trip, per
 * docs/14-database-design.md#entities), matching Sprint 16.0's asymmetric
 * routes exactly: `PATCH`/`DELETE /travel/itinerary/:id` carry no `tripId`
 * in the path, so this join is the *only* way to verify the caller owns the
 * item.
 */
@Injectable()
export class ItineraryItemsRepository {
  constructor(private readonly prisma: PrismaService) {}

  create(data: CreateItineraryItemData): Promise<ItineraryItem> {
    return this.prisma.itineraryItem.create({ data });
  }

  findById(id: string, userId: string): Promise<ItineraryItem | null> {
    return this.prisma.itineraryItem.findFirst({
      where: { id, trip: { userId, deletedAt: null } },
    });
  }

  /** Ownership-scoped through the trip, ordered for a chronological itinerary view. Callers must separately confirm the trip itself exists/belongs to the caller (see `ItineraryService`) so a nonexistent trip 404s even when it has zero items. */
  findByTripId(tripId: string, userId: string): Promise<ItineraryItem[]> {
    return this.prisma.itineraryItem.findMany({
      where: { tripId, trip: { userId, deletedAt: null } },
      orderBy: [{ date: 'asc' }, { orderIndex: 'asc' }],
    });
  }

  async update(
    id: string,
    userId: string,
    data: UpdateItineraryItemData,
  ): Promise<ItineraryItem | null> {
    const existing = await this.findById(id, userId);
    if (!existing) return null;
    return this.prisma.itineraryItem.update({ where: { id: existing.id }, data });
  }

  /** Hard delete (no `deletedAt` column — matches docs/14's "itinerary_items are never soft-deleted"). Returns the deleted row (or `null` if not found/not owned) so the caller can inspect `taskId` before it's gone. */
  async delete(id: string, userId: string): Promise<ItineraryItem | null> {
    const existing = await this.findById(id, userId);
    if (!existing) return null;
    return this.prisma.itineraryItem.delete({ where: { id: existing.id } });
  }

  /** The current highest `orderIndex` for a trip, or `null` if it has no items yet — used to append a newly-created item at the end. */
  async findMaxOrderIndex(tripId: string): Promise<number | null> {
    const top = await this.prisma.itineraryItem.findFirst({
      where: { tripId },
      orderBy: { orderIndex: 'desc' },
      select: { orderIndex: true },
    });
    return top?.orderIndex ?? null;
  }

  /** Reassigns `orderIndex` for every item in `updates`, atomically. Caller (`ItineraryService`) has already verified every id belongs to `tripId`/the caller. */
  async reorder(updates: { id: string; orderIndex: number }[]): Promise<void> {
    await this.prisma.$transaction(
      updates.map(({ id, orderIndex }) =>
        this.prisma.itineraryItem.update({ where: { id }, data: { orderIndex } }),
      ),
    );
  }

  /** Items due `date` or later, across every one of the user's trips whose status is in `statuses` — backs the Travel Dashboard's `upcomingItineraryItems` (see `TravelService.getDashboard`). */
  findUpcomingForUser(
    userId: string,
    date: Date,
    statuses: TripStatus[],
  ): Promise<ItineraryItem[]> {
    return this.prisma.itineraryItem.findMany({
      where: {
        date: { gte: date },
        trip: { userId, deletedAt: null, status: { in: statuses } },
      },
      orderBy: [{ date: 'asc' }, { orderIndex: 'asc' }],
    });
  }
}
