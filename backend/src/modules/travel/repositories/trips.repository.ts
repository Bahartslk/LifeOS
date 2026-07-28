import { Injectable } from '@nestjs/common';
import { Prisma, Trip, TripCategory, TripStatus } from '@prisma/client';
import { PrismaService } from '../../../prisma/prisma.service';

export interface CreateTripData {
  userId: string;
  title: string;
  description: string | null;
  destination: string;
  country: string;
  startDate: Date;
  endDate: Date;
  category: TripCategory;
  weatherTemperatureCelsius: number | null;
  windSpeedKmh: number | null;
  coverImageUrl: string | null;
}

export type UpdateTripData = Partial<
  Pick<
    CreateTripData,
    | 'title'
    | 'description'
    | 'destination'
    | 'country'
    | 'startDate'
    | 'endDate'
    | 'category'
    | 'weatherTemperatureCelsius'
    | 'windSpeedKmh'
    | 'coverImageUrl'
  >
> & { status?: TripStatus };

export interface FindManyQuery {
  status?: TripStatus;
  sortField: 'startDate' | 'createdAt';
  sortDirection: 'asc' | 'desc';
  cursor?: string;
  limit: number;
}

export interface FindManyResult {
  items: Trip[];
  hasMore: boolean;
}

/**
 * Sole data-access point for `trips` — mirrors `TasksRepository`'s shape
 * exactly (Sprint 16.0's "ownership checks must mirror PlannerModule"):
 * every method scopes by `userId`, defense in depth alongside
 * `TravelService`'s own ownership check.
 */
@Injectable()
export class TripsRepository {
  constructor(private readonly prisma: PrismaService) {}

  create(data: CreateTripData): Promise<Trip> {
    return this.prisma.trip.create({
      data: {
        userId: data.userId,
        title: data.title,
        description: data.description,
        destination: data.destination,
        country: data.country,
        startDate: data.startDate,
        endDate: data.endDate,
        category: data.category,
        weatherTemperatureCelsius: data.weatherTemperatureCelsius,
        windSpeedKmh: data.windSpeedKmh,
        coverImageUrl: data.coverImageUrl,
        status: TripStatus.PLANNED,
      },
    });
  }

  findById(id: string, userId: string): Promise<Trip | null> {
    return this.prisma.trip.findFirst({ where: { id, userId, deletedAt: null } });
  }

  async update(id: string, userId: string, data: UpdateTripData): Promise<Trip | null> {
    const result = await this.prisma.trip.updateMany({
      where: { id, userId, deletedAt: null },
      data,
    });
    if (result.count === 0) return null;
    return this.findById(id, userId);
  }

  async softDelete(id: string, userId: string): Promise<boolean> {
    const result = await this.prisma.trip.updateMany({
      where: { id, userId, deletedAt: null },
      data: { deletedAt: new Date() },
    });
    return result.count > 0;
  }

  /** Backs `GET /travel/trips` — `status` filter also satisfies "Upcoming/Active/Completed Trips" (TRIP FEATURES), matching how Planner's "Today's/Upcoming Tasks" are satisfied via filters on its own general list endpoint. */
  async findMany(userId: string, query: FindManyQuery): Promise<FindManyResult> {
    const where: Prisma.TripWhereInput = {
      userId,
      deletedAt: null,
      ...(query.status && { status: query.status }),
    };

    const rows = await this.prisma.trip.findMany({
      where,
      orderBy: [{ [query.sortField]: query.sortDirection }, { id: 'asc' }],
      ...(query.cursor && { cursor: { id: query.cursor }, skip: 1 }),
      take: query.limit + 1,
    });

    const hasMore = rows.length > query.limit;
    return { items: hasMore ? rows.slice(0, query.limit) : rows, hasMore };
  }

  /** Every active trip with the given status, ordered by `startDate` — backs each Dashboard bucket. */
  findByStatus(userId: string, status: TripStatus): Promise<Trip[]> {
    return this.prisma.trip.findMany({
      where: { userId, deletedAt: null, status },
      orderBy: { startDate: 'asc' },
    });
  }

  /** The soonest `PLANNED` trip by `startDate` — Dashboard's "Next Destination". */
  findNextUpcoming(userId: string): Promise<Trip | null> {
    return this.prisma.trip.findFirst({
      where: { userId, deletedAt: null, status: TripStatus.PLANNED },
      orderBy: { startDate: 'asc' },
    });
  }

  /** Dashboard's "Trip Count" — every active trip regardless of status. */
  countActive(userId: string): Promise<number> {
    return this.prisma.trip.count({ where: { userId, deletedAt: null } });
  }
}
