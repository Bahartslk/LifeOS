import { ApiProperty } from '@nestjs/swagger';

/**
 * Aggregate task counts, shared by the dashboard (all users) and the user
 * detail (one user). Counts only — never a task's title or description.
 */
export class AdminTaskStatsDto {
  @ApiProperty({ description: 'Active (not deleted) tasks.' })
  total!: number;

  @ApiProperty()
  todo!: number;

  @ApiProperty()
  inProgress!: number;

  @ApiProperty()
  done!: number;

  @ApiProperty({
    description:
      'Unfinished (TODO/IN_PROGRESS) tasks due before today. "Today" is the UTC date on the dashboard, and the date in the user\'s own timezone on a user detail.',
  })
  overdue!: number;

  @ApiProperty({ description: '0-100, round(done / total * 100); 0 when there are no tasks.' })
  completionRate!: number;
}

/** Aggregate trip counts by status. Counts only — never a trip's title, destination or country. */
export class AdminTripStatsDto {
  @ApiProperty({ description: 'Active (not deleted) trips.' })
  total!: number;

  @ApiProperty()
  planned!: number;

  @ApiProperty()
  ongoing!: number;

  @ApiProperty()
  completed!: number;

  @ApiProperty()
  cancelled!: number;
}
