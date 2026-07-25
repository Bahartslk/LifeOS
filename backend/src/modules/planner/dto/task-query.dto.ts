import { ApiPropertyOptional } from '@nestjs/swagger';
import { TaskPriority, TaskStatus } from '@prisma/client';
import { IsEnum, IsIn, IsOptional, IsUUID, Matches } from 'class-validator';
import { PaginationQueryDto } from '../../../common/dto/pagination-query.dto';
import { DATE_PATTERN } from '../../../common/utils/date-time.util';

const SORTABLE_FIELDS = ['dueDate', 'createdAt', 'priority'] as const;
const SORT_VALUES = SORTABLE_FIELDS.flatMap((field) => [field, `-${field}`]);

/**
 * `GET /planner/tasks`'s query shape — filters and sort exactly matching
 * docs/15-api-design.md#filtering-and-sorting's `tasks` row, pagination
 * reused from `PaginationQueryDto` (per docs/15-api-design.md#pagination)
 * rather than redefined.
 */
export class TaskQueryDto extends PaginationQueryDto {
  @ApiPropertyOptional({ enum: TaskStatus })
  @IsOptional()
  @IsEnum(TaskStatus)
  status?: TaskStatus;

  @ApiPropertyOptional({ enum: TaskPriority })
  @IsOptional()
  @IsEnum(TaskPriority)
  priority?: TaskPriority;

  @ApiPropertyOptional({ format: 'uuid' })
  @IsOptional()
  @IsUUID()
  taskListId?: string;

  @ApiPropertyOptional({
    example: '2026-08-01',
    description: 'Inclusive lower bound on dueDate, "YYYY-MM-DD".',
  })
  @IsOptional()
  @Matches(DATE_PATTERN, { message: 'dueAfter must be in "YYYY-MM-DD" format.' })
  dueAfter?: string;

  @ApiPropertyOptional({
    example: '2026-08-31',
    description: 'Inclusive upper bound on dueDate, "YYYY-MM-DD".',
  })
  @IsOptional()
  @Matches(DATE_PATTERN, { message: 'dueBefore must be in "YYYY-MM-DD" format.' })
  dueBefore?: string;

  @ApiPropertyOptional({
    enum: SORT_VALUES,
    default: 'dueDate',
    description: 'Leading "-" sorts descending, per docs/15-api-design.md#filtering-and-sorting.',
  })
  @IsOptional()
  @IsIn(SORT_VALUES, { message: `sort must be one of: ${SORT_VALUES.join(', ')}` })
  sort?: string;
}
