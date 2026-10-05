import { ApiPropertyOptional } from '@nestjs/swagger';
import { UserRole } from '@prisma/client';
import { Transform } from 'class-transformer';
import { IsEnum, IsIn, IsOptional, IsString, IsUUID, Length } from 'class-validator';
import { PaginationQueryDto } from '../../../common/dto/pagination-query.dto';

export const ADMIN_USER_STATUS_FILTERS = ['active', 'deleted', 'all'] as const;
export type AdminUserStatusFilter = (typeof ADMIN_USER_STATUS_FILTERS)[number];

const SORTABLE_FIELDS = ['createdAt', 'email'] as const;
export type AdminUserSortField = (typeof SORTABLE_FIELDS)[number];
export const ADMIN_USER_SORT_VALUES = SORTABLE_FIELDS.flatMap((field) => [field, `-${field}`]);

/**
 * `GET /admin/users`'s query: cursor pagination reused from
 * `PaginationQueryDto` (per docs/15-api-design.md#pagination), plus search,
 * filters and sort. Everything is validated against a fixed set of values,
 * so an unexpected value is a `400`, never an unfiltered query.
 */
export class AdminUsersQueryDto extends PaginationQueryDto {
  /**
   * Stricter than the shared DTO's plain string: the cursor is always a
   * user id, and a non-UUID would otherwise reach the database driver.
   */
  @ApiPropertyOptional({
    format: 'uuid',
    description: "Cursor from a previous page's meta.nextCursor",
  })
  @IsOptional()
  @IsUUID()
  declare cursor?: string;

  @ApiPropertyOptional({
    minLength: 2,
    maxLength: 100,
    description: 'Case-insensitive search in email and display name.',
  })
  @IsOptional()
  @Transform(({ value }) => (typeof value === 'string' ? value.trim() : value))
  @IsString()
  @Length(2, 100)
  q?: string;

  @ApiPropertyOptional({ enum: UserRole })
  @IsOptional()
  @IsEnum(UserRole)
  role?: UserRole;

  @ApiPropertyOptional({
    enum: ADMIN_USER_STATUS_FILTERS,
    default: 'active',
    description:
      'Which accounts to list. Soft-deleted accounts are excluded unless "deleted" or "all" is asked for.',
  })
  @IsOptional()
  @IsIn(ADMIN_USER_STATUS_FILTERS, {
    message: `status must be one of: ${ADMIN_USER_STATUS_FILTERS.join(', ')}`,
  })
  status: AdminUserStatusFilter = 'active';

  @ApiPropertyOptional({
    enum: ADMIN_USER_SORT_VALUES,
    default: '-createdAt',
    description: 'Leading "-" sorts descending, per docs/15-api-design.md#filtering-and-sorting.',
  })
  @IsOptional()
  @IsIn(ADMIN_USER_SORT_VALUES, {
    message: `sort must be one of: ${ADMIN_USER_SORT_VALUES.join(', ')}`,
  })
  sort: string = '-createdAt';
}
