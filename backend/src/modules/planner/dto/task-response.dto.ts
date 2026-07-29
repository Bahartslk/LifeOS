import { ApiProperty } from '@nestjs/swagger';
import { TaskCategory, TaskPriority, TaskSource, TaskStatus } from '@prisma/client';

/** The only shape a `Task` is ever sent over the API — `userId`/`deletedAt` never leave the repository layer. */
export class TaskResponseDto {
  @ApiProperty({ format: 'uuid' })
  id!: string;

  @ApiProperty()
  title!: string;

  @ApiProperty({ nullable: true, type: String })
  description!: string | null;

  @ApiProperty({ example: '2026-08-01' })
  dueDate!: string;

  @ApiProperty({ example: '15:30', nullable: true, type: String })
  dueTime!: string | null;

  @ApiProperty({ enum: TaskPriority })
  priority!: TaskPriority;

  @ApiProperty({ enum: TaskCategory })
  category!: TaskCategory;

  @ApiProperty({ enum: TaskStatus })
  status!: TaskStatus;

  @ApiProperty({ enum: TaskSource })
  source!: TaskSource;

  @ApiProperty({ format: 'uuid', nullable: true, type: String })
  taskListId!: string | null;

  @ApiProperty()
  createdAt!: Date;

  @ApiProperty()
  updatedAt!: Date;
}
