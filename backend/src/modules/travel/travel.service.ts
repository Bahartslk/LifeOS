import { BadRequestException, Injectable, NotFoundException } from '@nestjs/common';
import { Trip, TripStatus } from '@prisma/client';
import { fromDbDate, toDbDate } from '../../common/utils/date-time.util';
import { TripsRepository } from './repositories/trips.repository';
import { ItineraryItemsRepository } from './repositories/itinerary-items.repository';
import { CreateTripDto } from './dto/create-trip.dto';
import { UpdateTripDto } from './dto/update-trip.dto';
import { TripQueryDto } from './dto/trip-query.dto';
import { TripResponseDto } from './dto/trip-response.dto';
import { PaginatedTripsResponseDto } from './dto/paginated-trips-response.dto';
import { TravelDashboardResponseDto } from './dto/dashboard-response.dto';
import { toItineraryItemResponse } from './utils/itinerary-item-mapper.util';

type SortField = 'startDate' | 'createdAt';

/**
 * Trip CRUD + dashboard business logic — controllers stay thin, mirroring
 * `PlannerService`'s shape exactly (Sprint 16.0's "ownership checks must
 * mirror PlannerModule"). Itinerary item CRUD/reorder/Planner-task-linkage
 * logic lives in the separate `ItineraryService` — split to avoid a single
 * god service covering both trips and itinerary items, per this sprint's
 * explicit "Avoid: God services" rule; this class only reaches into
 * `ItineraryItemsRepository` for the Dashboard's cross-trip
 * `upcomingItineraryItems` query.
 *
 * `status` bucketing (Dashboard, and the `TripQueryDto.status` filter) reads
 * the stored `status` column directly, not derived from
 * `startDate`/`endDate` vs. today — a trip's lifecycle stage is this
 * column's explicit job, and no scheduled job exists yet to auto-transition
 * `PLANNED` -> `ONGOING` -> `COMPLETED` as dates pass (a real, documented
 * gap — see this sprint's Technical Debt).
 */
@Injectable()
export class TravelService {
  constructor(
    private readonly tripsRepository: TripsRepository,
    private readonly itineraryItemsRepository: ItineraryItemsRepository,
  ) {}

  async createTrip(userId: string, dto: CreateTripDto): Promise<TripResponseDto> {
    this.assertDateRange(dto.startDate, dto.endDate);

    const trip = await this.tripsRepository.create({
      userId,
      title: dto.title,
      description: dto.description ?? null,
      destination: dto.destination,
      country: dto.country,
      startDate: toDbDate(dto.startDate),
      endDate: toDbDate(dto.endDate),
      coverImageUrl: dto.coverImageUrl ?? null,
    });

    return this.toResponse(trip);
  }

  async updateTrip(userId: string, id: string, dto: UpdateTripDto): Promise<TripResponseDto> {
    const existing = await this.tripsRepository.findById(id, userId);
    if (!existing) {
      throw new NotFoundException('Trip not found.');
    }

    const nextStartDate = dto.startDate ?? fromDbDate(existing.startDate);
    const nextEndDate = dto.endDate ?? fromDbDate(existing.endDate);
    this.assertDateRange(nextStartDate, nextEndDate);

    const trip = await this.tripsRepository.update(id, userId, {
      ...(dto.title !== undefined && { title: dto.title }),
      ...(dto.description !== undefined && { description: dto.description }),
      ...(dto.destination !== undefined && { destination: dto.destination }),
      ...(dto.country !== undefined && { country: dto.country }),
      ...(dto.startDate !== undefined && { startDate: toDbDate(dto.startDate) }),
      ...(dto.endDate !== undefined && { endDate: toDbDate(dto.endDate) }),
      ...(dto.coverImageUrl !== undefined && { coverImageUrl: dto.coverImageUrl }),
      ...(dto.status !== undefined && { status: dto.status }),
    });

    if (!trip) {
      throw new NotFoundException('Trip not found.');
    }
    return this.toResponse(trip);
  }

