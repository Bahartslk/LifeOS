import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsISO8601, IsOptional, Matches } from 'class-validator';
import { DATE_PATTERN } from '../../../common/utils/date-time.util';

/**
 * `GET /planner/dashboard`'s query. `date` is the caller's own local
 * "today" — the mobile client sends its device date so the dashboard's
 * today/upcoming/overdue split matches the day the user is actually in,
 * rather than the server's UTC date (which is "yesterday" in Turkey
 * between 00:00 and 03:00). When omitted, `PlannerService.getDashboard`
 * falls back to today in the caller's `User.timezone`.
 */
export class DashboardQueryDto {
  @ApiPropertyOptional({
    example: '2026-10-02',
    description:
      'The caller\'s local "today", "YYYY-MM-DD". Defaults to today in the user\'s profile timezone.',
  })
  @IsOptional()
  @Matches(DATE_PATTERN, { message: 'date must be in "YYYY-MM-DD" format.' })
  @IsISO8601({ strict: true }, { message: 'date must be a valid calendar date.' })
  date?: string;
}
