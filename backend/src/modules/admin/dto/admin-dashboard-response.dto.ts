import { ApiProperty } from '@nestjs/swagger';
import { AdminTaskStatsDto, AdminTripStatsDto } from './admin-stats.dto';

export class AdminDashboardUserStatsDto {
  @ApiProperty({ description: 'Active (not soft-deleted) accounts.' })
  total!: number;

  @ApiProperty({ description: 'Soft-deleted accounts.' })
  deleted!: number;

  @ApiProperty({ description: 'Active accounts with the ADMIN role.' })
  admins!: number;

  @ApiProperty({ description: 'Active accounts created in the last 7 days.' })
  newLast7Days!: number;

  @ApiProperty({ description: 'Active accounts created in the last 30 days.' })
  newLast30Days!: number;

  @ApiProperty({
    description:
      'Active accounts that signed in or renewed their session in the last 7 days. This is NOT a measure of in-app usage: it counts accounts with at least one refresh token created in that period (a new one is created at every login and every token refresh).',
  })
  activeLast7Days!: number;
}

/**
 * `GET /admin/dashboard`'s response — system-wide aggregate counts only.
 * Task and trip figures cover active (not deleted) records of active
 * accounts, and expose no content of any individual record.
 */
export class AdminDashboardResponseDto {
  @ApiProperty({ type: AdminDashboardUserStatsDto })
  users!: AdminDashboardUserStatsDto;

  @ApiProperty({ type: AdminTaskStatsDto })
  tasks!: AdminTaskStatsDto;

  @ApiProperty({ type: AdminTripStatsDto })
  trips!: AdminTripStatsDto;

  @ApiProperty({ description: 'When these figures were computed.' })
  generatedAt!: Date;
}
