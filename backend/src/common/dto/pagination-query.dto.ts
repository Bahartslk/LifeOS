import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsInt, IsOptional, IsString, Max, Min } from 'class-validator';

/**
 * Shared cursor-pagination query shape, per docs/15-api-design.md#pagination.
 * Every list endpoint (`GET /planner/tasks` via `TaskQueryDto`,
 * `GET /travel/trips` via `TripQueryDto`) extends this instead of
 * redefining cursor/limit each time.
 */
export class PaginationQueryDto {
  @ApiPropertyOptional({ description: "Opaque cursor from a previous page's meta.nextCursor" })
  @IsOptional()
  @IsString()
  cursor?: string;

  @ApiPropertyOptional({ default: 20, minimum: 1, maximum: 100 })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(100)
  limit: number = 20;
}
