import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { TaskCategory, TaskPriority, TaskType } from '@prisma/client';
import {
  IsEnum,
  IsInt,
  IsNotEmpty,
  IsOptional,
  IsString,
  IsUUID,
  Matches,
  Max,
  MaxLength,
  Min,
} from 'class-validator';
import { DATE_PATTERN, TIME_PATTERN } from '../../../common/utils/date-time.util';

/** One day: the longest duration or reminder lead time accepted. */
const MAX_MINUTES = 1440;

export class CreateTaskDto {
  @ApiProperty({ example: 'Sabah Rutini', maxLength: 200 })
  @IsString()
  @IsNotEmpty()
  @MaxLength(200)
  title!: string;

  @ApiPropertyOptional({ maxLength: 2000 })
  @IsOptional()
  @IsString()
  @MaxLength(2000)
  description?: string;

  @ApiProperty({
    example: '2026-08-01',
    description: 'Date-only, "YYYY-MM-DD" — matches mobile\'s `TaskDueDate.date`.',
  })
  @Matches(DATE_PATTERN, { message: 'dueDate must be in "YYYY-MM-DD" format.' })
  dueDate!: string;

  @ApiPropertyOptional({
    example: '15:30',
    description:
      'Optional 24-hour "HH:mm" — matches mobile\'s `TaskDueDate.time`. Omit for an all-day task.',
  })
  @IsOptional()
  @Matches(TIME_PATTERN, { message: 'dueTime must be in 24-hour "HH:mm" format.' })
  dueTime?: string;

  @ApiPropertyOptional({
    enum: TaskType,
    default: TaskType.TASK,
    description: 'A `MEETING` must have a `dueTime`.',
  })
  @IsOptional()
  @IsEnum(TaskType)
  type?: TaskType;

  @ApiPropertyOptional({
    example: 30,
    minimum: 5,
    maximum: MAX_MINUTES,
    nullable: true,
    type: Number,
    description: 'How long the meeting (or task) takes. `null` clears it on update.',
  })
  @IsOptional()
  @IsInt()
  @Min(5)
  @Max(MAX_MINUTES)
  durationMinutes?: number | null;

  @ApiPropertyOptional({
    example: 'Ofis, toplantı odası 2',
    maxLength: 200,
    nullable: true,
    type: String,
    description: 'Where the meeting is. `null` clears it on update.',
  })
  @IsOptional()
  @IsString()
  @MaxLength(200)
  location?: string | null;

  @ApiPropertyOptional({
    example: 10,
    minimum: 0,
    maximum: MAX_MINUTES,
    nullable: true,
    type: Number,
    description:
      'Push reminder this many minutes before `dueTime` (0 = at the time). Needs a `dueTime`. Omitted on a new meeting: 10. `null` turns the reminder off.',
  })
  @IsOptional()
  @IsInt()
  @Min(0)
  @Max(MAX_MINUTES)
  reminderMinutesBefore?: number | null;

  @ApiProperty({ enum: TaskPriority })
  @IsEnum(TaskPriority)
  priority!: TaskPriority;

  @ApiProperty({ enum: TaskCategory })
  @IsEnum(TaskCategory)
  category!: TaskCategory;

  // No `source`: always server-assigned (`PLANNER`) — see `PlannerService.createTask`'s
  // doc comment for why a caller must never be able to set this itself.

  @ApiPropertyOptional({
    format: 'uuid',
    description: "Must be one of the caller's own task lists — validated server-side.",
  })
  @IsOptional()
  @IsUUID()
  taskListId?: string;
}
