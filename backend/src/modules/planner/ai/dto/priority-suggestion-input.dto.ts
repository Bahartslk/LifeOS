import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, Matches } from 'class-validator';
import { DATE_PATTERN, TIME_PATTERN } from '../../../../common/utils/date-time.util';
import { TaskContentInputDto } from './task-content-input.dto';

/** Body shape for `POST /planner/ai/priority-suggestion` — `TaskContentInputDto`'s fields plus the due date/time the suggestion is based on, matching `CreateTaskDto`'s own date/time validation exactly (same shared `DATE_PATTERN`/`TIME_PATTERN`). */
export class PrioritySuggestionInputDto extends TaskContentInputDto {
  @ApiProperty({ example: '2026-08-01', description: 'Date-only, "YYYY-MM-DD".' })
  @Matches(DATE_PATTERN, { message: 'dueDate must be in "YYYY-MM-DD" format.' })
  dueDate!: string;

  @ApiPropertyOptional({ example: '15:30', description: 'Optional 24-hour "HH:mm".' })
  @IsOptional()
  @Matches(TIME_PATTERN, { message: 'dueTime must be in 24-hour "HH:mm" format.' })
  dueTime?: string;
}
