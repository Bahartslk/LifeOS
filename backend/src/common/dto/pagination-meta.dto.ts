import { ApiProperty } from '@nestjs/swagger';

/**
 * Matches docs/15-api-design.md#pagination's envelope exactly — `{ data,
 * meta }`, not the plain `{ data }` `TransformResponseInterceptor` would
 * otherwise wrap a bare array in. Originally Planner-local
 * (`PaginatedTasksResponseDto`'s own file) — promoted here in Sprint 16.0
 * now that Travel's paginated trip list needs the identical shape, per
 * this project's "promote only once a second feature needs it" rule.
 */
export class PaginationMetaDto {
  @ApiProperty({ nullable: true, type: String })
  nextCursor!: string | null;

  @ApiProperty()
  limit!: number;

  @ApiProperty()
  hasMore!: boolean;
}
