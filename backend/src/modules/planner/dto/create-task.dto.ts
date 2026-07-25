import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { TaskPriority } from '@prisma/client';
import {
  IsEnum,
  IsNotEmpty,
  IsOptional,
  IsString,
  IsUUID,
  Matches,
  MaxLength,
} from 'class-validator';
import { DATE_PATTERN, TIME_PATTERN } from '../../../common/utils/date-time.util';

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

  @ApiProperty({ enum: TaskPriority })
  @IsEnum(TaskPriority)
  priority!: TaskPriority;

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