  /**
   * Soft delete only — deliberately does NOT cascade to this trip's
   * itinerary items or any Planner tasks they've created. Itinerary items
   * remain in the database (a soft-deleted trip is recoverable in
   * principle, per docs/14-database-design.md's soft-delete strategy);
   * cleaning up their linked tasks too is scoped out of this sprint (see
   * Technical Debt) rather than guessed at.
   */
  async deleteTrip(userId: string, id: string): Promise<void> {
    const deleted = await this.tripsRepository.softDelete(id, userId);
    if (!deleted) {
      throw new NotFoundException('Trip not found.');
    }
  }

  async getTrip(userId: string, id: string): Promise<TripResponseDto> {
    const trip = await this.tripsRepository.findById(id, userId);
    if (!trip) {
      throw new NotFoundException('Trip not found.');
    }
    return this.toResponse(trip);
  }

  async getTrips(userId: string, query: TripQueryDto): Promise<PaginatedTripsResponseDto> {
    const { field, direction } = this.parseSort(query.sort);

    const { items, hasMore } = await this.tripsRepository.findMany(userId, {
      status: query.status,
      sortField: field,
      sortDirection: direction,
      cursor: query.cursor,
      limit: query.limit,
    });

    return {
      data: items.map((trip) => this.toResponse(trip)),
      meta: {
        nextCursor: hasMore ? items[items.length - 1].id : null,
        limit: query.limit,
        hasMore,
      },
    };
  }

  /**
   * `GET /travel/dashboard` — the exact 6 fields Sprint 16.0's brief lists.
   * `upcomingTrips`/`activeTrips`/`completedTrips` read the `status` column
   * directly (`PLANNED`/`ONGOING`/`COMPLETED` respectively — `CANCELLED`
   * trips appear in none of the three, but still count toward `tripCount`).
   * `upcomingItineraryItems` spans every `PLANNED`/`ONGOING` trip's items
   * due today or later — `CANCELLED`/`COMPLETED` trips' items are excluded
   * since they're no longer "upcoming" in any planning sense.
   */
  async getDashboard(userId: string): Promise<TravelDashboardResponseDto> {
    const today = this.todayUtcMidnight();

    const [upcomingTrips, activeTrips, completedTrips, nextTrip, upcomingItems, tripCount] =
      await Promise.all([
        this.tripsRepository.findByStatus(userId, TripStatus.PLANNED),
        this.tripsRepository.findByStatus(userId, TripStatus.ONGOING),
        this.tripsRepository.findByStatus(userId, TripStatus.COMPLETED),
        this.tripsRepository.findNextUpcoming(userId),
        this.itineraryItemsRepository.findUpcomingForUser(userId, today, [
          TripStatus.PLANNED,
          TripStatus.ONGOING,
        ]),
        this.tripsRepository.countActive(userId),
      ]);

    return {
      upcomingTrips: upcomingTrips.map((trip) => this.toResponse(trip)),
      activeTrips: activeTrips.map((trip) => this.toResponse(trip)),
      completedTrips: completedTrips.map((trip) => this.toResponse(trip)),
      nextDestination: nextTrip?.destination ?? null,
      upcomingItineraryItems: upcomingItems.map((item) => toItineraryItemResponse(item)),
      tripCount,
    };
  }

  private assertDateRange(startDate: string, endDate: string): void {
    // Both are "YYYY-MM-DD" strings, so lexicographic comparison is exactly
    // date-order comparison — no need to parse into Date objects first.
    if (endDate < startDate) {
      throw new BadRequestException('endDate must not be before startDate.');
    }
  }

  private parseSort(sort: string | undefined): { field: SortField; direction: 'asc' | 'desc' } {
    if (!sort) return { field: 'startDate', direction: 'asc' };
    const direction = sort.startsWith('-') ? 'desc' : 'asc';
    const field = (sort.startsWith('-') ? sort.slice(1) : sort) as SortField;
    return { field, direction };
  }

  private todayUtcMidnight(): Date {
    return new Date(`${new Date().toISOString().slice(0, 10)}T00:00:00.000Z`);
  }

  private toResponse(trip: Trip): TripResponseDto {
    return {
      id: trip.id,
      title: trip.title,
      description: trip.description,
      destination: trip.destination,
      country: trip.country,
      startDate: fromDbDate(trip.startDate),
      endDate: fromDbDate(trip.endDate),
      status: trip.status,
      coverImageUrl: trip.coverImageUrl,
      createdAt: trip.createdAt,
      updatedAt: trip.updatedAt,
    };
  }
}
