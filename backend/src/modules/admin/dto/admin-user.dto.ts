import { ApiProperty } from '@nestjs/swagger';
import { UserRole } from '@prisma/client';
import { PaginationMetaDto } from '../../../common/dto/pagination-meta.dto';
import { AdminTaskStatsDto, AdminTripStatsDto } from './admin-stats.dto';

/**
 * One row of `GET /admin/users` — the account fields an admin needs to
 * find and recognize a user. Deliberately excludes everything else on the
 * account (password hash, bio, avatar, notification preferences) and all
 * of the user's own content.
 */
export class AdminUserSummaryDto {
  @ApiProperty({ format: 'uuid' })
  id!: string;

  @ApiProperty()
  email!: string;

  @ApiProperty()
  displayName!: string;

  @ApiProperty({ enum: UserRole })
  role!: UserRole;

  @ApiProperty({ example: 'Europe/Istanbul' })
  timezone!: string;

  @ApiProperty()
  createdAt!: Date;

  @ApiProperty({ nullable: true, type: Date, description: 'Set when the account is soft-deleted.' })
  deletedAt!: Date | null;

  @ApiProperty({ description: 'false when the account is soft-deleted.' })
  isActive!: boolean;
}

export class PaginatedAdminUsersResponseDto {
  @ApiProperty({ type: [AdminUserSummaryDto] })
  data!: AdminUserSummaryDto[];

  @ApiProperty({ type: PaginationMetaDto })
  meta!: PaginationMetaDto;
}

/**
 * `GET /admin/users/:id`'s response — the summary fields plus aggregate
 * counts of the user's tasks and trips. Counts only: no task or trip
 * content is ever returned. Also served for soft-deleted accounts
 * (`isActive: false`).
 */
export class AdminUserDetailResponseDto extends AdminUserSummaryDto {
  @ApiProperty({ example: 'tr' })
  language!: string;

  @ApiProperty()
  updatedAt!: Date;

  @ApiProperty({
    nullable: true,
    type: Date,
    description:
      "When the account last signed in or renewed its session (its newest refresh token's creation time), or null if it never has. Not a measure of in-app usage.",
  })
  lastActiveAt!: Date | null;

  @ApiProperty({ type: AdminTaskStatsDto })
  tasks!: AdminTaskStatsDto;

  @ApiProperty({ type: AdminTripStatsDto })
  trips!: AdminTripStatsDto;
}
