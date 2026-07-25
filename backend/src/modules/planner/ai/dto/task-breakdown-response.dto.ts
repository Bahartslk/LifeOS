import { ApiProperty } from '@nestjs/swagger';
import { AiCapabilityEnvelopeDto } from '../../../ai/dto/ai-capability-envelope.dto';

export class SubtaskDto {
  @ApiProperty()
  title!: string;

  @ApiProperty({ nullable: true, type: String })
  description!: string | null;

  @ApiProperty({ nullable: true, type: Number })
  estimatedDurationMinutes!: number | null;
}

export class TaskBreakdownResultDto {
  @ApiProperty({ type: [SubtaskDto] })
  subtasks!: SubtaskDto[];
}

/** `POST /planner/ai/tasks/:id/breakdown`'s response — suggested subtasks only; never persisted by this endpoint. */
export class TaskBreakdownResponseDto extends AiCapabilityEnvelopeDto {
  @ApiProperty({ type: TaskBreakdownResultDto })
  result!: TaskBreakdownResultDto;
}
