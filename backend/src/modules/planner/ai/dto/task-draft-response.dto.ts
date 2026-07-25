import { ApiProperty } from '@nestjs/swagger';
import { TaskPriority } from '@prisma/client';
import { AiCapabilityEnvelopeDto } from '../../../ai/dto/ai-capability-envelope.dto';

export class TaskDraftResultDto {
  @ApiProperty()
  title!: string;

  @ApiProperty({ nullable: true, type: String })
  description!: string | null;

  @ApiProperty({ example: '2026-08-01', nullable: true, type: String })
  date!: string | null;

  @ApiProperty({ example: '09:00', nullable: true, type: String })
  time!: string | null;

  @ApiProperty({ enum: TaskPriority })
  priority!: TaskPriority;

  @ApiProperty({ nullable: true, type: Number })
  estimatedDurationMinutes!: number | null;

  @ApiProperty({ type: [String] })
  tags!: string[];
}

/** `POST /planner/ai/tasks/draft`'s response — a draft only; never persisted by this endpoint (see `PlannerAiService`). */
export class TaskDraftResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: TaskDraftResultDto })
  result!: TaskDraftResultDto;
}
