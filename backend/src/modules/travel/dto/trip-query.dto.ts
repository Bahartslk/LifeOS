import { ApiPropertyOptional } from '@nestjs/swagger';
import { TripStatus } from '@prisma/client';
import { IsEnum, IsIn, IsOptional } from 'class-validator';
import { PaginationQueryDto } from '../../../common/dto/pagination-query.dto';

const SORTABLE_FIELDS = ['startDate', 'createdAt'] as const;
const SORT_VALUES = SORTABLE_FIELDS.flatMap((field) => [field, `-${field}`]);

/**
 * `GET /travel/trips`'s query shape. `status` also satisfies "Upcoming
 * Trips" (`?status=PLANNED`), "Active Trips" (`?status=ONGOING`), and
 * "Completed Trips" (`?status=COMPLETED`) from TRIP FEATURES — no separate
 * routes for those, mirroring how Planner's "Today's/Upcoming Tasks" are
 * satisfied via filters on its own general list endpoint rather than
 * dedicated routes.
 */
export class TripQueryDto extends PaginationQueryDto {
  @ApiPropertyOptional({ enum: TripStatus })
  @IsOptional()
  @IsEnum(TripStatus)
  status?: TripStatus;

  @ApiPropertyOptional({
    enum: SORT_VALUES,
    default: 'startDate',
    description: 'Leading "-" sorts descending, per docs/15-api-design.md#filtering-and-sorting.',
  })
  @IsOptional()
  @IsIn(SORT_VALUES, { message: `sort must be one of: ${SORT_VALUES.join(', ')}` })
  sort?: string;
}
